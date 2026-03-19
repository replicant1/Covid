package com.rodbailey.covid.domain

/**
 * Binary search utility functions for efficiently finding regions and other comparable data.
 */

/**
 * Performs a binary search on a sorted list to find elements matching a predicate.
 * Returns all indices where the predicate is true for values in the search range.
 *
 * **Precondition**: The list must be sorted by the comparable value.
 *
 * @param target The target value to search for
 * @param compareFn A function that compares an element with the target.
 *                  Returns: negative if element < target, 0 if equal, positive if element > target
 * @return The index of the target if found, or -1 if not found
 */
fun <T> List<T>.binarySearchIndex(target: String, compareFn: (T) -> Int): Int {
    var left = 0
    var right = size - 1

    while (left <= right) {
        val mid = left + (right - left) / 2
        val comparison = compareFn(this[mid])

        when {
            comparison == 0 -> return mid
            comparison < 0 -> left = mid + 1
            else -> right = mid - 1
        }
    }
    return -1
}

/**
 * Binary search that finds all regions starting with a given prefix.
 * The list must be sorted by region name.
 *
 * **Precondition**: The list must be sorted alphabetically by region name.
 *
 * @param prefix The prefix to search for (case-insensitive)
 * @return List of regions whose names start with the prefix
 */
fun List<Region>.binarySearchByPrefix(prefix: String): List<Region> {
    if (prefix.isEmpty()) return this
    if (isEmpty()) return emptyList()

    val prefixUpper = prefix.uppercase()

    // Binary search to find the first matching region
    var left = 0
    var right = size - 1
    var firstMatch = -1

    while (left <= right) {
        val mid = left + (right - left) / 2
        val midNameUpper = this[mid].name.uppercase()

        when {
            midNameUpper.startsWith(prefixUpper) -> {
                firstMatch = mid
                right = mid - 1  // Continue searching left for earlier matches
            }
            midNameUpper < prefixUpper -> left = mid + 1
            else -> right = mid - 1
        }
    }

    if (firstMatch == -1) return emptyList()

    // Collect all regions starting from firstMatch that match the prefix
    val results = mutableListOf<Region>()
    for (i in firstMatch until size) {
        if (this[i].name.uppercase().startsWith(prefixUpper)) {
            results.add(this[i])
        } else {
            break  // Since list is sorted, we can stop here
        }
    }

    return results
}

/**
 * Binary search to find a region by exact ISO code.
 * The list must be sorted by ISO code.
 *
 * **Precondition**: The list must be sorted alphabetically by ISO code.
 *
 * @param isoCode The 3-letter ISO code to search for
 * @return The region if found, null otherwise
 */
fun List<Region>.binarySearchByISO(isoCode: String): Region? {
    var left = 0
    var right = size - 1

    while (left <= right) {
        val mid = left + (right - left) / 2
        val comparison = this[mid].iso3Code.compareTo(isoCode)

        when {
            comparison == 0 -> return this[mid]
            comparison < 0 -> left = mid + 1
            else -> right = mid - 1
        }
    }
    return null
}

