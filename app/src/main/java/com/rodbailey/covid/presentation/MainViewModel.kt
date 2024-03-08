package com.rodbailey.covid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.R
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.usecase.MainUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
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

    // Error text from network failures. Use a Channel to prevent event duplication on
    // configuration change.
    private val errorChannel = Channel<UIText>()
    val errorFlow = errorChannel.receiveAsFlow()

    // Communicates UI state changes to corresponding view
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    @VisibleForTesting
     fun showErrorMessage(message: UIText) {
        viewModelScope.launch {
            errorChannel.send(message)
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
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_data_for, regionName))
                collapseDataPanel()
            } finally {
                _uiState.update {
                    it.copy(isDataPanelLoading = false)
                }
            }
        }
    }

    fun loadRegionList() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isRegionListLoading = true,
                        matchingRegions = mainUseCases.initialiseRegionListUseCase()
                    )
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while loading country list.")
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            } finally {
                _uiState.update {
                    it.copy(
                        isRegionListLoading = false
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
