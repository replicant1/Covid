package com.rodbailey.covid.data.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.data.net.CovidAPI
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DefaultRemoteDataSourceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeAPI: CovidAPI

    @Inject
    lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setup() {
        hiltRule.inject()

        println("**** Test: Injected fakeAPI = $fakeAPI")
        println("**** Test: Injected remoteDataSource = $remoteDataSource")
    }

    @Test
    fun load_regions_returns_all_fake_regions() = runTest{
        remoteDataSource.loadRegions().test {
            val result = awaitItem()
            Assert.assertEquals(FakeRegions.REGIONS.size, result.regions.size)
            awaitComplete()
        }
    }

    @Test
    fun load_report_data_with_good_iso_code() = runTest {
        remoteDataSource.loadReportDataByIso3Code("CHN").test {
            val result = awaitItem()
            Assert.assertEquals(10L, result.data.confirmed)
            Assert.assertEquals(20L, result.data.deaths)
            Assert.assertEquals(30L, result.data.recovered)
            Assert.assertEquals(40L, result.data.active)
            Assert.assertEquals(0.5F, result.data.fatalityRate)
            awaitComplete()
        }
    }

    @Test
    fun load_report_data_with_bad_iso_code_throws_exception() = runTest {
        var exceptionThrown = false
        try {
            remoteDataSource.loadReportDataByIso3Code("XXX").first()
        }
        catch (th : Throwable) {
            exceptionThrown = true
        }
        Assert.assertTrue(exceptionThrown)
    }

    @Test
    fun load_report_data_with_null_iso_code_returns_global_report_data() = runTest {
        remoteDataSource.loadReportDataByIso3Code(null).test {
            val result = awaitItem()
            Assert.assertEquals(FakeRegions.GLOBAL_REGION_STATS , result.data)
            awaitComplete()
        }
    }
}