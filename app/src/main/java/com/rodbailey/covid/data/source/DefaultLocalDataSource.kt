package com.rodbailey.covid.data.source

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.TransformUtils
import com.rodbailey.covid.domain.TransformUtils.regionEntityListToRegionList
import com.rodbailey.covid.domain.TransformUtils.regionListToRegionEntityList
import com.rodbailey.covid.domain.TransformUtils.regionStatsEntityListToReportDataList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DefaultLocalDataSource(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao
) : LocalDataSource {

    override suspend fun saveRegions(regions: List<Region>) {
        regionDao.insert(regionListToRegionEntityList(regions))
    }

    override suspend fun loadAllRegions(): Flow<List<Region>> {
        return regionDao.getAllRegionsStream().map { regionEntityList ->
            regionEntityList.map { regionEntity ->
                TransformUtils.regionEntityToRegion(regionEntity)
            }
        }
    }

    override suspend fun loadRegionsByIso3Code(iso3code: String): Flow<List<Region>> {
        return flow {
            emit(regionEntityListToRegionList(regionDao.getRegionsByIso3Code(iso3code)))
        }
    }

    override suspend fun saveReportData(iso3code: String, reportData: ReportData) {
        regionStatsDao.insert(TransformUtils.reportDataToRegionStatsEntity(iso3code, reportData))
    }

    override suspend fun loadReportDataByIso3Code(iso3code: String): Flow<List<ReportData>> {
        return flow {
            emit(regionStatsEntityListToReportDataList(regionStatsDao.getRegionStats(iso3code)))
        }
    }
}