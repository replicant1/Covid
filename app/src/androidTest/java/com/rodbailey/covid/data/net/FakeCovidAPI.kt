package com.rodbailey.covid.data.net

import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.RegionList
import com.rodbailey.covid.domain.Report

class FakeCovidAPI : CovidAPI {

    private var allMethodsThrowException = false

    private var aMethodWasCalledFlag = false

    override suspend fun getRegions(): RegionList {
        aMethodWasCalledFlag = true
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        return RegionList(FakeRegions.REGIONS.keys.sortedBy { it.name })
    }

    override suspend fun getReport(iso3Code: String?): Report {
        aMethodWasCalledFlag = true
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        return if (iso3Code == null) {
            Report(FakeRegions.GLOBAL_REGION_STATS)
        } else {
            val mapEntry = FakeRegions.REGIONS.filterKeys {
                region: Region ->  region.iso3Code == iso3Code
            }
            Report(mapEntry.values.first())
        }
    }

    fun setAllMethodsThrowException(value : Boolean) {
        allMethodsThrowException = value
    }

    fun wasCalled() : Boolean {
        return aMethodWasCalledFlag
    }

    fun clearWasCalled() {
        aMethodWasCalledFlag = false
    }
}