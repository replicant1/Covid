package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.ICovidRepository

class InitialiseRegionListUseCase(private val repository : ICovidRepository) {

    suspend operator fun invoke() : List<Region> {
        return repository.getRegions()
    }
}