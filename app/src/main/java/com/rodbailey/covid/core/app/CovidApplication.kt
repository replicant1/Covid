package com.rodbailey.covid.core.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class CovidApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        // Don't plant DebugTree unless BuildConfig.DEBUG is true
//        if (BuildConfig.DEBUG) {
//            Timber.plant(DebugTree())
//        }
    }
}