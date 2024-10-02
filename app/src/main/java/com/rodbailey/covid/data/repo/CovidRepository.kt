package com.rodbailey.covid.data.repo

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import kotlinx.coroutines.flow.Flow

/**
 * Provides covid data from some source - perhaps network, perhaps local database
 * - clients do not know.
 */
interface CovidRepository {

    suspend fun getReport(isoCode: String?): Flow<ReportData>

    fun getRegionsStream(): Flow<List<Region>>

    fun getRegionStatsStream(iso3code: String?): Flow<List<RegionStats>>

}