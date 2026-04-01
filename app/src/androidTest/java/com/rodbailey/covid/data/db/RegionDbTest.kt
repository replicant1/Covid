package com.rodbailey.covid.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class RegionDbTest {

    private lateinit var db: AppDatabase
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
    fun insert_two_regions_gives_region_count_of_two() = runTest {
        val region1 = RegionEntity(iso3code = "TWN", name = "Taiwan")
        val region2 = RegionEntity(iso3code = "CHN", name = "China")
        val regionList = listOf(region1, region2)

        val regionsFlow = regionDao.getAllRegionsStream()

        regionDao.insert(regionList)

        regionsFlow.test {
            val regionEntityList = awaitItem()
            Assert.assertEquals(2, regionEntityList.size)
            cancel()
        }
    }

    @Test
    fun get_region_count_returns_zero_for_empty_table() = runTest {
        Assert.assertEquals(0, regionDao.getRegionCount())
    }

    @Test
    fun get_region_count_reflects_number_of_inserted_regions() = runTest {
        val regions = listOf(
            RegionEntity(iso3code = "TWN", name = "Taiwan"),
            RegionEntity(iso3code = "CHN", name = "China"),
            RegionEntity(iso3code = "AUS", name = "Australia")
        )
        regionDao.insert(regions)
        Assert.assertEquals(regions.size, regionDao.getRegionCount())
    }
}