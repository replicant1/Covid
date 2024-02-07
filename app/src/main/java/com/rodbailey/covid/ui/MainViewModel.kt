package com.rodbailey.covid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.RegionList
import com.rodbailey.covid.dom.Report
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPI
import com.rodbailey.covid.net.CovidAPIClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isDataPanelExpanded = MutableStateFlow(false)
    val isDataPanelExpanded = _isDataPanelExpanded.asStateFlow()

    private val _isDataPanelLoading = MutableStateFlow(true)
    val isDataPanelLoading = _isDataPanelLoading.asStateFlow()

    private val _isRegionListLoading = MutableStateFlow(true)
    val isRegionListLoading = _isRegionListLoading.asStateFlow()

    val covidAPI = CovidAPIClient().getAPIClient()?.create(CovidAPI::class.java)

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

    private val _reportData = MutableStateFlow<ReportData>(
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

    fun collapseDataPanel() {
        _isDataPanelExpanded.value = false
    }

    fun loadReportDataForRegion(region: Region) {
        println("*** Beginning network load of report data for region ${region.iso3Code}")
        _isDataPanelExpanded.value = true
        _isDataPanelLoading.value = true
        val call : Call<Report>? = covidAPI?.getReport(region.iso3Code)
        call?.enqueue(
            object : Callback<Report> {
                override fun onResponse(call: Call<Report>?, response: Response<Report>?) {
                    println("** onResponse = response")
                    if (response != null) {
                        val loadedData = response.body().data
                        println("** loadedData = $loadedData")
                        _reportData.value = loadedData
                        _reportDataTitle.value = region.name
                        _isDataPanelExpanded.value = true
                        _isDataPanelLoading.value = false
                    }
                }

                override fun onFailure(call: Call<Report>?, t: Throwable?) {
                    println("** onFailure $t")
                    _isDataPanelLoading.value = false
                    _isDataPanelExpanded.value = false
                }
            }
        )
    }

    private fun loadRegionsFromNetwork() {
        println("*** Beginning network load of countries")
        _isRegionListLoading.value = true
        val call: Call<RegionList>? = covidAPI?.getRegions()
        call?.enqueue(
            object : Callback<RegionList> {
                override fun onResponse(call: Call<RegionList>?, response: Response<RegionList>?) {
                    println("*** onResponse: region count =  ${response?.body()?.regions?.size}")
                    _isRegionListLoading.value = false
                    if (response != null) {
                        _regions.value = response.body().regions.sortedBy { it.name }
                    }
                }

                override fun onFailure(call: Call<RegionList>?, t: Throwable?) {
                    println("*** onFailure: $t")
                    _isRegionListLoading.value = false
                }
            }
        )
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }
}