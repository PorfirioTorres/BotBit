package com.bitlogic.botbit.ui

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    when {
        isTablet -> TabletMenuScreen(
            bestScore = bestScore,
            onPlay = onPlay,
            onEndless = onEndless,
            onInventory = onInventory,
            onMissions = onMissions,
            onExit = onExit
        )
        isLandscape -> LandscapeMenuScreen(
            bestScore = bestScore,
            onPlay = onPlay,
            onEndless = onEndless,
            onInventory = onInventory,
            onMissions = onMissions,
            onExit = onExit
        )
        else -> PortraitMenuScreen(
            bestScore = bestScore,
            onPlay = onPlay,
            onEndless = onEndless,
            onInventory = onInventory,
            onMissions = onMissions,
            onExit = onExit
        )
    }
}

@Composable
fun LogoBotBit(modifier: Modifier = Modifier) {
    Canvas(modifier) {
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
}

// ============ PORTRAIT (Vertical) ============
@Composable
private fun PortraitMenuScreen(
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
        Spacer(Modifier.height(48.dp))
        
        LogoBotBit(Modifier.size(120.dp))

        Spacer(Modifier.height(16.dp))

        Text(
            text = "BotBit",
            color = Palette.Ink,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.weight(1f))

        // Botones en columna
        PokeButton("JUGAR", primary = true, onClick = onPlay)
        Spacer(Modifier.height(10.dp))
        PokeButton("MODO INFINITO", primary = false, onClick = onEndless)
        Spacer(Modifier.height(10.dp))
        PokeButton("MISIONES", primary = false, onClick = onMissions)
        Spacer(Modifier.height(10.dp))
        PokeButton("PERSONAJES", primary = false, onClick = onInventory)
        Spacer(Modifier.height(10.dp))
        PokeButton("SALIR", primary = false, onClick = onExit)

        Spacer(Modifier.weight(1f))

        // Puntuación
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Palette.Surface)
                .border(2.dp, Palette.Track, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Mejor puntuación: $bestScore",
                color = Palette.Blue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "v1.0 · BITLOGIC STUDIO",
            color = Palette.Muted,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ============ LANDSCAPE (Horizontal) ============
@Composable
private fun LandscapeMenuScreen(
    bestScore: Int,
    onPlay: () -> Unit,
    onEndless: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onExit: () -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna izquierda: Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoBotBit(Modifier.size(100.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "BotBit",
                color = Palette.Ink,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Columna derecha: Botones (2 columnas)
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("JUGAR", primary = true, onClick = onPlay)
                    Spacer(Modifier.height(8.dp))
                    PokeButton("MODO INFINITO", primary = false, onClick = onEndless)
                    Spacer(Modifier.height(8.dp))
                    PokeButton("MISIONES", primary = false, onClick = onMissions)
                }
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("PERSONAJES", primary = false, onClick = onInventory)
                    Spacer(Modifier.height(8.dp))
                    PokeButton("SALIR", primary = false, onClick = onExit)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Palette.Surface)
                            .border(2.dp, Palette.Track, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🏆 $bestScore",
                            color = Palette.Blue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============ TABLET ============
@Composable
private fun TabletMenuScreen(
    bestScore: Int,
    onPlay: () -> Unit,
    onEndless: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onExit: () -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo más grande en tablet
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            LogoBotBit(Modifier.size(180.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "BotBit",
                color = Palette.Ink,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Botones en 2 columnas con más espacio
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("JUGAR", primary = true, onClick = onPlay)
                    Spacer(Modifier.height(12.dp))
                    PokeButton("MODO INFINITO", primary = false, onClick = onEndless)
                    Spacer(Modifier.height(12.dp))
                    PokeButton("MISIONES", primary = false, onClick = onMissions)
                }
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("PERSONAJES", primary = false, onClick = onInventory)
                    Spacer(Modifier.height(12.dp))
                    PokeButton("SALIR", primary = false, onClick = onExit)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Palette.Surface)
                            .border(2.dp, Palette.Track, RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "🏆 Mejor puntuación: $bestScore",
                            color = Palette.Blue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
