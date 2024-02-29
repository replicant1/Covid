package com.rodbailey.covid.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegionDao {
    @Insert
    suspend fun insert(regions : List<RegionEntity>)

    @Query("select * from regions")
    suspend fun getAllRegions() : List<RegionEntity>

    @Query("select count(*) from regions")
    suspend fun getRegionCount() : Int

    @Query("select * from regions where iso3code = :iso3code")
    suspend fun getRegionsByIso3Code(iso3code : String) : List<RegionEntity>

    @Query("delete from regions")
    suspend fun deleteAllRegions()
}