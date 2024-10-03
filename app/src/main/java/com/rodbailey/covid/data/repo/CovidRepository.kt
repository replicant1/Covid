package com.rodbailey.covid.data.repo

import com.rodbailey.covid.domain.Region
import kotlinx.coroutines.flow.Flow

/**
 * Provides covid data from some source - perhaps network, perhaps local database
 * - clients do not know.
 */
interface CovidRepository {

    /**
     * @return Hot flow of all known regions in no particular order - never completes
     */
    fun getRegionsStream(): Flow<List<Region>>

    /**
     * @return Cold flow of covid stats for the given region - completes after one emission
     */
    suspend fun getRegionStatsStream(code: RegionCode): Flow<List<RegionStats>>

}