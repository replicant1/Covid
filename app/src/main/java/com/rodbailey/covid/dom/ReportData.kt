package com.rodbailey.covid.dom

import com.google.gson.annotations.SerializedName

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