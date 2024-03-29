package com.rodbailey.covid.domain

import com.rodbailey.covid.data.db.RegionEntity
import com.rodbailey.covid.data.db.RegionStatsEntity
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.TransformUtils
import org.junit.Assert.assertEquals
import org.junit.Test

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