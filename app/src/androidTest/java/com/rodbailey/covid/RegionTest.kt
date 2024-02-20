package com.rodbailey.covid

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rodbailey.covid.dom.Region
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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