package com.bitlogic.botbit.ui

import androidx.compose.ui.graphics.Color

/** Forma de las siluetas del fondo. Define como se genera el paisaje. */
enum class SkylineKind { HILLS, CAVE, CITY }

/**
 * Paleta completa de un nivel. Cambiar el tema cambia el juego entero
 * sin tocar una sola linea del motor.
 *
 * Criterio de color del terreno: el suelo pertenece al mismo paisaje que el
 * fondo (cesped sobre tierra, roca sobre cueva, banqueta sobre asfalto), y el
 * pozo es SIEMPRE mucho mas oscuro que cualquier cosa pisable. Ese contraste
 * es lo que hace que el hueco se lea al instante mientras corres.
 */
data class LevelTheme(
    val id: String,
    val skyTop: Color,
    val skyBottom: Color,
    val far: Color,
    val near: Color,

    // ---- Terreno ----
    /** Banda superior: cesped, musgo o banqueta. Es lo que pisa el jugador. */
    val groundTop: Color,
    /** Cuerpo del terreno: tierra, roca o asfalto. */
    val groundFill: Color,
    /** Vetas diagonales dentro del terreno. */
    val groundHatch: Color,
    /** Parte honda del terreno, mas oscura. Tambien se usa en las paredes del pozo. */
    val groundDeep: Color,
    /** Linea de borde superior. Tiene que ser la mas contrastada: ahi se aterriza. */
    val groundEdge: Color,

    // ---- Pozos ----
    val pitTop: Color,
    val pitBottom: Color,
    /** Color de aviso en el labio del pozo. */
    val pitWarn: Color,

    // ---- Obstaculos ----
    val blockFill: Color,
    val obstacleFill: Color,
    val obstacleEdge: Color,

    val decor: Color,
    val kind: SkylineKind,
    /** true = decoracion tipo estrellas (puntos fijos); false = nubes */
    val starField: Boolean = false
) {
    companion object {

        val PRADERA = LevelTheme(
            id = "pradera",
            skyTop = Color(0xFF8FD3F4),
            skyBottom = Color(0xFFD9F2FB),
            far = Color(0xFF7FB77E),
            near = Color(0xFF4E8D5B),

            groundTop = Color(0xFF5FA968),
            groundFill = Color(0xFF9C7248),
            groundHatch = Color(0xFF89613C),
            groundDeep = Color(0xFF6B4A2F),
            groundEdge = Color(0xFF15171A),

            pitTop = Color(0xFF2A2018),
            pitBottom = Color(0xFF120C08),
            pitWarn = Color(0xFFF5C63A),

            blockFill = Color(0xFFF6FBF4),
            obstacleFill = Color(0xFF5A6470),
            obstacleEdge = Color(0xFF15171A),

            decor = Color(0xFFFFFFFF),
            kind = SkylineKind.HILLS,
            starField = false
        )

        val CUEVA = LevelTheme(
            id = "cueva",
            skyTop = Color(0xFF1B1035),
            skyBottom = Color(0xFF3B2A63),
            far = Color(0xFF2A1C4A),
            near = Color(0xFF171029),

            groundTop = Color(0xFF1E6F6A),
            groundFill = Color(0xFF3A2B5F),
            groundHatch = Color(0xFF4C3A78),
            groundDeep = Color(0xFF231640),
            groundEdge = Color(0xFF00E5C0),

            pitTop = Color(0xFF120B22),
            pitBottom = Color(0xFF06040F),
            pitWarn = Color(0xFF00E5C0),

            blockFill = Color(0xFF6D4FA8),
            obstacleFill = Color(0xFF8E6FC4),
            obstacleEdge = Color(0xFF00E5C0),

            decor = Color(0xFF9AF2E3),
            kind = SkylineKind.CAVE,
            starField = true
        )

        val CIUDAD = LevelTheme(
            id = "ciudad",
            skyTop = Color(0xFF2B2D6B),
            skyBottom = Color(0xFFF58A5B),
            far = Color(0xFF4A3C74),
            near = Color(0xFF241E42),

            groundTop = Color(0xFF4A4470),
            groundFill = Color(0xFF332E52),
            groundHatch = Color(0xFF433C68),
            groundDeep = Color(0xFF241F3D),
            groundEdge = Color(0xFFFFC857),

            pitTop = Color(0xFF120F22),
            pitBottom = Color(0xFF06050D),
            pitWarn = Color(0xFFFFC857),

            blockFill = Color(0xFF8A93A6),
            obstacleFill = Color(0xFF6B7280),
            obstacleEdge = Color(0xFFFFC857),

            decor = Color(0xFFFFE9A8),
            kind = SkylineKind.CITY,
            starField = true
        )

        /** Tema por defecto para el modo infinito y para niveles sin campo "theme". */
        val DEFAULT = PRADERA

        fun byId(id: String?): LevelTheme = when (id?.lowercase()) {
            "cueva", "cave" -> CUEVA
            "ciudad", "city" -> CIUDAD
            "pradera", "hills" -> PRADERA
            else -> DEFAULT
        }
    }
}
