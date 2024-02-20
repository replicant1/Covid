package com.rodbailey.covid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.repo.ICovidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repo: ICovidRepository) : ViewModel() {

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
    private val _isRegionListLoading = MutableStateFlow(false)
    val isRegionListLoading = _isRegionListLoading.asStateFlow()

    private val allRegions = mutableListOf<Region>()

    // Currently matching regions
    private val _matchingRegions = MutableStateFlow(
        emptyList<Region>()
    )
    val matchingRegions = _matchingRegions.asStateFlow()
//        searchText
//        .combine(_regions) { text: String, regions: List<Region> ->
//            if (text.isBlank()) {
//                regions
//            } else {
//                regions.filter {
//                    it.matchesSearchQuery(text)
//                }
//            }
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = _regions.value,
//        )

    private val _reportData = MutableStateFlow(ReportData())
    val reportData = _reportData.asStateFlow()

    private val _reportDataTitle = MutableStateFlow("Initial Title")
    val reportDataTitle = _reportDataTitle.asStateFlow()

    @VisibleForTesting
    fun showErrorMessage(message: String) {
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

    @VisibleForTesting
    fun loadRegionsFromRepository() {
        viewModelScope.launch {
            try {
                _isRegionListLoading.value = true
                allRegions.clear()
                allRegions.addAll(repo.getRegions())
                updateMatchingRegionsPerSearchText()
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
        updateMatchingRegionsPerSearchText()
    }

    // Recalculate the regions list in light of the new search text
    private fun updateMatchingRegionsPerSearchText() {
        val text = _searchText.value
        _matchingRegions.value = if (text.isBlank()) {
            allRegions
        } else {
            allRegions.filter {
                it.matchesSearchQuery(text)
            }
        }
    }
}
