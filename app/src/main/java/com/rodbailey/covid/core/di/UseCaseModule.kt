package com.rodbailey.covid.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

//    @Provides
//    fun provideSearchRegionListUse(repository: ICovidRepository): SearchRegionListUseCase {
//        return SearchRegionListUseCase(repository)
//    }
//
//    @Provides
//    fun provideInitialiseRegionListUseCase(repository: ICovidRepository): InitialiseRegionListUseCase {
//        return InitialiseRegionListUseCase(repository)
//    }
//
//    @Provides
//    fun provideGetDataForRegionUseCase(repository: ICovidRepository): GetDataForRegionUseCase {
//        return GetDataForRegionUseCase(repository)
//    }
//
//    @Provides
//    fun provideGetDataForGlobalUseCase(repository: ICovidRepository): GetDataForGlobalUseCase {
//        return GetDataForGlobalUseCase(repository)
//    }
//
//    @Provides
//    fun provideMainUseCases(repository: ICovidRepository): MainUseCases {
//        return MainUseCases(
//            SearchRegionListUseCase(repository),
//            InitialiseRegionListUseCase(repository),
//            GetDataForRegionUseCase(repository),
//            GetDataForGlobalUseCase(repository)
//        )
//    }
}