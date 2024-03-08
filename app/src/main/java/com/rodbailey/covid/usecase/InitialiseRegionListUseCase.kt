package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.ICovidRepository
import timber.log.Timber

class InitialiseRegionListUseCase(private val repository : ICovidRepository) {

    suspend operator fun invoke() : List<Region> {
        Timber.d("**** Into InitialiseRegionListUseCase. About to call getRegions() on repository $repository")
        return repository.getRegions()
    }
}