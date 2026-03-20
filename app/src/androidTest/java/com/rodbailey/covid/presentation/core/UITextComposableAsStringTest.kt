package com.rodbailey.covid.presentation.core

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rodbailey.covid.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITextComposableAsStringTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun asString_composable_dynamicString_branch() {
        var actual = ""

        composeRule.setContent {
            actual = UIText.DynamicString("Bob").asString()
        }

        composeRule.runOnIdle {
            assertEquals("Bob", actual)
        }
    }

    @Test
    fun asString_composable_stringResource_branch() {
        var actual = ""

        composeRule.setContent {
            actual = UIText.StringResource(R.string.region_global).asString()
        }

        composeRule.runOnIdle {
            assertEquals("Global", actual)
        }
    }

    @Test
    fun asString_composable_compoundStringResource_branch() {
        var actual = ""

        composeRule.setContent {
            actual = UIText.CompoundStringResource(
                R.string.failed_to_load_data_for,
                UIText.StringResource(R.string.region_global)
            ).asString()
        }

        composeRule.runOnIdle {
            assertEquals("Failed to load data for \"Global\".", actual)
        }
    }
}

