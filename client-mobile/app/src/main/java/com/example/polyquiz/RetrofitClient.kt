package com.example.polyquiz

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// REFERENCE: https://medium.com/@desiappdev24/fetching-data-using-retrofit-in-jetpack-compose-a-complete-guide-97f4c2101cb7
// https://www.baeldung.com/retrofit
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response details
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: CommunicationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        retrofit.create(CommunicationService::class.java)
    }
}
