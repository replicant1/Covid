package com.rodbailey.covid.di

import com.rodbailey.covid.core.di.RepositoryModule
import com.rodbailey.covid.repo.FakeCovidRepository
import com.rodbailey.covid.data.repo.ICovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class])
@Module
object FakeRepositoryModule {

    @Provides
    fun provideFakeCovidRepository() : ICovidRepository {
        // Dispense the *fake* repository we use for testing, not the real one
        return FakeCovidRepository()
    }
}