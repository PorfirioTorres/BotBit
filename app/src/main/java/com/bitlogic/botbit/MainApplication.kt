package com.bitlogic.botbit

import android.app.Application
import com.bitlogic.botbit.utils.AnalyticsHelper
import com.bitlogic.botbit.utils.SoundManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsHelper.init(this)
        // Sin esto el SoundPool nunca se crea y los efectos no suenan.
        SoundManager.init(this)
    }
}
