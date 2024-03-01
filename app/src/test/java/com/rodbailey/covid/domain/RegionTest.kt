package com.rodbailey.covid.domain

import com.rodbailey.covid.domain.Region
import org.junit.Assert
import org.junit.Test

class RegionTest {

    @Test
    fun emptyStringMatchesAll() {
        val region = Region(iso3Code = "RRR", name = "A Region")
        Assert.assertTrue(region.matchesSearchQuery(""))
    }

    @Test
    fun subStringMatches() {
        val region = Region(iso3Code = "RRR", name = "A Region")
        Assert.assertTrue(region.matchesSearchQuery("egi"))
    }

    @Test
    fun matchIsCaseInsensitive() {
        val region = Region(iso3Code = "RRR", name = "A Region")
        Assert.assertTrue(region.matchesSearchQuery("reg"))
    }

    @Test
    fun firstLettersMatchMultiWord() {
        val region = Region("UAE", "United Arab Emirates")
        Assert.assertTrue(region.matchesSearchQuery("ua"))
        Assert.assertTrue(region.matchesSearchQuery("ae"))
        Assert.assertTrue(region.matchesSearchQuery("uae"))
    }

    @Test
    fun lastLettersDoNotMatchMultiWord() {
        val region = Region("UAE", "United Arab Emirates")
        Assert.assertFalse(region.matchesSearchQuery("da"))
        Assert.assertFalse(region.matchesSearchQuery("db"))
    }
}