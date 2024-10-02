package com.rodbailey.covid.data.source

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.toReportDataList
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.toRegionEntityList
import com.rodbailey.covid.domain.toRegionStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class DefaultLocalDataSource(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao
) : LocalDataSource {

    override suspend fun saveRegions(regions: List<Region>) {
        regionDao.insert(
            regions.toRegionEntityList()
        )
    }

    override suspend fun loadAllRegions(): Flow<List<Region>> {
//        return regionDao.getAllRegionsStream().map { regionEntityList ->
//            regionEntityList.map { regionEntity ->
//                TransformUtils.regionEntityToRegion(regionEntity)
//            }
//        }
        return emptyFlow()
    }

    override suspend fun loadRegionsByIso3Code(iso3code: String): Flow<List<Region>> {
        return flow {
//            emit(regionEntityListToRegionList(regionDao.getRegionsByIso3Code(iso3code)))
        }
    }

    override suspend fun saveReportData(iso3code: String, reportData: ReportData) {
        regionStatsDao.insert(reportData.toRegionStatsEntity(iso3code))
    }

    override suspend fun loadReportDataByIso3Code(iso3code: String): Flow<List<ReportData>> {
        return flow {
            emit(regionStatsDao.getRegionStats(iso3code).toReportDataList())
        }
    }
}