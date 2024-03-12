package com.rodbailey.covid.presentation.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Provides a way of passing a string resource from ViewModel to UI without
 * having access to application context at construction time
 */
sealed class UIText {

    /**
     * String coming from API or other programmatic source
     */
    data class DynamicString(val value : String) : UIText()

    /**
     * String from XML resource
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UIText()

    /**
     * String from XML resource and another [UIText]
     */
    class CompoundStringResource(
        @StringRes val resId: Int,
        val uiText: UIText
    ) : UIText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, args)
            is CompoundStringResource -> stringResource(resId, uiText.asString())
        }
    }

    fun asString(ctx: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> ctx.getString(resId, *args)
            is CompoundStringResource -> ctx.getString(resId, uiText.asString(ctx))
        }
    }
}