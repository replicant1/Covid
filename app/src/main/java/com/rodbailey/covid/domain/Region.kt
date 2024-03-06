package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName

/**
 * Geographical region that has a 3 letter ISO code.
 */
data class Region(
    @SerializedName("iso")
    val iso3Code:String,

    @SerializedName("name")
    val name:String) {

    fun matchesSearchQuery(query : String) : Boolean {
        return name.contains(query, ignoreCase = true)
    }
}