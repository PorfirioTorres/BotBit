package com.bitlogic.botbit.data

import androidx.compose.ui.graphics.Color
import com.bitlogic.botbit.ui.Palette

data class CharacterData(
    val id: String,
    val name: String,
    val type: String,
    val color: Color,
    val isClassic: Boolean = false,
    val locked: Boolean = false,
    val price: Int = 0,
    val stats: Map<String, Int> = mapOf("VELOCIDAD" to 3, "SALTO" to 4)
)

object Characters {
    val all = listOf(
        CharacterData(
            id = "classic",
            name = "BotBit Classic",
            type = "TIPO ROBOT / BASE",
            color = Palette.DarkYellow,
            isClassic = true,
            stats = mapOf("VELOCIDAD" to 3, "SALTO" to 4)
        ),
        CharacterData(
            id = "pyro",
            name = "BotBit Pyro",
            type = "TIPO FUEGO",
            color = Palette.Red,
            locked = true,
            price = 50,
            stats = mapOf("VELOCIDAD" to 4, "SALTO" to 3)
        ),
        CharacterData(
            id = "aqua",
            name = "BotBit Aqua",
            type = "TIPO AGUA",
            color = Palette.Blue,
            locked = true,
            price = 75,
            stats = mapOf("VELOCIDAD" to 3, "SALTO" to 5)
        ),
        CharacterData(
            id = "terra",
            name = "BotBit Terra",
            type = "TIPO TIERRA",
            color = Palette.Green,
            locked = true,
            price = 100,
            stats = mapOf("VELOCIDAD" to 4, "SALTO" to 4)
        ),
        CharacterData(
            id = "volt",
            name = "BotBit Volt",
            type = "TIPO ELÉCTRICO",
            color = Palette.Yellow,
            locked = true,
            price = 150,
            stats = mapOf("VELOCIDAD" to 5, "SALTO" to 3)
        ),
        CharacterData(
            id = "shadow",
            name = "BotBit Shadow",
            type = "TIPO SOMBRA",
            color = Palette.Purple,
            locked = true,
            price = 200,
            stats = mapOf("VELOCIDAD" to 2, "SALTO" to 5)
        )
    )
}
