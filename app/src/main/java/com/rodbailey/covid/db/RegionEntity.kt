package com.rodbailey.covid.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regions")
data class RegionEntity (
    @PrimaryKey
    val iso3code : String,
    @ColumnInfo(name = "name")
    val name : String
)