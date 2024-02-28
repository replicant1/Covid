package com.rodbailey.covid.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Provides a way of passing a string resource from ViewModel to UI without
 * having access to application context.
 */
sealed class UIText {

    /**
     * Strings coming from APIs
     */
    data class DynamicString(val value : String) : UIText()

    /**
     * Strings from XML resources
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UIText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, args)
        }
    }

    fun asString(ctx: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> ctx.getString(resId, args)
        }
    }
}