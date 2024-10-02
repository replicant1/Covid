package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.RegionStatsEntity
import com.rodbailey.covid.data.db.toRegion
import com.rodbailey.covid.data.db.toRegionStats
import com.rodbailey.covid.data.db.toRegionStatsList
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.source.LocalDataSource
import com.rodbailey.covid.data.source.RemoteDataSource
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.toRegionEntityList
import com.rodbailey.covid.domain.toRegionStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import timber.log.Timber

class DefaultCovidRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidApi: CovidAPI
) : CovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    override suspend fun getReport(isoCode: String?): Flow<ReportData> {
        val nullSafeIsoCode = isoCode ?: GLOBAL_ISO3_CODE
        val dbStatsCount = 0 // localDataSource.loadReportDataCount(nullSafeIsoCode).first()

        Timber.i("Into getReport() for iso $isoCode. Num matching records in db = $dbStatsCount")

        return if (dbStatsCount == 0) {
            remoteDataSource.loadReportDataByIso3Code(isoCode).map {
                localDataSource.saveReportData(nullSafeIsoCode, it.data)
                it.data
            }
        } else {
            localDataSource.loadReportDataByIso3Code(nullSafeIsoCode).map {
                it[0]
            }
        }
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