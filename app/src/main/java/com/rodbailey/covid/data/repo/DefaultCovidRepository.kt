package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.toRegion
import com.rodbailey.covid.data.db.toRegionStats
import com.rodbailey.covid.data.db.toRegionStatsList
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.toRegionEntityList
import com.rodbailey.covid.domain.toRegionStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class DefaultCovidRepository(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidApi: CovidAPI
) : CovidRepository {


    override suspend fun getRegionStatsStream(code: RegionCode): Flow<List<RegionStats>> {
        val dbStats = regionStatsDao.getRegionStats(code.chars)
        return flowOf(
            if (dbStats.isEmpty()) {
                val report = covidApi.getReport(codeToApiQueryParam(code))
                val entity = report.data.toRegionStatsEntity(code.chars)
                regionStatsDao.insert(entity)
                listOf(entity.toRegionStats())
            } else {
                dbStats.toRegionStatsList()
            }
        )
    }

    private fun codeToApiQueryParam(code: RegionCode): String? {
        return if (code is GlobalCode) {
            null
        } else {
            code.chars
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