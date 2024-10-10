package com.rodbailey.covid.data.repo


/**
 * @property the 3 character iso code for a region
 */
open class RegionCode(val chars: String)

/**
 * Code for all regions
 */
class GlobalCode : RegionCode("___")
