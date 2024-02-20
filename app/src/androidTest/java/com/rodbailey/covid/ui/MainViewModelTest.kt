package com.rodbailey.covid.ui

import androidx.compose.runtime.collectAsState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rodbailey.covid.CoroutinesTestRule
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.repo.FakeCovidRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Assumes
 */
@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        viewModel = MainViewModel(FakeCovidRepository())
    }

    @Test
    fun regionsLoadingProgressBarHiddenAtStartup() = runBlockingTest {
        Assert.assertFalse(viewModel.isRegionListLoading.value)
    }

    @Test
    fun regionsLoadAtStartup() = runBlockingTest {
        viewModel.loadRegionsFromRepository()
        Assert.assertEquals(FakeCovidRepository.REGIONS.size, viewModel.regions.value.size)
        Assert.assertFalse(viewModel.isRegionListLoading.value)

        // TODO: Check sorting of regions by name
    }

    @Test
    fun changesToSearchTextContentAreCached() = runBlockingTest {
        val searchText = "searchme"
        viewModel.onSearchTextChanged(searchText)
        Assert.assertEquals(searchText, viewModel.searchText.value)
    }

//    @Test
//    fun showErrorMessage() = runBlockingTest {
//        val errorText = "error-message"
//        val result = viewModel.errorMessage.collect() { println("***** $it")}
//        viewModel.showErrorMessage(errorText)
//    }

    @Test
    fun dataPanelIsHiddenAtStartup() = runBlockingTest {
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

   @Test
    fun collapseDataPanel_IsCaptured() = runBlockingTest {
        viewModel.collapseDataPanel()
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

    @Test
    fun loadReportForRegion_ReportDataAppearsInDataPanel() = runBlockingTest {
        viewModel.loadReportDataForRegion("China", "CHN")
        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals("China", viewModel.reportDataTitle.value)
        Assert.assertEquals(FakeCovidRepository.NON_GLOBAL_REPORT_DATA, viewModel.reportData.value)
    }

    @Test
    fun loadReportForGlobal_GlobalDataAppearsInDataPanel() = runBlockingTest {
        viewModel.loadReportDataForGlobal()
        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals(FakeCovidRepository.GLOBAL_DATA_SET_TITLE, viewModel.reportDataTitle.value)
        Assert.assertEquals(FakeCovidRepository.GLOBAL_REPORT_DATA, viewModel.reportData.value)
    }
}