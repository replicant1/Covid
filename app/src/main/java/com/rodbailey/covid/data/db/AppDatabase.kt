package com.rodbailey.covid.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RegionEntity::class, RegionStatsEntity::class], version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun regionDao() : RegionDao
    abstract fun regionStatsDao() : RegionStatsDao
}