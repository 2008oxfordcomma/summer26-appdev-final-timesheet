package com.example.timesheetapp.data.model

data class AddressResult(
    // We can't change this names since the API says these are the variable names it's expecting
    val display_name: String,
    val lat: String,
    val lon: String
)
