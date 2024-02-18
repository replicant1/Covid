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
class RegionDbTest {

    private lateinit var db:AppDatabase
    private lateinit var regionDao: RegionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        regionDao = db.regionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertRegion_retrievedRegionIsSame() = runBlocking() {
        val region = RegionEntity("AUS", "Australia")
        regionDao.insert(listOf(region))

        val result = regionDao.getAllRegions()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("AUS", result[0].iso3code)
        Assert.assertEquals("Australia", result[0].name)
    }

    @Test
    fun insertTwoRegions_regionCountIsTwo() = runBlocking {
        val region1 = RegionEntity("TWN", "Taiwan")
        val region2 = RegionEntity("CHN", "China")
        val regionList = listOf(region1, region2)

        regionDao.insert(regionList)

        val result = regionDao.getRegionCount()
        Assert.assertEquals(2, result)
    }

    @Test
    fun insertRegionAndDeleteIt_RegionCountIsZero() = runBlocking {
        val region = RegionEntity("AUS", "Australia")

        regionDao.insert(listOf(region))
        Assert.assertEquals(1, regionDao.getRegionCount())

        regionDao.deleteAllRegions()
        Assert.assertEquals(0, regionDao.getRegionCount())
    }

    @Test
    fun getNonExistentRegion_IsNull() = runBlocking {
        val result = regionDao.getRegion("XXX")
        Assert.assertNull(result)
    }
}