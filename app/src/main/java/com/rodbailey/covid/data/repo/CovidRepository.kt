package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.TransformUtils.regionEntityListToRegionList
import com.rodbailey.covid.domain.TransformUtils.regionListToRegionEntityList
import com.rodbailey.covid.domain.TransformUtils.regionStatsEntityToReportData
import com.rodbailey.covid.domain.TransformUtils.reportDataToRegionStatsEntity
import com.rodbailey.covid.data.net.CovidAPI
import timber.log.Timber


class CovidRepository(
    val regionDao: RegionDao,
    val regionStatsDao: RegionStatsDao,
    val covidAPI: CovidAPI
) : ICovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    /**
     * @see [ICovidRepository.getReport]
     */
    override suspend fun getReport(regionIso3Code: String?): ReportData {
        val regionStats = regionStatsDao.getRegionStats(regionIso3Code ?: GLOBAL_ISO3_CODE)
        return if (regionStats.isEmpty()) {
            // ReportData is not in the database, so get from network then cache in database
            val apiData = covidAPI.getReport(regionIso3Code).data
            saveRegionStatsToDb(regionIso3Code ?: GLOBAL_ISO3_CODE, apiData)
            apiData
        } else {
            // ReportData already cached in database
            regionStatsEntityToReportData(regionStats[0])
        }
    }

    /**
     * @see [ICovidRepository.getRegions]
     */
    override suspend fun getRegions(): List<Region> {
        return if (regionDao.getRegionCount() == 0) {
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
     * Load all cached regions from the db and sort ascending by name.
     *
     * @param db This app's database where covid data is cached
     * @return All cached regions sorted ascending by name
     */
    private suspend fun loadAndSortRegionsFromDb(): List<Region> {
        val allRegionEntities = regionDao.getAllRegions()
        Timber.i("${allRegionEntities.size} regions loaded from db")
        val allRegions = regionEntityListToRegionList(allRegionEntities)
        return allRegions.sortedBy { it.name }
    }

    /**
     * Save the given regions to the db.
     *
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