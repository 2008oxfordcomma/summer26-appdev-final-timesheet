package com.example.timesheetapp.data.model

/* references 
* https://nominatim.org/release-docs/develop/library/Getting-Started/
* https://nominatim.org/release-docs/develop/api/Overview/
*/

data class AddressResult(
    // We can't change this names since the API says these are the variable names it's expecting
    val display_name: String,
    val lat: String,
    val lon: String
)
