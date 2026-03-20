package com.rodbailey.covid.presentation.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import com.rodbailey.covid.R

class UITextTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun dynamic_string_as_string() {
        val dynamicString = UIText.DynamicString("Bob")
        val result = dynamicString.asString(context)
        assertEquals("Bob", result)
    }

    @Test
    fun dynamic_string_as_string_preserves_empty_and_whitespace() {
        assertEquals("", UIText.DynamicString("").asString(context))
        assertEquals("  Bob  ", UIText.DynamicString("  Bob  ").asString(context))
    }

    @Test
    fun string_resource_as_string() {
        val stringResource = UIText.StringResource(R.string.region_global)
        val result = stringResource.asString(context)
        assertEquals("Global", result)
    }

    @Test
    fun string_resource_as_string_with_vararg_format_arg() {
        val stringResource = UIText.StringResource(R.string.failed_to_load_data_for, "France")
        val result = stringResource.asString(context)
        val expected = context.getString(R.string.failed_to_load_data_for, "France")
        assertEquals(expected, result)
    }

    @Test
    fun string_resource_as_string_with_list_args_format_arg() {
        val stringResource = UIText.StringResource(
            resId = R.string.failed_to_load_data_for,
            args = listOf("Brazil")
        )
        val result = stringResource.asString(context)
        val expected = context.getString(R.string.failed_to_load_data_for, "Brazil")
        assertEquals(expected, result)
    }

    @Test
    fun compound_string_resource_as_string() {
        val stringResource = UIText.StringResource(R.string.region_global)
        val compoundStringResource = UIText.CompoundStringResource(
            R.string.failed_to_load_data_for, stringResource)
        val result = compoundStringResource.asString(context)
        assertEquals("Failed to load data for \"Global\".", result)
    }

    @Test
    fun compound_string_resource_with_dynamic_inner_text() {
        val inner = UIText.DynamicString("Japan")
        val compound = UIText.CompoundStringResource(R.string.failed_to_load_data_for, inner)
        val expected = context.getString(R.string.failed_to_load_data_for, "Japan")
        assertEquals(expected, compound.asString(context))
    }

    @Test
    fun compound_string_resource_nested_compound_is_resolved_recursively() {
        val inner = UIText.CompoundStringResource(
            R.string.failed_to_load_data_for,
            UIText.StringResource(R.string.region_global)
        )
        val outer = UIText.CompoundStringResource(R.string.failed_to_load_data_for, inner)

        val innerExpected = context.getString(
            R.string.failed_to_load_data_for,
            context.getString(R.string.region_global)
        )
        val expected = context.getString(R.string.failed_to_load_data_for, innerExpected)

        assertEquals(expected, outer.asString(context))
    }

    @Test
    fun data_class_value_semantics_dynamic_string() {
        val a = UIText.DynamicString("Global")
        val b = UIText.DynamicString("Global")
        val c = a.copy(value = "Brazil")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun data_class_value_semantics_string_resource() {
        val a = UIText.StringResource(R.string.failed_to_load_data_for, "Global")
        val b = UIText.StringResource(
            resId = R.string.failed_to_load_data_for,
            args = listOf("Global")
        )
        val c = a.copy(args = listOf("Brazil"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun data_class_value_semantics_compound_resource() {
        val a = UIText.CompoundStringResource(
            R.string.failed_to_load_data_for,
            UIText.StringResource(R.string.region_global)
        )
        val b = UIText.CompoundStringResource(
            R.string.failed_to_load_data_for,
            UIText.StringResource(R.string.region_global)
        )
        val c = a.copy(uiText = UIText.DynamicString("Canada"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun different_ui_text_types_with_similar_content_are_not_equal() {
        val dynamic = UIText.DynamicString("Global")
        val resource = UIText.StringResource(R.string.region_global)
        assertNotEquals(dynamic, resource)
    }
}