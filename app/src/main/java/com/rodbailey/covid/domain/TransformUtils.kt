package com.rodbailey.covid.domain

import com.rodbailey.covid.data.db.RegionEntity
import com.rodbailey.covid.data.db.RegionStatsEntity

object TransformUtils {

    fun regionStatsEntityToReportData(regionStatsEntity: RegionStatsEntity): ReportData {
        return ReportData(
            confirmed = regionStatsEntity.confirmed,
            deaths = regionStatsEntity.deaths,
            recovered = regionStatsEntity.recovered,
            active = regionStatsEntity.active,
            fatalityRate = regionStatsEntity.fatalityRate
        )
    }

    fun reportDataToRegionStatsEntity(isoCode: String, reportData: ReportData): RegionStatsEntity {
        return RegionStatsEntity(
            iso3code = isoCode,
            confirmed = reportData.confirmed,
            deaths = reportData.deaths,
            recovered = reportData.recovered,
            active = reportData.active,
            fatalityRate = reportData.fatalityRate
        )
    }

    fun regionToRegionEntity(region: Region): RegionEntity {
        return RegionEntity(iso3code = region.iso3Code, name = region.name)
    }

    fun regionListToRegionEntityList(regionList: List<Region>): List<RegionEntity> {
        return regionList.map { regionToRegionEntity(it) }
    }

    fun regionEntityToRegion(regionEntity: RegionEntity): Region {
        return Region(regionEntity.iso3code, name = regionEntity.name)
    }

    fun regionEntityListToRegionList(regionEntityList : List<RegionEntity>) : List<Region> {
        return regionEntityList.map { regionEntityToRegion(it) }
    }

    fun regionStatsEntityListToReportDataList(regionStatsEntityList: List<RegionStatsEntity>): List<ReportData> {
        return regionStatsEntityList.map { regionStatsEntityToReportData(it ) }
    }
}