package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.DefaultCovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCovidRepository(
        regionDao: RegionDao,
        regionStatsDao: RegionStatsDao,
        covidAPI: CovidAPI
    ): CovidRepository {
        // Dispense the *real* repository, not the fake we use for testing
        return DefaultCovidRepository(regionDao, regionStatsDao, covidAPI)
    }
}