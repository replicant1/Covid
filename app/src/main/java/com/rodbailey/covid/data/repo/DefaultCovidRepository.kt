package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.db.RegionDao
import com.rodbailey.covid.data.db.RegionStatsDao
import com.rodbailey.covid.data.db.toRegion
import com.rodbailey.covid.data.db.toRegionStats
import com.rodbailey.covid.data.db.toRegionStatsList
import com.rodbailey.covid.data.net.CovidAPI
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.toRegionEntityList
import com.rodbailey.covid.domain.toRegionStatsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class DefaultCovidRepository(
    private val regionDao: RegionDao,
    private val regionStatsDao: RegionStatsDao,
    private val covidApi: CovidAPI
) : CovidRepository {

    private val refreshMutex = Mutex()


    override fun getRegionStatsStream(code: RegionCode): Flow<List<RegionStats>> = flow {
        val dbStats = regionStatsDao.getRegionStats(code.chars)
        emit(
            if (dbStats.isEmpty()) {
                val report = covidApi.getReport(codeToApiQueryParam(code))
                val entity = report.data.toRegionStatsEntity(code.chars)
                regionStatsDao.insert(entity)
                listOf(entity.toRegionStats())
            } else {
                dbStats.toRegionStatsList()
            }
        )
    }

    private fun codeToApiQueryParam(code: RegionCode): String? {
        return if (code is GlobalCode) {
            null
        } else {
            code.chars
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRegionsStream(): Flow<List<Region>> {
        return regionDao.getAllRegionsStream()
            .map { regions ->
                regions.map { regionEntity ->
                    regionEntity.toRegion()
                }
            }.transformLatest { regions ->
                if (regions.isEmpty()) {
                    // The insertion of region data into the table by refreshRegions() will
                    // trigger a new emission containing the newly retrieved regions into the
                    // regionDao.getAllRegionsStream() above.
                    refreshRegions()
                }
                emit(regions)
            }
    }

    private suspend fun refreshRegions() {
        // Mutex serialises concurrent callers (e.g. two collectors of the cold regions flow).
        // "withLock" waits for the lock rather than skipping it.
        // Inside the lock we re-check the DB so the second caller skips the network request
        // if the first one already populated the table. ie. the second concurrent caller waits
        // for the first to finish, then finds the DB already populated and skips the
        // network call covidApi.getRegions()
        refreshMutex.withLock {
            if (regionDao.getRegionCount() > 0) return@withLock
            covidApi.getRegions().also {
                regionDao.insert(it.regions.toRegionEntityList())
            }
        }
    }
}