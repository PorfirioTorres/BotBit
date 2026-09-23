package com.bitlogic.botbit.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Helper para centralizar eventos de Google Analytics y Crashlytics.
 * 
 * Centralizar esto permite cambiar la implementación de analíticas en el futuro
 * sin tocar el código de las pantallas o el motor del juego.
 */
object AnalyticsHelper {

    private var analytics: FirebaseAnalytics? = null

    /**
     * Inicializa Analytics. Debe llamarse en MainApplication o MainActivity.
     */
    fun init(context: Context) {
        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(context)
        }
    }

    /**
     * Registra el inicio de una partida.
     */
    fun logGameStart(mode: String, character: String, level: String?) {
        val bundle = Bundle().apply {
            putString("mode", mode)
            putString("character", character)
            putString("level_id", level ?: "endless")
        }
        analytics?.logEvent("game_start", bundle)
        FirebaseCrashlytics.getInstance().setCustomKey("last_game_mode", mode)
    }

    /**
     * Registra el fin de una partida (muerte o victoria).
     */
    fun logGameOver(score: Int, coins: Int, result: String) {
        val bundle = Bundle().apply {
            putInt(FirebaseAnalytics.Param.SCORE, score)
            putInt("coins_collected", coins)
            putString("result", result) // "dead" o "completed"
        }
        analytics?.logEvent("game_over", bundle)
    }

    /**
     * Registra navegación entre pantallas.
     */
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Registra un error no fatal en Crashlytics.
     */
    fun recordNonFatalError(exception: Throwable, message: String) {
        FirebaseCrashlytics.getInstance().log(message)
        FirebaseCrashlytics.getInstance().recordException(exception)
    }
}
