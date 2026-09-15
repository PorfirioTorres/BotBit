package com.bitlogic.botbit.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.LevelData

/**
 * Selector de nivel con desbloqueo progresivo.
 *
 * Un nivel se abre cuando el anterior fue completado. El primero siempre esta
 * abierto. Sin esto los niveles 2 y 3 existian en assets pero nunca se podian
 * jugar: MainActivity solo cargaba level_01.
 */
@Composable
fun LevelSelectScreen(
    levels: List<LevelData>,
    isUnlocked: (index: Int) -> Boolean,
    coinsFor: (LevelData) -> Int,
    isCompleted: (LevelData) -> Boolean,
    onSelect: (LevelData) -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.LightBg)
            .safeDrawingPadding()
            .padding(horizontal = if (isTablet) 32.dp else 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "NIVELES",
                fontSize = if (isLandscape && !isTablet) 17.sp else 21.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
            Spacer(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(10.dp))

        if (isLandscape) {
            Row(Modifier.fillMaxSize()) {
                levels.forEachIndexed { i, lv ->
                    if (i > 0) Spacer(Modifier.width(12.dp))
                    LevelCard(
                        level = lv,
                        index = i,
                        unlocked = isUnlocked(i),
                        coins = coinsFor(lv),
                        completed = isCompleted(lv),
                        compact = true,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                levels.forEachIndexed { i, lv ->
                    LevelCard(
                        level = lv,
                        index = i,
                        unlocked = isUnlocked(i),
                        coins = coinsFor(lv),
                        completed = isCompleted(lv),
                        compact = false,
                        onSelect = onSelect,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelData,
    index: Int,
    unlocked: Boolean,
    coins: Int,
    completed: Boolean,
    compact: Boolean,
    onSelect: (LevelData) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val theme = LevelTheme.byId(level.theme)

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (unlocked) Palette.Surface else Palette.LightGrey)
            .border(
                width = if (unlocked) 3.dp else 2.dp,
                color = if (unlocked) Palette.Black else Palette.Track,
                shape = shape
            )
            .then(if (unlocked) Modifier.clickable { onSelect(level) } else Modifier)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Franja con los colores del tema: identifica el nivel de un vistazo
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            listOf(theme.skyTop, theme.near, theme.groundTop, theme.groundFill).forEach { c ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (unlocked) c else Palette.Track)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = level.name,
            fontSize = if (compact) 14.sp else 17.sp,
            fontWeight = FontWeight.Black,
            color = if (unlocked) Palette.Black else Palette.Muted
        )

        Spacer(Modifier.height(6.dp))

        if (unlocked) {
            Text(
                text = "Monedas  $coins / 3",
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.Blue
            )
            Text(
                text = "${level.obstacles.size} obstáculos · velocidad ${level.scrollSpeed}",
                fontSize = if (compact) 10.sp else 12.sp,
                color = Palette.Muted
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (completed) "COMPLETADO" else "TOCA PARA JUGAR",
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = if (completed) Palette.Green else Palette.Black
            )
        } else {
            Text(
                text = "BLOQUEADO",
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Muted
            )
            Text(
                text = "Completa el nivel ${index} para abrirlo",
                fontSize = if (compact) 10.sp else 12.sp,
                color = Palette.Muted
            )
        }
    }
}
