package com.rodbailey.covid

import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.repo.ICovidRepository
import com.rodbailey.covid.ui.MainViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MainViewModelMockkTest {
    private lateinit var repo: ICovidRepository
    private lateinit var viewModel: MainViewModel

    private val mts = newSingleThreadContext("UI thread")

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @get:Rule
//    val coroutinesTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        repo = mockk()
        viewModel = MainViewModel(repo)
        Dispatchers.setMain(mts)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        mts.close()
    }

    @Test
    fun regionsLoadingProgressBarHiddenAtStartup() {
        Assert.assertFalse(viewModel.isRegionListLoading.value)
    }

    @Test
    fun regionsLoadAtStartup() = runTest  {
        coEvery { repo.getRegions() } returns listOf(
            Region("CHN", "China"),
            Region("SGP", "Singapore"))
        viewModel.loadRegionsFromRepository()

        // Regions-loading progress bar is hidden
        Assert.assertFalse(viewModel.isRegionListLoading.value)

        // Verify that repo.getRegions() was called by viewModel.loadRegionsFromRepository()
        coVerify { repo.getRegions() }

        val matchingRegions = viewModel.matchingRegions.value
        Assert.assertEquals(2, matchingRegions.size)
        Assert.assertEquals("China", matchingRegions[0].name)
        Assert.assertEquals("Singapore", matchingRegions[1].name)
    }

    @Test
    fun dataPanelIsHiddenAtStartup() = runTest {
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

    @Test
    fun collapseDataPanelWorks() = runTest {
        viewModel.collapseDataPanel()
        Assert.assertFalse(viewModel.isDataPanelExpanded.value)
    }

    @Test
    fun loadReportForRegion_ReportDataAppearsInDataPanel() = runTest {
        coEvery { repo.getReport("CHN") } returns
                ReportData(10, 20, 30, 40)
        viewModel.loadReportDataForRegion("China", "CHN")

        // Verify that repo.getReport("CHN") was called
        coVerify { repo.getReport("CHN")}

        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals("China", viewModel.reportDataTitle.value)
        Assert.assertEquals(ReportData(10,20,30,40),
            viewModel.reportData.value)
    }

    @Test
    fun loadReportForGlobal_GlobalDataAppearsInPanel() = runTest {
        coEvery { repo.getReport(null)} returns ReportData(1,2,3,4,0.5F)
        viewModel.loadReportDataForGlobal()
        coVerify { repo.getReport(null)}
        Assert.assertTrue(viewModel.isDataPanelExpanded.value)
        Assert.assertFalse(viewModel.isDataPanelLoading.value)
        Assert.assertEquals("Global", viewModel.reportDataTitle.value)
        Assert.assertEquals(
            ReportData(1,2,3,4,0.5F),
            viewModel.reportData.value
        )
    }

    @Test
    fun emptySearchTextMatchesAllRegions() = runTest {
        coEvery { repo.getRegions() } returns listOf(
            Region("CHN", "China"),
            Region("SGP", "Singapore"))
        viewModel.loadRegionsFromRepository()
        coVerify { repo.getRegions() }
        viewModel.onSearchTextChanged("")
        Assert.assertEquals("", viewModel.searchText.value)
        Assert.assertEquals(2, viewModel.matchingRegions.value.size)
    }

    @Test
    fun searchTextChinaMatchesOneRegion() = runTest {
        coEvery { repo.getRegions() } returns listOf(
            Region("AUS", "Australia"),
            Region("ALK", "Alaska"))
        viewModel.loadRegionsFromRepository()
        coVerify { repo.getRegions() }
        viewModel.onSearchTextChanged("Alas")
        Assert.assertEquals(1, viewModel.matchingRegions.value.size)
        Assert.assertEquals("Alaska", viewModel.matchingRegions.value[0].name   )
    }
}