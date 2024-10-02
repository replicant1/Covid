package com.rodbailey.covid.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodbailey.covid.data.repo.RegionStats
import com.rodbailey.covid.domain.ReportData

@Entity(tableName = "stats")
data class RegionStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "iso3code")
    val iso3code: String,

    @ColumnInfo(name = "confirmed")
    val confirmed: Long,

    @ColumnInfo(name = "deaths")
    val deaths: Long,

    @ColumnInfo(name = "recovered")
    val recovered: Long,

    @ColumnInfo(name = "active")
    val active: Long,

    @ColumnInfo(name = "fatalityRate")
    val fatalityRate: Float
)

fun RegionStatsEntity.toRegionStats() =
    RegionStats(
        iso3Code = this.iso3code,
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        fatalityRate = this.fatalityRate
    )

fun RegionStatsEntity.toReportData() =
    ReportData(
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        fatalityRate = this.fatalityRate
    )
