package com.rodbailey.covid.presentation.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.rodbailey.covid.core.di.CoroutinesTestRule
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.net.FakeCovidAPI
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.presentation.MainViewModel
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelClosed
import com.rodbailey.covid.presentation.MainViewModel.DataPanelUIState.DataPanelOpenWithLoading
import com.rodbailey.covid.presentation.Result
import com.rodbailey.covid.presentation.core.UIText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * This test runs test the real [MainViewModel] backed by the [FakeCovidRepository]
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
@HiltAndroidTest
class MainViewModelTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Hilt won't let me @Inject a MainViewModel here. It says:
    //    Injection of an @HiltViewModel class is prohibited since it does not create a ViewModel instance correctly.
    //    Access the ViewModel via the Android APIs (e.g. ViewModelProvider) instead.
    //    Injected ViewModel: com.rodbailey.covid.presentation.MainViewModel
    lateinit var viewModel: MainViewModel

    @Inject
    // Hilt injects a [FakeCovidRepository] here.
    lateinit var fakeCovidRepository: CovidRepository

    @Before
    fun setup() {
        hiltRule.inject()

        // Have to construct viewModel manually here rather than inject. See comment above.
        viewModel = MainViewModel(fakeCovidRepository)
    }

    @Test
    fun sorted_region_list_loads_at_startup() = runTest {
        viewModel.uiState.test {
            // "asResult" flow automatically inserts "Loading" at flow start
            val item1 = awaitItem()
            Assert.assertTrue(item1.matchingRegions is Result.Loading)

            // Room flow emits empty region list at start
            val item2 = awaitItem()
            Assert.assertTrue(item2.matchingRegions is Result.Success)
            val item2Success = item2.matchingRegions as Result.Success
            Assert.assertTrue(item2Success.data.isEmpty())

            // Room flow next emits region list
            val item3 = awaitItem()
            Assert.assertTrue(item3.matchingRegions is Result.Success)
            val item3Success = item3.matchingRegions as Result.Success
            Assert.assertEquals(FakeRegions.REGIONS.size, item3Success.data.size)

            // Check sorting of regions by name
            Assert.assertEquals(
                FakeRegions.FIRST_REGION_BY_NAME.name,
                item3Success.data.first().name
            )
            Assert.assertEquals(
                FakeRegions.LAST_REGION_BY_NAME.name,
                item3Success.data.last().name
            )
        }
    }

    @Test
    fun data_panel_is_closed_at_startup() = runTest {
        viewModel.uiState.test {
            val item1 = awaitItem()
            Assert.assertTrue(item1.dataPanelUIState is DataPanelClosed)
        }
    }

    @Test
    fun search_text_of_brazil_matches_exactly_one_region() = runTest {
        // Type "Brazil" into the search box
        viewModel.processIntent(MainViewModel.MainIntent.OnSearchTextChanged("Brazil"))

        viewModel.uiState.test {
            val loading = awaitItem()
            val empty = awaitItem()
            val result = awaitItem()
            // Confirm region list contains just the element "Brazil"
            val resultAsSuccess = result.matchingRegions as Result.Success
            Assert.assertEquals(1, resultAsSuccess.data.size)
            Assert.assertEquals("Brazil", resultAsSuccess.data[0].name)
        }
    }

    @Test
    fun empty_search_text_matches_all_regions() = runTest {
        // Type "" into the search box
        viewModel.processIntent(MainViewModel.MainIntent.OnSearchTextChanged(""))

        viewModel.uiState.test {
            val loading = awaitItem()
            val empty = awaitItem()
            val result = awaitItem()
            // Confirm region list contains all regions
            val resultAsSuccess = result.matchingRegions as Result.Success
            Assert.assertEquals(FakeRegions.REGIONS.size, resultAsSuccess.data.size)
        }
    }

    @Test
    fun load_report_for_global_gives_global_data_in_data_panel() = runTest {
        viewModel.uiState.test {
            val loading = awaitItem()
            val regionsEmpty = awaitItem()
            val regionsLoaded = awaitItem()

            viewModel.processIntent(MainViewModel.MainIntent.LoadReportDataForGlobal)

            val dataPanelLoading = awaitItem()
            Assert.assertTrue(dataPanelLoading.dataPanelUIState is DataPanelOpenWithLoading)

            val dataPanelLoaded = awaitItem()
            Assert.assertTrue(dataPanelLoaded.dataPanelUIState is MainViewModel.DataPanelUIState.DataPanelOpenWithData)

            val data =
                (dataPanelLoaded.dataPanelUIState as MainViewModel.DataPanelUIState.DataPanelOpenWithData).reportData
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.active, data.active)
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.deaths, data.deaths)
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.confirmed, data.confirmed)
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.recovered, data.recovered)

            val title =
                (dataPanelLoaded.dataPanelUIState as MainViewModel.DataPanelUIState.DataPanelOpenWithData).reportDataTitle
            Assert.assertEquals(FakeRegions.GLOBAL_REGION.name, title.asString(context))
        }
    }

    @Test
    fun show_error_message() = runTest {
        viewModel.processIntent(
            MainViewModel.MainIntent.ShowErrorMessage(
                UIText.DynamicString("An error has occurred.")
            )
        )

        viewModel.errorFlow.test {
            val result = awaitItem()
            Assert.assertEquals(result.asString(context), "An error has occurred.")
        }
    }

//    @Test
//    fun exception_from_api_when_loading_stats_results_in_error_message() = runTest {
//        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(true)
//        viewModel.processIntent(MainViewModel.MainIntent.LoadReportDataForGlobal)
//
//        (fakeCovidRepository as FakeCovidRepository).setAllMethodsThrowException(false)
//
//        viewModel.errorFlow.test {
//            val result = awaitItem()
//            Assert.assertTrue(result.asString(context).contains("Fail"))
//        }
//    }

}