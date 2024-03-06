package com.rodbailey.covid.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

//    @Provides
//    fun provideCovidRepository(db : AppDatabase, apiHelper: CovidAPIHelper) : ICovidRepository {
//        // Dispense the *real* repository, not the fake we use for testing
//        return CovidRepository(db.regionDao(), db.regionStatsDao(), apiHelper)
//    }
}