package com.rodbailey.covid.di

import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.net.CovidAPI
import com.rodbailey.covid.repo.CovidRepository
import com.rodbailey.covid.repo.ICovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCovidRepository(db : AppDatabase, api: CovidAPI) : ICovidRepository {
        // Dispense the *real* repository, not the fake we use for testing
        return CovidRepository(db.regionDao(), db.regionStatsDao(), api)
    }
}