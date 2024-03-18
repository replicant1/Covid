package com.rodbailey.covid.data.source

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import kotlinx.coroutines.flow.Flow

/**
 * Provides unified access to data that is stored locally (cached). Terminology is "save" and
 * "load". Data is presented in terms of the abstractions in the internal model, and return types
 * are Flows.
 *
 * When app first runs there is no data locally. As it is loaded over the network in response to
 * user interactions, it is stored locally (cached) and the data is sourced from there in
 * future.
 *
 * The only way to clear the cache is to uninstall the app.
 */
interface LocalDataSource {

    suspend fun saveRegions(regions: List<Region>)

    suspend fun loadAllRegions() : Flow<List<Region>>

    suspend fun loadRegionCount() : Flow<Int>

    suspend fun loadRegionsByIso3Code(iso3code: String) : Flow<List<Region>>

    suspend fun loadRegionsByName(searchText : String) : Flow<List<Region>>

    suspend fun saveReportData(iso3code: String, reportData: ReportData)

    suspend fun loadReportDataByIso3Code(iso3code : String) : Flow<List<ReportData>>

    suspend fun loadReportDataCount(iso3code: String) : Flow<Int>

    suspend fun loadReportDataCount() : Flow<Int>
}