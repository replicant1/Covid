package com.rodbailey.covid.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class RegionStatsEntity (
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
