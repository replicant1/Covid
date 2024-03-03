package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.ReportData

class GetDataForRegionUseCase(private val repository: ICovidRepository)  {

    suspend operator fun invoke(regionIso3Code : String) :  ReportData {
        return repository.getReport(regionIso3Code)
    }
}