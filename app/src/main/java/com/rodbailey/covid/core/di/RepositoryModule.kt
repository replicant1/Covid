package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.source.LocalDataSource
import com.rodbailey.covid.data.source.RemoteDataSource
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
    fun provideCovidRepository(localDataSource : LocalDataSource, remoteDataSource: RemoteDataSource) : CovidRepository {
        // Dispense the *real* repository, not the fake we use for testing
        return DefaultCovidRepository(localDataSource, remoteDataSource)
    }
}