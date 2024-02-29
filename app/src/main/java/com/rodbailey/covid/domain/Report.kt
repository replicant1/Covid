package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName

/**
 * A collection of covid statistics
 */
data class Report(
    @SerializedName("data")
    val data : ReportData
)
