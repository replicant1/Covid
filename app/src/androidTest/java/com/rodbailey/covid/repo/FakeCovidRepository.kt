package com.rodbailey.covid.repo

import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData

class FakeCovidRepository() : ICovidRepository {

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
        const val GLOBAL_DATA_SET_TITLE = "Global"
    }

    /**
     * If tests set this to true, the next call to [getRegions] or [getReport] will throw
     * an exception.
     */
    var nextOpThrowsException = false

    /**
     * @see [ICovidRepository.getRegions]
     */
    override suspend fun getRegions(): List<Region> {
        if (nextOpThrowsException) {
            throw RuntimeException()
        }
        return REGIONS.sortedBy { it.name }
    }

    /**
     * @see [ICovidRepository.getReport]
     */
    override suspend fun getReport(regionIso3Code: String?): ReportData {
        if (nextOpThrowsException) {
            throw RuntimeException()
        }
        return when (regionIso3Code) {
            null -> GLOBAL_REPORT_DATA
            "AUS" -> AUS_REPORT_DATA
            else -> DEFAULT_REPORT_DATA
        }
    }

}