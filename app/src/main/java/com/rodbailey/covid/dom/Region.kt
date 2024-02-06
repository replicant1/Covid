package com.rodbailey.covid.dom

import com.google.gson.annotations.SerializedName

data class Region(
    @SerializedName("iso")
    val iso3Code:String,

    @SerializedName("name")
    val name:String)