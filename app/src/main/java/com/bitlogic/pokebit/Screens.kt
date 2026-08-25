package com.bitlogic.pokebit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    bestScore: Int,
    onPlay: () -> Unit,
    onEndless: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))

        Canvas(Modifier.size(140.dp)) {
            val r = size.minDimension / 2f - 6f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Palette.Surface, r, c)
            drawArc(
                color = Palette.Yellow,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2)
            )
            drawLine(Palette.Ink, Offset(c.x - r, c.y), Offset(c.x + r, c.y), strokeWidth = 9f)
            drawCircle(Palette.Ink, r, c, style = Stroke(9f))
            drawCircle(Palette.Surface, r * 0.24f, c)
            drawCircle(Palette.Ink, r * 0.24f, c, style = Stroke(9f))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "PokeBit",
            color = Palette.Ink,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        Spacer(Modifier.weight(1f))

        PokeButton("JUGAR", primary = true, onClick = onPlay)
        Spacer(Modifier.height(14.dp))
        PokeButton("MODO INFINITO", primary = false, onClick = onEndless)
        Spacer(Modifier.height(14.dp))
        PokeButton("SALIR", primary = false, onClick = onExit)

        Spacer(Modifier.weight(1f))

        Box(
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Palette.Surface)
                .border(2.dp, Palette.Track, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 9.dp)
        ) {
            Text(
                text = "Mejor puntuación: $bestScore",
                color = Palette.Blue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "v1.0 · BITLOGIC STUDIO",
            color = Palette.Muted,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun PokeButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(shape)
            .background(if (primary) Palette.Yellow else Palette.Surface)
            .border(3.dp, Palette.Ink, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Palette.Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

/** Panel que cubre la pantalla al morir, completar el nivel o pausar. */
@Composable
fun ResultPanel(
    title: String,
    score: Int,
    bestScore: Int,
    pokeballs: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onMenu: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Scrim)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Palette.Ink,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(28.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Palette.Surface)
                .border(3.dp, Palette.Ink, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "RESUMEN DE PARTIDA",
                color = Palette.Blue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).background(Palette.Track))
            Spacer(Modifier.height(14.dp))

            SummaryRow("Puntuación", score.toString(), Palette.Ink)
            Spacer(Modifier.height(10.dp))
            SummaryRow("Mejor puntuación", bestScore.toString(), Palette.Blue)
            Spacer(Modifier.height(10.dp))
            SummaryRow("Pokébolas recogidas", "$pokeballs / 3", Palette.Ink)
        }

        Spacer(Modifier.height(28.dp))

        PokeButton(primaryLabel, primary = true, onClick = onPrimary)
        Spacer(Modifier.height(14.dp))
        PokeButton("MENÚ PRINCIPAL", primary = false, onClick = onMenu)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Palette.Ink, fontSize = 15.sp)
        Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Black)
    }
}
