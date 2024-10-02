package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.CovidRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class InitialiseRegionListUseCase(private val repository : CovidRepository) {

    suspend operator fun invoke() : Flow<List<Region>> {
        //return repository.getRegionsStream()
        return emptyFlow()
    }
}