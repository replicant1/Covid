package com.rodbailey.covid.data.repo

/**
 * Represents one cached region stats row as seen by the presentation layer.
 *
 * @param iso3Code ISO3 country code for this entry.
 * @param ageMillis Age of this cache entry in milliseconds (currentTimeMillis - timestamp).
 */
data class CacheEntry(
    val iso3Code: String,
    val ageMillis: Long
)

/** Approximate SQLite storage occupied per stats row (used for total-bytes summary). */
const val BYTES_PER_STATS_ROW = 70
