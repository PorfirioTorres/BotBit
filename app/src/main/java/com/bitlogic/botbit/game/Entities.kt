package com.bitlogic.botbit.game

/**
 * Rectangulo alineado a los ejes, en unidades del mundo (tiles).
 * OJO: el eje Y crece hacia ARRIBA. El suelo esta en y = 0.
 */
data class AABB(
    val left: Float,
    val bottom: Float,
    val width: Float,
    val height: Float
) {
    val right: Float get() = left + width
    val top: Float get() = bottom + height

    fun overlaps(other: AABB): Boolean =
        left < other.right && right > other.left &&
        bottom < other.top && top > other.bottom

    /** Devuelve el mismo rectangulo encogido hacia su centro. */
    fun shrink(factor: Float): AABB {
        val nw = width * factor
        val nh = height * factor
        return AABB(
            left = left + (width - nw) / 2f,
            bottom = bottom + (height - nh) / 2f,
            width = nw,
            height = nh
        )
    }
}

enum class ObstacleType {
    /** Triangulo en el suelo. Contacto = muerte. */
    SPIKE,

    /** Cubo solido. Se puede aterrizar encima; tocarlo de lado = muerte. */
    BLOCK,

    RECTANGLE,

    /** Plataforma elevada. Mismas reglas que BLOCK. */
    PLATFORM,

    /** Coleccionable. No mata, suma puntos. */
    COIN


}

class Obstacle(
    val type: ObstacleType,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
) {
    var collected: Boolean = false

    val right: Float get() = x + w

    fun bounds(): AABB = AABB(x, y, w, h)

    /** Copia sin estado, para reiniciar el nivel sin recargar el JSON. */
    fun freshCopy(): Obstacle = Obstacle(type, x, y, w, h)
}

/** Tramo sin suelo. Si el jugador esta encima y no salta, cae y muere. */
class Gap(val x: Float, val w: Float) {
    val right: Float get() = x + w
}

class Player {
    var y = 0f
    var vy = 0f
    var onGround = true
    var rotation = 0f

    fun reset() {
        y = 0f
        vy = 0f
        onGround = true
        rotation = 0f
    }
}
