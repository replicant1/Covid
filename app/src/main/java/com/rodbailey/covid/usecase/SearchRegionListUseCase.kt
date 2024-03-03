package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.Region

class SearchRegionListUseCase(val repository: ICovidRepository) {

    suspend operator fun invoke(searchText: String): List<Region> {
        val allRegions = repository.getRegions()
        return if (searchText.isBlank()) {
            allRegions
        } else {
            allRegions.filter {
                it.matchesSearchQuery(searchText)
            }
        }
    }
}