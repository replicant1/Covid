package com.rodbailey.covid.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegionStatsDao {
    @Insert
    suspend fun insert(regionStatsEntity: RegionStatsEntity)

    @Query("select * from stats where iso3code = :iso3code")
    suspend fun getRegionStats(iso3code: String): List<RegionStatsEntity>

    @Query("select count(*) from stats where iso3code = :iso3code")
    suspend fun getRegionStatsCount(iso3code: String) : Int

    @Query("select count(*) from stats")
    suspend fun getRegionStatsCount(): Int

}