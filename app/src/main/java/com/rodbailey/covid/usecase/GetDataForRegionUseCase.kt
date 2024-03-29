package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.data.repo.CovidRepository
import kotlinx.coroutines.flow.Flow

class GetDataForRegionUseCase(private val repository: CovidRepository)  {

    suspend operator fun invoke(regionIso3Code : String) : Flow<ReportData> {
        return repository.getReport(regionIso3Code)
    }
}