package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.usecase.GetDataForGlobalUseCase
import com.rodbailey.covid.usecase.GetDataForRegionUseCase
import com.rodbailey.covid.usecase.InitialiseRegionListUseCase
import com.rodbailey.covid.usecase.MainUseCases
import com.rodbailey.covid.usecase.SearchRegionListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

    @Provides
    fun provideMainUseCases(repository: CovidRepository): MainUseCases {
        return MainUseCases(
            SearchRegionListUseCase(repository),
            InitialiseRegionListUseCase(repository),
            GetDataForRegionUseCase(repository),
            GetDataForGlobalUseCase(repository)
        )
    }
}