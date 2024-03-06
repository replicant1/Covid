package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName

/**
 * The statistical data for a given region (or multiple regions, depending on how it is retrieved.)
 */
data class ReportData(
    @SerializedName("confirmed")
    val confirmed: Long,

    @SerializedName("deaths")
    val deaths: Long,

    @SerializedName("recovered")
    val recovered: Long,

    @SerializedName("active")
    val active: Long,

    @SerializedName("fatality_rate")
    val fatalityRate: Float
)