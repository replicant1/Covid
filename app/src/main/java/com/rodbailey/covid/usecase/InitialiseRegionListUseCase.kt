package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.data.repo.ICovidRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class InitialiseRegionListUseCase(private val repository : ICovidRepository) {

    suspend operator fun invoke() : Flow<List<Region>> {
        return repository.getRegions()
    }
}