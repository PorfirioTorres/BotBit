package com.bitlogic.botbit.data

import android.content.Context
import com.bitlogic.botbit.game.GameKind

class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("botbit", Context.MODE_PRIVATE)

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST, 0)
        set(value) { prefs.edit().putInt(KEY_BEST, value).apply() }

    /** Saldo acumulado de todas las partidas. Se usa para comprar personajes. */
    var totalCoins: Int
        get() = prefs.getInt(KEY_TOTAL_COINS, 0)
        private set(value) { prefs.edit().putInt(KEY_TOTAL_COINS, value).apply() }

    /** Suma monedas al monedero global. */
    fun addCoins(amount: Int) {
        if (amount > 0) {
            totalCoins += amount
        }
    }

    /** Resta monedas al monedero global (para compras). Retorna true si hubo saldo. */
    fun spendCoins(amount: Int): Boolean {
        val current = totalCoins
        return if (current >= amount) {
            totalCoins = current - amount
            true
        } else false
    }

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

    // ---- Checkpoints de la Torre ----
    fun saveTowerCheckpoint(room: Int, x: Float, y: Float) {
        prefs.edit()
            .putInt("tower_room", room)
            .putFloat("tower_x", x)
            .putFloat("tower_y", y)
            .apply()
    }

    fun getTowerCheckpoint(): Triple<Int, Float, Float> {
        return Triple(
            prefs.getInt("tower_room", 0),
            prefs.getFloat("tower_x", 5f),
            prefs.getFloat("tower_y", 0f)
        )
    }

    fun clearTowerCheckpoint() {
        prefs.edit().remove("tower_room").remove("tower_x").remove("tower_y").apply()
    }

    // ---- Preferencia de control de la Arena ----
    // "FLOTANTE" = el joystick aparece donde pongas el dedo
    // "FIJO"     = siempre en la esquina inferior izquierda
    var joystickMode: String
        get() = prefs.getString("joystick_mode", "FLOTANTE") ?: "FLOTANTE"
        set(value) { prefs.edit().putString("joystick_mode", value).apply() }

    companion object {
        private const val KEY_BEST = "best_score"
        private const val KEY_TOTAL_COINS = "total_coins"
    }
}
