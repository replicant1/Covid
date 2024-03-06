package com.rodbailey.covid.usecase

class MainUseCases /*@Inject*/ constructor(
    val searchRegionListUseCase: SearchRegionListUseCase,
    val initialiseRegionListUseCase: InitialiseRegionListUseCase,
    val getDataForRegionUseCase: GetDataForRegionUseCase,
    val getDataForGlobalUseCase: GetDataForGlobalUseCase
)