package com.rodbailey.covid.data.repo

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import kotlinx.coroutines.flow.Flow

/**
 * Provides covid data from some source - perhaps network, perhaps local database
 * - clients do not know, the repository decides where to get the data.
 */
interface CovidRepository {

    /**
     * Get a report of covid statistics for the region identified by the given [isoCode]
     *
     * @param isoCode ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if no country with the given [isoCode] or other error
     */
     suspend fun getReport(isoCode : String?): Flow<ReportData>

    /**
     * Get a sorted list of all known regions that have covid statistics available through
     * [getReport].
     *
     * @return All known regions in ascending order by name
     * @throws Exception any error in getting the region list
     */
     //suspend fun getRegions(): Flow<List<Region>>

     fun getRegionsStream(): Flow<List<Region>>

     //suspend fun getRegionsByName(searchText: String) : Flow<List<Region>>
}