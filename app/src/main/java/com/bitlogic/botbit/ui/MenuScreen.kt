package com.bitlogic.botbit.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import com.bitlogic.botbit.R


@Composable
fun MenuScreen(
    onPlay: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onTerms: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    when {
        isTablet -> TabletMenuScreen(
            onPlay = onPlay,
            onInventory = onInventory,
            onMissions = onMissions,
            onTerms = onTerms,
            onSettings = onSettings,
            onExit = onExit
        )
        isLandscape -> LandscapeMenuScreen(
            onPlay = onPlay,
            onInventory = onInventory,
            onMissions = onMissions,
            onTerms = onTerms,
            onSettings = onSettings,
            onExit = onExit
        )
        else -> PortraitMenuScreen(
            onPlay = onPlay,
            onInventory = onInventory,
            onMissions = onMissions,
            onTerms = onTerms,
            onSettings = onSettings,
            onExit = onExit
        )
    }
}

@Composable
fun LogoBotBit(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.robot),
        contentDescription = "BotBit",
        modifier = modifier
    )
}

// ============ PORTRAIT (Vertical) ============
@Composable
private fun PortraitMenuScreen(
    onPlay: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onTerms: () -> Unit,
    onSettings: () -> Unit,
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
        PokeButton("PERSONAJES", primary = false, onClick = onInventory)
        Spacer(Modifier.height(10.dp))
        PokeButton("MISIONES", primary = false, onClick = onMissions)
        Spacer(Modifier.height(10.dp))
        PokeButton("AJUSTES", primary = false, onClick = onSettings)
        Spacer(Modifier.height(10.dp))
        PokeButton("SALIR", primary = false, onClick = onExit)

        Spacer(Modifier.weight(1f))

        Text(
            text = "v1.0 · BITLOGIC STUDIO",
            color = Palette.Muted,
            fontSize = 10.sp,
            // La prueba de Crashlytics se movio a Ajustes -> DESARROLLO.
            // Aqui cualquier jugador la tocaba sin querer y la app se cerraba.
        )

        Spacer(Modifier.height(16.dp))    }
}

// ============ LANDSCAPE (Horizontal) ============
@Composable
private fun LandscapeMenuScreen(
    onPlay: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onTerms: () -> Unit,
    onSettings: () -> Unit,
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
            TextButton(onClick = onTerms) {
                Text("Términos", color = Palette.Blue, fontSize = 12.sp)
            }
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
                    PokeButton("PERSONAJES", primary = false, onClick = onInventory)
                    Spacer(Modifier.height(8.dp))
                    PokeButton("MISIONES", primary = false, onClick = onMissions)
                }
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("AJUSTES", primary = false, onClick = onSettings)
                    Spacer(Modifier.height(8.dp))
                    PokeButton("SALIR", primary = false, onClick = onExit)
                }
            }
        }
    }
}

// ============ TABLET ============
@Composable
private fun TabletMenuScreen(
    onPlay: () -> Unit,
    onInventory: () -> Unit,
    onMissions: () -> Unit,
    onTerms: () -> Unit,
    onSettings: () -> Unit,
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
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onTerms) {
                Text("Términos y Condiciones", color = Palette.Blue, fontSize = 14.sp)
            }
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
                    PokeButton("PERSONAJES", primary = false, onClick = onInventory)
                    Spacer(Modifier.height(12.dp))
                    PokeButton("MISIONES", primary = false, onClick = onMissions)
                }
                Column(modifier = Modifier.weight(1f)) {
                    PokeButton("AJUSTES", primary = false, onClick = onSettings)
                    Spacer(Modifier.height(12.dp))
                    PokeButton("SALIR", primary = false, onClick = onExit)
                }
            }
        }
    }
}
