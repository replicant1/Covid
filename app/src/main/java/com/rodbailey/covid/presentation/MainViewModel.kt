package com.rodbailey.covid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.usecase.MainUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainUseCases: MainUseCases) : ViewModel() {

    data class UIState(
        val isDataPanelExpanded: Boolean = false,
        val isDataPanelLoading: Boolean = false,
        val isRegionListLoading: Boolean = false,
        val reportDataTitle: String = "",
        val reportData: ReportData = ReportData(),
        val searchText: String = "",
        val matchingRegions: List<Region> = emptyList()
    )

    // Communicates UI state changes to corresponding view
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    // Error text from network ops
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        println("**** Into MVM.init ****")
        println("**** mainUseCases = $mainUseCases ****")
        loadRegionList()
    }

    private fun showErrorMessage(message: String) {
        viewModelScope.launch {
            _errorMessage.emit(message)
        }
    }

    fun collapseDataPanel() {
        _uiState.update {
            it.copy(isDataPanelExpanded = false)
        }
    }

    private fun expandDataPanelLoading() {
        _uiState.update {
            it.copy(isDataPanelExpanded = true, isDataPanelLoading = true)
        }
    }

    fun loadReportDataForGlobal() {
        loadReportDataForRegion("Global", null)
    }

    fun loadReportDataForRegion(regionName: String, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                expandDataPanelLoading()

                val reportData = if (regionIso3Code == null) {
                    withContext(Dispatchers.IO) {
                        mainUseCases.getDataForGlobalUseCase()
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        mainUseCases.getDataForRegionUseCase(regionIso3Code)
                    }
                }

                _uiState.update {
                    it.copy(reportData = reportData, reportDataTitle = regionName)
                }
            } catch (ex: Exception) {
                showErrorMessage("Failed to load data for \"${regionName}\".")
                collapseDataPanel()
            } finally {
                _uiState.update {
                    it.copy(isDataPanelLoading = false)
                }
            }
        }
    }

    private fun loadRegionList() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isRegionListLoading = true,
                        matchingRegions = mainUseCases.initialiseRegionListUseCase()
                    )
                }
            } catch (ex: Exception) {
                println("Exception while loading counter list $ex")
                showErrorMessage("Failed to load country list.")
            } finally {
                _uiState.update {
                    it.copy(
                        isRegionListLoading =  false
                    )
                }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update {
            it.copy(searchText = text)
        }
        updateMatchingRegionsPerSearchText()
    }

    /**
     * Recalculate the regions list in light of the new search text
     */
    private fun updateMatchingRegionsPerSearchText() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uiState.update {
                    it.copy(matchingRegions = mainUseCases.searchRegionListUseCase(_uiState.value.searchText))
                }
            }
        }
    }
}
