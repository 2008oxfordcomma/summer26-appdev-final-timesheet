package com.example.timesheetapp.data.remote


// pulled straight from the API page
/* references 
* https://nominatim.org/release-docs/develop/library/Getting-Started/
* https://nominatim.org/release-docs/develop/api/Overview/
*/

import com.example.timesheetapp.data.model.AddressResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimAPI {
    @GET("search")
    suspend fun searchAddress(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5
    ): List<AddressResult>
}
