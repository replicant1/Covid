package com.rodbailey.covid.repo

import com.rodbailey.covid.db.RegionDao
import com.rodbailey.covid.db.RegionStatsDao
import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData
import com.rodbailey.covid.dom.TransformUtils.regionEntityListToRegionList
import com.rodbailey.covid.dom.TransformUtils.regionListToRegionEntityList
import com.rodbailey.covid.dom.TransformUtils.regionStatsEntityToReportData
import com.rodbailey.covid.dom.TransformUtils.reportDataToRegionStatsEntity
import com.rodbailey.covid.net.CovidAPI
import timber.log.Timber


class CovidRepository(
    val regionDao: RegionDao,
    val regionStatsDao: RegionStatsDao,
    val covidAPI: CovidAPI
) : ICovidRepository {

    companion object {
        private const val GLOBAL_ISO3_CODE = "___"
    }

    override suspend fun getReport(regionIso3Code: String?): ReportData {
        if (regionIso3Code != null) {
            val dataSets = regionStatsDao.getRegionStats(regionIso3Code)
            return if (dataSets.isEmpty()) {
                // ReportData is not in database, so get from network
                val apiData = covidAPI.getReport(regionIso3Code).data
                saveRegionStatsToDb(regionIso3Code, apiData)
                Timber.i("Data for $regionIso3Code has been retrieved from network")
                apiData
            } else {
                // Take the first result only
                Timber.i("Data for $regionIso3Code has been retrieved from database")
                regionStatsEntityToReportData(dataSets[0])
            }
        } else {
            val dbGlobal = regionStatsDao.getRegionStats(GLOBAL_ISO3_CODE)
            return if (dbGlobal.isEmpty()) {
                // Global data is not in database, so get from network
                val globalData = covidAPI.getReport(null).data
                saveRegionStatsToDb(GLOBAL_ISO3_CODE, globalData)
                Timber.i("Data for GLOBAL has been retrieved from network")
                globalData
            } else {
                // Get global data from database. Assert: there is only one
                Timber.i("Data for GLOBAL has been retrieved from database")
                regionStatsEntityToReportData(dbGlobal[0])
            }
        }
    }

    override suspend fun getRegions(): List<Region> {
        val numRegionsSaved = regionDao.getRegionCount()

        return if (numRegionsSaved == 0) {
            // Get regions from the network and store in the database. Return the network equiv.
            val allRegions: List<Region> = covidAPI.getRegions().regions
            saveRegionsToDb(allRegions)
            allRegions.sortedBy { it.name }
        } else {
            // Get regions from db and convert to network equiv.
            loadAndSortRegionsFromDb()
        }
    }

    /**
     * @param db This app's database where covid data is cached
     * @return All cached regions
     */
    private suspend fun loadAndSortRegionsFromDb(): List<Region> {
        val allRegionEntities = regionDao.getAllRegions()
        Timber.i("${allRegionEntities.size} regions loaded from db")
        val allRegions = regionEntityListToRegionList(allRegionEntities)
        return allRegions.sortedBy { it.name }
    }

    /**
     * @param regions [Region] instances from network, ready to cache
     */
    private suspend fun saveRegionsToDb(regions: List<Region>) {
        regionDao.insert(regionListToRegionEntityList(regions))
    }

    private suspend fun saveRegionStatsToDb(isoCode: String, stats: ReportData) {
        regionStatsDao.insert(reportDataToRegionStatsEntity(isoCode, stats))
    }
}