package com.rodbailey.covid.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rodbailey.covid.CoroutinesTestRule
import com.rodbailey.covid.repo.FakeCovidRepository
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
        Assert.assertFalse(viewModel.isRegionListLoading.value)
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
        Assert.assertEquals(FakeCovidRepository.REGIONS.size, viewModel.matchingRegions.value.size)
        Assert.assertFalse(viewModel.isRegionListLoading.value)

        // Check sorting of regions by name
        Assert.assertEquals("Afghanistan", viewModel.matchingRegions.value.first().name)
        Assert.assertEquals("Vietnam", viewModel.matchingRegions.value.last().name)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun showErrorMessage() = runTest(UnconfinedTestDispatcher()) {
        viewModel.showErrorMessage("An error has occurred.")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun dataPanelIsHiddenAtStartup() = runTest(UnconfinedTestDispatcher()) {
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collapseDataPanel_IsCaptured() = runTest(UnconfinedTestDispatcher()) {
        viewModel.collapseDataPanel()
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReportForRegion_ReportDataAppearsInDataPanel() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadReportDataForRegion("China", "CHN")
        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals("China", viewModel.reportDataTitle.value)
        Assert.assertEquals(FakeCovidRepository.DEFAULT_REPORT_DATA, viewModel.reportData.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReportForGlobal_GlobalDataAppearsInDataPanel() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadReportDataForGlobal()
        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals(
            FakeCovidRepository.GLOBAL_DATA_SET_TITLE,
            viewModel.reportDataTitle.value
        )
        Assert.assertEquals(FakeCovidRepository.GLOBAL_REPORT_DATA, viewModel.reportData.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun emptySearchTextMatchesAllRegions() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadRegionsFromRepository()
        viewModel.onSearchTextChanged("")
        Assert.assertEquals("", viewModel.searchText.value)
        Assert.assertEquals(FakeCovidRepository.REGIONS.size, viewModel.matchingRegions.value.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchTextBrazilMatchesOneRegion() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadRegionsFromRepository()
        viewModel.onSearchTextChanged("Brazil")
        Assert.assertEquals(1, viewModel.matchingRegions.value.size)
        Assert.assertEquals("Brazil", viewModel.matchingRegions.value[0].name)
    }
}