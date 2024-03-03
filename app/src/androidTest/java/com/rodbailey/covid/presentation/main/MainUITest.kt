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
import com.rodbailey.covid.presentation.core.MainActivity
import com.rodbailey.covid.presentation.main.MainScreenTag
import com.rodbailey.covid.data.repo.FakeCovidRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device or emulator. These tests are tied to
 * the data in [FakeCovidRepository].
 */
@HiltAndroidTest
class MainUITest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun search_field_is_displayed_on_startup() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).assertIsDisplayed()
    }

    @Test
    fun search_progress_indicator_is_displayed_on_startup() {
        rule.onNodeWithTag(MainScreenTag.TAG_PROGRESS_SEARCH.tag).assertIsDisplayed()
    }

    @Test
    fun aus_is_displayed_immediately_after_load() {
        rule.onNodeWithText("Australia").assertIsDisplayed()
    }

    @Test
    fun can_scroll_to_last_region_in_list() {
        rule.onNodeWithTag(MainScreenTag.TAG_LAZY_COLUMN_SEARCH.tag).performScrollToIndex(
            FakeCovidRepository.REGIONS.size - 1)
        rule.onNodeWithText("Vietnam").assertIsDisplayed()
    }

    @Test
    fun search_for_fg_matches_only_afghanistan() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextInput("fgh")

        // There should be an easier way to test that "Afghanistan" is the only child node
        // of the lazy column that is displayed. Apparently not.
        // https://issuetracker.google.com/issues/187188981
        rule.onNodeWithText("Afghanistan").assertIsDisplayed() // created and visible
        rule.onNodeWithText("Austria").assertIsNotDisplayed() // created but invisible
        rule.onNodeWithText("Australia").assertIsNotDisplayed() // created but invisible
    }

    @Test
    fun text_clearance_restores_search_results() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextInput("fgh")
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextClearance()

        rule.onNodeWithText("Afghanistan").assertIsDisplayed()
        rule.onNodeWithText("Austria").assertIsDisplayed()
        rule.onNodeWithText("Algeria").assertIsDisplayed()
    }

    @Test
    fun click_global_icon_shows_global_stats() {
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()
        
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithTag(useUnmergedTree = true, testTag = MainScreenTag.TAG_CARD_TITLE.tag).assertTextEquals("Global")
        rule.onNodeWithText("${FakeCovidRepository.GLOBAL_REPORT_DATA.confirmed}", useUnmergedTree = true).assertIsDisplayed() // confirmed cases
        rule.onNodeWithText("${FakeCovidRepository.GLOBAL_REPORT_DATA.deaths}", useUnmergedTree = true).assertIsDisplayed() // deaths
        rule.onNodeWithText("${FakeCovidRepository.GLOBAL_REPORT_DATA.active}", useUnmergedTree = true).assertIsDisplayed() // active cases
        rule.onNodeWithText("${FakeCovidRepository.GLOBAL_REPORT_DATA.fatalityRate}", useUnmergedTree = true).assertIsDisplayed() // fatality rate
    }

    @Test
    fun click_data_panel_collapses_data_panel() {
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()

        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).performClick()

        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertDoesNotExist()
    }

    @Test
    fun click_aus_shows_aus_stats() {
        rule.onNodeWithText("Australia").performClick()

        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithText(useUnmergedTree = true, text = "${FakeCovidRepository.AUS_REPORT_DATA.confirmed}").assertIsDisplayed() // confirmed cases
        rule.onNodeWithText(useUnmergedTree = true, text="${FakeCovidRepository.AUS_REPORT_DATA.deaths}").assertIsDisplayed() // deaths
        rule.onNodeWithText(useUnmergedTree = true, text = "${FakeCovidRepository.AUS_REPORT_DATA.active}") // active cases
        rule.onNodeWithText(useUnmergedTree = true, text = "${FakeCovidRepository.AUS_REPORT_DATA.fatalityRate}") // fatality rate
    }
}