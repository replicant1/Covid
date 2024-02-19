package com.rodbailey.covid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.repo.CovidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repo: CovidRepository): ViewModel() {

    // Text contents of search field
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Error text from network ops
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // True if regional data panel is showing
    private val _isDataPanelExpanded = MutableStateFlow(false)
    val isDataPanelExpanded = _isDataPanelExpanded.asStateFlow()

    // True if loading progress indicator is showing on regional data panel
    private val _isDataPanelLoading = MutableStateFlow(true)
    val isDataPanelLoading = _isDataPanelLoading.asStateFlow()

    // True if master list of countries is being loaded
    private val _isRegionListLoading = MutableStateFlow(true)
    val isRegionListLoading = _isRegionListLoading.asStateFlow()

    // Currently matching regions
    private val _regions = MutableStateFlow(
        emptyList<Region>()
    )
    val regions = searchText
        .combine(_regions) { text: String, regions: List<Region> ->
            if (text.isBlank()) {
                regions
            } else {
                regions.filter {
                    it.matchesSearchQuery(text)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _regions.value,
        )

    private val _reportData = MutableStateFlow(
        ReportData(
            confirmed = 10, deaths = 20, recovered = 30, active = 40, fatalityRate = 1.2F
        )
    )
    val reportData = _reportData.asStateFlow()

    private val _reportDataTitle = MutableStateFlow<String>("Initial Title")
    val reportDataTitle = _reportDataTitle.asStateFlow()

    init {
        loadRegionsFromNetwork()
    }

    private fun showErrorMessage(message: String) {
        viewModelScope.launch {
            _errorMessage.emit(message)
        }
    }

    fun collapseDataPanel() {
        _isDataPanelExpanded.value = false
    }

    fun loadReportDataForGlobal() {
        Timber.i("Loading report data for global")
        loadReportDataForRegion("Global", null)
    }

    fun loadReportDataForRegion(regionName: String, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                _isDataPanelExpanded.value = true
                _isDataPanelLoading.value = true
                _reportData.value = repo.getReport(regionIso3Code)
                _reportDataTitle.value = regionName
                Timber.i("Loaded region data for $regionName OK")
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while getting data for region ${regionName}")
                showErrorMessage("Failed to load data for \"${regionName}\".")
                _isDataPanelExpanded.value = false
            } finally {
                Timber.i("Finished loading for region $regionName")
                _isDataPanelLoading.value = false
            }
        }
    }


    private fun loadRegionsFromNetwork() {
        viewModelScope.launch {
            try {
                _isRegionListLoading.value = true
                _regions.value = repo.getRegions()
            } catch (ex: Exception) {
                Timber.e(ex, "Exception while loading counter list $ex")
                showErrorMessage("Failed to load country list.")
            } finally {
                _isRegionListLoading.value = false
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }
}
