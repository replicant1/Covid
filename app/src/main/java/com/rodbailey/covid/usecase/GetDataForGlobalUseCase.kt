package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.data.repo.CovidRepository
import kotlinx.coroutines.flow.Flow

class GetDataForGlobalUseCase(private val repository: CovidRepository) {
    suspend operator fun invoke() : Flow<ReportData> {
        return repository.getReport(null)
    }
}