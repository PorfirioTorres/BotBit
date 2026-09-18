package com.bitlogic.botbit.game

/**
 * Contrato que cumple cualquier modo de juego.
 *
 * Esta interfaz es la razon de ser del refactor. GameScreen ya no sabe si esta
 * corriendo un runner, una torre o una arena: solo sabe actualizar un
 * GameWorld con paso fijo y pedirle a un renderer que lo dibuje.
 *
 * Para agregar un modo NO se toca World ni GameScreen. Se crea una clase nueva
 * que implemente esto y un renderer que la dibuje.
 *
 * Reglas que todo mundo debe respetar:
 *  - update(dt) recibe SIEMPRE el mismo dt (paso fijo). Nada de delta variable.
 *  - No se asigna memoria dentro de update(): se usan pools o arreglos fijos.
 *  - El estado de animacion va aparte del estado de fisica.
 */
interface GameWorld {

    /** Que tipo de juego es. Lo usa la UI para elegir HUD y controles. */
    val kind: GameKind

    /** RUNNING, DEAD o COMPLETED. */
    val status: GameStatus

    /** Puntuacion actual. Cada modo la calcula a su manera. */
    val score: Int

    /** 0..1 para la barra de progreso. Los modos sin final devuelven 0. */
    val progress: Float

    /** Texto que se muestra en el HUD. */
    val title: String

    /** Reloj de animacion en segundos, avanzado con el paso fijo. */
    val elapsed: Float

    /** Avanza la simulacion un paso fijo. */
    fun update(dt: Float)

    /** Entrada del jugador, ya traducida por la UI. */
    fun onInput(event: InputEvent)

    /** Reinicia manteniendo el nivel y el personaje. */
    fun retry()
}

/**
 * Entrada unificada de los tres modos.
 *
 * La UI traduce el gesto concreto (tap, arrastre, joystick) a uno de estos,
 * y cada mundo interpreta solo los que le sirven. El runner ignora Move;
 * la arena ignora Press y Release.
 */
sealed interface InputEvent {

    /** Toque simple. Runner: saltar. */
    data object Tap : InputEvent

    /** Se presiono y se mantiene. Torre: empezar a cargar el salto. */
    data object Press : InputEvent

    /**
     * Se solto. Torre: ejecutar el salto cargado.
     * @param dirX -1 izquierda, 0 vertical, 1 derecha.
     */
    data class Release(val dirX: Float) : InputEvent

    /**
     * Direccion de movimiento continua, normalizada a -1..1.
     * Arena: mover al robot. Torre: caminar.
     */
    data class Move(val x: Float, val y: Float) : InputEvent
}
