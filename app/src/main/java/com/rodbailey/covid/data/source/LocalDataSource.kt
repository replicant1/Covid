package com.rodbailey.covid.data.source

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    suspend fun saveRegions(regions: List<Region>)

    suspend fun loadAllRegions() : Flow<List<Region>>

    suspend fun loadRegionCount() : Flow<Int>

    suspend fun loadRegionsByIso3Code(iso3code: String) : Flow<List<Region>>

    suspend fun loadRegionsByName(searchText : String) : Flow<List<Region>>

    suspend fun saveReportData(iso3code: String, reportData: ReportData)

    suspend fun loadReportData(iso3code : String) : Flow<List<ReportData>>

    suspend fun loadReportDataCount(iso3code: String) : Flow<Int>

    suspend fun loadReportDataCount() : Flow<Int>
}