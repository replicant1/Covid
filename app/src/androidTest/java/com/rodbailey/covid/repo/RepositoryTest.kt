package com.rodbailey.covid.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.db.RegionDao
import com.rodbailey.covid.db.RegionEntity
import com.rodbailey.covid.db.RegionStatsDao
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.RegionList
import com.rodbailey.covid.dom.Report
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPI
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class RepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var regionDao: RegionDao
    private lateinit var regionStatsDao : RegionStatsDao
    private lateinit var covidAPI : CovidAPI
    private lateinit var repo : CovidRepository
    private val covidAPICalledFlag : Flag = Flag()

    class Flag() {
        var isSet: Boolean = false

        fun set() {
            isSet = true
        }

        fun clear() {
            isSet = false
        }
    }

    class FakeCovidAPI(val flag: Flag) : CovidAPI {
        companion object {
            val REPORT_DATA = ReportData(
                confirmed = 1L,
                deaths = 2L,
                recovered = 3L,
                active = 4L,
                fatalityRate = 0.5F
            )
        }
        override suspend fun getReport(iso3Code: String?): Report {
            println("getReport called with ido3code = $iso3Code")
            flag.set()
            return Report(REPORT_DATA)
        }

        override suspend fun getRegions(): RegionList {
            flag.set()
            return RegionList(listOf(
                Region("AUS", "Australia"),
                Region("VNM", "Vietnam")
            ))
        }
    }

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        regionDao = db.regionDao()
        regionStatsDao = db.regionStatsDao()
        covidAPI = FakeCovidAPI(covidAPICalledFlag)
        repo = CovidRepository(regionDao, regionStatsDao, covidAPI)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun first_country_list_load_is_from_network() = runBlocking {
        val repoRegions = repo.getRegions()
        // All regions returned by FakeCovidAPI
        Assert.assertEquals(2, repoRegions.size)
        Assert.assertTrue(containsRegion(repoRegions, "AUS", "Australia"))
        Assert.assertTrue(containsRegion(repoRegions, "VNM", "Vietnam"))

        // Regions returned by FakeCovidAPI should also be cached in the database
        val cachedRegions = regionDao.getAllRegions()
        Assert.assertEquals(2, cachedRegions.size)
        Assert.assertTrue(containsRegionEntity(cachedRegions, "AUS", "Australia"))
        Assert.assertTrue(containsRegionEntity(cachedRegions,"VNM", "Vietnam"))

        // Regions should have been fetched from the network
        Assert.assertTrue(covidAPICalledFlag.isSet)
    }

    @Test
    fun second_country_list_load_is_from_database() = runBlocking {
        val repoRegions = repo.getRegions()
        // All regions returned by FakeCovidAPI
        Assert.assertEquals(2, repoRegions.size)
        Assert.assertTrue(containsRegion(repoRegions, "AUS", "Australia"))
        Assert.assertTrue(containsRegion(repoRegions, "VNM", "Vietnam"))

        covidAPICalledFlag.clear()
        repo.getRegions()

        Assert.assertFalse(covidAPICalledFlag.isSet) // No network call was made
    }

    @Test
    fun first_region_stats_load_is_from_network() = runBlocking {
        val repoData = repo.getReport("AUS")

        Assert.assertTrue(covidAPICalledFlag.isSet)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA, repoData)

        // The data just retrieved should also be cached in the database
        val cachedRepoData = regionStatsDao.getRegionStats("AUS")
        Assert.assertEquals(1, cachedRepoData.size)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA.active, cachedRepoData[0].active)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA.deaths, cachedRepoData[0].deaths)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA.fatalityRate, cachedRepoData[0].fatalityRate)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA.confirmed, cachedRepoData[0].confirmed)
        Assert.assertEquals(FakeCovidAPI.REPORT_DATA.recovered, cachedRepoData[0].recovered)
    }

    @Test
    fun second_region_stats_load_is_from_database() = runBlocking {
        repo.getReport("AUS")
        covidAPICalledFlag.clear()

        val repoData = repo.getReport("AUS")
        Assert.assertFalse(covidAPICalledFlag.isSet)


    }

    private fun containsRegion(allRegions: List<Region>, iso:String, name:String): Boolean {
        for (region in allRegions) {
            if (region.iso3Code == iso && region.name == name) {
                return true
            }
        }
        return false
    }

    private fun containsRegionEntity(allRegions: List<RegionEntity>, iso:String, name:String) : Boolean {
        for (region in allRegions) {
            if (region.iso3code == iso && region.name == name) {
                return true
            }
        }
        return false
    }
}