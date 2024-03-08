package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.TransformUtils.regionEntityListToRegionList
import com.rodbailey.covid.domain.TransformUtils.regionListToRegionEntityList
import com.rodbailey.covid.domain.TransformUtils.regionStatsEntityToReportData
import com.rodbailey.covid.domain.TransformUtils.reportDataToRegionStatsEntity
import timber.log.Timber

/**
 * Accesses covid data from some source - perhaps network, perhaps local database
 * ... clients do not know. Only network is currently supported.
 */
class CovidRepository(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidAPI: CovidAPI
) : ICovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    /**
     * @param isoCode ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if the slightest thing goes wrong
     */
    override suspend fun getReport(isoCode: String?): ReportData {
        println("getReport for iso = $isoCode begins")
        val safeIsoCode = isoCode ?: GLOBAL_ISO3_CODE
        val dbStatsCount = regionStatsDao.getRegionStatsCount(safeIsoCode)

        println("Num matching records in db = $dbStatsCount")

        return if (dbStatsCount == 0) {
            println("Saving stats for iso code $safeIsoCode to db")
            val apiReport = covidAPI.getReport(isoCode)
            saveRegionStatsToDb(safeIsoCode, apiReport.data)
            apiReport.data
        } else {
            val dbStats = regionStatsDao.getRegionStats(safeIsoCode)
            val uiStats = regionStatsEntityToReportData(dbStats[0])
            println("Returning stats for $safeIsoCode from database")
            uiStats
        }
    }

    /**
     * @return All known regions in ascending order by name
     * @throws Exception if the slightest thing goes wrong
     */
    override suspend fun getRegions(): List<Region> {
        Timber.d("**** Into real repository.getRegions. About to check if regions are in db")
        val numRegionsSaved = regionDao.getRegionCount()

        return if (numRegionsSaved == 0) {
            println("Regions NOT in db, so loading from network then saving to db")
            // Get regions from the network and store in the database. Return the network equiv.
            loadRegionsFromAPI()
        } else {
            println("Regions ARE in db, returning from db")
            // Get regions from db and convert to network equiv.
            loadRegionsFromDb()
        }
    }

    override suspend fun getRegionsByName(searchText: String): List<Region> {
        val entities = regionDao.getRegionsByName(searchText)
        return regionEntityListToRegionList(entities)
    }


    private suspend fun loadRegionsFromDb(): List<Region> {
        val unsortedRegions = regionDao.getAllRegions()
        return regionEntityListToRegionList(unsortedRegions).sortedBy { it.name }
    }

    private suspend fun loadRegionsFromAPI(): List<Region> {
        val unsortedRegions = covidAPI.getRegions()
        saveRegionsToDb(unsortedRegions.regions)
        return unsortedRegions.regions.sortedBy { it.name }
    }

    /**
     * @param db This apps database where covid data is cached
     * @param regions [Region] instances from network, ready to cache
     */
    private suspend fun saveRegionsToDb(regions: List<Region>) {
        regionDao.insert(regionListToRegionEntityList(regions))
    }

    /**
     * Save the given covid stats for a region to the db.
     *
     * @param isoCode 3 letter code for the region
     * @param stats covid statistics for the region
     */
    private suspend fun saveRegionStatsToDb(isoCode: String, stats: ReportData) {
        regionStatsDao.insert(reportDataToRegionStatsEntity(isoCode, stats))
    }

}