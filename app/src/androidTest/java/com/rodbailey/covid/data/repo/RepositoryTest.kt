package com.rodbailey.covid.data.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.db.AppDatabase
import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionEntity
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.data.source.RemoteDataSource
import com.rodbailey.covid.data.net.FakeCovidAPI
import com.rodbailey.covid.data.source.DefaultLocalDataSource
import com.rodbailey.covid.data.source.LocalDataSource
import com.rodbailey.covid.domain.Region
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import javax.inject.Inject

/**
 * These tests run with the REAL [DefaultCovidRepository] (under test), the [FakeCovidAPI] and an
 * in-memory Room Database that is empty at the beginning of each test.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
@HiltAndroidTest
class RepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeAPI: CovidAPI

    @Inject
    lateinit var remoteDataSource: RemoteDataSource

    // Don't inject the LocalDataSource because the regionDao and regionStatsDao below
    // will NOT be supplied to DefaultLocalDataSource's constructor. Instead, the *real*
    // regionDao and regionStatsDao will be supplied.
    lateinit var localDataSource: LocalDataSource

    private lateinit var db: AppDatabase
    private lateinit var regionDao: RegionDao
    private lateinit var regionStatsDao: RegionStatsDao
    private lateinit var repo: DefaultCovidRepository

    @Before
    fun setup() {
        hiltRule.inject()

        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        regionDao = db.regionDao()
        regionStatsDao = db.regionStatsDao()
        localDataSource = DefaultLocalDataSource(regionDao, regionStatsDao)
        repo = DefaultCovidRepository(localDataSource, remoteDataSource)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun first_country_list_load_is_from_network() = runBlocking {
        repo.getRegions().test {
            val repoRegions = awaitItem()
            // All regions returned by FakeCovidAPI
            Assert.assertEquals(FakeRegions.REGIONS.size, repoRegions.size)
            Assert.assertTrue(containsRegion(repoRegions, FakeRegions.regionByIso3Code("AUS")))
            Assert.assertTrue(containsRegion(repoRegions, FakeRegions.regionByIso3Code("NLD")))

            awaitComplete()
        }

        // Regions returned by FakeCovidAPI should also be cached in the database
        // as a side-effect of having been retrieved from the network
        val cachedRegions = regionDao.getAllRegions()
        Assert.assertEquals(FakeRegions.REGIONS.size, cachedRegions.size)
        Assert.assertTrue(
            containsRegionEntity(
                cachedRegions,
                FakeRegions.regionByIso3Code("AUS")
            )
        )
        Assert.assertTrue(
            containsRegionEntity(
                cachedRegions,
                FakeRegions.regionByIso3Code("NLD")
            )
        )

        // Regions should have been fetched from the network
        Assert.assertTrue((fakeAPI as FakeCovidAPI).wasCalled())
    }

    @Test
    fun second_country_list_load_is_from_database() = runTest {
        repo.getRegions().test {
            val repoRegions = awaitItem()

            // API was called, because country list isn't in database
            Assert.assertTrue((fakeAPI as FakeCovidAPI).wasCalled())

            // All regions returned by FakeCovidAPI
            Assert.assertEquals(FakeRegions.REGIONS.size, repoRegions.size)
            Assert.assertTrue(containsRegion(repoRegions, FakeRegions.regionByIso3Code("AUS")))
            Assert.assertTrue(containsRegion(repoRegions, FakeRegions.regionByIso3Code("NLD")))

            awaitComplete()
        }

        // Clear the "was called" flag for the API
        (fakeAPI as FakeCovidAPI).clearWasCalled()

        repo.getRegions().test {
            val repoRegions = awaitItem()
            Assert.assertEquals(FakeRegions.REGIONS.size, repoRegions.size)

            // API was NOT called, because country list came from database
            Assert.assertFalse((fakeAPI as FakeCovidAPI).wasCalled())

            awaitComplete()
        }
    }

    @Test
    fun first_region_stats_load_is_from_network_second_is_from_database() = runBlocking {
        repo.getReport("AUS").test {
            val repoData = awaitItem()
            // First load of AUS stats is from network
            Assert.assertTrue((fakeAPI as FakeCovidAPI).wasCalled())
            Assert.assertEquals(
                FakeRegions.REGIONS.filter { it.key.iso3Code == "AUS" }.values.first(),
                repoData
            )

            awaitComplete()
        }

        (fakeAPI as FakeCovidAPI).clearWasCalled()

        // The data just retrieved should also be cached in the database, meaning the API was not called
        val cachedStats = regionStatsDao.getRegionStats("AUS")
        // API wasn't called as data is now cached in the db
        Assert.assertFalse((fakeAPI as FakeCovidAPI).wasCalled())
        Assert.assertEquals(1, cachedStats.size)
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("AUS").active,
            cachedStats[0].active
        )
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("AUS").deaths,
            cachedStats[0].deaths
        )
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("AUS").fatalityRate,
            cachedStats[0].fatalityRate
        )
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("AUS").confirmed,
            cachedStats[0].confirmed
        )
        Assert.assertEquals(
            FakeRegions.reportDataByIso3Code("AUS").recovered,
            cachedStats[0].recovered
        )
    }

    @Test
    fun first_load_global_stats_is_from_network() = runBlocking {
        repo.getReport(null).test {
            val globalStats = awaitItem()
            Assert.assertTrue((fakeAPI as FakeCovidAPI).wasCalled())
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS, globalStats)

            awaitComplete()
        }

        // Check database for global stats
        val dbStats = regionStatsDao.getRegionStats(FakeRegions.GLOBAL_REGION.iso3Code)
        Assert.assertEquals(1, dbStats.size)
        Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.active, dbStats[0].active)
        Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.confirmed, dbStats[0].confirmed)
        Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.deaths, dbStats[0].deaths)
        Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS.recovered, dbStats[0].recovered)
        Assert.assertEquals(
            FakeRegions.GLOBAL_REGION_STATS.fatalityRate,
            dbStats[0].fatalityRate
        )
    }

    @Test
    fun search_for_already_cached_region_by_partial_name_gives_single_result() = runBlocking {
        repo.getRegions().test {
            val repoRegions = awaitItem()
            Assert.assertEquals(FakeRegions.REGIONS.size, repoRegions.size)

            awaitComplete()
        }

        val matchingDbRegions = regionDao.getRegionsByName("Viet")
        Assert.assertEquals(1, matchingDbRegions.size)
        Assert.assertEquals("Vietnam", matchingDbRegions[0].name)
    }

    @Test
    fun search_for_already_cached_region_by_partial_name_two_results() = runBlocking {
         repo.getRegions().test {
             val repoRegions = awaitItem()
             Assert.assertEquals(FakeRegions.REGIONS.size, repoRegions.size)

             awaitComplete()
        }

        val result = regionDao.getRegionsByName("au")
        Assert.assertEquals(2, result.size)
        Assert.assertTrue(
            (result[0].name == "Austria" && result[1].name == "Australia")
                    || (result[0].name == "Australia" && result[1].name == "Austria")
        )
    }

    private fun containsRegion(allRegions: List<Region>, target: Region): Boolean {
        for (region in allRegions) {
            if (region.iso3Code == target.iso3Code && region.name == target.name) {
                return true
            }
        }
        return false
    }

    private fun containsRegionEntity(
        allRegions: List<RegionEntity>, target: Region
    ): Boolean {
        for (region in allRegions) {
            if (region.iso3code == target.iso3Code && region.name == target.name) {
                return true
            }
        }
        return false
    }
}