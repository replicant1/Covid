package com.rodbailey.covid.net

import com.rodbailey.covid.dom.RegionList
import retrofit2.Call
import retrofit2.http.GET

interface CovidAPI {

    @GET("/api/regions")
    abstract fun getRegions(): Call<RegionList>
}