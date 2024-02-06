package com.rodbailey.covid.dom

import com.google.gson.annotations.SerializedName

/**
 * A collection of covid statistics
 */
data class Report(
    @SerializedName("data")
    val data : ReportData
)
