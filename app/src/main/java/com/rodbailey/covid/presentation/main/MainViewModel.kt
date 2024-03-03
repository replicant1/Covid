package com.rodbailey.covid.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodbailey.covid.R
import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.usecase.GetDataForGlobalUseCase
import com.rodbailey.covid.usecase.GetDataForRegionUseCase
import com.rodbailey.covid.usecase.InitialiseRegionListUseCase
import com.rodbailey.covid.usecase.SearchRegionListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repo: ICovidRepository,
    val searchRegionListUseCase: SearchRegionListUseCase,
    val initialiseRegionListUseCase: InitialiseRegionListUseCase,
    val getDataForRegionUseCase: GetDataForRegionUseCase,
    val getDataForGlobalUseCase: GetDataForGlobalUseCase
) : ViewModel() {

    data class UIState(
        val isDataPanelExpanded: Boolean = false,
        val isDataPanelLoading: Boolean = false,
        val isRegionListLoading: Boolean = false,
        val reportDataTitle: String = "",
        val reportData: ReportData = ReportData(),
        val searchText: String = "",
        val matchingRegions: List<Region> = emptyList()
    )

    // Error text from network failures etc
    private val errorChannel = Channel<UIText>()
    val errorFlow = errorChannel.receiveAsFlow()

    // Raw region lists as loaded from network and sorted (single source of truth)
    private val allRegions = mutableListOf<Region>()

    // Communicates state changes to corresponding view
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

    fun loadReportDataForGlobal() {
        Timber.i("Loading report data for global")
        loadReportDataForRegion("Global", null)
    }

    fun loadReportDataForRegion(regionName: String, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isDataPanelExpanded = true, isDataPanelLoading = true)
                }
                val loadedReportData =
                    if (regionIso3Code == null) {
                        getDataForGlobalUseCase()
                    } else {
                        getDataForRegionUseCase(regionIso3Code)
                    }
                _uiState.update {
                    it.copy(reportDataTitle = regionName, reportData = loadedReportData)
                }
                Timber.i("Loaded region data for $regionName OK")
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while getting data for region ${regionName}")
                showErrorMessage(
                    UIText.StringResource(
                        R.string.failed_to_load_data_for,
                        regionName
                    )
                )
                _uiState.update {
                    it.copy(isDataPanelExpanded = false)
                }
            } finally {
                Timber.i("Finished loading for region $regionName")
                _uiState.update {
                    it.copy(isDataPanelLoading = false)
                }
            }
        }
    }

    @VisibleForTesting
    fun loadRegionsFromRepository() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isRegionListLoading = true)
                }
                allRegions.clear()
                allRegions.addAll(initialiseRegionListUseCase())
                updateMatchingRegionsPerSearchText()
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while loading counter list $ex")
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            } finally {
                _uiState.update {
                    it.copy(isRegionListLoading = false)
                }
            }
        }
    }

    /**
     * Invoked when user modifies text in the search field. Triggers update of list of
     * matching regions.
     *
     * @param text New search string to match against [Region] names
     */
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
            _uiState.update {
                it.copy(matchingRegions = searchRegionListUseCase(_uiState.value.searchText))
            }
        }
    }
}
