package com.rodbailey.covid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.RegionList
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

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val covidAPI = CovidAPIClient().getAPIClient()?.create(CovidAPI::class.java)

    private val _regions = MutableStateFlow(
        emptyList<Region>()
//        listOf<Region>(
//            Region("AUS", "Australia"),
//            Region("CHN", "China"),
//            Region("JPN", "Japan"),
//            Region("THA", "Thailand"),
//            Region("KOR", "South Korea"),
//            Region("SGP", "Singapore")
//        )
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

    init {
        loadRegionsFromNetwork()
    }

    private fun loadRegionsFromNetwork() {
        println("*** Beginning network load of countries")
        val call: Call<RegionList>? = covidAPI?.getRegions()
        call?.enqueue(
            object : Callback<RegionList> {
                override fun onResponse(call: Call<RegionList>?, response: Response<RegionList>?) {
                    println("*** onResponse: region count =  ${response?.body()?.regions?.size}")
                    if (response != null) {
                        _regions.value = response.body().regions
                    }
                }

                override fun onFailure(call: Call<RegionList>?, t: Throwable?) {
                    println("*** onFailure: $t")
                }
            }
        )
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }
}