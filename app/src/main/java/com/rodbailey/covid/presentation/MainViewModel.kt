package com.rodbailey.covid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.R
import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.DefaultCovidRepository
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelClosed
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithData
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithLoading
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.CollapseDataPanel
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadRegionList
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForGlobal
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForRegion
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.OnSearchTextChanged
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.usecase.MainUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainUseCases: MainUseCases, repo: CovidRepository) :
    ViewModel() {

    /**
     * Putting the data common to all screens states in a data class lets us take advantage
     * of the "copy" function that Kotlin auto-generates for data classes, when it comes time
     * to move from one screen state to the next. The time-varying part of state is relegated to
     * the [DataPanelUIState].
     */
    data class UIState(
        val isRegionListLoading: Boolean = false,
        val searchText: String = "",
        val matchingRegions: List<Region> = emptyList(),
        val dataPanelUIState: DataPanelUIState = DataPanelClosed
    )

    /**
     * Inheritance hierarchy here covers the mutually exclusive states of the Data Panel UI and
     * ensures we don't carry unnecessary data (reportDataTitle, reportData) for states to which
     * the data does not apply to.
     */
    sealed interface DataPanelUIState {
        data object DataPanelClosed : DataPanelUIState
        data object DataPanelOpenWithLoading : DataPanelUIState
        data class DataPanelOpenWithData(
            val reportDataTitle: UIText,
            val reportData: ReportData
        ) : DataPanelUIState
    }

    /**
     * The "I" from "MVI" - this is how the view communicates intent to the ViewModel.
     */
    sealed interface MainIntent {
        data object CollapseDataPanel : MainIntent
        data object LoadReportDataForGlobal : MainIntent
        data class LoadReportDataForRegion(
            val regionName: UIText, val regionIso3Code: String?
        ) : MainIntent

        data object LoadRegionList : MainIntent
        data class OnSearchTextChanged(val text: String) : MainIntent
        data class ShowErrorMessage(val message: UIText) : MainIntent
    }

    init {
        loadRegionList()
    }

    private val regions: Flow<List<Region>> = repo.getRegionsStream()

    // Error text from network failures. Use a Channel to prevent event duplication on
    // configuration change.
    private val errorChannel = Channel<UIText>()
    val errorFlow = errorChannel.receiveAsFlow()

    private val searchText = MutableStateFlow("")
    private val isRegionListLoading = MutableStateFlow(false)
    private val dataPanelUIState = MutableStateFlow<DataPanelUIState>(DataPanelClosed)

    // Communicates UI state changes to corresponding view
    //private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = combine(
        regions,
        searchText,
        isRegionListLoading,
        dataPanelUIState
    ) { _regions, _searchText, _isRegionListLoading, _dataPanelUIState ->
        println("** Received in MainViewModel: _regions = $_regions")
        UIState(
            isRegionListLoading = _isRegionListLoading,
            dataPanelUIState = _dataPanelUIState,
            matchingRegions = _regions.sortedBy { it.name }.filter { region ->
                region.name.uppercase().contains(_searchText.uppercase())
            },
            searchText = _searchText
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UIState()
    )

    fun processIntent(mainIntent: MainIntent) {
        when (mainIntent) {
            is CollapseDataPanel -> collapseDataPanel()
            is LoadReportDataForGlobal -> loadReportDataForGlobal()
            is LoadReportDataForRegion -> loadReportDataForRegion(
                mainIntent.regionName,
                mainIntent.regionIso3Code
            )

            is OnSearchTextChanged -> onSearchTextChanged(mainIntent.text)
            is LoadRegionList -> loadRegionList()
            is MainIntent.ShowErrorMessage -> showErrorMessage(mainIntent.message)
        }
    }

    private fun showErrorMessage(message: UIText) {
        viewModelScope.launch {
            //errorChannel.send(message)
        }
    }

    private fun collapseDataPanel() {
        dataPanelUIState.value = DataPanelClosed
    }

    private fun loadReportDataForGlobal() {
        loadReportDataForRegion(UIText.StringResource(R.string.region_global), null)
    }

    private fun loadReportDataForRegion(regionName: UIText, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                if (regionIso3Code == null) {
                    mainUseCases.getDataForGlobalUseCase()
                } else {
                    mainUseCases.getDataForRegionUseCase(regionIso3Code)
                }.onStart {
                    dataPanelUIState.value = DataPanelOpenWithLoading
                }.collect { reportData: ReportData ->
                    Timber.i("Collected report data for $regionName = $reportData")
                    dataPanelUIState.value = DataPanelOpenWithData(regionName, reportData)
                }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while getting report data for region \"$regionName\"")
                showErrorMessage(
                    UIText.CompoundStringResource(
                        R.string.failed_to_load_data_for,
                        regionName
                    )
                )
                dataPanelUIState.value = DataPanelClosed
            }
        }
    }

    private fun loadRegionList() {
        viewModelScope.launch {
            try {
                mainUseCases.initialiseRegionListUseCase()
                    .onStart {
                        println("** Into loadRegionList.onStart")
                        isRegionListLoading.value = true
                    }
                    .onCompletion {
                        println("** Into loadRegionList.onCompletion")
                        isRegionListLoading.value = false
                    }
                    .collect {
                        println("** Into loadRegionList.collect with num regions = ${it.size}")
                    }
            } catch (th: Throwable) {
                Timber.e(th, "Exception while loading country list")
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            }
        }
    }

    private fun onSearchTextChanged(text: String) {
        searchText.value = text
    }

}
