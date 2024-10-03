package com.rodbailey.covid.data.repo

import com.rodbailey.covid.domain.ReportData

data class RegionStats(
    val iso3Code: String = "",
    val confirmed: Long = -1,
    val deaths: Long = -1,
    val recovered: Long = -1,
    val active: Long = -1,
    val fatalityRate: Float = -1f
)

fun RegionStats.toReportData() =
    ReportData(
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        fatalityRate = this.fatalityRate
    )

