package com.rodbailey.covid.repo

import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPIClient

/**
 * Accesses covid data from some source - perhaps network, perhaps local database
 * ... clients do not know. Only network is currently supported.
 */
class CovidRepository {
    private val covidAPI = CovidAPIClient().getAPIClient()

    /**
     * @param ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getReport(regionIso3Code : String?) : ReportData {
        return covidAPI.getReport(regionIso3Code).data
    }

    /**
     * @return All known regions in ascending order by name
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getRegions() : List<Region> {
        return covidAPI.getRegions().regions.sortedBy { it.name }
    }
}