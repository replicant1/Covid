package com.rodbailey.covid.presentation.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rodbailey.covid.core.di.CoroutinesTestRule
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.data.repo.FakeCovidRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    lateinit var fakeCovidRepository: FakeCovidRepository

    lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        fakeCovidRepository = FakeCovidRepository()
        viewModel = MainViewModel(fakeCovidRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regionsLoadingProgressBarHiddenAtStartup() = runTest(UnconfinedTestDispatcher()) {
        Assert.assertFalse(viewModel.uiState.value.isRegionListLoading)
    }

    @Test
    fun regionsLoadingGivesException() = runTest {
        fakeCovidRepository.nextOpThrowsException = true
        viewModel.loadRegionsFromRepository()
    }

    @Test
    fun regionStatsLoadingGivesException() = runTest {
        fakeCovidRepository.nextOpThrowsException = true
        viewModel.loadReportDataForRegion("Australia", "AUS")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regionsLoadAtStartup() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadRegionsFromRepository()
        Assert.assertEquals(FakeCovidRepository.REGIONS.size, viewModel.uiState.value.matchingRegions.size)
        Assert.assertFalse(viewModel.uiState.value.isRegionListLoading)

        // Check sorting of regions by name
        Assert.assertEquals("Afghanistan", viewModel.uiState.value.matchingRegions.first().name)
        Assert.assertEquals("Vietnam", viewModel.uiState.value.matchingRegions.last().name)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun showErrorMessage() = runTest(UnconfinedTestDispatcher()) {
        viewModel.showErrorMessage(UIText.DynamicString("An error has occurred."))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun dataPanelIsHiddenAtStartup() = runTest(UnconfinedTestDispatcher()) {
        Assert.assertFalse(viewModel.uiState.value.isDataPanelExpanded)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collapseDataPanel_IsCaptured() = runTest(UnconfinedTestDispatcher()) {
        viewModel.collapseDataPanel()
        Assert.assertFalse(viewModel.uiState.value.isDataPanelExpanded)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReportForRegion_ReportDataAppearsInDataPanel() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadReportDataForRegion("China", "CHN")
        Assert.assertTrue(viewModel.uiState.value.isDataPanelExpanded)
        Assert.assertFalse(viewModel.uiState.value.isDataPanelLoading)
        Assert.assertEquals("China", viewModel.uiState.value.reportDataTitle)
        Assert.assertEquals(FakeCovidRepository.DEFAULT_REPORT_DATA, viewModel.uiState.value.reportData)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReportForGlobal_GlobalDataAppearsInDataPanel() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadReportDataForGlobal()
        Assert.assertTrue(viewModel.uiState.value.isDataPanelExpanded)
        Assert.assertFalse(viewModel.uiState.value.isDataPanelLoading)
        Assert.assertEquals(
            FakeCovidRepository.GLOBAL_DATA_SET_TITLE,
            viewModel.uiState.value.reportDataTitle
        )
        Assert.assertEquals(FakeCovidRepository.GLOBAL_REPORT_DATA, viewModel.uiState.value.reportData)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun emptySearchTextMatchesAllRegions() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadRegionsFromRepository()
        viewModel.onSearchTextChanged("")
        Assert.assertEquals("", viewModel.uiState.value.searchText)
        Assert.assertEquals(FakeCovidRepository.REGIONS.size, viewModel.uiState.value.matchingRegions.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchTextBrazilMatchesOneRegion() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadRegionsFromRepository()
        viewModel.onSearchTextChanged("Brazil")
        Assert.assertEquals(1, viewModel.uiState.value.matchingRegions.size)
        Assert.assertEquals("Brazil", viewModel.uiState.value.matchingRegions[0].name)
    }
}