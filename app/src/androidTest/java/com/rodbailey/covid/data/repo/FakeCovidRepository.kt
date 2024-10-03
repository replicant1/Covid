package com.rodbailey.covid.data.repo

import com.rodbailey.covid.data.FakeRegions
import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData
import com.rodbailey.covid.domain.toRegionStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeCovidRepository() : CovidRepository {

    companion object {
        val REGIONS = listOf(
            Region("CHN", "China"),
            Region("TWN", "Taipei and environs"),
            Region("USA", "US"),
            Region("JPN", "Japan"),
            Region("THA", "Thailand"),
            Region("KOR", "Korea, South"),
            Region("SGP", "Singapore"),
            Region("PHL", "Philippines"),
            Region("MYS", "Malaysia"),
            Region("VNM", "Vietnam"),
            Region("AUS", "Australia"),
            Region("MEX", "Mexico"),
            Region("BRA", "Brazil"),
            Region("COL", "Colombia"),
            Region("FRA", "France"),
            Region("NPL", "Nepal"),
            Region("CAN", "Canada"),
            Region("KHM", "Cambodia"),
            Region("LKA", "Sri Lanka"),
            Region("CIV", "Cote d'Ivoire"),
            Region("DEU", "Germany"),
            Region("FIN", "Finland"),
            Region("ARE", "United Arab Emirates"),
            Region("IND", "India"),
            Region("ITA", "Italy"),
            Region("GBR", "United Kingdom"),
            Region("RUS", "Russia"),
            Region("SWE", "Sweden"),
            Region("ESP", "Spain"),
            Region("BEL", "Belgium"),
            Region("Others", "Others"),
            Region("EGY", "Egypt"),
            Region("IRN", "Iran"),
            Region("ISR", "Israel"),
            Region("LBN", "Lebanon"),
            Region("IRQ", "Iraq"),
            Region("OMN", "Oman"),
            Region("AFG", "Afghanistan"),
            Region("BHR", "Bahrain"),
            Region("KWT", "Kuwait"),
            Region("AUT", "Austria"),
            Region("DZA", "Algeria"),
            Region("HRV", "Croatia"),
            Region("CHE", "Switzerland"),
            Region("PAK", "Pakistan"),
            Region("GEO", "Georgia"),
            Region("GRC", "Greece"),
            Region("MKD", "North Macedonia"),
            Region("NOR", "Norway"),
            Region("ROU", "Romania"),
            Region("DNK", "Denmark"),
            Region("EST", "Estonia"),
            Region("NLD", "Netherlands"),
            Region("SMR", "San Marino")
        )
        val GLOBAL_REPORT_DATA = ReportData(
            confirmed = 10L, deaths = 20L, recovered = 30L, active = 40L, fatalityRate = 0.5F
        )
        val DEFAULT_REPORT_DATA = ReportData(
            confirmed = 5L, deaths = 6L, recovered = 7L, active = 8L, fatalityRate = 0.4F
        )
        val AUS_REPORT_DATA = ReportData(
            confirmed = 1234L,
            deaths = 2345L,
            recovered = 3456L,
            active = 4000L,
            fatalityRate = 0.6F
        )
    }

    /**
     * If tests set this to true, the next call to [getRegions] or [getReport] will throw
     * an exception.
     */
    private var allMethodsThrowException = false

    /**
     * @see [CovidRepository.getRegions]
     */
    override fun getRegionsStream(): Flow<List<Region>> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }
        return flow {
            emit(FakeRegions.REGIONS.keys.sortedBy { region -> region.name })
        }
    }

    override suspend fun getRegionStatsStream(iso3code: RegionCode): Flow<List<RegionStats>> {
        if (allMethodsThrowException) {
            throw RuntimeException()
        }

        val data = when {
            iso3code.chars == "AUS" -> AUS_REPORT_DATA.toRegionStats(iso3code.chars)
            iso3code is GlobalCode -> GLOBAL_REPORT_DATA.toRegionStats(GlobalCode().chars)
            else -> DEFAULT_REPORT_DATA.toRegionStats(iso3code.chars)
        }

        return flowOf(listOf(data))
    }

    fun setAllMethodsThrowException(value: Boolean) {
        allMethodsThrowException = value
    }

}