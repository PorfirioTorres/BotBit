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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MenuScreen(
    bestScore: Int,
    onPlay: () -> Unit,
    onEndless: () -> Unit,
    onInventory: () -> Unit,
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
    coins: Int,
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
            SummaryRow("Pokébolas recogidas", "$coins / 3", Palette.Ink)
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


@Composable
fun ScreenInventory(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(390.dp)
            .height(875.dp)
            .background(Palette.LightBg)
            .border(4.dp, Palette.Track, RoundedCornerShape(36.dp))
            .padding(0.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        StatusBar()
        HeaderNavigation(onBack = onBack)
        SelectionPreviewWrapper()
        GridWrapper()
        BottomArea()
    }
}

@Composable
fun HeaderNavigation(onBack: () -> Unit = {}) {  // <-- Añadir parámetro
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de retroceso con funcionalidad
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Palette.Surface)
                .border(2.5.dp, Palette.Black, RoundedCornerShape(12.dp))
                .shadow(2.dp)
                .clickable { onBack() }  // <-- Acción al hacer clic
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(3.dp, Palette.Black)
                    .align(Alignment.Center)
            )
        }

        Text(
            text = "INVENTARIO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )

        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "9:41",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Palette.Black
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(18.dp).background(Palette.Black))
            Box(modifier = Modifier.size(18.dp).background(Palette.Black))
            Box(modifier = Modifier.size(24.dp, 16.dp).background(Palette.Black))
        }
    }
}

@Composable
fun HeaderNavigation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Palette.Surface)
                .border(2.5.dp, Palette.Black, RoundedCornerShape(12.dp))
                .shadow(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(3.dp, Palette.Black)
                    .align(Alignment.Center)
            )
        }

        Text(
            text = "INVENTARIO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )

        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun SelectionPreviewWrapper() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(257.dp)
            .padding(horizontal = 24.dp, vertical = 0.dp)
    ) {
        PreviewCard()
    }
}

@Composable
fun PreviewCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(249.dp)
            .background(Palette.Surface)
            .border(3.dp, Palette.Black, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpritePlatform()
        IdentityStats()
    }
}

@Composable
fun SpritePlatform() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(16.dp)
                .align(Alignment.BottomCenter)
                .background(Palette.LightGrey)
                .border(2.dp, Palette.Black)
        )
        PixelAvatar(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun PixelAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(Palette.DarkYellow)
            .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(14.4.dp, 18.dp).background(Palette.Black))
            Box(modifier = Modifier.size(14.4.dp, 18.dp).background(Palette.Black))
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(21.6.dp)
        ) {
            Box(modifier = Modifier.size(18.dp, 9.dp).background(Palette.Black))
            Box(modifier = Modifier.size(18.dp, 9.dp).background(Palette.Black))
        }
    }
}

@Composable
fun IdentityStats() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "PokeBit Classic",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
            Text(
                text = "TIPO ELÉCTRICO / BASE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.Muted
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Palette.Track))
        Meters()
    }
}

@Composable
fun Meters() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StatRow(label = "VELOCIDAD", filledBlocks = 3)
        StatRow(label = "SALTO", filledBlocks = 4)
    }
}

@Composable
fun StatRow(label: String, filledBlocks: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (i <= filledBlocks) Palette.DarkYellow else Palette.LightGrey)
                        .border(1.5.dp, Palette.Black, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun GridWrapper() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GridRow(
            slots = listOf(
                SlotData("Clásico", Palette.DarkYellow, true),
                SlotData("Fuego", Palette.Red, false)
            )
        )
        GridRow(
            slots = listOf(
                SlotData("Agua", Palette.Blue, false),
                SlotData("Planta", Palette.Green, false, locked = true)
            )
        )
        GridRow(
            slots = listOf(
                SlotData("Eléctrico", Palette.Yellow, false, locked = true),
                SlotData("Sombra", Palette.Purple, false, locked = true)
            )
        )
    }
}

@Composable
fun GridRow(slots: List<SlotData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        slots.forEach { slot ->
            SlotCard(slot)
        }
    }
}

@Composable
fun SlotCard(slot: SlotData) {
    Box(
        modifier = Modifier
            .height(110.dp)
            .background(if (slot.isClassic) Palette.YellowBg else Palette.Surface)
            .border(
                width = if (slot.isClassic) 3.dp else 2.dp,
                color = if (slot.isClassic) Palette.DarkYellow else if (slot.locked) Palette.Track else Palette.Black,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(slot.color)
                    .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
            )
            Text(
                text = slot.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (slot.locked) Palette.Muted else Palette.Black
            )
        }
        if (slot.locked) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .background(Palette.Red)
                    .border(1.5.dp, Palette.Black, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Palette.Surface)
                )
            }
        }
    }
}

data class SlotData(
    val name: String,
    val color: Color,
    val isClassic: Boolean = false,
    val locked: Boolean = false
)

@Composable
fun BottomArea() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 24.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Palette.DarkYellow)
                .border(3.dp, Palette.Black, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Palette.Surface)
                    .border(2.dp, Palette.Black, RoundedCornerShape(12.dp))
            )
            Text(
                text = "SELECCIONAR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "DESBLOQUEA MÁS JUGANDO DIARIAMENTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.Muted
            )
            Box(
                modifier = Modifier
                    .width(134.dp)
                    .height(5.dp)
                    .background(Palette.Black)
                    .border(1.dp, Palette.Black, RoundedCornerShape(10.dp))
            )
        }
    }
}
