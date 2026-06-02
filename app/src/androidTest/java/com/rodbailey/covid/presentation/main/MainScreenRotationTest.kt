package com.rodbailey.covid.presentation.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.presentation.core.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Verifies that ViewModel-owned state (searchText, dataPanelUIState) survives a configuration
 * change (simulated via ActivityScenario.recreate()) while ephemeral composable state is
 * intentionally discarded.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class MainScreenRotationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeCovidRepository: CovidRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun search_text_survives_rotation() {
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag).performTextInput("Brazil")
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        // Search text is held in MainViewModel.searchText (MutableStateFlow) — survives rotation
        rule.onNodeWithTag(MainScreenTag.TAG_TEXT_SEARCH.tag)
            .assertTextContains("Brazil")
    }

    @Test
    fun data_panel_open_state_survives_rotation() {
        // Open the data panel with global stats
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        // dataPanelUIState is held in MainViewModel (MutableStateFlow) — panel stays open
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertIsDisplayed()
        rule.onNodeWithTag(useUnmergedTree = true, testTag = MainScreenTag.TAG_CARD_TITLE.tag)
            .assertTextContains(FakeRegions.GLOBAL_REGION.name)
    }

    @Test
    fun data_panel_closed_state_survives_rotation() {
        // Open then close the data panel
        rule.onNodeWithTag(MainScreenTag.TAG_ICON_GLOBAL.tag).performClick()
        rule.waitForIdle()
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).performClick()
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        // Panel was closed before rotation — should remain closed
        rule.onNodeWithTag(MainScreenTag.TAG_CARD.tag).assertDoesNotExist()
    }

    @Test
    fun region_list_visible_after_rotation() {
        // Wait for region list to load from FakeCovidRepository
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        // Region data is re-emitted from the repository flow after recreation
        rule.onNodeWithText(FakeRegions.FIRST_REGION_BY_NAME.name).assertIsDisplayed()
    }
}
