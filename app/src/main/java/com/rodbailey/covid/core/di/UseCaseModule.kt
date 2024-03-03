package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.usecase.InitialiseRegionListUseCase
import com.rodbailey.covid.usecase.SearchRegionListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

    @Provides
    fun provideSearchRegionListUse(repository: ICovidRepository) : SearchRegionListUseCase {
        return SearchRegionListUseCase(repository)
    }

    @Provides
    fun provideInitialiseRegionListUseCase(repository: ICovidRepository) : InitialiseRegionListUseCase {
        return InitialiseRegionListUseCase(repository)
    }
}