package com.rodbailey.covid.presentation.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import com.rodbailey.covid.R

class UITextTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun dynamic_string_as_string() {
        val dynamicString = UIText.DynamicString("Bob")
        val result = dynamicString.asString(context)
        Assert.assertEquals("Bob", result)
    }

    @Test
    fun string_resource_as_string() {
        val stringResource = UIText.StringResource(R.string.region_global)
        val result = stringResource.asString(context)
        Assert.assertEquals("Global", result)
    }
}