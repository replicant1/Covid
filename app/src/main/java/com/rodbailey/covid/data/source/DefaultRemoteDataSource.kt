package com.rodbailey.covid.data.source

import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.RegionList
import com.rodbailey.covid.domain.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DefaultRemoteDataSource(private val covidAPI: CovidAPI) : RemoteDataSource {
    init {
        println("**** $this constructed with $covidAPI")
    }

    override fun loadRegions() : Flow<RegionList> = flow {
        emit(covidAPI.getRegions())
    }

    override fun loadReportDataByIso3Code(iso3Code: String?) : Flow<Report> = flow {
        emit(covidAPI.getReport(iso3Code))
    }
}