package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName

data class RegionList(
    @SerializedName("data")
    val regions:List<Region>)