package com.rodbailey.covid.di

import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.repo.CovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCovidRepository(db : AppDatabase) : CovidRepository {
        return CovidRepository(db)
    }
}