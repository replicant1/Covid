package com.rodbailey.covid.repo

import com.rodbailey.covid.dom.Region
import com.rodbailey.covid.dom.ReportData

class FakeCovidRepository : ICovidRepository {

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
    }

    /**
     * @see [ICovidRepository.getRegions]
     */
    override suspend fun getRegions(): List<Region> {
        return REGIONS.sortedBy { it.name }
    }

    /**
     * @see [ICovidRepository.getReport]
     */
    override suspend fun getReport(regionIso3Code: String?): ReportData {
        return ReportData(
            confirmed = 10L,
            deaths = 20L,
            recovered = 30L,
            active = 40L,
            fatalityRate = 0.5F
        )
    }

}