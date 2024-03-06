package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.domain.Region

class SearchRegionListUseCase(private val repository: CovidRepository) {

    suspend operator fun invoke(searchText: String): List<Region> {
        return emptyList() // repository.getRegionsByName(searchText)
    }
}