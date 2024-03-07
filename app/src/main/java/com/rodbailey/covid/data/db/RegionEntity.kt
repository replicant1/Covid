package com.rodbailey.covid.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regions")
data class RegionEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "iso3code")
    val iso3code : String,

    @ColumnInfo(name = "name")
    val name : String

)