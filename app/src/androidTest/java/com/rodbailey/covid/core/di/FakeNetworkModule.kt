package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.net.FakeCovidAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object FakeNetworkModule {

    @Provides
    fun provideCovidAPI() : CovidAPI {
        // Provide the fake version of the API (backed by fake data).
        return FakeCovidAPI()
    }
}