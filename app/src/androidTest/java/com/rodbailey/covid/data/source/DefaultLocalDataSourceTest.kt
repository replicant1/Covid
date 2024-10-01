//package com.rodbailey.covid.data.source
//
//import androidx.room.Room
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import app.cash.turbine.test
//import com.rodbailey.covid.data.db.AppDatabase
//import com.rodbailey.covid.data.db.RegionDao
//import com.rodbailey.covid.data.db.RegionStatsDao
//import com.rodbailey.covid.domain.Region
//import com.rodbailey.covid.domain.ReportData
//import kotlinx.coroutines.test.runTest
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//
///**
// * These tests mirror the tests in RegionDbTest.kt and RegionStatsDbTest.kt. But these tests
// * operator on LocalDataSource methods returning Flows of basic data types instead of the basic
// * data types themselves.
// */
//@RunWith(AndroidJUnit4::class)
//class DefaultLocalDataSourceTest {
//
//    private lateinit var db: AppDatabase
//    private lateinit var regionDao: RegionDao
//    private lateinit var regionStatsDao : RegionStatsDao
//
//    private lateinit var localDataSource: LocalDataSource
//
//    @Before
//    fun setup() {
//        db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            AppDatabase::class.java
//        ).allowMainThreadQueries().build()
//        regionDao = db.regionDao()
//        regionStatsDao = db.regionStatsDao()
//        localDataSource = DefaultLocalDataSource(regionDao, regionStatsDao)
//    }
//
//    @After
//    fun teardown() {
//        db.close()
//    }
//
//    @Test
//    fun save_region_then_retrieve_is_same() = runTest {
//        val region = Region(iso3Code = "AUS", name = "Australia")
//        localDataSource.saveRegions(listOf(region))
//
//        localDataSource.loadAllRegions().test {
//            val result = awaitItem()
//            Assert.assertEquals(region, result[0])
//
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_two_regions_gives_region_count_of_two() = runTest {
//        val region1 = Region(iso3Code = "TWN", name = "Taiwan")
//        val region2 = Region(iso3Code = "CHN", name = "China")
//        val regionList = listOf(region1, region2)
//
//        localDataSource.saveRegions(regionList)
//
//        localDataSource.loadRegionCount().test {
//            val result = awaitItem()
//            Assert.assertEquals(2, result)
//
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun load_non_existent_region_is_empty() = runTest {
//        localDataSource.loadRegionsByIso3Code("XZX").test {
//            val result = awaitItem()
//            Assert.assertTrue(result.isEmpty())
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_three_regions_load_one_by_full_name_in_correct_case() = runTest {
//        val alabama = Region(iso3Code = "AAA", name = "Alabama")
//        val bahamas = Region(iso3Code = "BBB", name = "Bahamas")
//        val canada = Region(iso3Code = "CCC", name = "Canada")
//
//        localDataSource.saveRegions(listOf(alabama, bahamas, canada))
//
//        localDataSource.loadRegionsByName("Alabama").test {
//            val result = awaitItem()
//            Assert.assertEquals(1, result.size)
//            Assert.assertEquals("AAA", result[0].iso3Code)
//            Assert.assertEquals("Alabama", result[0].name)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_three_regions_retrieve_one_by_full_name_in_upper_case() = runTest {
//        val alabama = Region(iso3Code = "AAA", name = "Alabama")
//        val bahamas = Region(iso3Code = "BBB", name = "Bahamas")
//        val canada = Region(iso3Code = "CCC", name = "Canada")
//
//        localDataSource.saveRegions(listOf(alabama, bahamas, canada))
//
//        localDataSource.loadRegionsByName("CANADA").test {
//            val result = awaitItem()
//            Assert.assertEquals(1, result.size)
//            Assert.assertEquals("CCC", result[0].iso3Code)
//            Assert.assertEquals("Canada", result[0].name)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_three_regions_retrieve_one_by_partial_name_in_correct_case() = runTest {
//        val alabama = Region(iso3Code = "AAA", name = "Alabama")
//        val bahamas = Region(iso3Code = "BBB", name = "Bahamas")
//        val canada = Region(iso3Code = "CCC", name = "Canada")
//
//        localDataSource.saveRegions(listOf(alabama, bahamas, canada))
//
//        localDataSource.loadRegionsByName("Bah").test {
//            val result = awaitItem()
//            Assert.assertEquals(1, result.size)
//            Assert.assertEquals("BBB", result[0].iso3Code)
//            Assert.assertEquals("Bahamas", result[0].name)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_three_regions_retrieve_two_by_partial_name_in_lower_case() = runTest {
//        val alabama = Region(iso3Code = "AAA", name = "Alabama")
//        val bahamas = Region(iso3Code = "BBB", name = "Bahamas")
//        val canada = Region(iso3Code = "CCC", name = "Canada")
//
//        localDataSource.saveRegions(listOf(alabama, bahamas, canada))
//
//        localDataSource.loadRegionsByName("ba").test {
//            val result = awaitItem()
//            Assert.assertEquals(2, result.size)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_three_regions_retrieve_by_first_letters_of_words_fails() = runTest {
//        val alabama = Region(iso3Code = "AAA", name = "Alabama")
//        val bahamas = Region(iso3Code = "HTO", name = "Home Town")
//        val canada = Region(iso3Code = "CCC", name = "Canada")
//
//        localDataSource.saveRegions(listOf(alabama, bahamas, canada))
//
//        localDataSource.loadRegionsByName("HT").test {
//            val result = awaitItem()
//            Assert.assertEquals(0, result.size)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_regions_with_same_iso_code_and_retrieve_by_iso_code() = runTest {
//        val region1 = Region("ABC", "Alice")
//        val region2 = Region("ABC", "Bob")
//
//        localDataSource.saveRegions(listOf(region1, region2))
//
//        localDataSource.loadRegionsByIso3Code("ABC").test {
//            val result = awaitItem()
//            Assert.assertEquals(2, result.size)
//            Assert.assertTrue(
//                (result[0].name == "Alice" && result[1].name == "Bob") ||
//                        (result[0].name == "Bob" && result[1].name == "Alice")
//            )
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_stats_loaded_stats_are_same() = runTest {
//        val statsIn = ReportData(confirmed = 12L, deaths = 34L, recovered = 56L, active = 78L, fatalityRate = 0.9F)
//        localDataSource.saveReportData("AUS", statsIn)
//
//        localDataSource.loadReportDataByIso3Code("AUS").test {
//            val result = awaitItem()
//            Assert.assertNotNull(result)
//            Assert.assertFalse(result.isEmpty())
//            Assert.assertEquals(12L, result[0].confirmed)
//            Assert.assertEquals(34L, result[0].deaths)
//            Assert.assertEquals(56L, result[0].recovered)
//            Assert.assertEquals(78L, result[0].active)
//            Assert.assertEquals(0.9F, result[0].fatalityRate)
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun load_bad_iso3code_is_empty() = runTest {
//        localDataSource.loadReportDataByIso3Code("XXX").test {
//            val result = awaitItem()
//            Assert.assertTrue(result.isEmpty())
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_two_stats_same_iso3code() = runTest {
//        val stats1 = ReportData(confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L,
//            fatalityRate = 0.5F)
//        val stats2 = ReportData(confirmed = 1L, deaths = 2L, recovered = 3L, active = 4L,
//            fatalityRate = 0.6F)
//        localDataSource.saveReportData("ABC", stats1)
//        localDataSource.saveReportData("ABC", stats2)
//
//        localDataSource.loadReportDataByIso3Code("ABC").test {
//            val result = awaitItem()
//            Assert.assertEquals(2, result.size)
//            Assert.assertNotEquals(result[0], result[1])
//            awaitComplete()
//        }
//    }
//
//    @Test
//    fun save_two_stats_with_different_iso_codes_load_one_by_iso_code() = runTest {
//        val stats1 = ReportData(confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L,
//            fatalityRate = 0.5F)
//        val stats2 = ReportData(confirmed = 1L, deaths = 2L, recovered = 3L, active = 4L,
//            fatalityRate = 0.6F)
//        localDataSource.saveReportData("ABC", stats1)
//        localDataSource.saveReportData("DEF", stats2)
//
//        localDataSource.loadReportDataByIso3Code("ABC").test {
//            val result = awaitItem()
//            Assert.assertEquals(1, result.size)
//            Assert.assertEquals(10L, result[0].confirmed)
//            Assert.assertEquals(20L, result[0].deaths)
//            Assert.assertEquals(30L, result[0].recovered)
//
//            awaitComplete()
//        }
//    }
//}