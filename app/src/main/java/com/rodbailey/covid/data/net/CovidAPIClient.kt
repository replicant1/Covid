package com.rodbailey.covid.data.net

import com.rodbailey.covid.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class CovidAPIClient {
    fun getAPIClient(): CovidAPI {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }

        return Retrofit.Builder()
            .baseUrl("https://covid-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(builder.build())
            .build()
            .create(CovidAPI::class.java)
    }
}