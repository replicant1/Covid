package com.rodbailey.covid.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

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
    fun insert_region_then_retrieve_is_same() = runTest {
        val region = RegionEntity(iso3code = "AUS", name = "Australia")
        regionDao.insert(listOf(region))

        regionDao.getAllRegions().test {
            val result = awaitItem()
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("AUS", result[0].iso3code)
            Assert.assertEquals("Australia", result[0].name)
        }
    }

    @Test
    fun insert_two_regions_gives_region_count_of_two() = runTest {
        val region1 = RegionEntity(iso3code = "TWN", name = "Taiwan")
        val region2 = RegionEntity(iso3code = "CHN", name = "China")
        val regionList = listOf(region1, region2)

        regionDao.insert(regionList)

        regionDao.getRegionCount().test {
            val result = awaitItem()
            Assert.assertEquals(2, result)
        }
    }

    @Test
    fun insert_region_and_delete_it_gives_region_count_of_zero() = runTest {
        val region = RegionEntity(iso3code = "AUS", name = "Australia")

        regionDao.insert(listOf(region))
        regionDao.deleteAllRegions()

        regionDao.getRegionCount().test {
            val result = awaitItem()
            Assert.assertEquals(0, result)
        }
    }

    @Test
    fun get_non_existent_region_is_empty() = runTest {
        regionDao.getRegionsByIso3Code("XXX").test {
            val results = awaitItem()
            Assert.assertTrue(results.isEmpty())
        }
    }

    @Test
    fun insert_two_regions_different_primary_keys() = runTest {
        val region1 = RegionEntity(iso3code = "TWN", name = "North Taiwan")
        val region2 = RegionEntity(iso3code = "TWN", name = "South Taiwan")

        regionDao.insert(listOf(region1, region2))

        regionDao.getAllRegions().test {
            val allRegions = awaitItem()
            Assert.assertEquals(2, allRegions.size)
            Assert.assertNotEquals(allRegions[0].id, allRegions[1].id)
        }
    }

    @Test
    fun insert_then_delete_all_regions_gives_zero_region_count() = runTest {
        val region1 = RegionEntity(iso3code = "AAA", name = "Alabama")
        val region2 = RegionEntity(iso3code = "BBB", name = "Bahamas")

        regionDao.insert(listOf(region1, region2))
        regionDao.deleteAllRegions()

        regionDao.getRegionCount().test {
            val count = awaitItem()
            Assert.assertEquals(0, count)
        }
    }

    @Test
    fun insert_three_regions_retrieve_one_by_full_name_in_correct_case() = runTest {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        regionDao.getRegionsByName("Alabama").test {
            val result = awaitItem()
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("AAA", result[0].iso3code)
            Assert.assertEquals("Alabama", result[0].name)
        }
    }

    @Test
    fun insert_three_regions_retrieve_one_by_full_name_in_upper_case() = runTest {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        regionDao.getRegionsByName("CANADA").test {
            val result = awaitItem()
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("CAN", result[0].iso3code)
            Assert.assertEquals("Canada", result[0].name)
        }
    }

    @Test
    fun insert_three_regions_retrieve_one_by_partial_name_in_correct_case() = runTest {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        regionDao.getRegionsByName("Bah").test {
            val result = awaitItem()
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("BBB", result[0].iso3code)
            Assert.assertEquals("Bahamas", result[0].name)
        }
    }

    @Test
    fun insert_three_regions_retrieve_two_by_partial_name_in_lower_case() = runTest {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        // "ba" should match Alabama and Bahamas
        regionDao.getRegionsByName("ba").test {
            val result = awaitItem()
            Assert.assertEquals(2, result.size)
        }
    }

    @Test
    fun insert_three_regions_retrieve_by_first_letters_of_words_fails() = runTest {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "HTO", name = "Home Town")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        regionDao.getRegionsByName("HT").test {
            val result = awaitItem()
            Assert.assertEquals(0, result.size)
        }
    }

    @Test
    fun insert_regions_with_same_iso_code_and_retrieve_by_iso_code() = runTest {
        val region1 = RegionEntity(iso3code = "ABC", name = "Alice")
        val region2 = RegionEntity(iso3code = "ABC", name = "Bob")

        regionDao.insert(listOf(region1, region2))

        regionDao.getRegionsByIso3Code("ABC").test {
            val result = awaitItem()
            Assert.assertEquals(2, result.size)
            Assert.assertTrue(
                (result[0].name == "Alice" && result[1].name == "Bob") ||
                        (result[0].name == "Bob" && result[1].name == "Alice")
            )
        }
    }

}