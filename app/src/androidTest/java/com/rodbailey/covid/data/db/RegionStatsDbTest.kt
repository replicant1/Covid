package com.rodbailey.covid.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class RegionStatsDbTest {

    private lateinit var db: AppDatabase
    private lateinit var statsDao: RegionStatsDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        statsDao = db.regionStatsDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_stats_retrieved_stats_are_same() = runBlocking {
        val statsIn = RegionStatsEntity(
            iso3code = "AUS",
            confirmed = 1234L,
            deaths = 101L,
            recovered = 2345L,
            active = 999L,
            fatalityRate = 0.5F
        )
        statsDao.insert(statsIn)

        val statsOut = statsDao.getRegionStats("AUS")
        Assert.assertNotNull(statsOut)
        Assert.assertFalse(statsOut.isEmpty())
        Assert.assertEquals("AUS", statsOut[0].iso3code)
        Assert.assertEquals(1234L, statsOut[0].confirmed)
        Assert.assertEquals(101L, statsOut[0].deaths)
        Assert.assertEquals(2345L, statsOut[0].recovered)
        Assert.assertEquals(999L, statsOut[0].active)
    }

    @Test
    fun retrieve_bad_iso3Code_is_empty() = runBlocking {
        val result = statsDao.getRegionStats("UAS")
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun insert_two_stats_unique_primary_keys() = runBlocking {
        val stats1 = RegionStatsEntity(
            iso3code = "AAA", confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L,
            fatalityRate = 0.5F
        )
        val stats2 = RegionStatsEntity(
            iso3code = "AAA", confirmed = 1L, deaths = 2L, recovered = 3L, active = 4L,
            fatalityRate = 0.5F
        )
        statsDao.insert(stats1)
        statsDao.insert(stats2)

        val results = statsDao.getRegionStats("AAA")
        Assert.assertNotEquals(results[0].id, results[1].id)
    }

    @Test
    fun insert_two_stats_with_different_iso_codes_retrieve_one_by_iso_code() = runBlocking {
        val stats1 = RegionStatsEntity(
            iso3code = "ABC", confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L,
            fatalityRate = 0.5F
        )
        val stats2 = RegionStatsEntity(
            iso3code = "DEF", confirmed = 1L, deaths = 2L, recovered = 3L, active = 4L,
            fatalityRate = 0.6F
        )
        statsDao.insert(stats1)
        statsDao.insert(stats2)

        val results = statsDao.getRegionStats("ABC")
        Assert.assertTrue(results.isNotEmpty())
        Assert.assertEquals("ABC", results[0].iso3code)
        Assert.assertEquals(10L, results[0].confirmed)
        Assert.assertEquals(20L, results[0].deaths)
        Assert.assertEquals(30L, results[0].recovered)
        Assert.assertEquals(40L, results[0].active)
        Assert.assertEquals(0.5F, results[0].fatalityRate)

        val results2 = statsDao.getRegionStatsCount("ABC")
        Assert.assertEquals(1, results2)

        val results3 = statsDao.getRegionStatsCount()
        Assert.assertEquals(2, results3)

    }
}