package com.bitlogic.botbit

import android.app.Application
import com.bitlogic.botbit.utils.AnalyticsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsHelper.init(this)
    }
}
