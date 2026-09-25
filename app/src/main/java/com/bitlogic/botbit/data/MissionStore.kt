package com.bitlogic.botbit.data

import android.content.Context

class MissionStore(context: Context) {
    private val prefs = context.getSharedPreferences("missions", Context.MODE_PRIVATE)

    fun getMissionProgress(missionId: String): Int = 
        prefs.getInt("progress_$missionId", 0)

    fun saveMissionProgress(missionId: String, progress: Int) {
        prefs.edit().putInt("progress_$missionId", progress).apply()
    }

    fun isMissionCompleted(missionId: String): Boolean = 
        prefs.getBoolean("completed_$missionId", false)

    fun markMissionCompleted(missionId: String, completed: Boolean = true) {
        prefs.edit().putBoolean("completed_$missionId", completed).apply()
    }

    fun isMissionClaimed(missionId: String): Boolean =
        prefs.getBoolean("mission_claimed_$missionId", false)

    fun markMissionClaimed(missionId: String) {
        prefs.edit().putBoolean("mission_claimed_$missionId", true).apply()
    }
}
