package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.net.CovidAPIHelper
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.RegionList
import com.rodbailey.covid.domain.Report
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.TransformUtils.regionEntityListToRegionList
import com.rodbailey.covid.domain.TransformUtils.regionListToRegionEntityList
import com.rodbailey.covid.domain.TransformUtils.regionStatsEntityToReportData
import com.rodbailey.covid.domain.TransformUtils.reportDataToRegionStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Accesses covid data from some source - perhaps network, perhaps local database
 * ... clients do not know. Once retrieved from network, data is cached in local database.
 */
class DefaultCovidRepository(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidAPIHelper: CovidAPIHelper
) : CovidRepository {

    companion object {
        const val GLOBAL_ISO3_CODE = "___"
    }

    /**
     * @param isoCode ISO-3 alpha code for region, or null for "Global"
     * @return Covid stats for the region with the given ISO-3 code
     * @throws Exception if the slightest thing goes wrong
     */
    override suspend fun getReport(isoCode: String?): Flow<ReportData> {
        val safeIsoCode = isoCode ?: GLOBAL_ISO3_CODE
        val dbStatsCount =
            regionStatsDao.getRegionStatsCount(safeIsoCode)//.first() // or .single()?

        Timber.i("Into getReport() for iso $isoCode. Num matching records in db = $dbStatsCount")

        return if (dbStatsCount == 0) {
            println("Saving stats for iso code $safeIsoCode to db")
            covidAPIHelper.getReport(isoCode).map { apiReport: Report ->
                saveRegionStatsToDb(safeIsoCode, apiReport.data)
                apiReport.data
            }
        } else {
            flow {
                val dbStats = regionStatsDao.getRegionStats(safeIsoCode).first()
                val uiStats = regionStatsEntityToReportData(dbStats)
                Timber.d("Returning stats for $safeIsoCode from database")
                emit(uiStats)
            }
        }
    }

    /**
     * @return All known regions in ascending order by name
     * @throws Exception if the slightest thing goes wrong
     */
    override suspend fun getRegions(): Flow<List<Region>> {
        val dbRegionCount = regionDao.getRegionCount()
        return if (dbRegionCount == 0) {
            loadRegionsFromAPI()
        } else {
            flow { emit(loadRegionsFromDb()) }
        }
    }

    override suspend fun getRegionsByName(searchText: String): Flow<List<Region>> {
        return flow {
            emit(regionEntityListToRegionList(regionDao.getRegionsByName(searchText).sortedBy {it.name}))
        }
    }

    private suspend fun loadRegionsFromDb(): List<Region> {
        return regionEntityListToRegionList(regionDao.getAllRegions().sortedBy { it.name })
    }

    private suspend fun loadRegionsFromAPI(): Flow<List<Region>> {
        return covidAPIHelper.getRegions().map { unsortedRegions: RegionList ->
            saveRegionsToDb(unsortedRegions.regions)
            unsortedRegions.regions.sortedBy { region: Region -> region.name }
        }
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