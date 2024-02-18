package com.rodbailey.covid.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegionStatsDao {
    @Insert
    suspend fun insert(regionStatsEntity : RegionStatsEntity)

    @Query("select * from stats where iso3code = :iso3code")
    suspend fun getRegionStats(iso3code : String) : List<RegionStatsEntity>

    @Query("delete from stats")
    suspend fun deleteAllRegionStats()

    @Query("select count(*) from stats")
    suspend fun getRegionStatsCount() : Int
}