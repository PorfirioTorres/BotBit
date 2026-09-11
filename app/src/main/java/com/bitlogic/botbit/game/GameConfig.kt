package com.bitlogic.botbit.game

object GameConfig {
    // --- Camara ---
    /** Cuantos tiles caben a lo ancho de la pantalla. */
    const val TILES_VISIBLE_X = 12f  // Valor base, se ajusta segun pantalla

    /** Altura del suelo como fraccion del alto del area de juego. */
    const val GROUND_SCREEN_RATIO = 0.74f

    // --- Jugador ---
    const val PLAYER_X = 1.5f
    const val PLAYER_SIZE = 1.0f

    // --- Fisica ---
    const val GRAVITY = 70f
    const val JUMP_VELOCITY = 18.5f
    const val BASE_SCROLL_SPEED = 8.5f
    const val ROTATION_SPEED = 340f
    const val JUMP_BUFFER = 0.12f

    // --- Colisiones ---
    const val LETHAL_SHRINK = 0.55f
    const val SPIKE_SHRINK = 0.5f
    const val LANDING_TOLERANCE = 0.05f
    const val DEATH_Y = -4f

    // --- Bucle ---
    const val FIXED_STEP = 1f / 120f
    const val MAX_FRAME_DELTA = 0.05f

    // --- Puntuacion ---
    const val POINTS_PER_TILE = 10
    const val POINTS_PER_COIN = 50  // Antes POINTS_PER_POKEBALL

    // --- Modo infinito ---
    const val ENDLESS_ACCEL = 0.06f
    const val ENDLESS_MAX_SPEED = 12f
    const val ENDLESS_REST = 5f
}
