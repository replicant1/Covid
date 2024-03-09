package com.rodbailey.covid.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
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
    fun insertRegion_retrievedRegionIsSame() = runTest {
        val region = RegionEntity(iso3code = "AUS", name = "Australia")
        regionDao.insert(listOf(region))

        val result = regionDao.getAllRegions()
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("AUS", result[0].iso3code)
        Assert.assertEquals("Australia", result[0].name)
    }

    @Test
    fun insertTwoRegions_regionCountIsTwo() = runTest {
        val region1 = RegionEntity(iso3code = "TWN", name = "Taiwan")
        val region2 = RegionEntity(iso3code = "CHN", name = "China")
        val regionList = listOf(region1, region2)

        regionDao.insert(regionList)

        val result = regionDao.getRegionCount()
        Assert.assertEquals(2, result)
    }

    @Test
    fun insertRegionAndDeleteIt_RegionCountIsZero() = runBlocking {
        val region = RegionEntity(iso3code = "AUS", name = "Australia")

        regionDao.insert(listOf(region))
        regionDao.deleteAllRegions()

        val result = regionDao.getRegionCount()
        Assert.assertEquals(0, result)
    }

    @Test
    fun getNonExistentRegion_IsEmpty() = runBlocking {
        val results = regionDao.getRegionsByIso3Code("XXX")
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun insertTwoRegions_DifferentPrimaryKeys() = runBlocking {
        val region1 = RegionEntity(iso3code = "TWN", name = "North Taiwan")
        val region2 = RegionEntity(iso3code = "TWN", name = "South Taiwan")

        regionDao.insert(listOf(region1, region2))

        val allRegions = regionDao.getAllRegions()
        Assert.assertEquals(2, allRegions.size)
        Assert.assertNotEquals(allRegions[0].id, allRegions[1].id)
    }

    @Test
    fun insertThenDeleteAllRegions_emptyCount() = runBlocking {
        val region1 = RegionEntity(iso3code = "AAA", name = "Alabama")
        val region2 = RegionEntity(iso3code = "BBB", name = "Bahamas")

        regionDao.insert(listOf(region1, region2))
        regionDao.deleteAllRegions()

        val count = regionDao.getRegionCount()
        Assert.assertEquals(0, count)
    }

    @Test
    fun insert_three_regions_retrieve_one_by_full_name_in_correct_case() = runBlocking {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        val result = regionDao.getRegionsByName("Alabama")
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("AAA", result[0].iso3code)
        Assert.assertEquals("Alabama", result[0].name)
    }

    @Test
    fun insert_three_regions_retrieve_one_by_full_name_in_upper_case() = runBlocking {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        val result = regionDao.getRegionsByName("CANADA")
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("CAN", result[0].iso3code)
        Assert.assertEquals("Canada", result[0].name)
    }

    @Test
    fun insert_three_regions_retrieve_one_by_partial_name_in_correct_case() = runBlocking {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        val result = regionDao.getRegionsByName("Bah")
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("BBB", result[0].iso3code)
        Assert.assertEquals("Bahamas", result[0].name)
    }

    @Test
    fun insert_three_regions_retrieve_two_by_partial_name_in_lower_case() = runBlocking {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "BBB", name = "Bahamas")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        // "ba" should match Alabama and Bahamas
        val result = regionDao.getRegionsByName("ba")
        Assert.assertEquals(2, result.size)
    }

    @Test
    fun insert_three_regions_retrieve_by_first_letters_of_words_fails() = runBlocking {
        val alabama = RegionEntity(iso3code = "AAA", name = "Alabama")
        val bahamas = RegionEntity(iso3code = "HTO", name = "Home Town")
        val canada = RegionEntity(iso3code = "CAN", name = "Canada")

        regionDao.insert(listOf(alabama, bahamas, canada))

        val result = regionDao.getRegionsByName("HT")
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun insert_regions_with_same_iso_code_and_retrieve_by_iso_code() = runBlocking {
        val region1 = RegionEntity(iso3code = "ABC", name = "Alice")
        val region2 = RegionEntity(iso3code = "ABC", name = "Bob")

        regionDao.insert(listOf(region1, region2))

        val result = regionDao.getRegionsByIso3Code("ABC")
        Assert.assertEquals(2, result.size)
        Assert.assertTrue(
            (result[0].name == "Alice" && result[1].name == "Bob") ||
                    (result[0].name == "Bob" && result[1].name == "Alice")
        )
    }

}