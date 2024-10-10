package com.rodbailey.covid.domain

import com.rodbailey.covid.data.db.RegionEntity
import com.rodbailey.covid.data.db.RegionStatsEntity
import com.rodbailey.covid.data.db.toRegion
import com.rodbailey.covid.data.db.toRegionList
import com.rodbailey.covid.data.db.toReportData
import com.rodbailey.covid.data.repo.RegionCode
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
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

    private val regionCodeAus = RegionCode("AUS")

    private val regionEntityAus = RegionEntity(
        iso3code = "AUS",
        name = "Australia"
    )

    @Test
    fun regionStatsEntityToReportData() {
        val actual = regionStatsEntityAus.toReportData()
        assertEquals(reportDataAus, actual)
    }

    @Test
    fun reportDataToRegionStatsEntity() {
        assertEquals(
            regionStatsEntityAus,
            reportDataAus.toRegionStatsEntity("AUS")
        )
    }

    @Test
    fun regionToRegionEntity() {
        assertEquals(
            regionAus.toRegionEntity(),
            regionEntityAus
        )
    }

    @Test
    fun regionEntityToRegion() {
        assertEquals(
            regionEntityAus.toRegion(),
            regionAus
        )
    }

    @Test
    fun regionListToRegionEntityList() {
        val inList = listOf(regionAus)
        val outList = inList.toRegionEntityList()
        assertEquals(1, outList.size)
        assertEquals(regionEntityAus, outList[0])
    }

    @Test
    fun regionEntityListToRegionList() {
        val inList = listOf(regionEntityAus)
        val outList =inList.toRegionList()
        assertEquals(1, outList.size)
        assertEquals(regionAus, outList[0])
    }

    @Test
    fun regionToRegionCode() {
        assertEquals(
            "AUS",
            regionAus.toRegionCode().chars
        )
    }
}