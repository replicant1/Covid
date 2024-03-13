package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.db.AppDatabase
import com.rodbailey.covid.data.net.CovidAPIHelper
import com.rodbailey.covid.data.repo.DefaultCovidRepository
import com.rodbailey.covid.data.repo.CovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCovidRepository(db : AppDatabase, apiHelper: CovidAPIHelper) : CovidRepository {
        // Dispense the *real* repository, not the fake we use for testing
        return DefaultCovidRepository(db.regionDao(), db.regionStatsDao(), apiHelper)
    }
}