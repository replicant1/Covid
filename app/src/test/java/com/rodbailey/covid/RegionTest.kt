package com.rodbailey.covid

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
}