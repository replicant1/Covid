package com.rodbailey.covid.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {
    @Insert
    suspend fun insert(regions: List<RegionEntity>)

    @Query("select * from regions")
    fun getAllRegionsStream(): Flow<List<RegionEntity>>

    @Query("select count(*) from regions")
    suspend fun getRegionCount(): Int

}