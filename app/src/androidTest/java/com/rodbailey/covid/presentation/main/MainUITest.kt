package com.rodbailey.covid.presentation.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.repo.FakeCovidRepository
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.presentation.core.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device. These tests are tied to the
 * present state of the data at https://covid-api.com. They also contain some workarounds
 * for general problems with the testing of Jetpack Compose applications.
 *
 * See [waitForCountryListToLoad] for example.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainUITest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun search_field_is_displayed_on_startup() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).assertIsDisplayed()
    }

    @Test
    fun search_progress_indicator_is_displayed_on_startup() {
        rule.onNodeWithTag(MainScreenTag.TAG_PROGRESS_SEARCH.tag).assertIsDisplayed()
    }

    @Test
    fun first_country_alphabetically_is_displayed_on_startup(): Unit = runBlocking {
        FakeCovidRepository().getRegionsStream().test {
            val empty = awaitItem() // Empty list
            val regions = awaitItem() // Regions
            rule.onNodeWithText(regions.first().name).assertIsDisplayed()
            awaitComplete()
        }
    }

    @Test
    fun can_scroll_to_last_country_alphabetically_in_country_list() {
        rule.onNodeWithTag(MainScreenTag.TAG_LAZY_COLUMN_SEARCH.tag).performScrollToIndex(FakeRegions.NUM_REGIONS - 1)
        rule.onNodeWithText(FakeRegions.LAST_REGION_BY_NAME.name).assertIsDisplayed()
    }

    @Test
    fun search_for_fgh_matches_only_afghanistan() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextInput("fgh")

        // There should be an easy way to test that "Afghanistan" is the only displayed child node
        // of the lazy column that is displayed. Apparently not.
        // https://issuetracker.google.com/issues/187188981
        rule.onNodeWithText("Afghanistan").assertIsDisplayed()
        rule.onNodeWithText("Algeria").assertIsNotDisplayed()
        rule.onNodeWithText("Australia").assertIsNotDisplayed()
    }

    @Test
    fun clear_text_after_search_for_fgh_restores_full_country_list() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextInput("fgh")
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextClearance()

        rule.onNodeWithText("Afghanistan").assertIsDisplayed()
        rule.onNodeWithText("Algeria").assertIsDisplayed()
        rule.onNodeWithText("Australia").assertIsDisplayed()
    }

    @Test
    fun click_global_icon_shows_global_stats_in_data_panel() {
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()

        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithTag(useUnmergedTree = true, testTag = MainScreenTag.TAG_CARD_TITLE.tag).assertTextEquals(FakeRegions.GLOBAL_REGION.name)
        val globalStats = FakeRegions.GLOBAL_REGION_STATS
        rule.onNodeWithText(globalStats.confirmed.toString(), useUnmergedTree = true).assertIsDisplayed() // confirmed cases
        rule.onNodeWithText(globalStats.deaths.toString(), useUnmergedTree = true).assertIsDisplayed() // deaths
        rule.onNodeWithText(globalStats.active.toString(), useUnmergedTree = true).assertIsDisplayed() // active cases
        rule.onNodeWithText(globalStats.fatalityRate.toString(), useUnmergedTree = true).assertIsDisplayed() // fatality rate
    }

    @Test
    fun click_australia_shows_australia_stats_in_data_panel() {
        rule.onNodeWithText("Australia").performClick()

        val ozStats = FakeRegions.REGIONS.filterKeys { region: Region ->
            region.iso3Code == "AUS"
        }.values.elementAt(0)

        rule.onNodeWithTag("tag.card").assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = ozStats.confirmed.toString()).assertIsDisplayed() // confirmed cases
        rule.onNodeWithText(useUnmergedTree = true, text = ozStats.deaths.toString()).assertIsDisplayed() // deaths
        rule.onNodeWithText(useUnmergedTree = true, text = ozStats.active.toString()) // active cases
        rule.onNodeWithText(useUnmergedTree = true, text = ozStats.fatalityRate.toString()) // fatality rate
    }

    @Test
    fun scroll_to_last_region_and_click_shows_region_stats_in_data_panel(): Unit = runBlocking {
        rule.onNodeWithTag(MainScreenTag.TAG_LAZY_COLUMN_SEARCH.tag).performScrollToIndex(FakeRegions.NUM_REGIONS - 1)

        val lastRegion = FakeCovidRepository().getRegionsStream().first().last()
        rule.onNodeWithText(lastRegion.name).performClick()

        val lastRegionStats = FakeRegions.REGIONS.get(lastRegion)

        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = lastRegionStats?.confirmed.toString()).assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = lastRegionStats?.deaths.toString()).assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = lastRegionStats?.active.toString()).assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = lastRegionStats?.fatalityRate.toString()).assertIsDisplayed()
    }

    @Test
    fun tapping_on_data_panel_when_showing_hides_the_data_panel() {
        // Open data panel by clicking global icon
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()

        // Confirm that data panel is expanded
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()

        // Tap the data card
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).performClick()

        // COnfirm that data panel is now hidden
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertDoesNotExist()
    }

}