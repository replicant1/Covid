package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.Region

class InitialiseRegionListUseCase(val repository : ICovidRepository) {

    suspend operator fun invoke() : List<Region> {
        return repository.getRegions()
    }
}