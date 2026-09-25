package com.bitlogic.botbit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.ArenaWorld
import com.bitlogic.botbit.game.TowerWorld

/**
 * HUD de LA TORRE.
 *
 * Muestra altura y sala en vez de la barra de progreso del Runner. En un juego
 * donde caerse cuesta altura, el numero que importa es cuanto subiste, no un
 * porcentaje de recorrido.
 */
@Composable
fun TowerHud(world: TowerWorld, paused: Boolean, onTogglePause: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Palette.Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "SALA ${world.room + 1}",
                fontSize = 10.sp, fontWeight = FontWeight.Black,
                color = Palette.Muted, letterSpacing = 1.sp
            )
            Text(
                "${world.altura.toInt()} m",
                fontSize = 22.sp, fontWeight = FontWeight.Black, color = Palette.Black
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "RECORD",
                fontSize = 9.sp, fontWeight = FontWeight.Black,
                color = Palette.Muted, letterSpacing = 1.sp
            )
            Text(
                "${world.record.toInt()} m",
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Palette.Blue
            )
        }

        PauseBox(paused, onTogglePause)
    }
}

/**
 * HUD de la ARENA.
 *
 * Corazones, tiempo sobrevivido y barra de experiencia. El tiempo es la
 * puntuacion real del genero: no importa cuantos mates, importa cuanto duras.
 */
@Composable
fun ArenaHud(world: ArenaWorld, paused: Boolean, onTogglePause: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Barra de experiencia hacia el siguiente nivel
        Canvas(Modifier.fillMaxWidth().height(8.dp)) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(Palette.Track, Offset.Zero, size, r)
            if (world.progress > 0f) {
                drawRoundRect(
                    Palette.Blue, Offset.Zero,
                    Size(size.width * world.progress, size.height), r
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Corazones
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(ArenaWorld.MAX_HEARTS) { i ->
                    Canvas(Modifier.size(18.dp)) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2.4f
                        val lleno = i < world.hearts
                        drawCircle(if (lleno) Palette.Red else Palette.Track, r, c)
                        drawCircle(Palette.Black, r, c, style = Stroke(2f))
                    }
                    Spacer(Modifier.width(5.dp))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TIEMPO",
                    fontSize = 9.sp, fontWeight = FontWeight.Black,
                    color = Palette.Muted, letterSpacing = 1.sp
                )
                Text(
                    formatoTiempo(world.elapsed),
                    fontSize = 18.sp, fontWeight = FontWeight.Black, color = Palette.Black
                )
            }

            PauseBox(paused, onTogglePause)
        }
    }
}

private fun formatoTiempo(segundos: Float): String {
    val t = segundos.toInt()
    return "%d:%02d".format(t / 60, t % 60)
}

@Composable
private fun PauseBox(paused: Boolean, onTogglePause: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Palette.Surface)
            .border(2.dp, Palette.Black, RoundedCornerShape(10.dp))
            .clickable { onTogglePause() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (paused) "\u25B6" else "\u23F8",
            fontSize = 13.sp, fontWeight = FontWeight.Black, color = Palette.Black
        )
    }
}
