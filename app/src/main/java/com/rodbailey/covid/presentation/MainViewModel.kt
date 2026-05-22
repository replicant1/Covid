package com.rodbailey.covid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.R
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.GlobalCode
import com.rodbailey.covid.data.repo.RegionCode
import com.rodbailey.covid.data.repo.toReportData
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelClosed
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithData
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.CollapseDataPanel
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForGlobal
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.LoadReportDataForRegion
import com.rodbailey.covid.presentation.MainViewModel.MainIntent.OnSearchTextChanged
import com.rodbailey.covid.presentation.core.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: CovidRepository,
) :
    ViewModel() {

    /**
     * Putting the data common to all screens states in a data class lets us take advantage
     * of the "copy" function that Kotlin auto-generates for data classes, when it comes time
     * to move from one screen state to the next. The time-varying part of state is relegated to
     * the [DataPanelUIState].
     */
    data class UIState(
        val searchText: String = "",
        val matchingRegions: Result<List<Region>> = Result.Loading,
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
     * How the view communicates intent/actions to this ViewModel.
     */
    sealed interface MainIntent {
        data object CollapseDataPanel : MainIntent
        data object LoadReportDataForGlobal : MainIntent
        data class LoadReportDataForRegion(
            val regionName: UIText, val regionIso3Code: RegionCode
        ) : MainIntent

        data class OnSearchTextChanged(val text: String) : MainIntent
    }

    // Error text from network failures etc. Use a Channel to prevent event duplication on
    // configuration change. BUFFERED capacity avoids a race where an error is emitted before
    // the UI has subscribed to errorFlow — with the default RENDEZVOUS (capacity = 0) the send
    // would suspend until there is a receiver, and the message could be lost if the sending
    // coroutine is cancelled before a collector starts.
    private val errorChannel = Channel<UIText>(Channel.BUFFERED)
    val errorFlow = errorChannel.receiveAsFlow()

    private val regions: Flow<Result<List<Region>>> = repo.getRegionsStream().asResult()
    private val searchText = MutableStateFlow("")
    private val dataPanelUIState = MutableStateFlow<DataPanelUIState>(DataPanelClosed)

    // Filtered region list recomputed only when regions or search text change
    private val filteredRegions = combine(regions, searchText) { aRegions, aSearchText ->
        Pair(matchingRegionsResult(aRegions, aSearchText), aSearchText)
    }

    init {
        // Show the error message exactly once when the regions flow fails, not on every
        // combine re-emission (e.g. each keystroke) that would otherwise repeat the toast.
        viewModelScope.launch {
            if (regions.filter { it is Result.Error }.firstOrNull() != null) {
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            }
        }
    }

    // Communicates UI state changes to the view
    val uiState: StateFlow<UIState> = combine(
        filteredRegions,
        dataPanelUIState,
    ) { (aMatchingRegions, aSearchText), aDataPanelUIState ->
        UIState(
            dataPanelUIState = aDataPanelUIState,
            matchingRegions = aMatchingRegions,
            searchText = aSearchText
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UIState()
    )

    private fun matchingRegionsResult(aRegions: Result<List<Region>>, aSearchText: String) =
        if (aRegions is Result.Success) {
            val sortedRegions = aRegions.data.sortedBy { it.name }
            // Use case insensitive substring matching if search text is provided, otherwise return all
            val matchingRegions = if (aSearchText.isNotEmpty()) {
                sortedRegions.filter { it.name.contains(aSearchText, ignoreCase = true) }
            } else {
                sortedRegions
            }
            Result.Success(matchingRegions)
        } else {
            aRegions
        }

    fun processIntent(mainIntent: MainIntent) {
        when (mainIntent) {
            is CollapseDataPanel -> collapseDataPanel()
            is LoadReportDataForGlobal -> loadReportDataForGlobal()
            is LoadReportDataForRegion -> loadReportDataForRegion(
                mainIntent.regionName,
                mainIntent.regionIso3Code
            )

            is OnSearchTextChanged -> onSearchTextChanged(mainIntent.text)
        }
    }

    private fun showErrorMessage(message: UIText) {
        viewModelScope.launch {
            errorChannel.send(message)
        }
    }

    private fun collapseDataPanel() {
        dataPanelUIState.value = DataPanelClosed
    }

    private fun loadReportDataForGlobal() {
        loadReportDataForRegion(UIText.StringResource(R.string.region_global), GlobalCode())
    }

    private var loadReportJob: Job? = null

    private fun loadReportDataForRegion(regionName: UIText, regionIso3Code: RegionCode) {
        loadReportJob?.cancel()
        loadReportJob = viewModelScope.launch {
            try {
                dataPanelUIState.value = DataPanelUIState.DataPanelOpenWithLoading
                val regionStatsList = repo.getRegionStatsStream(regionIso3Code).first()
                if (regionStatsList.isEmpty()) {
                    showErrorMessage(
                        UIText.CompoundStringResource(
                            R.string.no_data_available_for,
                            regionName
                        )
                    )
                    dataPanelUIState.value = DataPanelClosed
                } else {
                    dataPanelUIState.value =
                        DataPanelOpenWithData(regionName, regionStatsList.first().toReportData())
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

    private fun onSearchTextChanged(text: String) {
        searchText.value = text
    }
}
