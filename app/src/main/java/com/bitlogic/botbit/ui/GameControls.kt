package com.bitlogic.botbit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.InputEvent
import kotlin.math.hypot
import kotlin.math.min

/** Cómo se dibuja el joystick de la Arena. Lo elige el jugador en Ajustes. */
enum class JoystickMode { FLOTANTE, FIJO }

/**
 * Controles de LA TORRE: dos botones abajo.
 *
 * Mantener un botón carga el salto; soltarlo lo ejecuta hacia ese lado.
 * Tocar el centro carga y salta en vertical, que en una torre en zigzag se
 * necesita seguido.
 *
 * La carga se maneja con Press/Release y no con un tap, porque la fuerza del
 * salto es proporcional al tiempo que se mantiene.
 */
@Composable
fun TowerControls(
    chargeRatio: Float,
    onInput: (InputEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {

        // Barra de carga sobre los botones
        if (chargeRatio > 0f) {
            androidx.compose.foundation.Canvas(
                Modifier.fillMaxWidth().size(8.dp).align(Alignment.TopCenter)
            ) {
                drawRoundRect(
                    Palette.Track, Offset.Zero, size,
                    androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                )
                drawRoundRect(
                    if (chargeRatio > 0.92f) Palette.Red else Palette.DarkYellow,
                    Offset.Zero,
                    androidx.compose.ui.geometry.Size(size.width * chargeRatio, size.height),
                    androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChargeButton("◀", dirX = -1f, onInput = onInput)
            ChargeButton("▲", dirX = 0f, onInput = onInput, small = true)
            ChargeButton("▶", dirX = 1f, onInput = onInput)
        }
    }
}

@Composable
private fun ChargeButton(
    label: String,
    dirX: Float,
    onInput: (InputEvent) -> Unit,
    small: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val side = if (small) 62.dp else 84.dp

    Box(
        Modifier
            .size(side)
            .clip(CircleShape)
            .background(if (pressed) Palette.DarkYellow else Palette.Surface)
            .border(3.dp, Palette.Black, CircleShape)
            // awaitEachGesture en vez de detectTapGestures: aquí importa cuánto
            // tiempo se mantiene el dedo, no si hubo un tap.
            .pointerInput(dirX) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    onInput(InputEvent.Press)
                    do {
                        val ev = awaitPointerEvent()
                    } while (ev.changes.any { it.pressed })
                    pressed = false
                    onInput(InputEvent.Release(dirX))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (small) 22.sp else 30.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )
    }
}

/**
 * Joystick virtual de la ARENA. Dos modos, elegidos por el jugador:
 *
 *  FLOTANTE: aparece donde pongas el dedo. Funciona igual en un celular chico
 *            que en una tablet, porque no depende de dónde alcance el pulgar.
 *  FIJO:     siempre en la esquina inferior izquierda. Más predecible si
 *            juegas con el teléfono apoyado.
 *
 * Emite InputEvent.Move con la dirección normalizada a -1..1, que es lo que
 * ArenaWorld espera.
 */
@Composable
fun VirtualJoystick(
    mode: JoystickMode,
    onInput: (InputEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var center by remember { mutableStateOf<Offset?>(null) }
    var knob by remember { mutableStateOf(Offset.Zero) }
    var active by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(mode) {
                val radiusPx = 44.dp.toPx()
                val fixedCenter = Offset(
                    x = 24.dp.toPx() + radiusPx,
                    y = size.height - 24.dp.toPx() - radiusPx
                )
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val origin = if (mode == JoystickMode.FLOTANTE) down.position else fixedCenter
                    center = origin
                    active = true
                    knob = Offset.Zero
                    onInput(InputEvent.Move(0f, 0f))

                    do {
                        val ev = awaitPointerEvent()
                        val pos = ev.changes.first().position
                        var d = pos - origin
                        val len = hypot(d.x, d.y)
                        if (len > radiusPx) d = d * (radiusPx / len)
                        knob = d
                        onInput(
                            InputEvent.Move(
                                (d.x / radiusPx).coerceIn(-1f, 1f),
                                (d.y / radiusPx).coerceIn(-1f, 1f)
                            )
                        )
                        ev.changes.forEach { it.consume() }
                    } while (ev.changes.any { it.pressed })

                    active = false
                    knob = Offset.Zero
                    if (mode == JoystickMode.FLOTANTE) center = null
                    onInput(InputEvent.Move(0f, 0f))   // soltar = detenerse
                }
            }
    ) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val radiusPx = 44.dp.toPx()
            val origin = center ?: if (mode == JoystickMode.FIJO) {
                Offset(24.dp.toPx() + radiusPx, size.height - 24.dp.toPx() - radiusPx)
            } else null

            if (origin != null) {
                val alpha = if (active) 0.55f else 0.22f
                drawCircle(Palette.Black.copy(alpha = alpha * 0.35f), radiusPx, origin)
                drawCircle(
                    Palette.Black.copy(alpha = alpha), radiusPx, origin,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    Palette.DarkYellow.copy(alpha = min(1f, alpha + 0.35f)),
                    radiusPx * 0.42f, origin + knob
                )
                drawCircle(
                    Palette.Black.copy(alpha = alpha + 0.2f),
                    radiusPx * 0.42f, origin + knob,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

/** Indicador de modo para el HUD, reutilizable. */
@Composable
fun ModeBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Surface)
            .border(2.dp, Palette.Black, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Palette.Black)
    }
}
