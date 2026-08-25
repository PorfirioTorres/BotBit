package com.bitlogic.pokebit.data

import android.content.Context

/**
 * Guardado local. Se usa SharedPreferences a proposito: cero dependencias
 * y cero corrutinas para la Fase 1. Cuando llegue la progresion de evoluciones
 * (Fase 3) conviene migrar a Room, porque ya vas a tener relaciones
 * criatura -> etapa -> niveles completados.
 */
class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("pokebit", Context.MODE_PRIVATE)

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST, 0)
        set(value) {
            prefs.edit().putInt(KEY_BEST, value).apply()
        }

    fun isLevelCompleted(levelId: String): Boolean =
        prefs.getBoolean("done_$levelId", false)

    fun markLevelCompleted(levelId: String) {
        prefs.edit().putBoolean("done_$levelId", true).apply()
    }

    fun pokeballsFor(levelId: String): Int = prefs.getInt("balls_$levelId", 0)

    fun savePokeballs(levelId: String, count: Int) {
        if (count > pokeballsFor(levelId)) {
            prefs.edit().putInt("balls_$levelId", count).apply()
        }
    }

    private companion object {
        const val KEY_BEST = "best_score"
    }
}
