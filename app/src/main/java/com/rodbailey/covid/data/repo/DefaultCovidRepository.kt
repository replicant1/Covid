package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.toRegion
import com.rodbailey.covid.data.db.toRegionStatsList
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.toRegionEntityList
import com.rodbailey.covid.domain.toRegionStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class DefaultCovidRepository(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidApi: CovidAPI
) : CovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    override fun getRegionStatsStream(iso3code : String?): Flow<List<RegionStats>> {
        val nullSafeIsoCode = iso3code ?: GLOBAL_ISO3_CODE
        return regionStatsDao.getRegionStatsStream(nullSafeIsoCode)
            .distinctUntilChanged()
            .map {
                it.toRegionStatsList()
            }
            .onEach {
                if (it.isEmpty()) {
                    val report = covidApi.getReport(iso3code)
                    regionStatsDao.insert(
                        report.data.toRegionStatsEntity(nullSafeIsoCode)
                    )
                }
            }
    }

    override fun getRegionsStream(): Flow<List<Region>> {
        return regionDao.getAllRegionsStream()
            .map { regions ->
                regions.map { regionEntity ->
                    regionEntity.toRegion()
                }
            }.onEach {
                if (it.isEmpty()) {
                    // The insertion of region data into the table by refreshRegions() will
                    // trigger a new emission containing the newly retrieved regions into the
                    // regionDao.getAllRegionsStream() above.
                    refreshRegions()
                }
            }
    }

    private suspend fun refreshRegions() {
        covidApi.getRegions()
            .also {
                regionDao.insert(
                    it.regions.toRegionEntityList()
                )
            }
    }
}