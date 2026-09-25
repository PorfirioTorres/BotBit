package com.bitlogic.botbit.game.missions

enum class MissionType {
    COMPLETE_LEVEL,
    COLLECT_COINS,  // Antes COLLECT_POKEBALLS
    REACH_SCORE,
    PLAY_ENDLESS,
    JUMP_COUNT,
    /** Monedas en UNA sola partida, sin morir. */
    COINS_IN_ONE_RUN,
    /** Tiles recorridos sumando todas las partidas. */
    TOTAL_DISTANCE
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

data class MissionProgress(
    val mission: Mission,
    var progress: Int = 0,
    /** Objetivo cumplido. NO significa que ya se haya cobrado la recompensa. */
    var completed: Boolean = false,
    /** La recompensa ya se entrego. Solo pasa al presionar "Reclamar". */
    var claimed: Boolean = false
) {
    /** Cumplida pero sin cobrar: es la que muestra el boton. */
    val claimable: Boolean get() = completed && !claimed
}
