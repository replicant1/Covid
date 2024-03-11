package com.rodbailey.covid.usecase

import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.data.repo.ICovidRepository
import kotlinx.coroutines.flow.Flow

class GetDataForGlobalUseCase(private val repository: ICovidRepository) {
    suspend operator fun invoke() : Flow<ReportData> {
        return repository.getReport(null)
    }
}