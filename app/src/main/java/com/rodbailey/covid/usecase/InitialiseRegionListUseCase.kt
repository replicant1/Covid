package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.CovidRepository

class InitialiseRegionListUseCase(private val repository : CovidRepository) {

    suspend operator fun invoke() : List<Region> {
        return repository.getRegions()
    }
}