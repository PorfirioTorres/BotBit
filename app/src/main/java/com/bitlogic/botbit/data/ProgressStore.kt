package com.bitlogic.botbit.data

import android.content.Context
import com.bitlogic.botbit.game.GameKind

class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("botbit", Context.MODE_PRIVATE)

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST, 0)
        set(value) { prefs.edit().putInt(KEY_BEST, value).apply() }

    fun isLevelCompleted(levelId: String): Boolean =
        prefs.getBoolean("done_$levelId", false)

    fun markLevelCompleted(levelId: String) {
        prefs.edit().putBoolean("done_$levelId", true).apply()
    }

    fun coinsFor(levelId: String): Int = prefs.getInt("coins_$levelId", 0)

    fun saveCoins(levelId: String, count: Int) {
        if (count > coinsFor(levelId)) {
            prefs.edit().putInt("coins_$levelId", count).apply()
        }
    }

    fun bestScoreForLevel(levelId: String): Int = prefs.getInt("best_$levelId", 0)

    fun saveBestScoreForLevel(levelId: String, score: Int) {
        if (score > bestScoreForLevel(levelId)) {
            prefs.edit().putInt("best_$levelId", score).apply()
        }
    }

    fun getSelectedCharacter(): String {
        return prefs.getString("selected_character", "classic") ?: "classic"
    }

    fun saveSelectedCharacter(characterId: String) {
        prefs.edit().putString("selected_character", characterId).apply()
    }

    fun unlockCharacter(characterId: String) {
        val unlocked = prefs.getStringSet("unlocked_characters", mutableSetOf("classic")) ?: mutableSetOf()
        val newUnlocked = unlocked.toMutableSet()
        newUnlocked.add(characterId)
        prefs.edit().putStringSet("unlocked_characters", newUnlocked).apply()
    }

    fun isCharacterUnlocked(characterId: String): Boolean {
        val unlocked = prefs.getStringSet("unlocked_characters", mutableSetOf("classic")) ?: mutableSetOf()
        return unlocked.contains(characterId)
    }

    // ---- Progreso por tipo de juego ----
    // RUNNER usa la llave vieja ("best_score") para no perder lo ya guardado.
    // Los modos nuevos usan "best_score_<kind>". Asi, si mas adelante deciden
    // separar la progresion por completo, ya esta el espacio de nombres listo
    // y no hace falta migrar nada.
    private fun bestKey(kind: GameKind): String =
        if (kind == GameKind.RUNNER) KEY_BEST else "${KEY_BEST}_${kind.storageKey}"

    fun bestScoreFor(kind: GameKind): Int = prefs.getInt(bestKey(kind), 0)

    fun saveBestScore(kind: GameKind, value: Int) {
        if (value > bestScoreFor(kind)) {
            prefs.edit().putInt(bestKey(kind), value).apply()
        }
    }

    companion object {
        private const val KEY_BEST = "best_score"
    }
}
