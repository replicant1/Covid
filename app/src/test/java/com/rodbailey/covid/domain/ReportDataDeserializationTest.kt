package com.rodbailey.covid.domain

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportDataDeserializationTest {

    private val gson = Gson()

    @Test
    fun all_fields_deserialize_correctly_from_json() {
        val json = """
            {
              "confirmed": 1000,
              "deaths": 200,
              "recovered": 700,
              "active": 100,
              "fatality_rate": 0.25
            }
        """.trimIndent()

        val result = gson.fromJson(json, ReportData::class.java)

        assertEquals(1000L, result.confirmed)
        assertEquals(200L, result.deaths)
        assertEquals(700L, result.recovered)
        assertEquals(100L, result.active)
        assertEquals(0.25F, result.fatalityRate, 0.001F)
    }

    @Test
    fun fatality_rate_snake_case_key_maps_to_fatalityRate_property() {
        // A wrong @SerializedName would silently leave fatalityRate at its default of -1F
        val json = """{"fatality_rate": 3.14}"""
        val result = gson.fromJson(json, ReportData::class.java)
        assertEquals(3.14F, result.fatalityRate, 0.001F)
    }

    @Test
    fun missing_fields_produce_default_values() {
        val result = gson.fromJson("{}", ReportData::class.java)
        assertEquals(-1L, result.confirmed)
        assertEquals(-1L, result.deaths)
        assertEquals(-1L, result.recovered)
        assertEquals(-1L, result.active)
        assertEquals(-1F, result.fatalityRate, 0.001F)
    }

    @Test
    fun report_data_field_unwraps_correctly_from_report_json() {
        // Verifies the Report wrapper's @SerializedName("data") mapping
        val json = """{"data": {"confirmed": 42, "fatality_rate": 1.5}}"""
        val report = gson.fromJson(json, Report::class.java)
        assertEquals(42L, report.data.confirmed)
        assertEquals(1.5F, report.data.fatalityRate, 0.001F)
    }
}
