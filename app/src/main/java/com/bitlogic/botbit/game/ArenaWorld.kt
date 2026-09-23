package com.bitlogic.botbit.game

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * MODO ARENA (estilo Vampire Survivors).
 * 
 * Vista cenital con movimiento libre en 8 direcciones y disparo automático.
 * Implementa pooling de enemigos y balas para mantener 60 FPS con +200 entidades.
 */
class ArenaWorld(
    private val characterId: String = "classic"
) : GameWorld {

    override val kind: GameKind get() = GameKind.ARENA
    override var status: GameStatus = GameStatus.RUNNING
        private set
    override var elapsed: Float = 0f
        private set

    // --- Estado del jugador ---
    var px = 0f; private set
    var py = 0f; private set
    private var vx = 0f
    private var vy = 0f
    
    // --- Progresión ---
    override var score: Int = 0; private set
    override var coins: Int = 0; private set
    override var attempts: Int = 1; private set
    override val progress: Float get() = (xp.toFloat() / xpToNextLevel).coerceIn(0f, 1f)
    override val title: String get() = "ARENA - NIVEL $level"

    private var level = 1
    private var xp = 0
    private var xpToNextLevel = 100

    // --- Pooling ---
    val enemies = Array(MAX_ENEMIES) { ArenaEnemy() }
    val bullets = Array(MAX_BULLETS) { ArenaBullet() }
    
    private var spawnTimer = 0f
    private var shootTimer = 0f

    override fun update(dt: Float) {
        if (status != GameStatus.RUNNING) return
        elapsed += dt

        // 1. Movimiento del jugador
        px += vx * dt * PLAYER_SPEED
        py += vy * dt * PLAYER_SPEED

        // 2. Disparo automático al enemigo más cercano
        shootTimer -= dt
        if (shootTimer <= 0f) {
            val target = findClosestEnemy()
            if (target != null) {
                spawnBullet(target)
                shootTimer = SHOOT_RATE
            }
        }

        // 3. Generación de enemigos (fuera de cámara)
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawnEnemy()
            spawnTimer = SPAWN_RATE / (1f + elapsed * 0.05f) // Dificultad creciente
        }

        // 4. Actualización de Balas y Enemigos
        updateEntities(dt)

        // 5. Colisiones (Simplificadas por ahora)
        checkCollisions()
    }

    private fun findClosestEnemy(): ArenaEnemy? {
        var closest: ArenaEnemy? = null
        var minDist = Float.MAX_VALUE
        for (e in enemies) {
            if (!e.active) continue
            val distSq = (e.x - px) * (e.x - px) + (e.y - py) * (e.y - py)
            if (distSq < minDist) {
                minDist = distSq
                closest = e
            }
        }
        return if (minDist < 100f) closest else null // Rango máximo
    }

    private fun spawnBullet(target: ArenaEnemy) {
        val b = bullets.find { !it.active } ?: return
        val angle = atan2(target.y - py, target.x - px)
        b.active = true
        b.x = px
        b.y = py
        b.vx = cos(angle) * BULLET_SPEED
        b.vy = sin(angle) * BULLET_SPEED
        b.life = 2.0f
    }

    private fun spawnEnemy() {
        val e = enemies.find { !it.active } ?: return
        val angle = (0..359).random() * (Math.PI / 180f).toFloat()
        val dist = 12f // Fuera de la vista típica
        e.active = true
        e.x = px + cos(angle) * dist
        e.y = py + sin(angle) * dist
        e.hp = 1
    }

    private fun updateEntities(dt: Float) {
        for (b in bullets) {
            if (!b.active) continue
            b.x += b.vx * dt
            b.y += b.vy * dt
            b.life -= dt
            if (b.life <= 0) b.active = false
        }

        for (e in enemies) {
            if (!e.active) continue
            val angle = atan2(py - e.y, px - e.x)
            e.x += cos(angle) * ENEMY_SPEED * dt
            e.y += sin(angle) * ENEMY_SPEED * dt
        }
    }

    private fun checkCollisions() {
        for (e in enemies) {
            if (!e.active) continue
            
            // Enemigo vs Jugador
            val distPlayerSq = (e.x - px) * (e.x - px) + (e.y - py) * (e.y - py)
            if (distPlayerSq < 0.5f) {
                // Game Over por ahora al primer toque
                status = GameStatus.DEAD
            }

            // Enemigo vs Balas
            for (b in bullets) {
                if (!b.active) continue
                val distBulletSq = (e.x - b.x) * (e.x - b.x) + (e.y - b.y) * (e.y - b.y)
                if (distBulletSq < 0.4f) {
                    e.active = false
                    b.active = false
                    score += 10
                    gainXp(20)
                }
            }
        }
    }

    private fun gainXp(amount: Int) {
        xp += amount
        if (xp >= xpToNextLevel) {
            level++
            xp = 0
            xpToNextLevel = (xpToNextLevel * 1.2f).toInt()
            // TODO: Mostrar selector de mejora
        }
    }

    override fun onInput(event: InputEvent) {
        if (event is InputEvent.Move) {
            vx = event.x
            vy = event.y
        }
    }

    override fun retry() {
        px = 0f; py = 0f; vx = 0f; vy = 0f
        score = 0; coins = 0; level = 1; xp = 0
        enemies.forEach { it.active = false }
        bullets.forEach { it.active = false }
        status = GameStatus.RUNNING
    }

    companion object {
        const val MAX_ENEMIES = 100
        const val MAX_BULLETS = 50
        const val PLAYER_SPEED = 6f
        const val ENEMY_SPEED = 3.5f
        const val BULLET_SPEED = 12f
        const val SHOOT_RATE = 0.8f
        const val SPAWN_RATE = 1.5f
    }
}

class ArenaEnemy {
    var active = false
    var x = 0f
    var y = 0f
    var hp = 1
}

class ArenaBullet {
    var active = false
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var life = 0f
}
