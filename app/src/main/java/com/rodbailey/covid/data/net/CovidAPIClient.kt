package com.rodbailey.covid.data.net

import com.rodbailey.covid.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class CovidAPIClient {
    fun getAPIClient(): CovidAPI {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        return Retrofit.Builder()
            .baseUrl("https://covid-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(CovidAPI::class.java)
    }
}