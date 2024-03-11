package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.db.AppDatabase
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.net.CovidAPIHelper
import com.rodbailey.covid.data.net.CovidAPIHelperImpl
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.ICovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCovidRepository(db : AppDatabase, apiHelper: CovidAPIHelper) : ICovidRepository {
        // Dispense the *real* repository, not the fake we use for testing
        return CovidRepository(db.regionDao(), db.regionStatsDao(), apiHelper)
    }

    @Provides
    fun provideCovidAPIHelper(covidAPI: CovidAPI) : CovidAPIHelper {
        return CovidAPIHelperImpl(covidAPI)
    }
}