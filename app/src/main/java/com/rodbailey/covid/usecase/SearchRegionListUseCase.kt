package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.Region

class SearchRegionListUseCase(private val repository: ICovidRepository) {

    suspend operator fun invoke(searchText: String): List<Region> {
        return repository.getRegionsByName(searchText)
    }
}