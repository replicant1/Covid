package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.domain.Region
import kotlinx.coroutines.flow.Flow

class SearchRegionListUseCase(private val repository: CovidRepository) {

    suspend operator fun invoke(searchText: String): Flow<List<Region>> {
        return repository.getRegionsByName(searchText)
    }
}