package com.bitlogic.botbit.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fondo con parallax de tres capas, generado con formas (sin PNG).
 *
 * Regla de rendimiento: las rutas se construyen UNA sola vez y se reutilizan.
 * Solo se reconstruyen si cambia el tamano de la pantalla o el tema.
 * Crear un Path por frame es lo que genera basura y provoca tirones.
 */
class BackgroundRenderer {

    private var builtThemeId = ""
    private var builtWidth = 0f
    private var builtGround = 0f

    private val farPath = Path()
    private val nearPath = Path()
    private var patternPx = 0f

    private var skyBrush: Brush? = null
    private var skyHeight = 0f

    // Decoracion (estrellas o nubes) pregenerada en coordenadas del patron
    private val decorX = FloatArray(DECOR_COUNT)
    private val decorY = FloatArray(DECOR_COUNT)
    private val decorR = FloatArray(DECOR_COUNT)

    fun draw(
        scope: DrawScope,
        theme: LevelTheme,
        scrollX: Float,
        tile: Float,
        groundY: Float,
        elapsed: Float
    ) = with(scope) {

        build(theme, size.width, groundY, tile)

        // ---- Capa 0: cielo ----
        skyBrush?.let { drawRect(it, Offset.Zero, Size(size.width, groundY)) }

        // ---- Capa 1: decoracion lejana (estrellas fijas o nubes que flotan) ----
        val decorSpeed = if (theme.starField) 0.04f else 0.12f
        drawRepeating(scrollX * decorSpeed * tile) { dx ->
            translate(dx, 0f) {
                for (i in 0 until DECOR_COUNT) {
                    if (theme.starField) {
                        // Parpadeo sutil, desfasado por indice
                        val twinkle = 0.55f + 0.45f * sin((elapsed * 1.7f + i * 1.3f).toDouble()).toFloat()
                        drawCircle(
                            color = theme.decor.copy(alpha = 0.25f + 0.55f * twinkle),
                            radius = decorR[i],
                            center = Offset(decorX[i], decorY[i])
                        )
                    } else {
                        val bob = sin((elapsed * 0.5f + i * 0.9f).toDouble()).toFloat() * tile * 0.10f
                        val c = Offset(decorX[i], decorY[i] + bob)
                        drawCircle(theme.decor.copy(alpha = 0.85f), decorR[i], c)
                        drawCircle(
                            theme.decor.copy(alpha = 0.85f),
                            decorR[i] * 0.75f,
                            Offset(c.x + decorR[i] * 0.9f, c.y + decorR[i] * 0.2f)
                        )
                        drawCircle(
                            theme.decor.copy(alpha = 0.85f),
                            decorR[i] * 0.65f,
                            Offset(c.x - decorR[i] * 0.85f, c.y + decorR[i] * 0.25f)
                        )
                    }
                }
            }
        }

        // ---- Capa 2: siluetas lejanas (15% de la velocidad) ----
        drawRepeating(scrollX * 0.15f * tile) { dx ->
            translate(dx, 0f) { drawPath(farPath, theme.far) }
        }

        // ---- Capa 3: siluetas cercanas (38% de la velocidad) ----
        drawRepeating(scrollX * 0.38f * tile) { dx ->
            translate(dx, 0f) { drawPath(nearPath, theme.near) }
        }
    }

    /**
     * Dibuja el bloque suficientes veces para cubrir la pantalla.
     * Cuando una copia sale por la izquierda, la siguiente ya entro por la derecha:
     * ese es todo el truco del scroll infinito.
     */
    private inline fun DrawScope.drawRepeating(rawOffset: Float, block: (Float) -> Unit) {
        if (patternPx <= 0f) return
        val offset = -(rawOffset % patternPx)
        val copies = ceil(size.width / patternPx).toInt() + 2
        for (i in 0 until copies) {
            block(offset + i * patternPx)
        }
    }

    private fun build(theme: LevelTheme, width: Float, groundY: Float, tile: Float) {
        if (theme.id == builtThemeId && width == builtWidth && groundY == builtGround) return

        builtThemeId = theme.id
        builtWidth = width
        builtGround = groundY
        skyHeight = groundY

        skyBrush = Brush.verticalGradient(
            colors = listOf(theme.skyTop, theme.skyBottom),
            startY = 0f,
            endY = groundY
        )

        // El patron mide 20 tiles. Debe empezar y terminar en la misma altura
        // para que las copias encajen sin costura visible.
        patternPx = tile * 20f

        val rng = Random(theme.id.hashCode())

        farPath.reset()
        nearPath.reset()

        when (theme.kind) {
            SkylineKind.HILLS -> {
                buildHills(farPath, patternPx, groundY, groundY * 0.42f, 5, rng)
                buildHills(nearPath, patternPx, groundY, groundY * 0.24f, 7, rng)
            }
            SkylineKind.CAVE -> {
                buildCave(farPath, patternPx, groundY, groundY * 0.34f, 9, rng)
                buildCave(nearPath, patternPx, groundY, groundY * 0.20f, 13, rng)
            }
            SkylineKind.CITY -> {
                buildCity(farPath, patternPx, groundY, groundY * 0.50f, 8, rng)
                buildCity(nearPath, patternPx, groundY, groundY * 0.30f, 12, rng)
            }
        }

        // Decoracion
        for (i in 0 until DECOR_COUNT) {
            decorX[i] = rng.nextFloat() * patternPx
            decorY[i] = groundY * (0.06f + rng.nextFloat() * 0.42f)
            decorR[i] = if (theme.starField) {
                tile * (0.025f + rng.nextFloat() * 0.035f)
            } else {
                tile * (0.18f + rng.nextFloat() * 0.16f)
            }
        }
    }

    // ---------- Generadores de silueta ----------

    /**
     * Colinas por segmentos en vez de curvas Bezier: la ruta se construye una
     * sola vez, asi que 12 segmentos por loma no cuestan nada y evitamos
     * depender de APIs de Path que cambian entre versiones de Compose.
     */
    private fun buildHills(path: Path, w: Float, baseY: Float, amp: Float, bumps: Int, rng: Random) {
        path.moveTo(0f, baseY)
        val step = w / bumps
        val segments = 12
        for (i in 0 until bumps) {
            val x0 = i * step
            val h = amp * (0.55f + rng.nextFloat() * 0.45f)
            for (j in 1..segments) {
                val f = j.toFloat() / segments
                // media onda de seno: sube y baja volviendo exactamente a baseY
                val y = baseY - h * sin((f * Math.PI.toFloat()).toDouble()).toFloat()
                path.lineTo(x0 + step * f, y)
            }
        }
        path.lineTo(w, baseY)
        path.close()
    }

    private fun buildCave(path: Path, w: Float, baseY: Float, amp: Float, spikes: Int, rng: Random) {
        path.moveTo(0f, baseY)
        val step = w / spikes
        for (i in 0 until spikes) {
            val x0 = i * step
            val h = amp * (0.35f + rng.nextFloat() * 0.65f)
            path.lineTo(x0 + step / 2f, baseY - h)
            path.lineTo(x0 + step, baseY)
        }
        path.lineTo(w, baseY)
        path.close()
    }

    private fun buildCity(path: Path, w: Float, baseY: Float, amp: Float, count: Int, rng: Random) {
        path.moveTo(0f, baseY)
        val step = w / count
        for (i in 0 until count) {
            val x0 = i * step
            val gap = step * 0.12f
            val h = amp * (0.30f + rng.nextFloat() * 0.70f)
            path.lineTo(x0 + gap, baseY)
            path.lineTo(x0 + gap, baseY - h)
            // Antena en algunos edificios
            if (rng.nextInt(4) == 0) {
                val mid = x0 + step / 2f
                path.lineTo(mid - step * 0.04f, baseY - h)
                path.lineTo(mid, baseY - h - amp * 0.22f)
                path.lineTo(mid + step * 0.04f, baseY - h)
            }
            path.lineTo(x0 + step - gap, baseY - h)
            path.lineTo(x0 + step - gap, baseY)
        }
        path.lineTo(w, baseY)
        path.close()
    }

    private companion object {
        const val DECOR_COUNT = 16
    }
}
