package com.rodbailey.covid.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class CovidAPIClient {
    fun getAPIClient(): CovidAPI {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl("https://covid-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(CovidAPI::class.java)
    }
}