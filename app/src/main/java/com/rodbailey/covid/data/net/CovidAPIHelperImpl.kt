package com.rodbailey.covid.data.net

import kotlinx.coroutines.flow.flow

class CovidAPIHelperImpl(private val covidAPI: CovidAPI) : CovidAPIHelper {
    override fun getRegions() = flow {
        println("**** Into HelperImpl getRegions about to emit")
        emit(covidAPI.getRegions())
        println("**** Exiting HellperImpl getRegions")
    }

    override fun getReport(iso3Code: String?) = flow {
        emit(covidAPI.getReport(iso3Code))
    }
}