package com.rodbailey.covid.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.usecase.GetDataForGlobalUseCase
import com.rodbailey.covid.usecase.GetDataForRegionUseCase
import com.rodbailey.covid.usecase.InitialiseRegionListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(covidAPI: CovidAPI) : ViewModel() {

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

//    private val repo = CovidRepository(getApplication<Application>().applicationContext)

    init {
        println("**** Into MVM.init ****")
        println("**** covidAPI = $covidAPI ****")
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
        println("*** Loading report data for global ***")
        loadReportDataForRegion("Global", null)
    }

    fun loadReportDataForRegion(regionName: String, regionIso3Code: String?) {
        viewModelScope.launch {
            try {
                _isDataPanelExpanded.value = true
                _isDataPanelLoading.value = true
//                _reportData.value = if (regionIso3Code == null) {
//                    GetDataForGlobalUseCase(repo)()
//                } else {
//                    GetDataForRegionUseCase(repo)(regionIso3Code)
//                }
                _reportDataTitle.value = regionName
                println("*** Loaded region data for $regionName OK")
            } catch (ex: Exception) {
                println("Exception while getting data for region ${regionName}")
                showErrorMessage("Failed to load data for \"${regionName}\".")
                _isDataPanelExpanded.value = false
            } finally {
                println("*** Finished loading for rebion $regionName")
                _isDataPanelLoading.value = false
            }
        }
    }


    private fun loadRegionsFromNetwork() {
        viewModelScope.launch {
            try {
                _isRegionListLoading.value = true
//                _regions.value = InitialiseRegionListUseCase(repo)()
            } catch (ex: Exception) {
                println("Exception while loading counter list $ex")
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
