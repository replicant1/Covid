package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.source.LocalDataSource
import com.rodbailey.covid.data.source.RemoteDataSource
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DefaultCovidRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : CovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    override suspend fun getReport(isoCode: String?): Flow<ReportData> {
        val nullSafeIsoCode = isoCode ?: GLOBAL_ISO3_CODE
        val dbStatsCount = localDataSource.loadReportDataCount(nullSafeIsoCode).first()

        Timber.i("Into getReport() for iso $isoCode. Num matching records in db = $dbStatsCount")

        return if (dbStatsCount == 0) {
            remoteDataSource.getReport(isoCode).map {
                localDataSource.saveReportData(nullSafeIsoCode, it.data)
                it.data
            }
        } else {
            localDataSource.loadReportData(nullSafeIsoCode).map {
                it[0]
            }
        }
    }

    override suspend fun getRegions(): Flow<List<Region>> {
        val dbRegionCount = localDataSource.loadRegionCount().first()

        return if (dbRegionCount == 0) {
            remoteDataSource.getRegions().map {
                localDataSource.saveRegions(it.regions)
                it.regions
            }
        } else {
            localDataSource.loadAllRegions()
        }
    }

    override suspend fun getRegionsByName(searchText: String): Flow<List<Region>> {
        return localDataSource.loadRegionsByName(searchText)
    }

}