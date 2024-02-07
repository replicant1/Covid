package com.rodbailey.covid.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {
    @Insert
    suspend fun insert(regions : List<RegionEntity>)

    @Query("select * from regions")
    suspend fun getAllRegions() : List<RegionEntity>

    @Query("select count(*) from regions")
    suspend fun getRegionCount() : Int

    @Query("delete from regions")
    suspend fun deleteAllRegions()
}