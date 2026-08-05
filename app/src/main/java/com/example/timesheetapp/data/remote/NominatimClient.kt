package com.example.timesheetapp.data.remote

// pulled straight from the API page
/* references 
* https://nominatim.org/release-docs/develop/library/Getting-Started/
* https://nominatim.org/release-docs/develop/api/Overview/
*/

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NominatimClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Nominatim asks that we include an email address in case they need to get ahold of us
                .header("User-Agent", "TimeSheetApp/1.0 (2008oxfordcomma@gmail.com")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: NominatimAPI = retrofit.create(NominatimAPI::class.java)
}
