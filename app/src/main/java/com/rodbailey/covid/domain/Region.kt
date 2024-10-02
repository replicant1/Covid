package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName
import com.rodbailey.covid.data.db.RegionEntity

/**
 * Geographical region that has a 3 letter ISO code.
 */
data class Region(
    @SerializedName("iso")
    val iso3Code: String,

    @SerializedName("name")
    val name: String
) {
}

fun Region.toRegionEntity() =
    RegionEntity(
        iso3code = this.iso3Code,
        name = this.name
    )

fun List<Region>.toRegionEntityList() = this.map { it.toRegionEntity() }
