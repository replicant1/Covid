package com.rodbailey.covid.presentation.cachestats

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.presentation.Result
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests for [CacheStatsViewModel] backed by [FakeCovidRepository].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
@HiltAndroidTest
class CacheStatsViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeCovidRepository: CovidRepository

    lateinit var viewModel: CacheStatsViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        // Reset cache entries to empty before each test
        (fakeCovidRepository as FakeCovidRepository).setCacheEntries(emptyList())
        viewModel = CacheStatsViewModel(fakeCovidRepository, ApplicationProvider.getApplicationContext())
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    fun initial_state_is_loading() = runTest {
        viewModel.uiState.test {
            val first = awaitItem()
            Assert.assertTrue(
                "Expected Result.Loading for initial state, got ${first.entries}",
                first.entries is Result.Loading
            )
        }
    }

    // -------------------------------------------------------------------------
    // Empty cache
    // -------------------------------------------------------------------------

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun empty_cache_emits_success_with_empty_list() =
        runTest(UnconfinedTestDispatcher()) {
            // FakeCovidRepository already configured with empty list in @Before
            viewModel.uiState.test {
                skipItems(1) // skip Result.Loading
                val state = awaitItem()
                Assert.assertTrue(
                    "Expected Result.Success, got ${state.entries}",
                    state.entries is Result.Success
                )
                val entries = (state.entries as Result.Success).data
                Assert.assertTrue("Expected empty list", entries.isEmpty())
            }
        }

    // -------------------------------------------------------------------------
    // Cache with entries
    // -------------------------------------------------------------------------

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun cache_with_entries_emits_success_with_correct_count() =
        runTest(UnconfinedTestDispatcher()) {
            val testEntries = listOf(
                CacheEntry("AFG", 60_000L),
                CacheEntry("AUS", 120_000L),
                CacheEntry("BRA", 180_000L),
            )
            (fakeCovidRepository as FakeCovidRepository).setCacheEntries(testEntries)
            // Rebuild ViewModel so it picks up the new entries
            viewModel = CacheStatsViewModel(fakeCovidRepository, ApplicationProvider.getApplicationContext())

            viewModel.uiState.test {
                skipItems(1) // skip Result.Loading
                val state = awaitItem()
                Assert.assertTrue(
                    "Expected Result.Success, got ${state.entries}",
                    state.entries is Result.Success
                )
                val entries = (state.entries as Result.Success).data
                Assert.assertEquals("Expected 3 entries", 3, entries.size)
            }
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun cache_entries_preserve_iso3_codes_and_age_values() =
        runTest(UnconfinedTestDispatcher()) {
            val testEntries = listOf(
                CacheEntry("DEU", 300_000L),
                CacheEntry("FRA", 600_000L),
            )
            (fakeCovidRepository as FakeCovidRepository).setCacheEntries(testEntries)
            viewModel = CacheStatsViewModel(fakeCovidRepository, ApplicationProvider.getApplicationContext())

            viewModel.uiState.test {
                skipItems(1) // skip Result.Loading
                val state = awaitItem()
                val entries = (state.entries as Result.Success).data

                Assert.assertEquals("DEU", entries[0].iso3Code)
                Assert.assertEquals(300_000L, entries[0].ageMillis)
                Assert.assertEquals("FRA", entries[1].iso3Code)
                Assert.assertEquals(600_000L, entries[1].ageMillis)
            }
        }

    // -------------------------------------------------------------------------
    // Single entry edge case
    // -------------------------------------------------------------------------

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun single_cache_entry_emits_success_with_one_entry() =
        runTest(UnconfinedTestDispatcher()) {
            (fakeCovidRepository as FakeCovidRepository).setCacheEntries(
                listOf(CacheEntry("USA", 45_000L))
            )
            viewModel = CacheStatsViewModel(fakeCovidRepository, ApplicationProvider.getApplicationContext())

            viewModel.uiState.test {
                skipItems(1) // skip Result.Loading
                val state = awaitItem()
                val entries = (state.entries as Result.Success).data
                Assert.assertEquals(1, entries.size)
                Assert.assertEquals("USA", entries[0].iso3Code)
                Assert.assertEquals(45_000L, entries[0].ageMillis)
            }
        }
}
