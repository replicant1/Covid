package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.data.repo.ICovidRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import timber.log.Timber

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class])
@Module
object FakeRepositoryModule {

    @Provides
    fun provideFakeCovidRepository() : ICovidRepository {
        // Dispense the *fake* repository we use for testing, not the real one
        Timber.d("**** Dispensing fake covid repo ****")
        return FakeCovidRepository()
    }
}