package com.bitlogic.botbit.game

import com.bitlogic.botbit.data.Characters
import com.bitlogic.botbit.game.missions.MissionManager
import com.bitlogic.botbit.game.missions.MissionType
import com.bitlogic.botbit.ui.Palette
import kotlin.math.roundToInt
import kotlin.random.Random

enum class GameMode { LEVEL, ENDLESS, INVENTORY }
enum class GameStatus { RUNNING, DEAD, COMPLETED }

class World(
    val mode: GameMode,
    val level: LevelData?,
    private val missionManager: MissionManager? = null,
    private val characterId: String = "classic",
    private val tilesVisibleX: Float = GameConfig.TILES_VISIBLE_X
) {

    val player = Player()
    val obstacles = ArrayList<Obstacle>()
    val gaps = ArrayList<Gap>()

    var scrollX = 0f
        private set

    var status = GameStatus.RUNNING
        private set

    var coins = 0
        private set

    var attempts = 1
        private set

    var speed = GameConfig.BASE_SCROLL_SPEED
        private set

    private val endless = EndlessGenerator(Random(System.nanoTime()))
    private var jumpBuffer = 0f
    private var lastScoreNotified = 0

    // ---- Estado SOLO para animacion. No participa en la fisica ni en colisiones. ----
    /** Reloj del juego en segundos. Avanza con el paso fijo, no con el reloj del sistema. */
    var elapsed = 0f
        private set

    /** 1.0 justo al aterrizar, decae a 0. Sirve para el aplastado del robot. */
    var landImpact = 0f
        private set

    /** Se enciende un instante al aterrizar para lanzar la nube de polvo. */
    var dustBurst = 0f
        private set

    val score: Int
        get() = (scrollX * GameConfig.POINTS_PER_TILE).toInt() +
                coins * GameConfig.POINTS_PER_COIN

    val progress: Float
        get() = if (mode == GameMode.LEVEL && level != null) {
            (scrollX / level.lengthTiles).coerceIn(0f, 1f)
        } else 0f

    val title: String
        get() = level?.name ?: "MODO INFINITO"

    // Color del robot seleccionado
    val characterColor: androidx.compose.ui.graphics.Color
        get() = Characters.all.find { it.id == characterId }?.color ?: Palette.DarkYellow

    init {
        start(firstRun = true)
    }

    fun retry() = start(firstRun = false)

    private fun start(firstRun: Boolean) {
        if (!firstRun) attempts++
        player.reset()
        scrollX = 0f
        coins = 0
        jumpBuffer = 0f
        status = GameStatus.RUNNING
        obstacles.clear()
        gaps.clear()
        lastScoreNotified = 0

        if (mode == GameMode.LEVEL && level != null) {
            level.obstacles.forEach { obstacles.add(it.freshCopy()) }
            gaps.addAll(level.gaps)
            speed = level.scrollSpeed
        } else {
            speed = GameConfig.BASE_SCROLL_SPEED
            endless.reset()
            endless.generateUpTo(tilesVisibleX * 3f, obstacles, gaps)
        }
    }

    fun onTap() {
        if (status == GameStatus.RUNNING) jumpBuffer = GameConfig.JUMP_BUFFER
    }

    fun update(dt: Float) {
        if (status != GameStatus.RUNNING) return

        // Animacion: avanza con el mismo dt fijo para que nunca se desincronice.
        elapsed += dt
        landImpact = (landImpact - dt * 5f).coerceAtLeast(0f)
        dustBurst = (dustBurst - dt * 2.2f).coerceAtLeast(0f)

        if (mode == GameMode.ENDLESS) {
            speed = (speed + GameConfig.ENDLESS_ACCEL * dt)
                .coerceAtMost(GameConfig.ENDLESS_MAX_SPEED)
            endless.generateUpTo(scrollX + tilesVisibleX * 3f, obstacles, gaps)
            prune()
        }

        scrollX += speed * dt

        val prevY = player.y
        val wasAirborne = !player.onGround

        // 1. Salto
        if (jumpBuffer > 0f) {
            jumpBuffer -= dt
            if (player.onGround) {
                player.vy = GameConfig.JUMP_VELOCITY
                player.onGround = false
                jumpBuffer = 0f
                missionManager?.updateProgress(MissionType.JUMP_COUNT)
            }
        }

        // 2. Integracion
        player.vy -= GameConfig.GRAVITY * dt
        player.y += player.vy * dt
        if (!player.onGround) player.rotation += GameConfig.ROTATION_SPEED * dt

        // 3. Resolucion de contactos
        var grounded = false

        val centerX = scrollX + GameConfig.PLAYER_X + GameConfig.PLAYER_SIZE / 2f
        var overGap = false
        for (g in gaps) {
            if (g.right < centerX) continue
            if (g.x > centerX) break
            overGap = true
            break
        }

        if (!overGap && player.y <= 0f && player.vy <= 0f) {
            player.y = 0f
            player.vy = 0f
            grounded = true
        }

        var body = AABB(
            left = scrollX + GameConfig.PLAYER_X,
            bottom = player.y,
            width = GameConfig.PLAYER_SIZE,
            height = GameConfig.PLAYER_SIZE
        )

        for (ob in obstacles) {
            if (ob.x > body.right + 2f) break
            if (ob.right < body.left - 2f) continue

            when (ob.type) {
                ObstacleType.COIN -> {
                    if (!ob.collected && body.overlaps(ob.bounds())) {
                        ob.collected = true
                        coins++
                        missionManager?.updateProgress(MissionType.COLLECT_COINS)
                    }
                }

                ObstacleType.SPIKE -> {
                    val lethal = body.shrink(GameConfig.LETHAL_SHRINK)
                    if (lethal.overlaps(ob.bounds().shrink(GameConfig.SPIKE_SHRINK))) {
                        status = GameStatus.DEAD
                        return
                    }
                }

                ObstacleType.BLOCK, ObstacleType.PLATFORM, ObstacleType.RECTANGLE -> {
                    if (body.overlaps(ob.bounds())) {
                        val cameFromAbove =
                            player.vy <= 0f && prevY >= ob.y + ob.h - GameConfig.LANDING_TOLERANCE
                        if (cameFromAbove) {
                            player.y = ob.y + ob.h
                            player.vy = 0f
                            grounded = true
                            body = body.copy(bottom = player.y)
                        } else {
                            status = GameStatus.DEAD
                            return
                        }
                    }
                }
            }
        }

        if (player.y < GameConfig.DEATH_Y) {
            status = GameStatus.DEAD
            return
        }

        if (grounded && wasAirborne) {
            player.rotation = (player.rotation / 90f).roundToInt() * 90f
            landImpact = 1f
            dustBurst = 1f
        }
        player.onGround = grounded

        val currentScore = score
        if (currentScore >= lastScoreNotified + 100) {
            missionManager?.updateProgress(MissionType.REACH_SCORE, currentScore)
            lastScoreNotified = currentScore
        }

        if (mode == GameMode.LEVEL && level != null && scrollX >= level.lengthTiles) {
            status = GameStatus.COMPLETED
        }
    }

    private fun prune() {
        val limit = scrollX - 5f
        while (obstacles.isNotEmpty() && obstacles[0].right < limit) obstacles.removeAt(0)
        while (gaps.isNotEmpty() && gaps[0].right < limit) gaps.removeAt(0)
    }
}
