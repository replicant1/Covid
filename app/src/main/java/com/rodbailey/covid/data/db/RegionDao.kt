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
    fun getAllRegions(): Flow<List<RegionEntity>>

    @Query("select count(*) from regions")
    fun getRegionCount(): Flow<Int>

    @Query("delete from regions")
    fun deleteAllRegions()

    @Query("select * from regions where iso3code = :iso3code")
    fun getRegionsByIso3Code(iso3code: String): Flow<List<RegionEntity>>

    @Query("select * from regions where name like '%' || :search || '%' order by name asc")
    fun getRegionsByName(search: String): Flow<List<RegionEntity>>
}