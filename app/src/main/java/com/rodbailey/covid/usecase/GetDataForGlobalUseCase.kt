package com.rodbailey.covid.usecase

import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.ReportData

class GetDataForGlobalUseCase(private val repository: ICovidRepository) {
    suspend operator fun invoke() : ReportData {
        return repository.getReport(null)
    }
}