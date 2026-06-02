package com.rodbailey.covid.presentation.cachestats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.presentation.core.MainActivity
import com.rodbailey.covid.presentation.main.MainScreenTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Verifies that CacheStatsViewModel-owned state (sortOption) survives a configuration change.
 * Navigation to CacheStatsScreen is performed via triple-tap in @Before so that the real
 * CacheStatsViewModel is in play (as opposed to CacheStatsUITest which uses a stateless
 * composable with no ViewModel).
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class CacheStatsScreenRotationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeCovidRepository: CovidRepository

    @Before
    fun setup() {
        hiltRule.inject()
        // Navigate to CacheStatsScreen via triple-tap on the search field.
        // PointerEventPass.Initial ensures the triple-tap modifier intercepts all three
        // presses before the search field handles them.
        val searchField = rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag)
        searchField.performClick()
        searchField.performClick()
        searchField.performClick()
        rule.waitForIdle()
    }

    @Test
    fun cache_stats_title_visible_after_rotation() {
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        rule.onNodeWithText("Cache Statistics").assertIsDisplayed()
    }

    @Test
    fun back_button_visible_after_rotation() {
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        rule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun summary_table_visible_after_rotation() {
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        rule.onNodeWithText("Entries").assertIsDisplayed()
    }

    @Test
    fun sort_option_survives_rotation() {
        // Change the sort option from the default (ISO Code up) to Age (up)
        rule.onNodeWithText("ISO Code (up)").performClick()
        rule.onAllNodesWithText("Age (up)").onFirst().performClick()
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        // sortOption is held in CacheStatsViewModel (MutableStateFlow) — survives rotation
        rule.onNodeWithText("Age (up)").assertIsDisplayed()
    }
}
