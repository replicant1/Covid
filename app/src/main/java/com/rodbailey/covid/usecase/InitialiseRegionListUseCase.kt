package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.CovidRepository
import kotlinx.coroutines.flow.Flow

class InitialiseRegionListUseCase(private val repository : CovidRepository) {

    suspend operator fun invoke() : Flow<List<Region>> {
        return repository.getRegions()
    }
}