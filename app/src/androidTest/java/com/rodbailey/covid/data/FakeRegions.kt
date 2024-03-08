package com.rodbailey.covid.data

import com.rodbailey.covid.domain.Region
import com.rodbailey.covid.domain.ReportData

/**
 * Source of all data returned by [FakeCovidRepository] and [FakeCovidAPI]
 */
object FakeRegions {

    val GLOBAL_REGION = Region("___", "Global")
    val GLOBAL_REGION_STATS = ReportData(
        confirmed = 88L,
        deaths = 66L,
        recovered = 55L,
        active = 54L,
        fatalityRate = 9.5F
    )

    val REGIONS = mapOf(
        Region("CHN", "China") to ReportData(
            confirmed = 10L,
            deaths = 20L,
            recovered = 30L,
            active = 40L,
            fatalityRate = 0.5F
        ),
        Region("TWN", "Taipei and environs") to ReportData(
            confirmed = 5L,
            deaths = 7L,
            recovered = 9L,
            active = 11L,
            fatalityRate = 1.3F
        ),
        Region("USA", "US") to ReportData(
            confirmed = 30L,
            deaths = 50L,
            recovered = 70L,
            active = 90L,
            fatalityRate = 3.5F
        ),
        Region("JPN", "Japan") to ReportData(
            confirmed = 60L,
            deaths = 70L,
            recovered = 380L,
            active = 430L,
            fatalityRate = 0.55F
        ),
        Region("THA", "Thailand") to ReportData(
            confirmed = 305L,
            deaths = 405L,
            recovered = 505L,
            active = 605L,
            fatalityRate = 0.75F
        ),
        Region("KOR", "Korea, South") to ReportData(
            confirmed = 16L,
            deaths = 26L,
            recovered = 36L,
            active = 46L,
            fatalityRate = 0.56F
        ),
        Region("SGP", "Singapore") to ReportData(
            confirmed = 10L,
            deaths = 20L,
            recovered = 30L,
            active = 40L,
            fatalityRate = 0.5F
        ),
        Region("PHL", "Philippines") to ReportData(
            confirmed = 1000L,
            deaths = 2000L,
            recovered = 3000L,
            active = 4000L,
            fatalityRate = 50.5F
        ),
        Region("MYS", "Malaysia") to ReportData(
            confirmed = 111L,
            deaths = 222L,
            recovered = 333L,
            active = 444L,
            fatalityRate = 5.55F
        ),
        Region("VNM", "Vietnam") to ReportData(
            confirmed = 108L,
            deaths = 208L,
            recovered = 308L,
            active = 408L,
            fatalityRate = 8.58F
        ),
        Region("AUS", "Australia") to ReportData(
            confirmed = 10000L,
            deaths = 20000L,
            recovered = 30000L,
            active = 40000L,
            fatalityRate = 18.55F
        ),
        Region("MEX", "Mexico") to ReportData(
            confirmed = 810L,
            deaths = 820L,
            recovered = 830L,
            active = 840L,
            fatalityRate = 80.5F
        ),
        Region("BRA", "Brazil") to ReportData(
            confirmed = 109L,
            deaths = 209L,
            recovered = 309L,
            active = 409L,
            fatalityRate = 9.5F
        ),
        Region("COL", "Colombia") to ReportData(
            confirmed = 11L,
            deaths = 120L,
            recovered = 130L,
            active = 140L,
            fatalityRate = 10.5F
        ),
        Region("FRA", "France") to ReportData(
            confirmed = 1077L,
            deaths = 2077L,
            recovered = 30777L,
            active = 4077L,
            fatalityRate = 7.57F
        ),
        Region("NPL", "Nepal") to ReportData(
            confirmed = 1033L,
            deaths = 2033L,
            recovered = 3033L,
            active = 4033L,
            fatalityRate = 33.5F
        ),
        Region("CAN", "Canada") to ReportData(
            confirmed = 4000L,
            deaths = 400L,
            recovered = 40L,
            active = 44L,
            fatalityRate = 4.5F
        ),
        Region("KHM", "Cambodia") to ReportData(
            confirmed = 510L,
            deaths = 520L,
            recovered = 530L,
            active = 540L,
            fatalityRate = 5.5F
        ),
        Region("LKA", "Sri Lanka") to ReportData(
            confirmed = 710L,
            deaths = 720L,
            recovered = 730L,
            active = 740L,
            fatalityRate = 7.5F
        ),
        Region("CIV", "Cote d'Ivoire") to ReportData(
            confirmed = 222L,
            deaths = 333L,
            recovered = 444L,
            active = 555L,
            fatalityRate = 6.6F
        ),
        Region("DEU", "Germany") to ReportData(
            confirmed = 7010L,
            deaths = 7020L,
            recovered = 7030L,
            active = 7040L,
            fatalityRate = 7.57F
        ),
        Region("FIN", "Finland") to ReportData(
            confirmed = 123L,
            deaths = 234L,
            recovered = 345L,
            active = 456L,
            fatalityRate = 5.6F
        ),
        Region("ARE", "United Arab Emirates") to ReportData(
            confirmed = 5L,
            deaths = 25L,
            recovered = 355L,
            active = 4555L,
            fatalityRate = 55.5F
        ),
        Region("IND", "India") to ReportData(
            confirmed = 1L,
            deaths = 2L,
            recovered = 3L,
            active = 4L,
            fatalityRate = 0.5F
        ),
        Region("ITA", "Italy") to ReportData(
            confirmed = 60L,
            deaths = 70L,
            recovered = 80L,
            active = 90L,
            fatalityRate = 10.5F
        ),
        Region("GBR", "United Kingdom") to ReportData(
            confirmed = 4L,
            deaths = 16L,
            recovered = 64L,
            active = 128L,
            fatalityRate = 4.5F
        ),
        Region("RUS", "Russia") to ReportData(
            confirmed = 106L,
            deaths = 720L,
            recovered = 308L,
            active = 490L,
            fatalityRate = 1.5F
        ),
        Region("SWE", "Sweden") to ReportData(
            confirmed = 1011L,
            deaths = 2220L,
            recovered = 3330L,
            active = 4044L,
            fatalityRate = 56.5F
        ),
        Region("ESP", "Spain") to ReportData(
            confirmed = 1L,
            deaths = 10L,
            recovered = 100L,
            active = 10000L,
            fatalityRate = 1.51F
        ),
        Region("BEL", "Belgium") to ReportData(
            confirmed = 303L,
            deaths = 404L,
            recovered = 505L,
            active = 606L,
            fatalityRate = 0.75F
        ),
        Region("Others", "Others") to ReportData(
            confirmed = 810L,
            deaths = 820L,
            recovered = 380L,
            active = 480L,
            fatalityRate = 0.85F
        ),
        Region("EGY", "Egypt") to ReportData(
            confirmed = 109L,
            deaths = 920L,
            recovered = 390L,
            active = 409L,
            fatalityRate = 0.95F
        ),
        Region("IRN", "Iran") to ReportData(
            confirmed = 10001L,
            deaths = 20002L,
            recovered = 30003L,
            active = 40004L,
            fatalityRate = 0.55F
        ),
        Region("ISR", "Israel") to ReportData(
            confirmed = 10002L,
            deaths = 20003L,
            recovered = 30004L,
            active = 40005L,
            fatalityRate = 0.56F
        ),
        Region("LBN", "Lebanon") to ReportData(
            confirmed = 110L,
            deaths = 2110L,
            recovered = 1130L,
            active = 4101L,
            fatalityRate = 1.5F
        ),
        Region("IRQ", "Iraq") to ReportData(
            confirmed = 5L,
            deaths = 25L,
            recovered = 580L,
            active = 430L,
            fatalityRate = 3.5F
        ),
        Region("OMN", "Oman") to ReportData(
            confirmed = 190L,
            deaths = 290L,
            recovered = 390L,
            active = 490L,
            fatalityRate = 0.98F
        ),
        Region("AFG", "Afghanistan") to ReportData(
            confirmed = 910L,
            deaths = 920L,
            recovered = 930L,
            active = 940L,
            fatalityRate = 0.95F
        ),
        Region("BHR", "Bahrain") to ReportData(
            confirmed = 1860L,
            deaths = 2860L,
            recovered = 3860L,
            active = 4860L,
            fatalityRate = 08.5F
        ),
        Region("KWT", "Kuwait") to ReportData(
            confirmed = 10L,
            deaths = 13L,
            recovered = 33L,
            active = 46L,
            fatalityRate = 5.5F
        ),
        Region("AUT", "Austria") to ReportData(
            confirmed = 1870L,
            deaths = 2870L,
            recovered = 3870L,
            active = 4870L,
            fatalityRate = 8.57F
        ),
        Region("DZA", "Algeria") to ReportData(
            confirmed = 10L,
            deaths = 110L,
            recovered = 2230L,
            active = 330L,
            fatalityRate = 4.5F
        ),
        Region("HRV", "Croatia") to ReportData(
            confirmed = 19L,
            deaths = 29L,
            recovered = 39L,
            active = 49L,
            fatalityRate = 0.59F
        ),
        Region("CHE", "Switzerland") to ReportData(
            confirmed = 1055L,
            deaths = 2550L,
            recovered = 3055L,
            active = 4550L,
            fatalityRate = 5.5F
        ),
        Region("PAK", "Pakistan") to ReportData(
            confirmed = 1009L,
            deaths = 2010L,
            recovered = 3020L,
            active = 4030L,
            fatalityRate = 7.5F
        ),
        Region("GEO", "Georgia") to ReportData(
            confirmed = 13L,
            deaths = 2033L,
            recovered = 3330L,
            active = 4034L,
            fatalityRate = 2.5F
        ),
        Region("GRC", "Greece") to ReportData(
            confirmed = 10L,
            deaths = 200L,
            recovered = 300L,
            active = 400L,
            fatalityRate = 8.5F
        ),
        Region("MKD", "North Macedonia") to ReportData(
            confirmed = 505L,
            deaths = 606L,
            recovered = 707L,
            active = 808L,
            fatalityRate = 9.5F
        ),
        Region("NOR", "Norway") to ReportData(
            confirmed = 103L,
            deaths = 203L,
            recovered = 303L,
            active = 403L,
            fatalityRate = 0.35F
        ),
        Region("ROU", "Romania") to ReportData(
            confirmed = 9910L,
            deaths = 9920L,
            recovered = 9930L,
            active = 9940L,
            fatalityRate = 99.5F
        ),
        Region("DNK", "Denmark") to ReportData(
            confirmed = 102L,
            deaths = 203L,
            recovered = 304L,
            active = 405L,
            fatalityRate = 0.56F
        ),
        Region("EST", "Estonia") to ReportData(
            confirmed = 110L,
            deaths = 210L,
            recovered = 310L,
            active = 410L,
            fatalityRate = 1.5F
        ),
        Region("NLD", "Netherlands") to ReportData(
            confirmed = 1910L,
            deaths = 2920L,
            recovered = 3930L,
            active = 4940L,
            fatalityRate = 4.9F
        ),
        Region("SMR", "San Marino") to ReportData(
            confirmed = 4L,
            deaths = 3L,
            recovered = 2L,
            active = 1L,
            fatalityRate = 17.9F
        )
    )

    val FIRST_REGION_BY_NAME = REGIONS.keys.sortedBy { it.name }.first()
    val LAST_REGION_BY_NAME = REGIONS.keys.sortedBy { it.name }.last()
    val NUM_REGIONS = REGIONS.size
}