package com.rodbailey.covid.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.R
import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.presentation.core.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repo: ICovidRepository) : ViewModel() {

    // Text contents of search field
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Error text from network failures etc
    private val errorChannel = Channel<UIText>()
    val errorFlow = errorChannel.receiveAsFlow()

    // True if regional data panel is showing
    private val _isDataPanelExpanded = MutableStateFlow(false)
    val isDataPanelExpanded = _isDataPanelExpanded.asStateFlow()

    // True if loading progress indicator is showing on regional data panel
    private val _isDataPanelLoading = MutableStateFlow(true)
    val isDataPanelLoading = _isDataPanelLoading.asStateFlow()

    // True if master list of countries is being loaded
    private val _isRegionListLoading = MutableStateFlow(false)
    val isRegionListLoading = _isRegionListLoading.asStateFlow()

    // Raw region lists as loaded from network and sorted.
    private val allRegions = mutableListOf<Region>()

    // Subset of [allRegions] that matches the current [searchText]
    private val _matchingRegions = MutableStateFlow(
        emptyList<Region>()
    )
    val matchingRegions = _matchingRegions.asStateFlow()

    // The covid statistical data appearing in the data panel
    private val _reportData = MutableStateFlow(ReportData())
    val reportData = _reportData.asStateFlow()

    // Title string at the top of the data panel
    private val _reportDataTitle = MutableStateFlow("")
    val reportDataTitle = _reportDataTitle.asStateFlow()

    @VisibleForTesting
    fun showErrorMessage(message: UIText) {
        viewModelScope.launch {
            errorChannel.send(message)
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
                showErrorMessage(
                    UIText.StringResource(
                        R.string.failed_to_load_data_for,
                        regionName
                    )
                )
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
                showErrorMessage(UIText.StringResource(R.string.failed_to_load_country_list))
            } finally {
                _isRegionListLoading.value = false
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
        _searchText.value = text
        updateMatchingRegionsPerSearchText()
    }

    /**
     * Recalculate the regions list in light of the new search text
     */
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
