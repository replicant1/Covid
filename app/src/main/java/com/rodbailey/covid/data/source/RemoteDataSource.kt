package com.rodbailey.covid.data.source

import com.rodbailey.covid.domain.RegionList
import com.rodbailey.covid.domain.Report
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to remote data available over network. Terminology is "save" and "load".
 * Data is presented in terms of the abstractions in the internal model and return type
 * are Flows.
 */
interface RemoteDataSource {

    fun loadRegions() : Flow<RegionList>

    fun loadReportDataByIso3Code(iso3Code : String?) : Flow<Report>
}