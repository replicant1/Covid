package com.rodbailey.covid.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionStatsDao {
    @Insert
    suspend fun insert(regionStatsEntity: RegionStatsEntity)

    @Query("select * from stats where iso3code = :iso3code")
    fun getRegionStats(iso3code: String): List<RegionStatsEntity>

    @Query("select count(*) from stats where iso3code = :iso3code")
    fun getRegionStatsCount(iso3code: String) : Int

    @Query("delete from stats")
    fun deleteAllRegionStats()

    @Query("select count(*) from stats")
    fun getRegionStatsCount(): Int

    @Query("select * from stats")
    suspend fun getAllRegionStats(): List<RegionStatsEntity>
}