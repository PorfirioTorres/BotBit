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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.GameKind
import com.bitlogic.botbit.game.GameMode

/**
 * Selector de tipo de juego.
 *
 * En vertical las tarjetas se apilan con scroll; en horizontal se ponen lado a
 * lado. Nunca se usan alturas fijas apiladas con weight(1f) encima, que fue lo
 * que colapso la rejilla del inventario en horizontal.
 */
@Composable
fun ModeSelectScreen(
    onSelect: (GameKind, GameMode) -> Unit,
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
                text = "ELIGE TU MODO",
                fontSize = if (isLandscape && !isTablet) 17.sp else 21.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
            Spacer(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(10.dp))

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                GameKind.entries.forEachIndexed { i, kind ->
                    if (i > 0) Spacer(Modifier.width(12.dp))
                    ModeCard(
                        kind = kind,
                        compact = true,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                GameKind.entries.forEach { kind ->
                    ModeCard(
                        kind = kind,
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
private fun ModeCard(
    kind: GameKind,
    compact: Boolean,
    onSelect: (GameKind, GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = kind.available
    val shape = RoundedCornerShape(18.dp)

    // rememberScrollState se llama SIEMPRE, aunque no se use: llamarlo dentro
    // de un if rompe la memorizacion posicional de Compose.
    val cardScroll = rememberScrollState()

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) Palette.Surface else Palette.LightGrey)
            .border(
                width = if (enabled) 3.dp else 2.dp,
                color = if (enabled) Palette.Black else Palette.Track,
                shape = shape
            )
            .padding(14.dp)
            // Solo scrollea en horizontal, donde la tarjeta tiene alto acotado
            // por weight(1f). En vertical el Column de afuera ya scrollea, y
            // anidar dos scrolls verticales tira la app.
            .then(if (compact) Modifier.verticalScroll(cardScroll) else Modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = kind.title,
                fontSize = if (compact) 17.sp else 20.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) Palette.Black else Palette.Muted
            )
            if (!enabled) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Palette.Track)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PRÓXIMAMENTE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Palette.Muted
                    )
                }
            }
        }

        Text(
            text = kind.tagline,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Palette.Blue else Palette.Muted
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = kind.description,
            fontSize = if (compact) 11.sp else 13.sp,
            color = Palette.Muted,
            lineHeight = if (compact) 15.sp else 18.sp
        )

        Spacer(Modifier.height(12.dp))

        if (enabled) {
            if (kind == GameKind.RUNNER) {
                ModeButton("NIVEL", primary = true, compact = compact) {
                    onSelect(kind, GameMode.LEVEL)
                }
                Spacer(Modifier.height(8.dp))
                ModeButton("INFINITO", primary = false, compact = compact) {
                    onSelect(kind, GameMode.ENDLESS)
                }
            } else {
                ModeButton("JUGAR", primary = true, compact = compact) {
                    onSelect(kind, GameMode.LEVEL)
                }
            }
        } else {
            Text(
                text = "En desarrollo",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.Muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    primary: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (compact) 40.dp else 48.dp)
            .clip(shape)
            .background(if (primary) Palette.DarkYellow else Palette.Surface)
            .border(2.dp, Palette.Black, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (compact) 13.sp else 15.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black,
            letterSpacing = 1.sp
        )
    }
}
