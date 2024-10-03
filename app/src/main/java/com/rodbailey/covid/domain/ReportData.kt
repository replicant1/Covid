package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName
import com.rodbailey.covid.data.db.RegionStatsEntity
import com.rodbailey.covid.data.repo.RegionStats

/**
 * The statistical data for a given region (or multiple regions, depending on how it is retrieved.)
 */
data class ReportData(
    @SerializedName("confirmed")
    val confirmed: Long = -1L,

    @SerializedName("deaths")
    val deaths: Long = -1L,

    @SerializedName("recovered")
    val recovered: Long = -1L,

    @SerializedName("active")
    val active: Long = -1L,

    @SerializedName("fatality_rate")
    val fatalityRate: Float = -1F
)

fun ReportData.toRegionStatsEntity(iso3code: String): RegionStatsEntity =
    RegionStatsEntity(
        iso3code = iso3code,
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        fatalityRate = this.fatalityRate
    )

fun ReportData.toRegionStats(iso3code: String) : RegionStats =
    RegionStats(
        iso3Code = iso3code,
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        fatalityRate = this.fatalityRate
    )