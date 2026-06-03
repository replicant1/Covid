package com.rodbailey.covid.presentation.cachestats

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.rodbailey.covid.data.repo.CacheEntry
import com.rodbailey.covid.presentation.theme.CovidTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CacheStatsUITest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val threeEntries = persistentListOf(
        CacheEntry("AFG", 2_400_000L),  // ~40 minutes old
        CacheEntry("AUS",   600_000L),  // ~10 minutes old
        CacheEntry("BRA", 3_600_000L),  // ~60 minutes old
    )

    private fun setContent(
        entries: ImmutableList<CacheEntry> = threeEntries,
        sortOption: SortOption = SortOption.ISO_CODE_ASC,
        onSortOptionSelected: (SortOption) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        rule.setContent {
            CovidTheme {
                CacheStatsScreenContent(
                    entries = entries,
                    sortOption = sortOption,
                    onSortOptionSelected = onSortOptionSelected,
                    onDismiss = onDismiss
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Top bar
    // -------------------------------------------------------------------------

    @Test
    fun screen_shows_cache_statistics_title() {
        setContent()
        rule.onNodeWithText("Cache Statistics").assertIsDisplayed()
    }

    @Test
    fun back_button_is_displayed() {
        setContent()
        rule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun back_button_invokes_on_dismiss() {
        var dismissed = false
        setContent(onDismiss = { dismissed = true })
        rule.onNodeWithContentDescription("Back").performClick()
        assertEquals(true, dismissed)
    }

    // -------------------------------------------------------------------------
    // Summary table
    // -------------------------------------------------------------------------

    @Test
    fun empty_cache_shows_zero_entries_in_summary() {
        setContent(entries = persistentListOf())
        // The summary "Entries" row value is "0"
        rule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun summary_shows_correct_entry_count() {
        setContent(entries = threeEntries)
        rule.onNodeWithText(threeEntries.size.toString()).assertIsDisplayed()
    }

    @Test
    fun summary_table_labels_are_displayed() {
        setContent()
        rule.onNodeWithText("Entries").assertIsDisplayed()
        rule.onNodeWithText("Total size").assertIsDisplayed()
        rule.onNodeWithText("Average age").assertIsDisplayed()
        rule.onNodeWithText("Oldest entry").assertIsDisplayed()
        rule.onNodeWithText("Youngest entry").assertIsDisplayed()
    }

    @Test
    fun oldest_entry_iso_code_appears_in_summary() {
        // BRA has the largest ageMillis (3_600_000) so is the oldest
        setContent(entries = threeEntries)
        rule.onNodeWithText("Oldest entry").assertIsDisplayed()
        // The value cell contains "BRA — <formatted age>"
        rule.onAllNodesWithText("BRA", substring = true).assertCountEquals(2) // summary + chart bar
    }

    @Test
    fun youngest_entry_iso_code_appears_in_summary() {
        // AUS has the smallest ageMillis (600_000) so is the youngest
        setContent(entries = threeEntries)
        // The value cell contains "AUS — <formatted age>"
        rule.onAllNodesWithText("AUS", substring = true).assertCountEquals(2) // summary + chart bar
    }

    // -------------------------------------------------------------------------
    // Bar chart
    // -------------------------------------------------------------------------

    @Test
    fun iso_codes_appear_as_bar_labels_in_chart() {
        setContent(entries = threeEntries)
        rule.onNodeWithText("AFG").assertIsDisplayed()
        rule.onNodeWithText("AUS").assertIsDisplayed()
        rule.onNodeWithText("BRA").assertIsDisplayed()
    }

    @Test
    fun empty_cache_shows_no_bar_labels() {
        setContent(entries = persistentListOf())
        rule.onNodeWithText("AFG").assertDoesNotExist()
        rule.onNodeWithText("AUS").assertDoesNotExist()
    }

    // -------------------------------------------------------------------------
    // Sort control
    // -------------------------------------------------------------------------

    @Test
    fun sort_control_label_is_displayed() {
        setContent()
        rule.onNodeWithText("Sort by").assertIsDisplayed()
    }

    @Test
    fun current_sort_option_is_shown_in_sort_control() {
        setContent(sortOption = SortOption.ISO_CODE_ASC)
        rule.onNodeWithText("ISO Code (up)").assertIsDisplayed()
    }

    @Test
    fun opening_sort_dropdown_shows_all_four_options() {
        setContent()
        // Click the text field anchor to open the dropdown
        rule.onNodeWithText("ISO Code (up)").performClick()
        // The anchor text + the menu item both exist — assert both variants appear
        rule.onAllNodesWithText("ISO Code (up)").assertCountEquals(2)
        rule.onNodeWithText("ISO Code (down)").assertIsDisplayed()
        rule.onNodeWithText("Age (up)").assertIsDisplayed()
        rule.onNodeWithText("Age (down)").assertIsDisplayed()
    }

    @Test
    fun selecting_age_asc_from_dropdown_invokes_callback() {
        var selected: SortOption? = null
        setContent(onSortOptionSelected = { selected = it })
        rule.onNodeWithText("ISO Code (up)").performClick()
        rule.onAllNodesWithText("Age (up)").onFirst().performClick()
        assertEquals(SortOption.AGE_ASC, selected)
    }

    @Test
    fun selecting_iso_code_desc_from_dropdown_invokes_callback() {
        var selected: SortOption? = null
        setContent(onSortOptionSelected = { selected = it })
        rule.onNodeWithText("ISO Code (up)").performClick()
        rule.onNodeWithText("ISO Code (down)").performClick()
        assertEquals(SortOption.ISO_CODE_DESC, selected)
    }

    @Test
    fun sort_iso_asc_shows_all_entries_in_chart() {
        setContent(
            entries = threeEntries,
            sortOption = SortOption.ISO_CODE_ASC
        )
        // All three ISO codes should be visible regardless of sort
        rule.onNodeWithText("AFG").assertIsDisplayed()
        rule.onNodeWithText("AUS").assertIsDisplayed()
        rule.onNodeWithText("BRA").assertIsDisplayed()
    }

    @Test
    fun sort_age_desc_shows_all_entries_in_chart() {
        setContent(
            entries = threeEntries,
            sortOption = SortOption.AGE_DESC
        )
        rule.onNodeWithText("AFG").assertIsDisplayed()
        rule.onNodeWithText("AUS").assertIsDisplayed()
        rule.onNodeWithText("BRA").assertIsDisplayed()
    }
}
