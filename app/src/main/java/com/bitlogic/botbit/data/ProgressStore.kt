package com.bitlogic.botbit.data

import android.content.Context

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

    companion object {
        private const val KEY_BEST = "best_score"
    }
}
