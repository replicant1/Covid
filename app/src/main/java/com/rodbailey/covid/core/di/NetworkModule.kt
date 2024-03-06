package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.net.CovidAPIClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    fun provideCovidAPI() : CovidAPI {
        return CovidAPIClient().getAPIClient()
    }

//    @Provides
//    fun provideCovidAPIHelper(covidAPI: CovidAPI) : CovidAPIHelper {
//        return CovidAPIHelperImpl(covidAPI)
//    }
}