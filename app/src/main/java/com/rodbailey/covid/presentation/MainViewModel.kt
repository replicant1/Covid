package com.rodbailey.covid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.R
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.usecase.MainUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainUseCases: MainUseCases) : ViewModel() {

    open class SecondUIState {
        object Noop : SecondUIState()
        object RegionListLoading : SecondUIState()

        object DataPanelLoading : SecondUIState()
        class DataPanelLoaded(val reportDataTitle : UIText, val reportData: ReportData) : SecondUIState()
        object DataPanelClosed : SecondUIState()
    }

    data class UIState(
//        val isDataPanelExpanded: Boolean = false,
//        val isDataPanelLoading: Boolean = false,
//        val isRegionListLoading: Boolean = false,
//        val reportDataTitle: UIText = UIText.DynamicString(""),
//        val reportData: ReportData = ReportData(),
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

    private val _secondUIState = MutableStateFlow(SecondUIState())
    val secondUIState = _secondUIState.asStateFlow()

    @VisibleForTesting
    fun showErrorMessage(message: UIText) {
        viewModelScope.launch {
            errorChannel.send(message)
        }
    }

    fun collapseDataPanel() {
//        _uiState.update {
//            it.copy(isDataPanelExpanded = false)
//        }
        _secondUIState.value = SecondUIState.DataPanelClosed
    }

    fun loadReportDataForGlobal() {
        loadReportDataForRegion(UIText.StringResource(R.string.region_global), null)
    }

    fun loadReportDataForRegion(regionName: UIText, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                if (regionIso3Code == null) {
                    mainUseCases.getDataForGlobalUseCase()
                } else {
                    mainUseCases.getDataForRegionUseCase(regionIso3Code)
                }.onStart {
//                    _uiState.update {
//                        it.copy(
//                            isDataPanelExpanded = true,
//                            isDataPanelLoading = true
//                        )
//                    }
                    _secondUIState.value = SecondUIState.DataPanelLoading
                }.onCompletion {
//                    _uiState.update {
//                        it.copy(isDataPanelLoading = false)
//                    }
                }.collect { reportData: ReportData ->
                    Timber.i("Collected report data for $regionName = $reportData")
//                    _uiState.update {
//                        it.copy(
//                            reportDataTitle = regionName,
//                            reportData = reportData
//                        )
//                    }
                    _secondUIState.value = SecondUIState.DataPanelLoaded(regionName, reportData)
                }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while getting report data for region \"$regionName\"")
                showErrorMessage(
                    UIText.CompoundStringResource(
                        R.string.failed_to_load_data_for,
                        regionName
                    )
                )
//                _uiState.update {
//                    it.copy(isDataPanelExpanded = false)
//                }
                _secondUIState.value = SecondUIState.DataPanelClosed
            }
        }
    }

    @VisibleForTesting
    fun loadRegionList() {
        viewModelScope.launch {
            try {
                mainUseCases.initialiseRegionListUseCase()
                    .onStart {
//                        _uiState.update {
//                            it.copy(isRegionListLoading = true)
//                        }
                        _secondUIState.value = SecondUIState.RegionListLoading
                    }
                    .onCompletion {
//                        _uiState.update {
//                            it.copy(isRegionListLoading = false)
//                        }
                        _secondUIState.value = SecondUIState.Noop
                    }
                    .collect {
                        updateMatchingRegionsPerSearchText()
//                        _uiState.update {
//                            it.copy(isRegionListLoading = false)
//                        }
                        _secondUIState.value = SecondUIState.Noop
                    }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while loading country list")
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
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
            mainUseCases.searchRegionListUseCase(_uiState.value.searchText)
                .collect { matches ->
                    _uiState.update {
                        it.copy(matchingRegions = matches)
                    }
                }
        }
    }
}
