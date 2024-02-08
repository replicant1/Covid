package com.rodbailey.covid

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
class MainInstrumentedTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun search_field_is_displayed() {
        rule.onNodeWithTag("tag.text.search").assertIsDisplayed()
    }

    @Test
    fun search_progress_indicator_is_displayed() {
        rule.onNodeWithTag("tag.progress.search").assertIsDisplayed()
    }

    @Test
    fun argentina_is_displayed() {
        waitForCountryListToLoad()
        rule.onNodeWithText("Argentina").assertIsDisplayed()
    }

    @Test
    fun can_scroll_to_zimbabwe() {
        waitForCountryListToLoad()
        rule.onNodeWithTag("tag.lazy.column.search").performScrollToIndex(218)
        rule.onNodeWithText("Zimbabwe").assertIsDisplayed()
    }

    @Test
    fun search_for_fg_matches_only_afghanistan() {
        waitForCountryListToLoad()
        rule.onNodeWithTag("tag.text.search").performTextInput("fgh")

        // There should be a way to test that "Afghanistan" is the only child node
        // of the lazy column that is displayed. Apparently not.
        // https://issuetracker.google.com/issues/187188981
        rule.onNodeWithText("Afghanistan").assertIsDisplayed() // created and visible
        rule.onNodeWithText("Fiji").assertDoesNotExist() // not created (fragile!)
        rule.onNodeWithText("Albania").assertIsNotDisplayed() // created but invisible (fragile!)
    }

    @Test
    fun text_clearance_restores_search_results() {
        waitForCountryListToLoad()
        rule.onNodeWithTag("tag.text.search").performTextInput("fgh")
        rule.onNodeWithTag("tag.text.search").performTextClearance()

        rule.onNodeWithText("Afghanistan").assertIsDisplayed()
        rule.onNodeWithText("Albania").assertIsDisplayed()
        rule.onNodeWithText("Algeria").assertIsDisplayed()
    }

    @Test
    fun click_global_icon_shows_global_stats() {
        waitForCountryListToLoad()

        rule.onNodeWithTag("tag.icon.global").performClick()
        
        waitForCountryStatsToLoad()

        // Only checking title - should also check data in table underneath
        rule.onNodeWithTag("tag.card").assertIsDisplayed()
        rule.onNodeWithTag(useUnmergedTree = true, testTag = "tag.card.title")
            .assertTextEquals("Global")
    }

    /**
     * This is a stop-gap measure only. Detecting "idle" on a Jetpack Compose interface seems
     * to be problematic at the moment. Should be able to use "idle" or wait for progress
     * bar to disappear.
     */
    private fun waitForCountryListToLoad() {
        Thread.sleep(5000)
    }

    /**
     * @see #waitForCountryListToLoad
     */
    private fun waitForCountryStatsToLoad() {
        Thread.sleep(5000)
    }
}