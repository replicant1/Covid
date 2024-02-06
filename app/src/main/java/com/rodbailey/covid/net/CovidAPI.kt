package com.rodbailey.covid.net

import com.rodbailey.covid.dom.RegionList
import com.rodbailey.covid.dom.Report
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CovidAPI {

    /**
     * @return All known regions. Ordering unspecified.
     */
    @GET("/api/regions")
    abstract fun getRegions(): Call<RegionList>

    /**
     * Get covid statistics for a given region or all regions.
     * 
     * @param iso3Code ISO 3-letter alpha code of the region, as returned by [getRegions].
     * Pass null to get global report (ie. all regions)
     * @return Statistics for the region specified by [iso3Code]
     */
    @GET("/api/reports/total")
    abstract fun getReport(@Query("iso") iso3Code : String?) : Call<Report>
}