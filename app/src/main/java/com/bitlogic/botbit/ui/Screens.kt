package com.bitlogic.botbit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bitlogic.botbit.data.CharacterData
import com.bitlogic.botbit.ui.inventory.InventoryViewModel

@Composable
fun ScreenInventory(
    onBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val characters by viewModel.characters.collectAsState()
    val selectedId by viewModel.selectedCharacter.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.LightBg)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "INVENTARIO",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
            Spacer(Modifier.size(40.dp))
        }
        
        Spacer(Modifier.height(16.dp))
        
        val selected = characters.find { it.id == selectedId } ?: characters.first()
        PreviewCard(character = selected)
        
        Spacer(Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(characters) { character ->
                SlotCard(
                    character = character,
                    isSelected = character.id == selectedId,
                    onSelect = { viewModel.selectCharacter(it) },
                    onBuy = { viewModel.buyCharacter(it) }
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.confirmSelection() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Palette.DarkYellow)
        ) {
            Text(
                text = "SELECCIONAR",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
        }
        
        Text(
            text = "DESBLOQUEA MAS JUGANDO DIARIAMENTE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PreviewCard(character: CharacterData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Palette.Surface)
            .border(3.dp, Palette.Black, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RobotPreview(color = character.color)
        Spacer(Modifier.height(8.dp))
        Text(
            text = character.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )
        Text(
            text = character.type,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Palette.Track))
        Spacer(Modifier.height(8.dp))
        character.stats.forEach { (label, value) ->
            StatRow(label = label, filledBlocks = value)
        }
    }
}

@Composable
fun RobotPreview(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(16.dp)
                .align(Alignment.BottomCenter)
                .background(Palette.LightGrey)
                .border(2.dp, Palette.Black)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopCenter)
                .background(color)
                .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
        ) {
            // Antenas
            Box(modifier = Modifier.size(6.dp, 18.dp).background(Palette.Black).align(Alignment.TopStart))
            Box(modifier = Modifier.size(6.dp, 18.dp).background(Palette.Black).align(Alignment.TopEnd))
            // Ojos LED
            Box(modifier = Modifier.size(12.dp, 8.dp).background(Palette.Surface).align(Alignment.CenterStart).offset(x = 4.dp))
            Box(modifier = Modifier.size(12.dp, 8.dp).background(Palette.Surface).align(Alignment.CenterEnd).offset(x = (-4).dp))
            // Boca
            Box(modifier = Modifier.size(20.dp, 4.dp).background(Palette.Surface).align(Alignment.BottomCenter).offset(y = (-6).dp))
        }
    }
}

@Composable
fun SlotCard(
    character: CharacterData,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onBuy: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(if (isSelected) Palette.YellowBg else Palette.Surface)
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = if (isSelected) Palette.DarkYellow else if (character.locked) Palette.Track else Palette.Black,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { if (!character.locked) onSelect(character.id) }
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
                    .background(character.color)
                    .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
            )
            Text(
                text = character.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (character.locked) Palette.Muted else Palette.Black
            )
            
            if (character.locked) {
                Button(
                    onClick = { onBuy(character.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Yellow),
                    modifier = Modifier.height(24.dp).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "MONEDA ${character.price}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.Black
                    )
                }
            }
        }
        
        if (character.locked) {
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
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .background(Palette.Green)
                    .border(1.5.dp, Palette.Black, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.Center)
                        .background(Palette.Surface)
                        .border(1.dp, Palette.Black, RoundedCornerShape(50))
                )
            }
        }
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
            Box(Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Palette.Track))
            Spacer(Modifier.height(14.dp))

            SummaryRow("Puntuacion", score.toString(), Palette.Ink)
            Spacer(Modifier.height(10.dp))
            SummaryRow("Mejor puntuacion", bestScore.toString(), Palette.Blue)
            Spacer(Modifier.height(10.dp))
            SummaryRow("Monedas recogidas", "$coins / 3", Palette.Ink)
        }

        Spacer(Modifier.height(28.dp))

        PokeButton(primaryLabel, primary = true, onClick = onPrimary)
        Spacer(Modifier.height(14.dp))
        PokeButton("MENU PRINCIPAL", primary = false, onClick = onMenu)
    }
}

@Composable
fun PokeButton(
    text: String,
    primary: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) Palette.Yellow else Palette.Surface,
            contentColor = Palette.Ink
        ),
        border = if (!primary) BorderStroke(2.dp, Palette.Ink) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
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
