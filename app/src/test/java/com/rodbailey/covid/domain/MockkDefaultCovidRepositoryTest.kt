package com.rodbailey.covid.domain

import app.cash.turbine.test
import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionEntity
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.RegionStatsEntity
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.repo.CovidRepository
import com.rodbailey.covid.data.repo.DefaultCovidRepository
import com.rodbailey.covid.data.repo.RegionCode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test DefaultCovidRepository by supplying mock dependent objects to it.
 */

class MockkDefaultCovidRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var mRegionDao: RegionDao

    private lateinit var mRegionStatsDao: RegionStatsDao

    private lateinit var mCovidAPI: CovidAPI

    private lateinit var mRepo: CovidRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mRegionDao = mockk<RegionDao>()
        mRegionStatsDao = mockk<RegionStatsDao>()
        mCovidAPI = mockk<CovidAPI>()
        mRepo = DefaultCovidRepository(mRegionDao, mRegionStatsDao, mCovidAPI)
    }

    @Test
    fun repo_gets_regions_from_api_at_first(): Unit = runBlocking {
        coEvery { mCovidAPI.getRegions() } returns RegionList(listOf(Region("AUS", "Australia")))
        coEvery { mRegionDao.getAllRegionsStream() } returns flowOf(
            emptyList(),
            listOf(RegionEntity(1, "TWN", "Taiwan"))
        )
        val regionListSlot = slot<List<RegionEntity>>()
        coEvery { mRegionDao.insert(capture(regionListSlot)) } just runs

        val regionsFlow = mRepo.getRegionsStream()

        regionsFlow.test {
            coVerify(exactly = 1) { mCovidAPI.getRegions() } // Retrieved regions from API
            coVerify(exactly = 0) { mCovidAPI.getReport(any()) } // Didn't call this - unneeded

            // The valdue returned by the API is inserted into the db
            Assert.assertTrue(regionListSlot.isCaptured)
            Assert.assertEquals(1, regionListSlot.captured.size)
            Assert.assertEquals("AUS", regionListSlot.captured.first().iso3code)

            // Returned empty list first time, region list second time
            coVerify(exactly = 1) { mRegionDao.getAllRegionsStream() }

            val item1 = awaitItem() // []

            val item2 = awaitItem()
            Assert.assertEquals("TWN", item2.first().iso3Code)
            Assert.assertEquals("Taiwan", item2.first().name)
            awaitComplete()
        }
    }

    @Test
    fun repo_gets_regions_from_db_on_second(): Unit = runBlocking {
        coEvery { mRegionDao.getAllRegionsStream() } returns flowOf(
            listOf(
                RegionEntity(
                    1,
                    "AUS",
                    "Australia"
                )
            )
        )

        val regionsFlow = mRepo.getRegionsStream()

        regionsFlow.test {
            coVerify(exactly = 1) { mRegionDao.getAllRegionsStream() }
            coVerify(exactly = 0) { mCovidAPI.getRegions() }
            val item1 = awaitItem()
            Assert.assertEquals("AUS", item1.first().iso3Code)
            Assert.assertEquals("Australia", item1.first().name)
            awaitComplete()
        }
    }

    @Test
    fun repo_first_access_of_region_stats_comes_from_api(): Unit = runBlocking {
        coEvery { mCovidAPI.getReport("AUS") } returns
                Report(
                    ReportData(
                        confirmed = 1,
                        deaths = 2,
                        recovered = 3,
                        active = 4,
                        fatalityRate = 0.87F
                    )
                )
        coEvery { mRegionStatsDao.getRegionStats("AUS") } returns emptyList()
        coEvery { mRegionStatsDao.insert(any())} just runs

        val regionStatsFlow = mRepo.getRegionStatsStream(RegionCode("AUS"))

        regionStatsFlow.test {
            coVerify(exactly = 1) { mCovidAPI.getReport("AUS") }
            coVerify(exactly = 1) { mRegionStatsDao.getRegionStats("AUS")}
            val item1 = awaitItem()
            Assert.assertEquals(1, item1.size)
            Assert.assertEquals("AUS", item1.first().iso3Code)
            awaitComplete()
        }
    }


}