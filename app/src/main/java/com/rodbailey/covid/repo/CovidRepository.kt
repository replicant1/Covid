package com.rodbailey.covid.repo

import android.content.Context
import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.db.RegionDao
import com.rodbailey.covid.db.RegionEntity
import com.rodbailey.covid.db.RegionStatsDao
import com.rodbailey.covid.db.RegionStatsEntity
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPIClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Accesses covid data from some source - perhaps network, perhaps local database
 * ... clients do not know.
 */
class CovidRepository(val appContext: Context) {
    companion object {
        private const val GLOBAL_ISO3_CODE = "___"
    }

    private val covidAPI = CovidAPIClient().getAPIClient()

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface CovidRepositoryEntryPoint {
        fun regionStatsDao() : RegionStatsDao
        fun regionDao() : RegionDao
    }

    /**
     * @return [RegionDao] as provided by Hilt
     */
    private fun getRegionDao() : RegionDao {
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            CovidRepositoryEntryPoint::class.java
        )
        return entryPoint.regionDao()
    }

    private fun getRegionStatsDao() : RegionStatsDao {
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            CovidRepositoryEntryPoint::class.java
        )
        return entryPoint.regionStatsDao()
    }

    /**
     * @param regionIso3Code ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getReport(regionIso3Code: String?): ReportData {
        if (regionIso3Code != null) {
            val dataSets = getRegionStatsDao().getRegionStats(regionIso3Code)
            if (dataSets.isEmpty()) {
                // ReportData is not in database, so get from network
                val apiData = covidAPI.getReport(regionIso3Code).data
                saveRegionStatsToDb(regionIso3Code, apiData)
                println("Data for $regionIso3Code has been retrieved from network")
                return apiData
            } else {
                // Take the first result only
                println("Data for $regionIso3Code has been retrieved from database")
                return toReportData(dataSets[0])
            }
        } else {
            val dbGlobal = getRegionStatsDao().getRegionStats(GLOBAL_ISO3_CODE)
            if (dbGlobal.isEmpty()) {
                // Global data is not in database, so get from network
                val globalData = covidAPI.getReport(null).data
                saveRegionStatsToDb(GLOBAL_ISO3_CODE, globalData)
                println("Data for GLOBAL has been retrieved from network")
                return globalData
            } else {
                // Get global data from database. Assert: there is only one
                println("Data for GLOBAL has been reterieved from database")
                return toReportData(dbGlobal[0])
            }
        }
    }

    private fun toReportData(dbStats : RegionStatsEntity) : ReportData {
        return ReportData(
            confirmed = dbStats.confirmed,
            deaths = dbStats.deaths,
            recovered = dbStats.recovered,
            active = dbStats.active,
            fatalityRate = dbStats.fatalityRate
        )
    }

    private suspend fun saveRegionStatsToDb(isoCode : String, stats: ReportData) {
        getRegionStatsDao().insert(
            RegionStatsEntity(
                iso3code = isoCode,
                confirmed = stats.confirmed,
                deaths = stats.deaths,
                recovered = stats.recovered,
                active = stats.active,
                fatalityRate = stats.fatalityRate
            )
        )
    }

    /**
     * @return All known regions in ascending order by name
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getRegions(): List<Region> {
        val numRegionsSaved = getRegionDao().getRegionCount()

        return if (numRegionsSaved == 0) {
            // Get regions from the network and store in the database. Return the network equiv.
            val allRegions: List<Region> = covidAPI.getRegions().regions
            saveRegionsToDb(allRegions)
            allRegions.sortedBy { it.name }
        } else {
            // Get regions from db and convert to network equiv.
            loadRegionsFromDb()
        }
    }

    /**
     * @param db This app's database where covid data is cached
     * @return All cached regions
     */
    private suspend fun loadRegionsFromDb(): List<Region> {
        val allRegionEntities = getRegionDao().getAllRegions()
        println("${allRegionEntities.size} regions loaded from db")
        val allRegions = mutableListOf<Region>()
        for (regionEntity in allRegionEntities) {
            allRegions.add(Region(iso3Code = regionEntity.iso3code, name = regionEntity.name))
        }
        return allRegions.sortedBy { it.name }
    }

    /**
     * @param db This apps database where covid data is cached
     * @param regions [Region] instances from network, ready to cache
     */
    private suspend fun saveRegionsToDb(regions: List<Region>) {
        val allRegionEntities = toRegionEntities(regions)
        getRegionDao().insert(allRegionEntities)
        val rcount = getRegionDao().getRegionCount()
        println("$rcount new regions saved to db")
    }

    /**
     * @param regions [Region] list as just retrieved from network
     * @return Equivalent [RegionEntity] list ready to save to db
     */
    private fun toRegionEntities(regions: List<Region>): List<RegionEntity> {
        val result = mutableListOf<RegionEntity>()
        for (region in regions) {
            result.add(RegionEntity(iso3code = region.iso3Code, name = region.name))
        }
        return result
    }
}