package com.bitlogic.botbit.game.missions

import com.bitlogic.botbit.data.MissionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class MissionManager(
    private val store: MissionStore
) {
    private val _missions = MutableStateFlow<List<MissionProgress>>(emptyList())
    val missions: StateFlow<List<MissionProgress>> = _missions.asStateFlow()

    private var missionCounter = 0
¿
    init {
        loadMissions()
    }

    private fun loadMissions() {
        val allMissions = listOf(
            Mission(
                id = "mission_1",
                title = "PRIMEROS PASOS",
                description = "Completa el nivel 1",
                type = MissionType.COMPLETE_LEVEL,
                target = 1,
                rewardCoins = 5,
                rewardCharacter = "pyro"
            ),
            Mission(
                id = "mission_2",
                title = "COLECCIONISTA",
                description = "Recolecta 10 monedas",
                type = MissionType.COLLECT_COINS,
                target = 10,
                rewardCoins = 15
            ),
            Mission(
                id = "mission_3",
                title = "EXPLORADOR",
                description = "Alcanza 1000 puntos",
                type = MissionType.REACH_SCORE,
                target = 1000,
                rewardCoins = 20,
                rewardCharacter = "volt"
            ),
            Mission(
                id = "mission_4",
                title = "SALTADOR",
                description = "Realiza 50 saltos",
                type = MissionType.JUMP_COUNT,
                target = 50,
                rewardCoins = 10
            ),
            Mission(
                id = "mission_5",
                title = "MAESTRO INFINITO",
                description = "Sobrevive 100 tiles en modo infinito",
                type = MissionType.PLAY_ENDLESS,
                target = 100,
                rewardCoins = 30,
                rewardCharacter = "shadow"
            )
        )

        _missions.value = allMissions.map { mission ->
            MissionProgress(
                mission = mission,
                progress = store.getMissionProgress(mission.id),
                completed = store.isMissionCompleted(mission.id)
            )
        }
    }

    fun completeMission(missionId: String) {
        val updated = _missions.value.map { mission ->
            if (mission.mission.id == missionId && !mission.completed) {
                store.saveMissionProgress(mission.mission.id, mission.mission.target)
                store.markMissionCompleted(mission.mission.id)
                applyReward(mission.mission)
                mission.copy(progress = mission.mission.target, completed = true)
            } else {
                mission
            }
        }
        _missions.value = updated
    }

    fun resetMission(missionId: String) {
        val updated = _missions.value.map { mission ->
            if (mission.mission.id == missionId) {
                store.saveMissionProgress(mission.mission.id, 0)
                store.markMissionCompleted(mission.mission.id, false)
                mission.copy(progress = 0, completed = false)
            } else {
                mission
            }
        }
        _missions.value = updated
    }

    fun deleteMission(missionId: String) {
        val updated = _missions.value.filterNot { it.mission.id == missionId }
        store.saveMissionProgress(missionId, 0)
        store.markMissionCompleted(missionId, false)
        _missions.value = updated
    }

    fun refreshMissions() {
        val newMissions = generateNewMissions(5)
        _missions.value = newMissions
    }

    private fun generateNewMissions(count: Int): List<MissionProgress> {
        missionCounter++
        val random = Random(System.currentTimeMillis())
        val missions = mutableListOf<MissionProgress>()

        val types = listOf(
            MissionType.COMPLETE_LEVEL,
            MissionType.COLLECT_COINS,
            MissionType.REACH_SCORE,
            MissionType.PLAY_ENDLESS,
            MissionType.JUMP_COUNT
        )

        val rewards = listOf("pyro", "aqua", "terra", "volt", "shadow")

        for (i in 0 until count) {
            val selectedType = types[random.nextInt(types.size)]
            val target = when (selectedType) {
                MissionType.COMPLETE_LEVEL -> random.nextInt(1, 3)
                MissionType.COLLECT_COINS -> random.nextInt(5, 15)
                MissionType.REACH_SCORE -> random.nextInt(300, 1000)
                MissionType.PLAY_ENDLESS -> random.nextInt(50, 200)
                MissionType.JUMP_COUNT -> random.nextInt(20, 80)
            }

            val rewardCharacter = if (random.nextBoolean()) rewards[random.nextInt(rewards.size)] else null

            val newMission = Mission(
                id = "mission_${System.currentTimeMillis()}_$i",
                title = "MISIÓN ${missionCounter}.${i + 1}",
                description = when (selectedType) {
                    MissionType.COMPLETE_LEVEL -> "Completa $target nivel(es)"
                    MissionType.COLLECT_COINS -> "Recolecta $target monedas"
                    MissionType.REACH_SCORE -> "Alcanza $target puntos"
                    MissionType.PLAY_ENDLESS -> "Sobrevive $target tiles en modo infinito"
                    MissionType.JUMP_COUNT -> "Realiza $target saltos"
                },
                type = selectedType,
                target = target,
                rewardCoins = random.nextInt(5, 25),
                rewardCharacter = rewardCharacter
            )

            missions.add(
                MissionProgress(
                    mission = newMission,
                    progress = 0,
                    completed = false
                )
            )
        }

        return missions
    }

    private fun applyReward(mission: Mission) {
        val character = mission.rewardCharacter
        if (character != null) {
            unlockCharacter(character)
        }
    }

    private fun unlockCharacter(characterId: String) {
        // Guardar en ProgressStore que el personaje está desbloqueado
    }

    fun updateProgress(type: MissionType, amount: Int = 1) {
        val updated = _missions.value.map { mission ->
            if (mission.mission.type == type && !mission.completed) {
                val newProgress = mission.progress + amount
                store.saveMissionProgress(mission.mission.id, newProgress)
                if (newProgress >= mission.mission.target) {
                    mission.copy(progress = newProgress, completed = true).also {
                        store.markMissionCompleted(mission.mission.id)
                        applyReward(mission.mission)
                    }
                } else {
                    mission.copy(progress = newProgress)
                }
            } else {
                mission
            }
        }
        _missions.value = updated
    }
}
