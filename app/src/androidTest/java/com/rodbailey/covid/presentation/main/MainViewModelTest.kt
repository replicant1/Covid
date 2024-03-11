package com.rodbailey.covid.presentation.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.rodbailey.covid.core.di.CoroutinesTestRule
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.net.FakeCovidAPI
import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.data.repo.ICovidRepository
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.core.UIText
import com.rodbailey.covid.usecase.MainUseCases
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * This test runs against the real [MainViewModel] backed by the [FakeCovidRepository] and
 * the [FakeCovidAPI]
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
@HiltAndroidTest
class MainViewModelTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Inject
    lateinit var fakeCovidRepository: ICovidRepository

    @Inject
    lateinit var fakeCovidAPI: CovidAPI

    // Hilt doesn't allow injection of an @HiltViewModel here
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var mainUseCases: MainUseCases

    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = MainViewModel(mainUseCases)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun county_list_loading_progress_bar_is_initially_hidden_at_startup() = runBlocking {
        Assert.assertFalse(viewModel.uiState.value.isRegionListLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun country_list_content_loads_at_startup() = runBlocking {
        viewModel.loadRegionList()

        val result = viewModel.uiState.value

        Assert.assertFalse(result.isRegionListLoading)
        Assert.assertEquals(FakeRegions.REGIONS.size, result.matchingRegions.size)

        // Check sorting of regions by name
        Assert.assertEquals(
            FakeRegions.FIRST_REGION_BY_NAME.name,
            result.matchingRegions.first().name
        )
        Assert.assertEquals(
            FakeRegions.LAST_REGION_BY_NAME.name,
            result.matchingRegions.last().name
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun show_error_message() = runBlocking {
        viewModel.showErrorMessage(UIText.DynamicString("An error has occurred."))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun data_panel_is_hidden_at_startup() = runBlocking {
        Assert.assertFalse(viewModel.uiState.value.isDataPanelExpanded)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collapse_data_panel() = runBlocking {
        viewModel.collapseDataPanel()
        Assert.assertFalse(viewModel.uiState.value.isDataPanelExpanded)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun load_report_for_region_results_in_report_data_in_data_panel() = runBlocking {
        viewModel.loadReportDataForRegion(UIText.DynamicString("China"), "CHN")

        // Wait for data to load from [FakeCovidAPI]
        timePasses()

        Assert.assertTrue(viewModel.uiState.value.isDataPanelExpanded)
        Assert.assertFalse(viewModel.uiState.value.isDataPanelLoading)
        Assert.assertEquals(UIText.DynamicString("China"), viewModel.uiState.value.reportDataTitle)
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("CHN"),
            viewModel.uiState.value.reportData
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun load_report_for_global_gives_global_data_in_data_panel() = runBlocking {
        viewModel.loadReportDataForGlobal()

        timePasses()

        Assert.assertTrue(viewModel.uiState.value.isDataPanelExpanded)
        Assert.assertFalse(viewModel.uiState.value.isDataPanelLoading)
        Assert.assertEquals(
            FakeRegions.GLOBAL_REGION.name,
            viewModel.uiState.value.reportDataTitle.asString(context)
        )
        Assert.assertEquals(
            FakeRegions.GLOBAL_REGION_STATS,
            viewModel.uiState.value.reportData
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun empty_search_text_matches_all_regions() = runBlocking {
        viewModel.loadRegionList()
        viewModel.onSearchTextChanged("")
        Assert.assertEquals("", viewModel.uiState.value.searchText)
        Assert.assertEquals(
            FakeRegions.REGIONS.size,
            viewModel.uiState.value.matchingRegions.size
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun search_text_of_brazil_matches_exactly_one_region() = runBlocking {
        viewModel.loadRegionList()
        viewModel.onSearchTextChanged("Brazil")

        timePasses()

        val result = viewModel.uiState
        Assert.assertEquals(1, result.value.matchingRegions.size)
        Assert.assertEquals("Brazil", viewModel.uiState.value.matchingRegions[0].name)
    }

    @Test
    fun search_text_of_xxx_matches_no_regions() = runBlocking {
        viewModel.loadRegionList()
        viewModel.onSearchTextChanged("xxx")

        timePasses()

        val result = viewModel.uiState.value
        Assert.assertTrue(result.matchingRegions.isEmpty())
    }

    @Test
    fun exception_from_api_when_loading_country_list_results_in_error_message() = runBlocking {
        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(true)
        viewModel.loadRegionList()
        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(false)

        viewModel.errorFlow.test {
            val result = awaitItem()
            Assert.assertTrue(result.asString(context).contains("Fail"))
        }
    }

    @Test
    fun exception_from_api_when_loading_stats_results_in_error_message() = runBlocking {
        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(true)
        viewModel.loadReportDataForGlobal()
        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(false)

         viewModel.errorFlow.test {
             val result = awaitItem()
             Assert.assertTrue(result.asString(context).contains("Fail"))
         }
    }

    private fun timePasses() {
        Thread.sleep(500)
    }
}