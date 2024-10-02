package com.rodbailey.covid.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

//    @Provides
//    fun provideMainUseCases(repository: CovidRepository): MainUseCases {
//        return MainUseCases(
//            SearchRegionListUseCase(repository),
//            InitialiseRegionListUseCase(repository),
//            GetDataForRegionUseCase(repository),
//            GetDataForGlobalUseCase(repository)
//        )
//    }
}