package com.rodbailey.covid.domain

import com.google.gson.annotations.SerializedName

/**
 * Geographical region that has a 3 letter ISO code and a name.
 */
data class Region(
    @SerializedName("iso")
    val iso3Code:String,

    @SerializedName("name")
    val name:String) {

    /**
     * @return true if the given query string matches against the [name]. If the [name]
     * of the Region contains multiple words, a query that contains the first letters
     * of those words (or some substring thereof) will constitute a match.
     */
    fun matchesSearchQuery(query : String) : Boolean {
        var result = false

        if (name.contains(query, ignoreCase = true)) {
            result = true
        }
        else if (name.contains(" ")) {
            var firstLetters  = ""
            name.split(" ").forEach {
                if (it.isNotEmpty()) {
                    firstLetters += it[0]
                }
            }
            result = firstLetters.contains(query, ignoreCase = true)
        }

        return result
    }
}