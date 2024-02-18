package com.rodbailey.covid.di

import android.content.Context
import androidx.room.Room
import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.db.RegionDao
import com.rodbailey.covid.db.RegionStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "covid"
        ).build()
    }

    @Provides
    fun provideRegionDao(db: AppDatabase): RegionDao {
        return db.regionDao()
    }

    @Provides
    fun provideRegStatsDao(db: AppDatabase): RegionStatsDao {
        return db.regionStatsDao()
    }
}