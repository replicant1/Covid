package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.repo.CovidRepository

class GetDataForGlobalUseCase(private val repository: CovidRepository) {
    suspend operator fun invoke() : ReportData {
        return repository.getReport(null)
    }
}