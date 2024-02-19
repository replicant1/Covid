package com.rodbailey.covid

import com.rodbailey.covid.db.RegionEntity
import com.rodbailey.covid.db.RegionStatsEntity
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.dom.TransformUtils
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TransformUtilsTest {

    private val regionStatsEntityAus = RegionStatsEntity(
        iso3code = "AUS",
        confirmed = 10L,
        deaths = 20L,
        recovered = 30L,
        active = 40L,
        fatalityRate = 0.5F
    )

    private val reportDataAus = ReportData(
        confirmed = 10L,
        deaths = 20L,
        recovered = 30L,
        active = 40L,
        fatalityRate = 0.5F
    )

    private val regionAus = Region(iso3Code = "AUS", name = "Australia")

    private val regionEntityAus = RegionEntity(
        iso3code = "AUS",
        name = "Australia"
    )

    @Test
    fun regionStatsEntityToReportData() {
        val actual = TransformUtils.regionStatsEntityToReportData(regionStatsEntityAus)
        assertEquals(reportDataAus, actual)
    }

    @Test
    fun reportDataToRegionStatsEntity() {
        assertEquals(
            regionStatsEntityAus,
            TransformUtils.reportDataToRegionStatsEntity("AUS", reportDataAus)
        )
    }

    @Test
    fun regionToRegionEntity() {
        assertEquals(
            TransformUtils.regionToRegionEntity(regionAus),
            regionEntityAus
        )
    }

    @Test
    fun regionEntityToRegion() {
        assertEquals(
            TransformUtils.regionEntityToRegion(regionEntityAus),
            regionAus
        )
    }

    @Test
    fun regionListToRegionEntityList() {
        val inList = listOf(regionAus)
        val outList = TransformUtils.regionListToRegionEntityList(inList)
        assertEquals(1, outList.size)
        assertEquals(regionEntityAus, outList[0])
    }

    @Test
    fun regionEntityListToRegionList() {
        val inList = listOf(regionEntityAus)
        val outList = TransformUtils.regionEntityListToRegionList(inList)
        assertEquals(1, outList.size)
        assertEquals(regionAus, outList[0])
    }
}