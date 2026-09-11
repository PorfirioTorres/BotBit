package com.bitlogic.botbit.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.bitlogic.botbit.game.GameConfig
import com.bitlogic.botbit.game.ObstacleType
import com.bitlogic.botbit.game.World
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dibuja el mundo completo en un solo Canvas.
 *
 * Todo lo que se anima aqui es COSMETICO. El hitbox, la gravedad y las
 * colisiones no cambian: el verificador de niveles sigue siendo valido.
 */
fun DrawScope.drawWorld(
    world: World,
    tilesVisibleX: Float = GameConfig.TILES_VISIBLE_X,
    theme: LevelTheme = LevelTheme.DEFAULT,
    scratch: RenderScratch
) {
    val tile = size.width / tilesVisibleX
    val groundY = size.height * GameConfig.GROUND_SCREEN_RATIO
    val stroke = tile * 0.075f
    val t = world.elapsed

    fun sx(worldX: Float): Float = (worldX - world.scrollX) * tile
    fun sy(worldY: Float): Float = groundY - worldY * tile

    val from = world.scrollX - 1f
    val to = world.scrollX + tilesVisibleX + 1f

    // ---- Fondo con parallax ----
    scratch.background.draw(this, theme, world.scrollX, tile, groundY, t)

    // ---- Suelo y pozos ----
    var cursor = from
    for (g in world.gaps) {
        if (g.right <= from) continue
        if (g.x >= to) break
        if (g.x > cursor) drawGround(sx(cursor), sx(g.x), groundY, stroke, theme, tile)
        // El pozo se recorta a la parte visible para no dibujar de mas
        val pitL = if (g.x < from) from else g.x
        val pitR = if (g.right > to) to else g.right
        drawPit(sx(pitL), sx(pitR), groundY, stroke, theme, tile, g.x >= from, g.right <= to, scratch)
        if (g.right > cursor) cursor = g.right
    }
    if (cursor < to) drawGround(sx(cursor), sx(to), groundY, stroke, theme, tile)

    // ---- Obstaculos ----
    for (ob in world.obstacles) {
        if (ob.right < from) continue
        if (ob.x > to) break

        when (ob.type) {
            ObstacleType.SPIKE -> {
                val path = scratch.path
                path.reset()
                path.moveTo(sx(ob.x), sy(ob.y))
                path.lineTo(sx(ob.x + ob.w / 2f), sy(ob.y + ob.h))
                path.lineTo(sx(ob.right), sy(ob.y))
                path.close()
                drawPath(path, theme.obstacleFill)
                drawPath(path, theme.obstacleEdge, style = Stroke(stroke))
                // Brillo en la punta: hace que el peligro se lea al instante
                drawCircle(
                    theme.obstacleEdge.copy(alpha = 0.35f + 0.35f * sin((t * 4f + ob.x).toDouble()).toFloat()),
                    tile * 0.07f,
                    Offset(sx(ob.x + ob.w / 2f), sy(ob.y + ob.h))
                )
            }

            ObstacleType.BLOCK -> {
                val topLeft = Offset(sx(ob.x), sy(ob.y + ob.h))
                val boxSize = Size(ob.w * tile, ob.h * tile)
                val radius = CornerRadius(tile * 0.12f, tile * 0.12f)
                drawRoundRect(theme.blockFill, topLeft, boxSize, radius)
                drawRoundRect(theme.obstacleEdge, topLeft, boxSize, radius, style = Stroke(stroke))
                drawCircle(
                    theme.obstacleEdge.copy(alpha = 0.5f),
                    tile * 0.06f,
                    Offset(topLeft.x + boxSize.width / 2f, topLeft.y + boxSize.height / 2f)
                )
            }

            ObstacleType.PLATFORM -> {
                val topLeft = Offset(sx(ob.x), sy(ob.y + ob.h))
                val boxSize = Size(ob.w * tile, ob.h * tile)
                drawRect(theme.blockFill, topLeft, boxSize)
                drawRect(theme.obstacleEdge, topLeft, Size(boxSize.width, stroke * 1.4f))
                drawRect(theme.obstacleEdge, topLeft, boxSize, style = Stroke(stroke * 0.7f))
            }

            ObstacleType.COIN -> {
                if (!ob.collected) {
                    val phase = t * 3.4f + ob.x * 0.7f
                    val spin = abs(cos(phase.toDouble()).toFloat()).coerceAtLeast(0.14f)
                    val bob = sin((phase * 0.75f).toDouble()).toFloat() * tile * 0.10f
                    val cx = sx(ob.x + ob.w / 2f)
                    val cy = sy(ob.y + ob.h / 2f) + bob
                    val r = ob.w * tile / 2f

                    // Halo
                    drawCircle(Palette.Yellow.copy(alpha = 0.20f), r * 1.7f, Offset(cx, cy))

                    scale(spin, 1f, pivot = Offset(cx, cy)) {
                        drawCircle(Palette.Yellow, r, Offset(cx, cy))
                        drawCircle(Palette.Ink, r, Offset(cx, cy), style = Stroke(stroke * 0.9f))
                        drawCircle(Palette.Surface, r * 0.30f, Offset(cx, cy))
                        drawCircle(Palette.Ink, r * 0.30f, Offset(cx, cy), style = Stroke(stroke * 0.6f))
                    }
                }
            }

            else -> {}
        }
    }

    // ---- Polvo al aterrizar ----
    if (world.dustBurst > 0f) {
        val d = world.dustBurst
        val baseX = GameConfig.PLAYER_X * tile + GameConfig.PLAYER_SIZE * tile / 2f
        val baseY = sy(world.player.y)
        for (i in 0 until 6) {
            val dir = if (i % 2 == 0) -1f else 1f
            val spread = (1f - d) * tile * (0.6f + i * 0.16f)
            drawCircle(
                color = theme.groundTop.copy(alpha = d * 0.6f),
                radius = tile * 0.13f * d,
                center = Offset(baseX + dir * spread, baseY - tile * 0.10f - (1f - d) * tile * 0.25f)
            )
        }
    }

    // ---- Jugador (Robot) ----
    val p = world.player
    val s = GameConfig.PLAYER_SIZE * tile
    val left = GameConfig.PLAYER_X * tile
    val top = sy(p.y) - s
    val cx = left + s / 2f
    val cy = top + s / 2f

    if (p.onGround) {
        // En el suelo: aplastado y estirado. El pivote son los pies.
        val squashY = 1f - world.landImpact * 0.26f
        val squashX = 1f / squashY
        scale(squashX, squashY, pivot = Offset(cx, top + s)) {
            drawRobot(world, theme, left, top, s, stroke, t, scratch)
        }
    } else {
        // En el aire: gira, y se estira un poco segun la velocidad vertical.
        val stretch = 1f + (p.vy / GameConfig.JUMP_VELOCITY).coerceIn(-1f, 1f) * 0.10f
        rotate(degrees = p.rotation, pivot = Offset(cx, cy)) {
            scale(1f / stretch, stretch, pivot = Offset(cx, cy)) {
                drawRobot(world, theme, left, top, s, stroke, t, scratch)
            }
        }
    }
}

private fun DrawScope.drawRobot(
    world: World,
    theme: LevelTheme,
    left: Float,
    top: Float,
    s: Float,
    stroke: Float,
    t: Float,
    scratch: RenderScratch
) {
    val robotColor = world.characterColor

    // Propulsor: solo en el aire, con parpadeo rapido
    if (!world.player.onGround) {
        val flame = 0.55f + 0.45f * sin((t * 38f).toDouble()).toFloat()
        val fw = s * 0.34f
        val fh = s * 0.40f * flame
        val fx = left + s / 2f - fw / 2f
        val fy = top + s
        val flamePath = scratch.path
        flamePath.reset()
        flamePath.moveTo(fx, fy)
        flamePath.lineTo(fx + fw / 2f, fy + fh)
        flamePath.lineTo(fx + fw, fy)
        flamePath.close()
        drawPath(flamePath, Palette.Orange.copy(alpha = 0.9f))

        val inner = scratch.path2
        inner.reset()
        inner.moveTo(fx + fw * 0.28f, fy)
        inner.lineTo(fx + fw / 2f, fy + fh * 0.62f)
        inner.lineTo(fx + fw * 0.72f, fy)
        inner.close()
        drawPath(inner, Palette.LightYellow)
    }

    // Antenas con luces que alternan
    val blinkA = sin((t * 5f).toDouble()).toFloat() > 0f
    drawRect(Palette.Ink, Offset(left + s * 0.15f, top - s * 0.30f), Size(s * 0.06f, s * 0.30f))
    drawCircle(
        if (blinkA) Palette.Red else Palette.Red.copy(alpha = 0.35f),
        s * 0.08f, Offset(left + s * 0.18f, top - s * 0.32f)
    )
    drawRect(Palette.Ink, Offset(left + s * 0.79f, top - s * 0.30f), Size(s * 0.06f, s * 0.30f))
    drawCircle(
        if (!blinkA) Palette.Blue else Palette.Blue.copy(alpha = 0.35f),
        s * 0.08f, Offset(left + s * 0.82f, top - s * 0.32f)
    )

    // Cuerpo
    val radius = CornerRadius(s * 0.22f, s * 0.22f)
    drawRoundRect(robotColor, Offset(left, top), Size(s, s), radius)
    drawRoundRect(Palette.Ink, Offset(left, top), Size(s, s), radius, style = Stroke(stroke * 1.3f))

    // Ojos LED con parpadeo cada ~3.4 s
    val blinking = (t % 3.4f) < 0.12f
    if (blinking) {
        drawRect(Palette.Ink, Offset(left + s * 0.20f, top + s * 0.35f), Size(s * 0.15f, s * 0.035f))
        drawRect(Palette.Ink, Offset(left + s * 0.65f, top + s * 0.35f), Size(s * 0.15f, s * 0.035f))
    } else {
        drawRect(Palette.Surface, Offset(left + s * 0.20f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.22f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))
        drawRect(Palette.Surface, Offset(left + s * 0.65f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.67f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))
    }

    // Boca LED: barras que laten
    val pulse = 0.5f + 0.5f * sin((t * 6f).toDouble()).toFloat()
    drawRect(Palette.Surface, Offset(left + s * 0.35f, top + s * 0.60f), Size(s * 0.30f, s * 0.06f))
    drawRect(
        theme.groundEdge.copy(alpha = 0.35f + 0.65f * pulse),
        Offset(left + s * 0.36f, top + s * 0.61f), Size(s * 0.28f, s * 0.04f)
    )

    // Tornillos
    drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.10f, top + s * 0.10f))
    drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.90f, top + s * 0.10f))
    drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.10f, top + s * 0.90f))
    drawCircle(Palette.Ink, s * 0.04f, Offset(left + s * 0.90f, top + s * 0.90f))
}

/**
 * El terreno se dibuja por capas, como un corte de tierra:
 * cesped arriba, tierra con vetas en medio, y una banda mas oscura al fondo.
 * La linea de borde es la mas contrastada porque ahi es donde se aterriza.
 */
private fun DrawScope.drawGround(
    left: Float,
    right: Float,
    groundY: Float,
    stroke: Float,
    theme: LevelTheme,
    tile: Float
) {
    val width = right - left
    if (width <= 0f) return
    val depth = size.height - groundY
    val topBand = tile * 0.24f

    // Cuerpo: tierra / roca / asfalto
    drawRect(theme.groundFill, Offset(left, groundY), Size(width, depth))

    // Vetas diagonales
    clipRect(left = left, top = groundY, right = right, bottom = size.height) {
        var x = left - depth
        while (x < right + depth) {
            drawLine(
                theme.groundHatch,
                Offset(x, size.height),
                Offset(x + depth, groundY),
                strokeWidth = stroke * 0.7f
            )
            x += stroke * 11f
        }
    }

    // Zona honda
    drawRect(
        theme.groundDeep.copy(alpha = 0.6f),
        Offset(left, groundY + depth * 0.5f),
        Size(width, depth * 0.5f)
    )

    // Banda superior: cesped / musgo / banqueta
    drawRect(theme.groundTop, Offset(left, groundY), Size(width, topBand))
    drawRect(
        theme.groundDeep.copy(alpha = 0.35f),
        Offset(left, groundY + topBand),
        Size(width, stroke * 0.9f)
    )

    // Borde superior
    drawRect(theme.groundEdge, Offset(left, groundY - stroke), Size(width, stroke * 1.3f))
}

/**
 * Pozo: vacio oscuro con paredes visibles.
 *
 * El contraste hace el trabajo. El terreno es claro y texturizado; el pozo es
 * casi negro y liso. A la velocidad del juego no hay tiempo de analizar formas,
 * solo de registrar "ahi no hay piso".
 */
private fun DrawScope.drawPit(
    left: Float,
    right: Float,
    groundY: Float,
    stroke: Float,
    theme: LevelTheme,
    tile: Float,
    showLeftWall: Boolean,
    showRightWall: Boolean,
    scratch: RenderScratch
) {
    val width = right - left
    if (width <= 0f) return
    val depth = size.height - groundY

    // Vacio en tres bandas: se oscurece hacia abajo sin usar degradados
    // (un Brush por frame seria basura para el recolector).
    drawRect(theme.pitTop, Offset(left, groundY), Size(width, depth))
    drawRect(
        theme.pitBottom.copy(alpha = 0.55f),
        Offset(left, groundY + depth * 0.3f),
        Size(width, depth * 0.7f)
    )
    drawRect(
        theme.pitBottom,
        Offset(left, groundY + depth * 0.65f),
        Size(width, depth * 0.35f)
    )

    // Paredes: corte del terreno a los lados del hueco
    val wallW = (tile * 0.18f).coerceAtMost(width / 2f)
    val wallH = depth * 0.55f
    if (showLeftWall) {
        drawRect(theme.groundDeep, Offset(left, groundY), Size(wallW, wallH))
        drawRect(theme.groundTop, Offset(left, groundY), Size(wallW, tile * 0.24f))
        drawRect(theme.groundEdge, Offset(left, groundY), Size(stroke, wallH))
    }
    if (showRightWall) {
        drawRect(theme.groundDeep, Offset(right - wallW, groundY), Size(wallW, wallH))
        drawRect(theme.groundTop, Offset(right - wallW, groundY), Size(wallW, tile * 0.24f))
        drawRect(theme.groundEdge, Offset(right - stroke, groundY), Size(stroke, wallH))
    }

    // Marcas de aviso en el labio: pequenos triangulos apuntando al vacio
    val markW = tile * 0.16f
    val markH = tile * 0.13f
    val spacing = tile * 0.42f
    val warn = theme.pitWarn.copy(alpha = 0.75f)
    val path = scratch.path
    path.reset()
    var mx = left + spacing * 0.5f
    while (mx + markW < right) {
        path.moveTo(mx, groundY + stroke)
        path.lineTo(mx + markW / 2f, groundY + stroke + markH)
        path.lineTo(mx + markW, groundY + stroke)
        path.close()
        mx += spacing
    }
    drawPath(path, warn)
}
