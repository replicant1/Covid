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

    data class DataPanelOpenWithData(
        val reportDataTitle: UIText = UIText.DynamicString(""),
        val reportData: ReportData = ReportData(),
        val copyMe: UIState
    ) : UIState(copyMe) {
        override fun clone(): DataPanelOpenWithData {
            return DataPanelOpenWithData(reportDataTitle, reportData, this)
        }
    }

    data class DataPanelOpenWithLoading(val copyMe: UIState) : UIState(copyMe) {
        override fun clone(): DataPanelOpenWithLoading {
            return DataPanelOpenWithLoading(this)
        }
    }

    data class DataPanelClosed(val copyMe: UIState) : UIState(copyMe) {
        override fun clone(): DataPanelClosed {
            return DataPanelClosed(this)
        }
    }

    open class UIState(
        var isRegionListLoading: Boolean = false,
        var searchText: String = "",
        var matchingRegions: List<Region> = emptyList()
    ) {
        constructor(other: UIState) :
                this(
                    other.isRegionListLoading,
                    other.searchText,
                    other.matchingRegions
                )

        open fun clone(): UIState {
            return UIState(this)
        }
    }

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
            DataPanelClosed(it)
        }
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
                    _uiState.update {
                        DataPanelOpenWithLoading(it)
                    }
                }.collect { reportData: ReportData ->
                    Timber.i("Collected report data for $regionName = $reportData")
                    _uiState.update {
                        DataPanelOpenWithData(regionName, reportData, it)
                    }
                }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while getting report data for region \"$regionName\"")
                showErrorMessage(
                    UIText.CompoundStringResource(
                        R.string.failed_to_load_data_for,
                        regionName
                    )
                )
                _uiState.update {
                    DataPanelClosed(it)
                }
            }
        }
    }

    @VisibleForTesting
    fun loadRegionList() {
        viewModelScope.launch {
            try {
                mainUseCases.initialiseRegionListUseCase()
                    .onStart {
                        _uiState.update {
                            it.clone().apply {
                                isRegionListLoading = true
                            }
                        }
                    }
                    .onCompletion {
                        _uiState.update {
                            it.clone().apply {
                                isRegionListLoading = false
                            }
                        }
                    }
                    .collect {
                        updateMatchingRegionsPerSearchText()
                        _uiState.update {
                            it.clone().apply {
                                isRegionListLoading = false
                            }
                        }
                    }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while loading country list")
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update {
            it.clone().apply {
                searchText = text
            }
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
                        it.clone().apply {
                            matchingRegions = matches
                        }
                    }
                }
        }
    }
}
