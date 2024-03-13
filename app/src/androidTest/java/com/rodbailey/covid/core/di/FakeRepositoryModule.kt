package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.data.repo.CovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class])
@Module
object FakeRepositoryModule {

    @Provides
    @Singleton
    fun provideFakeCovidRepository() : CovidRepository {
        // Dispense the *fake* repository we use for testing, not the real one
        return FakeCovidRepository()
    }
}