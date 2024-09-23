package com.rodbailey.covid.usecase

import javax.inject.Inject


class MainUseCases constructor(
    val searchRegionListUseCase: SearchRegionListUseCase,
    val initialiseRegionListUseCase: InitialiseRegionListUseCase,
    val getDataForRegionUseCase: GetDataForRegionUseCase,
    val getDataForGlobalUseCase: GetDataForGlobalUseCase
)