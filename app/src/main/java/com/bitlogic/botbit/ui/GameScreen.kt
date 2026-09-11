package com.bitlogic.botbit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.bitlogic.botbit.game.GameConfig
import com.bitlogic.botbit.game.GameMode
import com.bitlogic.botbit.game.GameStatus
import com.bitlogic.botbit.game.LevelData
import com.bitlogic.botbit.game.World
import com.bitlogic.botbit.game.missions.MissionManager

@Composable
fun GameScreen(
    mode: GameMode,
    level: LevelData?,
    bestScore: Int,
    selectedCharacter: String = "classic",
    missionManager: MissionManager? = null,
    onRunFinished: (score: Int, coins: Int, completed: Boolean) -> Unit,
    onMenu: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val tilesVisible = if (isTablet) 16f else GameConfig.TILES_VISIBLE_X
    
    val world = remember(mode, level, missionManager, selectedCharacter, tilesVisible) { 
        World(mode, level, missionManager, selectedCharacter, tilesVisible) 
    }

    // Tema visual del nivel y renderizador de fondo.
    // El BackgroundRenderer cachea sus rutas: se crea una vez, no por frame.
    val theme = remember(level) { LevelTheme.byId(level?.theme) }
    // Objetos de dibujo reutilizables: se crean una vez, no por frame.
    val scratch = remember { RenderScratch() }

    val frame = remember { mutableStateOf(0) }

    var status by remember { mutableStateOf(GameStatus.RUNNING) }
    var paused by remember { mutableStateOf(false) }
    var hudScore by remember { mutableStateOf(0) }
    var hudCoins by remember { mutableStateOf(0) }
    var hudProgress by remember { mutableStateOf(0f) }
    var finalScore by remember { mutableStateOf(0) }

    LaunchedEffect(world) {
        var last = 0L
        var accumulator = 0f
        var hudTick = 0

        while (true) {
            withFrameNanos { now ->
                if (last != 0L && !paused && world.status == GameStatus.RUNNING) {
                    val delta = ((now - last) / 1_000_000_000f)
                        .coerceAtMost(GameConfig.MAX_FRAME_DELTA)
                    accumulator += delta

                    var guard = 0
                    while (accumulator >= GameConfig.FIXED_STEP && guard < 12) {
                        world.update(GameConfig.FIXED_STEP)
                        accumulator -= GameConfig.FIXED_STEP
                        guard++
                    }
                    if (guard >= 12) accumulator = 0f
                }
                last = now
                frame.value++

                if (world.status != status) {
                    finalScore = world.score
                    status = world.status
                }
                if (++hudTick >= 4) {
                    hudTick = 0
                    hudScore = world.score
                    hudCoins = world.coins
                    hudProgress = world.progress
                }
            }
        }
    }

    LaunchedEffect(status) {
        if (status != GameStatus.RUNNING) {
            onRunFinished(finalScore, world.coins, status == GameStatus.COMPLETED)
        }
    }

    Box(Modifier.fillMaxSize().background(Palette.Bg)) {

        Column(Modifier.fillMaxSize().safeDrawingPadding()) {

            Hud(
                title = world.title,
                progress = hudProgress,
                showProgress = mode == GameMode.LEVEL,
                score = hudScore,
                coins = hudCoins,
                paused = paused,
                onTogglePause = { paused = !paused }
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(world) {
                        detectTapGestures(onPress = { world.onTap() })
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    frame.value
                    drawWorld(world, tilesVisible, theme, scratch)
                }

                if (world.attempts == 1 && hudProgress < 0.04f && mode == GameMode.LEVEL) {
                    TapHint(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
                }
            }
        }

        if (paused && status == GameStatus.RUNNING) {
            ResultPanel(
                title = "PAUSA",
                score = hudScore,
                bestScore = bestScore,
                coins = hudCoins,
                primaryLabel = "CONTINUAR",
                onPrimary = { paused = false },
                onMenu = onMenu
            )
        }

        if (status != GameStatus.RUNNING) {
            ResultPanel(
                title = if (status == GameStatus.COMPLETED) "NIVEL SUPERADO" else "FIN DE JUEGO",
                score = finalScore,
                bestScore = maxOf(bestScore, finalScore),
                coins = world.coins,
                primaryLabel = "REINTENTAR",
                onPrimary = {
                    world.retry()
                    status = GameStatus.RUNNING
                    paused = false
                },
                onMenu = onMenu
            )
        }
    }
}

@Composable
private fun Hud(
    title: String,
    progress: Float,
    showProgress: Boolean,
    score: Int,
    coins: Int,
    paused: Boolean,
    onTogglePause: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (showProgress) {
            Canvas(Modifier.fillMaxWidth().height(10.dp)) {
                val r = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(Palette.Track, Offset.Zero, size, r)
                if (progress > 0f) {
                    drawRoundRect(
                        Palette.Yellow,
                        Offset.Zero,
                        Size(size.width * progress, size.height),
                        r
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Palette.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Palette.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Puntos: " + score.toString().padStart(4, '0'),
                color = Palette.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    Canvas(Modifier.size(16.dp).padding(end = 0.dp)) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            if (index < coins) Palette.Yellow else Palette.Track,
                            r, c
                        )
                        drawCircle(Palette.Ink, r, c, style = Stroke(1.6f))
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.Surface)
                        .border(2.dp, Palette.Ink, RoundedCornerShape(10.dp))
                        .clickable { onTogglePause() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (paused) "\u25B6" else "\u23F8",
                        color = Palette.Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun TapHint(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth(0.86f)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Surface.copy(alpha = 0.85f))
            .border(2.dp, Palette.Track, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PULSA LA PANTALLA PARA SALTAR",
            color = Palette.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}
