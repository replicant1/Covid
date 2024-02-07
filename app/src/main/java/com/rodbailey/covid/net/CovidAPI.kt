package com.rodbailey.covid.net

import com.rodbailey.covid.dom.RegionList
import com.rodbailey.covid.dom.Report
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Note: Retrofit handles "suspend" functions by performing call on background thread ie.
 * behind the scenes it behaves like a normal Call.enqueue operation. By not using the "Response"
 * type we force failures to be turned into exceptions. Retrofit makes these main-safe anyway.
 */
interface CovidAPI {

    /**
     * @return All known regions. Ordering unspecified.
     */
    @GET("/api/regions")
    suspend fun getRegions(): RegionList

    /**
     * Get covid statistics for a given region or all regions.
     * 
     * @param iso3Code ISO 3-letter alpha code of the region, as returned by [getRegions].
     * Pass null to get global report (ie. all regions)
     * @return Statistics for the region specified by [iso3Code]
     */
    @GET("/api/reports/total")
    suspend fun getReport(@Query("iso") iso3Code : String?) : Report
}