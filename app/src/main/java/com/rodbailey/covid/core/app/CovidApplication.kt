package com.rodbailey.covid.core.app

import android.app.Application
import com.rodbailey.covid.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree


@HiltAndroidApp
class CovidApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        // Don't plant DebugTree unless BuildConfig.DEBUG is true
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}