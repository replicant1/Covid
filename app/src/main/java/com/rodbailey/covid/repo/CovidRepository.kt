package com.rodbailey.covid.repo

import android.content.Context
import androidx.room.Room
import com.rodbailey.covid.db.AppDatabase
import com.rodbailey.covid.db.RegionEntity
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.net.CovidAPIClient

/**
 * Accesses covid data from some source - perhaps network, perhaps local database
 * ... clients do not know. Only network is currently supported.
 */
class CovidRepository(private val appContext: Context) {
    private val covidAPI = CovidAPIClient().getAPIClient()

    /**
     * @param ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getReport(regionIso3Code: String?): ReportData {
        return covidAPI.getReport(regionIso3Code).data
    }

    /**
     * @return All known regions in ascending order by name
     * @throws Exception if the slightest thing goes wrong
     */
    suspend fun getRegions(): List<Region> {
        println("*** ======== about to build ROOM database =============")

        val appDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, "covid").build()
        println("*** ==== databasse bulding finished =====")

        val numRegionsSaved = appDatabase.regionDao().getRegionCount()
        println("*** ==== numRegionsSaved = $numRegionsSaved")

        return if (numRegionsSaved == 0) {
            // Get regions from the network and store in the database. Return the network equiv.
            val allRegions: List<Region> = covidAPI.getRegions().regions
            saveRegionsToDb(appDatabase, allRegions)
            allRegions.sortedBy { it.name }
        } else {
            // Get regions from db and convert to network equiv.
            loadRegionsFromDb(appDatabase)
        }
    }

    private suspend fun loadRegionsFromDb(db: AppDatabase): List<Region> {
        val allRegionEntities = db.regionDao().getAllRegions()
        val allRegions = mutableSetOf<Region>()
        for (regionEntity in allRegionEntities) {
            allRegions.add(Region(iso3Code = regionEntity.iso3code, name = regionEntity.name))
        }
        return allRegions.sortedBy { it.name }
    }

    private suspend fun saveRegionsToDb(db: AppDatabase, regions: List<Region>) {
        println("*** ==== passed ${regions.size} regions to converter function")

        val allRegionEntities = toRegionEntities(regions)
        println("*** === converted entities = $allRegionEntities")

        println("*** === about to insert entities")
        db.regionDao().insert(allRegionEntities)
        println("*** === back from inserting entities")

        val rcount = db.regionDao().getRegionCount()
        println("*** === new region count = $rcount")
    }

    private fun toRegionEntities(regions: List<Region>): List<RegionEntity> {
        val result = mutableListOf<RegionEntity>()
        val usedCodes = mutableSetOf<String>()
        for (region in regions) {
            if (usedCodes.contains(region.iso3Code)) {
                println("*** === iso3code duplication of \"${region.iso3Code}\"")
            } else {
                usedCodes.add(region.iso3Code)
                result.add(RegionEntity(iso3code = region.iso3Code, name = region.name))
            }
        }
        return result
    }
}