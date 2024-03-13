package com.rodbailey.covid.data.source

import com.rodbailey.covid.data.net.CovidAPI
import kotlinx.coroutines.flow.flow

class DefaultRemoteDataSource(private val covidAPI: CovidAPI) : RemoteDataSource {
    override fun getRegions() = flow {
        emit(covidAPI.getRegions())
    }

    override fun getReport(iso3Code: String?) = flow {
        emit(covidAPI.getReport(iso3Code))
    }
}