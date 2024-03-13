package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.source.DefaultLocalDataSource
import com.rodbailey.covid.data.source.LocalDataSource
import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.source.DefaultRemoteDataSource
import com.rodbailey.covid.data.source.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataSourceModule {

    @Provides
    @Singleton
    fun provideRemoteDataSource(covidAPI: CovidAPI): RemoteDataSource {
        return DefaultRemoteDataSource(covidAPI)
    }

    @Provides
    @Singleton
     fun provideLocalDataSource(regionDao: RegionDao, regionStatsDao: RegionStatsDao): LocalDataSource {
         return DefaultLocalDataSource(regionDao, regionStatsDao)
     }
}