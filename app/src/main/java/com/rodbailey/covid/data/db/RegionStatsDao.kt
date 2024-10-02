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
    suspend fun getRegionStats(iso3code: String): List<RegionStatsEntity>

    @Query("select * from stats where iso3code = :iso3code")
    fun getRegionStatsStream(iso3code: String): Flow<List<RegionStatsEntity>>
}