package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.data.repo.ICovidRepository
import kotlinx.coroutines.flow.Flow

class GetDataForRegionUseCase(private val repository: ICovidRepository)  {

    suspend operator fun invoke(regionIso3Code : String) : Flow<ReportData> {
        return repository.getReport(regionIso3Code)
    }
}