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
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color

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
fun ScreenInventory() {
    Column(
        modifier = Modifier
            .width(390.dp)
            .height(875.dp)
            .background(Palette.LightBg)
            .border(4.dp, color(0xFFD1D5DB), RoundedCornerShape(36.dp))
            .padding(0.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Bar
        StatusBar()

        // Header Navigation
        HeaderNavigation()

        // Selection Preview Wrapper
        SelectionPreviewWrapper()

        // Grid Wrapper
        GridWrapper()

        // Bottom Area
        BottomArea()
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
            color = color(0xFF111827)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // iOS Signal Icon (placeholder)
            Box(modifier = Modifier.size(18.dp).background(Color(0xFF111827)))
            // Wi-Fi Icon (placeholder)
            Box(modifier = Modifier.size(18.dp).background(Color(0xFF111827)))
            // Battery Icon (placeholder)
            Box(modifier = Modifier.size(24.dp, 16.dp).background(Color(0xFF111827)))
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
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White)
                .border(2.5.dp, Color(0xFF111827), RoundedCornerShape(12.dp))
                .shadow(2.dp, Color(0x10000000))
        ) {
            // Arrow Left (placeholder)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(3.dp, Color(0xFF111827))
                    .align(Alignment.Center)
            )
        }

        Text(
            text = "INVENTARIO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827)
        )

        // Frame placeholder
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
            .background(Color.White)
            .border(3.dp, Color(0xFF111827), RoundedCornerShape(20.dp))
            .shadow(4.dp, Color(0x10000000))
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
        // Podium shadow
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(16.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFE5E7EB))
                .border(2.dp, Color(0xFF111827))
        )
        // Sprite bounce
        PixelAvatar(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun PixelAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(Color(0xFFFFCB05))
            .border(3.dp, Color(0xFF111827), RoundedCornerShape(8.dp))
    ) {
        // Ears
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(14.4.dp, 18.dp).background(Color(0xFF111827)))
            Box(modifier = Modifier.size(14.4.dp, 18.dp).background(Color(0xFF111827)))
        }
        // Face
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(21.6.dp)
        ) {
            Box(modifier = Modifier.size(18.dp, 9.dp).background(Color(0xFF111827)))
            Box(modifier = Modifier.size(18.dp, 9.dp).background(Color(0xFF111827)))
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
                color = Color(0xFF111827)
            )
            Text(
                text = "TIPO ELÉCTRICO / BASE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD1D5DB)))
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
            color = Color(0xFF6B7280)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (i <= filledBlocks) Color(0xFFFFCB05) else Color(0xFFE5E7EB))
                        .border(1.5.dp, Color(0xFF111827), RoundedCornerShape(2.dp))
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
                SlotData("Clásico", Color(0xFFFFCB05), true),
                SlotData("Fuego", Color(0xFFEF4444), false)
            )
        )
        GridRow(
            slots = listOf(
                SlotData("Agua", Color(0xFF3B82F6), false),
                SlotData("Planta", Color(0xFF10B981), false, locked = true)
            )
        )
        GridRow(
            slots = listOf(
                SlotData("Eléctrico", Color(0xFFFBBF24), false, locked = true),
                SlotData("Sombra", Color(0xFF6B21A8), false, locked = true)
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
            .weight(1f)
            .height(110.dp)
            .background(if (slot.isClassic) Color(0xFFFFFEE6) else Color.White)
            .border(
                width = if (slot.isClassic) 3.dp else 2.dp,
                color = if (slot.isClassic) Color(0xFFFFCB05) else if (slot.locked) Color(0xFFD1D5DB) else Color(0xFF111827),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(4.dp, Color(0x25FFCB05))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(slot.color)
                    .border(3.dp, Color(0xFF111827), RoundedCornerShape(8.dp))
            )
            Text(
                text = slot.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (slot.locked) Color(0xFF6B7280) else Color(0xFF111827)
            )
        }
        if (slot.locked) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFFEF4444))
                    .border(1.5.dp, Color(0xFF111827), RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Color.White)
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
        // Select Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFFFFCB05))
                .border(3.dp, Color(0xFF111827), RoundedCornerShape(16.dp))
                .shadow(4.dp, Color(0x15000000))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF111827), RoundedCornerShape(12.dp))
            )
            Text(
                text = "SELECCIONAR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
        }

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "DESBLOQUEA MÁS JUGANDO DIARIAMENTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )
            Box(
                modifier = Modifier
                    .width(134.dp)
                    .height(5.dp)
                    .background(Color(0xFF111827))
                    .border(1.dp, Color(0xFF111827), RoundedCornerShape(10.dp))
            )
        }
    }
}

