package com.rodbailey.covid.core.di

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.usecase.GetDataForGlobalUseCase
import com.rodbailey.covid.usecase.GetDataForRegionUseCase
import com.rodbailey.covid.usecase.InitialiseRegionListUseCase
import com.rodbailey.covid.usecase.MainUseCases
import com.rodbailey.covid.usecase.SearchRegionListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

    @Provides
    fun provideSearchRegionListUse(repository: ICovidRepository): SearchRegionListUseCase {
        return SearchRegionListUseCase(repository)
    }

    @Provides
    fun provideInitialiseRegionListUseCase(repository: ICovidRepository): InitialiseRegionListUseCase {
        Timber.d("**** Into UseCaseModule providing an InitialiseRegionListUseCase with repository of $repository")
        return InitialiseRegionListUseCase(repository)
    }

    @Provides
    fun provideGetDataForRegionUseCase(repository: ICovidRepository): GetDataForRegionUseCase {
        return GetDataForRegionUseCase(repository)
    }

    @Provides
    fun provideGetDataForGlobalUseCase(repository: ICovidRepository): GetDataForGlobalUseCase {
        return GetDataForGlobalUseCase(repository)
    }

    @Provides
    fun provideMainUseCases(repository: ICovidRepository): MainUseCases {
        val result = MainUseCases(
            SearchRegionListUseCase(repository),
            InitialiseRegionListUseCase(repository),
            GetDataForRegionUseCase(repository),
            GetDataForGlobalUseCase(repository)
        )
        Timber.d("**** Into real UseCaseModule providing  MainUseCases $result based on repository of $repository")
        return result
    }
}