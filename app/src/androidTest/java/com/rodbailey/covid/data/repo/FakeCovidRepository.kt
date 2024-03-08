package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import timber.log.Timber

class FakeCovidRepository() : ICovidRepository {

    private var allMethodsThrowException = false

    /**
     * @see [ICovidRepository.getRegions]
     */
    override suspend fun getRegions(): List<Region> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        return FakeRegions.REGIONS.keys.sortedBy { it.name }
    }

    /**
     * @see [ICovidRepository.getReport]
     */
    override suspend fun getReport(isoCode: String?): ReportData {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        if (isoCode == null) {
            return FakeRegions.GLOBAL_REGION_STATS
        }
        val result = FakeRegions.REGIONS.keys.filter { it.iso3Code == isoCode }
        if (result.isEmpty()) {
            Timber.e("No region found with ISO code $isoCode")
            throw RuntimeException("No region found with ISO code $isoCode")
        }
        val matchingData = FakeRegions.REGIONS[result[0]]
        return matchingData ?: throw RuntimeException("No region found with ISO code $isoCode")
    }

    override suspend fun getRegionsByName(searchText: String): List<Region> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        return FakeRegions.REGIONS.keys.filter { region ->
            region.name.contains(searchText, ignoreCase = true)
        }.sortedBy { it.name }
    }

    fun setAllMethodsThrowException(value: Boolean) {
        allMethodsThrowException = value
    }

}