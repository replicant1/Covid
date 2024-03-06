package com.rodbailey.covid.core.di

import android.content.Context
import androidx.room.Room
import com.rodbailey.covid.data.db.AppDatabase
import com.rodbailey.covid.data.db.RegionDao
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

//    @Provides
//    fun provideRegStatsDao(db: AppDatabase): RegionStatsDao {
//        return db.regionStatsDao()
//    }
}