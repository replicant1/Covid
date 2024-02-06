package com.rodbailey.covid.dom

import com.google.gson.annotations.SerializedName

data class RegionList(
    @SerializedName("data")
    val regions:List<Region>)