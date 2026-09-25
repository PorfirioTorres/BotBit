package com.bitlogic.botbit.game

import com.bitlogic.botbit.data.ProgressStore
import kotlin.math.roundToInt

/**
 * MODO TORRE (estilo Jump King).
 * 
 * Implementación funcional del motor de ascenso. 
 * Cumple con el contrato GameWorld y puede ser corrido por GameScreen.
 */
class TowerWorld(
    private val level: LevelData?,
    private val store: ProgressStore, // NUEVO
    private val characterId: String = "classic"
) : GameWorld {

    override val kind: GameKind get() = GameKind.TOWER

    override var status: GameStatus = GameStatus.RUNNING
        private set

    override var elapsed: Float = 0f
        private set

    override val score: Int get() = (maxHeight * 10).toInt()
    override val progress: Float get() = (room.toFloat() / TOTAL_ROOMS).coerceIn(0f, 1f)
    override val title: String get() = level?.name ?: "LA TORRE"
    override val coins: Int get() = 0
    override val attempts: Int get() = 1

    val obstacles: List<Obstacle> get() = level?.obstacles ?: emptyList()

    // --- Estado del jugador ---
    var x = 5f; private set // Empezar al centro (ancho típico 12 tiles)
    var y = 0f; private set
    private var vx = 0f
    private var vy = 0f
    var onGround = true; private set
    var rotation = 0f; private set

    // --- Carga del salto ---
    var charging = false; private set
    var charge = 0f; private set

    /** Sala visible. La camara no interpola: salta de sala en sala. */
    var room = 0; private set

    /** Altura actual en "metros" para el HUD. */
    val altura: Float get() = y

    /** Altura maxima alcanzada en este intento. */
    val record: Float get() = maxHeight
    private var maxHeight = 0f

    init {
        // Cargar ultimo checkpoint al iniciar
        val (cRoom, cX, cY) = store.getTowerCheckpoint()
        room = cRoom
        x = cX
        y = cY
        maxHeight = y
    }

    override fun update(dt: Float) {
        if (status != GameStatus.RUNNING) return
        elapsed += dt

        if (charging) charge = (charge + dt).coerceAtMost(CHARGE_MAX)

        val prevY = y
        val wasAirborne = !onGround

        // 1. Integracion
        vy -= GameConfig.GRAVITY * dt
        y += vy * dt
        x += vx * dt
        
        if (!onGround) {
            rotation += GameConfig.ROTATION_SPEED * dt
        }

        // 2. Colisiones y resolucion
        var grounded = false
        
        // Suelo base del nivel
        if (y <= 0f && vy <= 0f) {
            y = 0f
            vy = 0f
            grounded = true
        }

        val body = AABB(
            left = x,
            bottom = y,
            width = GameConfig.PLAYER_SIZE,
            height = GameConfig.PLAYER_SIZE
        )

        level?.obstacles?.forEach { ob ->
            if (ob.type == ObstacleType.COIN) return@forEach
            
            val bounds = ob.bounds()
            if (body.overlaps(bounds)) {
                if (ob.type == ObstacleType.CHECKPOINT) {
                    if (onGround) {
                        store.saveTowerCheckpoint(room, x, y)
                    }
                    return@forEach
                }

                // Colision desde arriba (aterrizar)
                val cameFromAbove = vy <= 0f && prevY >= bounds.top - GameConfig.LANDING_TOLERANCE
                if (cameFromAbove) {
                    y = bounds.top
                    vy = 0f
                    vx = 0f // Al caer en plataforma se detiene el deslizamiento de rebote
                    grounded = true
                } else {
                    // Colision lateral o desde abajo
                    // Rebote simple en X
                    if (x < bounds.left || x + GameConfig.PLAYER_SIZE > bounds.right) {
                        vx = -vx * BOUNCE
                        // Empujar fuera para evitar quedar atrapado
                        if (x < bounds.left) x = bounds.left - GameConfig.PLAYER_SIZE - 0.01f
                        else x = bounds.right + 0.01f
                    }
                    // Si choca con el techo de un bloque
                    if (vy > 0f && y + GameConfig.PLAYER_SIZE > bounds.bottom && prevY + GameConfig.PLAYER_SIZE <= bounds.bottom) {
                        vy = -vy * BOUNCE
                        y = bounds.bottom - GameConfig.PLAYER_SIZE - 0.01f
                    }
                }
            }
        }

        // Limites laterales de la torre
        if (x < 0f) {
            x = 0f
            vx = -vx * BOUNCE
        } else if (x + GameConfig.PLAYER_SIZE > TOWER_WIDTH) {
            x = TOWER_WIDTH - GameConfig.PLAYER_SIZE
            vx = -vx * BOUNCE
        }

        if (grounded && wasAirborne) {
            rotation = (rotation / 90f).roundToInt() * 90f
        }
        onGround = grounded
        
        // 3. Gestion de salas (Camara)
        val currentYInRoom = y % ROOM_HEIGHT
        val targetRoom = (y / ROOM_HEIGHT).toInt()
        if (targetRoom != room) {
            room = targetRoom
        }
        
        maxHeight = maxOf(maxHeight, y)

        // Victoria: llegar a la plataforma de meta. TowerWorld nunca ponia
        // COMPLETED, asi que la torre no tenia final aunque la subieras entera.
        if (y >= GOAL_HEIGHT && onGround) {
            status = GameStatus.COMPLETED
        }
    }

    override fun onInput(event: InputEvent) {
        when (event) {
            is InputEvent.Press -> if (onGround) { 
                charging = true
                charge = 0f 
                vx = 0f // No se puede caminar mientras se carga
            }
            is InputEvent.Release -> {
                if (charging && onGround) {
                    val power = (charge / CHARGE_MAX).coerceIn(0.2f, 1f)
                    vy = JUMP_MIN + (JUMP_MAX - JUMP_MIN) * power
                    vx = event.dirX * SIDE_SPEED * power
                    onGround = false
                }
                charging = false
                charge = 0f
            }
            is InputEvent.Move -> if (onGround && !charging) {
                vx = event.x * WALK_SPEED
            }
            is InputEvent.Tap -> { 
                // Tap corto puede ser un salto rapido
                if (onGround && !charging) {
                    vy = JUMP_MIN
                    onGround = false
                }
            }
        }
    }

    override fun retry() {
        val (cRoom, cX, cY) = store.getTowerCheckpoint()
        x = cX; y = cY; vx = 0f; vy = 0f
        onGround = true; charging = false; charge = 0f
        room = cRoom
        maxHeight = y
        status = GameStatus.RUNNING
        elapsed = 0f
    }

    /** Fraccion de carga 0..1, para dibujar la barra sobre el robot. */
    val chargeRatio: Float get() = if (charging) charge / CHARGE_MAX else 0f

    private companion object {
        /** Altura de la plataforma de meta del tower_map verificado. */
        const val GOAL_HEIGHT = 300f
        const val CHARGE_MAX = 0.65f   // segundos para carga completa
        const val JUMP_MIN = 12f
        const val JUMP_MAX = 24f
        const val SIDE_SPEED = 9f
        const val WALK_SPEED = 5f
        const val BOUNCE = 0.5f
        const val ROOM_HEIGHT = 15f    // tiles de alto por sala
        const val TOWER_WIDTH = 12f     // ancho total de la torre
        const val TOTAL_ROOMS = 20      // salas totales hasta la cima
    }
}
