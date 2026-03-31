package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.toRegionStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeCovidRepository() : CovidRepository {

    /**
     * If tests set this to true, the next call to [getRegions] or [getReport] will throw
     * an exception.
     */
    private var allMethodsThrowException = false

    /**
     * If tests set this to true, [getRegionStatsStream] will return an empty list.
     */
    private var regionStatsEmpty = false

    /**
     * @see [CovidRepository.getRegions]
     */
    override fun getRegionsStream(): Flow<List<Region>> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        // Emulate the flow produced by Room that first emits "empty" before emitting an
        // updated region list.
        return flowOf(
            emptyList(),
            FakeRegions.REGIONS.keys.toList())
    }

    override fun getRegionStatsStream(iso3code: RegionCode): Flow<List<RegionStats>> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }

        if (regionStatsEmpty) {
            return flowOf(emptyList())
        }

        val reportData = if (iso3code is GlobalCode) {
            FakeRegions.GLOBAL_REGION_STATS
        } else {
            val region = FakeRegions.REGIONS.keys.first { it.iso3Code == iso3code.chars }
            FakeRegions.REGIONS[region]
        }

        return flowOf(listOf(reportData!!.toRegionStats(iso3code.chars)))
    }

    fun setAllMethodsThrowException(value: Boolean) {
        allMethodsThrowException = value
    }

    fun setRegionStatsEmpty(value: Boolean) {
        regionStatsEmpty = value
    }

}