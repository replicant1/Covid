package com.rodbailey.covid.data.net

import com.rodbailey.covid.domain.RegionList
import com.rodbailey.covid.domain.Report
import kotlinx.coroutines.flow.Flow

interface CovidAPIHelper {
    fun getRegions() : Flow<RegionList>
    fun getReport(iso3Code : String?) : Flow<Report>
}