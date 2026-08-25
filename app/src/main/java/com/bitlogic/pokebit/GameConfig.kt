package com.bitlogic.pokebit.game

/**
 * Todos los numeros que definen como se siente el juego.
 * Las unidades del mundo son TILES, no pixeles: asi el juego se ve igual
 * en cualquier resolucion de pantalla.
 *
 * Geometria del salto con estos valores:
 *   tiempo en el aire = 2 * JUMP_VELOCITY / GRAVITY      = 0.53 s
 *   altura maxima     = JUMP_VELOCITY^2 / (2 * GRAVITY)  = 2.44 tiles
 *   alcance           = tiempo * BASE_SCROLL_SPEED       = 4.49 tiles
 *
 * Si cambias GRAVITY o JUMP_VELOCITY, vuelve a correr build_level.py
 * para confirmar que los niveles siguen siendo superables.
 */
object GameConfig {

    // --- Camara ---
    /** Cuantos tiles caben a lo ancho de la pantalla. */
    const val TILES_VISIBLE_X = 12f

    /** Altura del suelo como fraccion del alto del area de juego. */
    const val GROUND_SCREEN_RATIO = 0.74f

    // --- Jugador ---
    const val PLAYER_X = 2.5f
    const val PLAYER_SIZE = 0.9f

    // --- Fisica ---
    const val GRAVITY = 70f
    const val JUMP_VELOCITY = 18.5f
    const val BASE_SCROLL_SPEED = 8.5f

    /** Grados por segundo que gira el jugador mientras esta en el aire. */
    const val ROTATION_SPEED = 340f

    /** Ventana en segundos para registrar un toque hecho justo antes de aterrizar. */
    const val JUMP_BUFFER = 0.12f

    // --- Colisiones ---
    /** El hitbox mortal es mas chico que el sprite: hace que el juego se sienta justo. */
    const val LETHAL_SHRINK = 0.55f

    /** Los picos tambien usan un hitbox reducido porque son triangulos, no cuadrados. */
    const val SPIKE_SHRINK = 0.5f

    /** Margen para decidir si un contacto con un bloque es aterrizaje o choque lateral. */
    const val LANDING_TOLERANCE = 0.05f

    /** Altura a la que se considera que el jugador cayo en un hueco. */
    const val DEATH_Y = -4f

    // --- Bucle ---
    /** Paso fijo de simulacion. Fijo = fisica identica en cualquier celular. */
    const val FIXED_STEP = 1f / 120f

    /** Tope de tiempo por frame, evita saltos gigantes al volver de segundo plano. */
    const val MAX_FRAME_DELTA = 0.05f

    // --- Puntuacion ---
    const val POINTS_PER_TILE = 10
    const val POINTS_PER_POKEBALL = 50

    // --- Modo infinito ---
    const val ENDLESS_ACCEL = 0.06f
    const val ENDLESS_MAX_SPEED = 12f

    /** Tiles de descanso entre bloques de obstaculos generados. */
    const val ENDLESS_REST = 5f
}
