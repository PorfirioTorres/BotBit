package com.bitlogic.botbit.game

/**
 * Tipo de juego. Es ORTOGONAL a GameMode:
 *   GameKind  = que juego es (correr, torre, arena)
 *   GameMode  = como se juega ese juego (nivel fijo o infinito)
 *
 * Un GameKind nuevo NO se agrega con ifs dentro de World. Se agrega
 * implementando GameWorld en una clase aparte. Ver GameWorld.kt.
 */
enum class GameKind(
    val title: String,
    val tagline: String,
    val description: String,
    /** false = aparece en el selector pero bloqueado. */
    val available: Boolean,
    /** Orientacion en la que el modo se juega mejor. */
    val prefersPortrait: Boolean
) {
    RUNNER(
        title = "CARRERA",
        tagline = "Un toque para saltar",
        description = "Recorre el nivel esquivando picos y pozos. " +
                "Un solo error y vuelves a empezar.",
        available = true,
        prefersPortrait = false
    ),

    TOWER(
        title = "LA TORRE",
        tagline = "Sube sin caerte",
        description = "Manten presionado para cargar el salto y sueltalo para " +
                "brincar. No hay muerte: si caes, pierdes altura.",
        available = true,
        prefersPortrait = true
    ),

    ARENA(
        title = "ARENA",
        tagline = "Sobrevive a las oleadas",
        description = "Vista cenital. Te mueves y el robot dispara solo. " +
                "Junta experiencia y aguanta lo mas posible.",
        available = false,
        prefersPortrait = true
    );

    /** Prefijo para las llaves de guardado. Permite separar el progreso despues sin migracion. */
    val storageKey: String get() = name.lowercase()
}
