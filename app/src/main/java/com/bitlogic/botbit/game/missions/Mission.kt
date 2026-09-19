package com.bitlogic.botbit.game.missions

enum class MissionType {
    COMPLETE_LEVEL,
    COLLECT_COINS,  // Antes COLLECT_POKEBALLS
    REACH_SCORE,
    PLAY_ENDLESS,
    JUMP_COUNT
}

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val target: Int,
    val rewardCoins: Int = 0,  // Antes rewardPokeballs
    val rewardCharacter: String? = null
)

data class .MissionProgress(
    val mission: Mission,
    var progress: Int = 0,
    var completed: Boolean = false
)
