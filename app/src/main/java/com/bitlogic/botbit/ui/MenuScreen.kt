package com.bitlogic.botbit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
    onInventory: () -> Unit,
    onMissions: () -> Unit,
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
            text = "BotBit",
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
        PokeButton("MISIONES", primary = false, onClick = onMissions)
        Spacer(Modifier.height(14.dp))
        PokeButton("PERSONAJES", primary = false, onClick = onInventory)
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
                text = "Mejor puntuacion: $bestScore",
                color = Palette.Blue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "v1.0 BITLOGIC STUDIO",
            color = Palette.Muted,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(20.dp))
    }
}
