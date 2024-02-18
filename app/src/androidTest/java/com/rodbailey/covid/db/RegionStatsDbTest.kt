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
        Assert.assertEquals("AUS", statsOut!!.iso3code)
        Assert.assertEquals(1234L, statsOut!!.confirmed)
        Assert.assertEquals(101L, statsOut!!.deaths)
        Assert.assertEquals(2345L, statsOut!!.recovered)
        Assert.assertEquals(999L, statsOut!!.active)
    }

    @Test
    fun retrieveBadIso3Code_IsNull() = runBlocking {
        val result = statsDao.getRegionStats("UAS")
        Assert.assertNull(result)
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

        statsDao.deleteAllStats()
        val rowCount = statsDao.getRegionStatsCount()
        Assert.assertEquals(0, rowCount)
    }
}