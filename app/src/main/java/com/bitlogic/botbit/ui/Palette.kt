package com.bitlogic.botbit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Modo de color elegido en Ajustes.
 *  - SISTEMA: sigue el modo oscuro del telefono.
 *  - CLARO / OSCURO: fijo, sin importar el telefono.
 */
enum class ThemeMode(val label: String) {
    SISTEMA("SISTEMA"),
    CLARO("CLARO"),
    OSCURO("OSCURO");

    companion object {
        fun from(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SISTEMA
    }
}

/**
 * Colores de la interfaz, con version clara y oscura.
 *
 * COMO FUNCIONA EL CAMBIO DE TEMA
 * -------------------------------
 * [isDark] es estado de Compose. Los colores que cambian con el tema son
 * getters que lo leen, asi que al cambiar [isDark] todo lo que uso esos
 * colores se vuelve a pintar solo. Por eso las pantallas siguen escribiendo
 * `Palette.Bg`, `Palette.Ink`, etc. sin cambios.
 *
 * Los colores de acento (amarillos, rojo, verde, azul...) son iguales en ambos
 * modos: son la identidad de BotBit y se leen bien sobre claro y oscuro.
 *
 * El mundo del juego (Canvas) NO cambia de tema: usa [Light] porque el cielo y
 * el suelo vienen de LevelTheme y siempre son claros.
 */
object Palette {

    /** true = interfaz oscura. Lo fija MainActivity segun el ajuste guardado. */
    var isDark by mutableStateOf(false)

    /** Colores del modo claro. Tambien los usa el renderizador del juego. */
    object Light {
        val Bg = Color(0xFFF2F3F5)
        val Surface = Color(0xFFFFFFFF)
        val Ink = Color(0xFF15171A)
        val Grey = Color(0xFF8E949C)
        val Track = Color(0xFFD8DBDF)
        val Muted = Color(0xFF6B7280)
        val Scrim = Color(0xCCF2F3F5)
        val LightBg = Color(0xFFF9FAFB)
        val LightGrey = Color(0xFFE5E7EB)
        val YellowBg = Color(0xFFFFFEE6)
        val Black = Color(0xFF111827)
    }

    /** Colores del modo oscuro. */
    object Dark {
        val Bg = Color(0xFF121317)
        val Surface = Color(0xFF1E2027)
        val Ink = Color(0xFFECEDEE)
        val Grey = Color(0xFF9AA0A8)
        val Track = Color(0xFF3A3E47)
        val Muted = Color(0xFFA1A7B0)
        val Scrim = Color(0xE6121317)
        val LightBg = Color(0xFF181A1F)
        val LightGrey = Color(0xFF2C2F36)
        val YellowBg = Color(0xFF2E2A12)
        /** En oscuro los bordes y textos "negros" pasan a claros. */
        val Black = Color(0xFFECEDEE)
    }

    // ---- Colores que cambian con el tema ----
    val Bg get() = if (isDark) Dark.Bg else Light.Bg
    val Surface get() = if (isDark) Dark.Surface else Light.Surface
    val Ink get() = if (isDark) Dark.Ink else Light.Ink
    val Grey get() = if (isDark) Dark.Grey else Light.Grey
    val Track get() = if (isDark) Dark.Track else Light.Track
    val Muted get() = if (isDark) Dark.Muted else Light.Muted
    val Scrim get() = if (isDark) Dark.Scrim else Light.Scrim
    val LightBg get() = if (isDark) Dark.LightBg else Light.LightBg
    val LightGrey get() = if (isDark) Dark.LightGrey else Light.LightGrey
    val YellowBg get() = if (isDark) Dark.YellowBg else Light.YellowBg
    /** Texto y bordes gruesos del estilo cartoon. */
    val Black get() = if (isDark) Dark.Black else Light.Black

    /**
     * Texto sobre botones amarillos. Siempre oscuro: texto claro sobre
     * amarillo no se lee, sin importar el tema.
     */
    val OnAccent = Color(0xFF111827)

    // ---- Acentos: iguales en ambos modos ----
    val Yellow = Color(0xFFF5C63A)
    val YellowDeep = Color(0xFFD9A81F)
    val Blue = Color(0xFF2563EB)
    val Red = Color(0xFFEF4444)
    val Green = Color(0xFF10B981)
    val Purple = Color(0xFF6B21A8)
    val Orange = Color(0xFFF97316)
    val LightYellow = Color(0xFFFCD34D)
    val DarkYellow = Color(0xFFFFCB05)
    val White = Color(0xFFFFFFFF)
}
