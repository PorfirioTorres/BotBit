package com.bitlogic.botbit.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import com.bitlogic.botbit.game.GameConfig
import com.bitlogic.botbit.game.ObstacleType
import com.bitlogic.botbit.game.World

fun DrawScope.drawWorld(world: World) {
    val tile = size.width / GameConfig.TILES_VISIBLE_X
    val groundY = size.height * GameConfig.GROUND_SCREEN_RATIO
    val stroke = tile * 0.075f

    fun sx(worldX: Float): Float = (worldX - world.scrollX) * tile
    fun sy(worldY: Float): Float = groundY - worldY * tile

    val from = world.scrollX - 1f
    val to = world.scrollX + GameConfig.TILES_VISIBLE_X + 1f

    // ---- Suelo ----
    var cursor = from
    for (g in world.gaps) {
        if (g.right <= from) continue
        if (g.x >= to) break
        if (g.x > cursor) drawGround(sx(cursor), sx(g.x), groundY, stroke)
        if (g.right > cursor) cursor = g.right
    }
    if (cursor < to) drawGround(sx(cursor), sx(to), groundY, stroke)

    // ---- Obstaculos ----
    for (ob in world.obstacles) {
        if (ob.right < from) continue
        if (ob.x > to) break

        when (ob.type) {
            ObstacleType.SPIKE -> {
                val path = Path().apply {
                    moveTo(sx(ob.x), sy(ob.y))
                    lineTo(sx(ob.x + ob.w / 2f), sy(ob.y + ob.h))
                    lineTo(sx(ob.right), sy(ob.y))
                    close()
                }
                drawPath(path, Palette.Grey)
                drawPath(path, Palette.Ink, style = Stroke(stroke))
            }

            ObstacleType.BLOCK -> {
                val topLeft = Offset(sx(ob.x), sy(ob.y + ob.h))
                val boxSize = Size(ob.w * tile, ob.h * tile)
                val radius = CornerRadius(tile * 0.12f, tile * 0.12f)
                drawRoundRect(Palette.Surface, topLeft, boxSize, radius)
                drawRoundRect(Palette.Ink, topLeft, boxSize, radius, style = Stroke(stroke))
            }

            ObstacleType.PLATFORM -> {
                val topLeft = Offset(sx(ob.x), sy(ob.y + ob.h))
                val boxSize = Size(ob.w * tile, ob.h * tile)
                drawRect(Palette.Surface, topLeft, boxSize)
                drawRect(Palette.Ink, topLeft, Size(boxSize.width, stroke * 1.4f))
                drawRect(Palette.Ink, topLeft, boxSize, style = Stroke(stroke * 0.7f))
            }

            // MONEDAS
            ObstacleType.COIN -> {
                if (!ob.collected) {
                    val cx = sx(ob.x + ob.w / 2f)
                    val cy = sy(ob.y + ob.h / 2f)
                    val r = ob.w * tile / 2f
                    // Moneda dorada
                    drawCircle(Palette.Yellow, r, Offset(cx, cy))
                    drawLine(
                        Palette.Ink,
                        Offset(cx - r, cy),
                        Offset(cx + r, cy),
                        strokeWidth = stroke * 0.9f
                    )
                    drawCircle(Palette.Ink, r, Offset(cx, cy), style = Stroke(stroke * 0.9f))
                    drawCircle(Palette.Surface, r * 0.26f, Offset(cx, cy))
                    drawCircle(Palette.Ink, r * 0.26f, Offset(cx, cy), style = Stroke(stroke * 0.6f))
                }
            }

            else -> {}
        }
    }

    // ---- Jugador (Robot) ----
    val p = world.player
    val s = GameConfig.PLAYER_SIZE * tile
    val left = GameConfig.PLAYER_X * tile
    val top = sy(p.y) - s
    val cx = left + s / 2f
    val cy = top + s / 2f
    
    val robotColor = world.characterColor

    rotate(degrees = p.rotation, pivot = Offset(cx, cy)) {
        // Antenas
        drawRect(Palette.Ink, Offset(left + s * 0.15f, top - s * 0.30f), Size(s * 0.06f, s * 0.30f))
        drawCircle(Palette.Red, s * 0.08f, Offset(left + s * 0.18f, top - s * 0.32f))
        drawRect(Palette.Ink, Offset(left + s * 0.79f, top - s * 0.30f), Size(s * 0.06f, s * 0.30f))
        drawCircle(Palette.Blue, s * 0.08f, Offset(left + s * 0.82f, top - s * 0.32f))

        // Cuerpo del robot
        val radius = CornerRadius(s * 0.22f, s * 0.22f)
        drawRoundRect(robotColor, Offset(left, top), Size(s, s), radius)
        drawRoundRect(Palette.Ink, Offset(left, top), Size(s, s), radius, style = Stroke(stroke * 1.3f))

        // Ojos LED
        drawRect(Palette.Surface, Offset(left + s * 0.20f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.22f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))
        drawRect(Palette.Surface, Offset(left + s * 0.65f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.67f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))

        // Línea de boca LED
        drawRect(Palette.Surface, Offset(left + s * 0.35f, top + s * 0.60f), Size(s * 0.30f, s * 0.06f))
        drawRect(Palette.Ink, Offset(left + s * 0.36f, top + s * 0.61f), Size(s * 0.28f, s * 0.04f))
        
        // Detalles metálicos
        drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.10f, top + s * 0.10f))
        drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.90f, top + s * 0.10f))
        drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.10f, top + s * 0.90f))
        drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.90f, top + s * 0.90f))
    }
}

private fun DrawScope.drawGround(left: Float, right: Float, groundY: Float, stroke: Float) {
    val width = right - left
    if (width <= 0f) return
    val depth = size.height - groundY

    drawRect(Palette.Surface, Offset(left, groundY), Size(width, depth))

    clipRect(left = left, top = groundY, right = right, bottom = size.height) {
        var x = left - depth
        while (x < right + depth) {
            drawLine(
                Palette.Track,
                Offset(x, size.height),
                Offset(x + depth, groundY),
                strokeWidth = stroke * 0.7f
            )
            x += stroke * 11f
        }
    }

    drawRect(Palette.Ink, Offset(left, groundY - stroke), Size(width, stroke))
}
