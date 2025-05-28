package com.example.test.application

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }


    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}