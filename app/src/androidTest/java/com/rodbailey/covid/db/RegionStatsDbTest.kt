package com.rodbailey.covid.db

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
    private lateinit var statsDao : RegionStatsDao

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
    fun insertStats_retrievedStatsAreSame() = runBlocking {
        val statsIn = RegionStatsEntity(
            iso3code = "AUS",
            confirmed = 1234L,
            deaths = 101L,
            recovered = 2345L,
            active = 999L
        )
        statsDao.insert(statsIn)

        val statsOut =  statsDao.getRegionStats("AUS")

        Assert.assertNotNull(statsOut)
        Assert.assertFalse(statsOut.isEmpty())
        Assert.assertEquals("AUS", statsOut[0].iso3code)
        Assert.assertEquals(1234L, statsOut[0].confirmed)
        Assert.assertEquals(101L, statsOut[0].deaths)
        Assert.assertEquals(2345L, statsOut[0].recovered)
        Assert.assertEquals(999L, statsOut[0].active)
    }

    @Test
    fun retrieveBadIso3Code_IsEmpty() = runBlocking {
        val result = statsDao.getRegionStats("UAS")
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAll_RowCountIsZero() = runBlocking {
        val ausStats = RegionStatsEntity(
            iso3code = "AUS",
            confirmed = 10L,
            deaths = 20L,
            recovered = 30L,
            active = 40L
        )
        statsDao.insert(ausStats)

        statsDao.deleteAllRegionStats()
        val rowCount = statsDao.getRegionStatsCount()
        Assert.assertEquals(0, rowCount)
    }

    @Test
    fun insertTwoStats_UniquePrimaryKeys() = runBlocking {
        val stats1 = RegionStatsEntity(
            iso3code = "AAA", confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L
        )
        val stats2 = RegionStatsEntity(
            iso3code = "AAA", confirmed = 1L, deaths = 2L, recovered = 3L, active = 4L
        )
        statsDao.insert(stats1)
        statsDao.insert(stats2)

        val results = statsDao.getRegionStats("AAA")
        Assert.assertNotEquals(results[0].id, results[1].id)
    }
}