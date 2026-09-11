package com.bitlogic.botbit.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
    
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // En horizontal el alto util cae a ~360dp. Apilar cabecera + preview (200dp)
    // + rejilla + boton no cabe, y la rejilla con weight(1f) se quedaba en 0dp:
    // por eso desaparecia y no se podia deslizar. En horizontal pasamos a dos
    // columnas, que ademas aprovecha el ancho sobrante.
    val columns = when {
        isTablet && isLandscape -> 3
        isTablet -> 3
        isLandscape -> 3
        else -> 2
    }

    val selected = characters.find { it.id == selectedId } ?: characters.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.LightBg)
            .safeDrawingPadding()
            .padding(horizontal = if (isTablet) 32.dp else 20.dp, vertical = 12.dp)
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
                fontSize = if (isLandscape && !isTablet) 18.sp else 22.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Black
            )
            Spacer(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(12.dp))

        if (isLandscape) {
            // ---- HORIZONTAL: preview a la izquierda, rejilla a la derecha ----
            Row(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight()
                ) {
                    if (selected != null) {
                        PreviewCard(
                            character = selected,
                            isTablet = isTablet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SelectButton(
                        isCompact = true,
                        onClick = { viewModel.confirmSelection() }
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(0.62f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(characters) { character ->
                            SlotCard(
                                character = character,
                                isSelected = character.id == selectedId,
                                onSelect = { viewModel.selectCharacter(it) },
                                onBuy = { viewModel.buyCharacter(it) },
                                isTablet = isTablet
                            )
                        }
                    }
                    Text(
                        text = "DESBLOQUEA MAS JUGANDO DIARIAMENTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.Muted,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ---- VERTICAL: el diseno original ----
            if (selected != null) {
                PreviewCard(character = selected, isTablet = isTablet)
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(characters) { character ->
                    SlotCard(
                        character = character,
                        isSelected = character.id == selectedId,
                        onSelect = { viewModel.selectCharacter(it) },
                        onBuy = { viewModel.buyCharacter(it) },
                        isTablet = isTablet
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SelectButton(isCompact = false, onClick = { viewModel.confirmSelection() })

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
}

@Composable
private fun SelectButton(isCompact: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 46.dp else 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Palette.DarkYellow)
    ) {
        Text(
            text = "SELECCIONAR",
            fontSize = if (isCompact) 15.sp else 18.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )
    }
}

@Composable
fun PreviewCard(
    character: CharacterData,
    isTablet: Boolean = false,
    /** En horizontal se pasa Modifier.weight(1f) para que ocupe el alto disponible. */
    modifier: Modifier = Modifier.height(if (isTablet) 240.dp else 200.dp)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Palette.Surface)
            .border(3.dp, Palette.Black, RoundedCornerShape(20.dp))
            .padding(16.dp)
            // Red de seguridad: si la tarjeta queda muy baja, el contenido
            // se desliza en vez de recortarse.
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RobotPreview(color = character.color, modifier = Modifier.size(if (isTablet) 80.dp else 60.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = character.name,
            fontSize = if (isTablet) 24.sp else 20.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black
        )
        Text(
            text = character.type,
            fontSize = if (isTablet) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Palette.Track))
        Spacer(Modifier.height(8.dp))
        character.stats.forEach { (label, value) ->
            StatRow(label = label, filledBlocks = value, isTablet = isTablet)
        }
    }
}

@Composable
fun RobotPreview(color: Color, modifier: Modifier = Modifier.size(60.dp), boxHeight: Dp = 80.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
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
            modifier = modifier
                .align(Alignment.TopCenter)
                .background(color)
                .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
        ) {
            // Antenas
            Box(modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(0.1f).background(Palette.Black).align(Alignment.TopStart))
            Box(modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(0.1f).background(Palette.Black).align(Alignment.TopEnd))
            // Ojos LED
            Box(modifier = Modifier.fillMaxHeight(0.2f).fillMaxWidth(0.2f).background(Palette.Surface).align(Alignment.CenterStart).offset(x = 4.dp))
            Box(modifier = Modifier.fillMaxHeight(0.2f).fillMaxWidth(0.2f).background(Palette.Surface).align(Alignment.CenterEnd).offset(x = (-4).dp))
            // Boca
            Box(modifier = Modifier.fillMaxHeight(0.1f).fillMaxWidth(0.3f).background(Palette.Surface).align(Alignment.BottomCenter).offset(y = (-6).dp))
        }
    }
}

@Composable
fun SlotCard(
    character: CharacterData,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onBuy: (String) -> Unit,
    isTablet: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTablet) 150.dp else 130.dp)
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
                    .size(if (isTablet) 40.dp else 28.dp)
                    .background(character.color)
                    .border(3.dp, Palette.Black, RoundedCornerShape(8.dp))
            )
            Text(
                text = character.name,
                fontSize = if (isTablet) 15.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (character.locked) Palette.Muted else Palette.Black
            )
            
            if (character.locked) {
                Button(
                    onClick = { onBuy(character.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Yellow),
                    modifier = Modifier.height(if (isTablet) 30.dp else 24.dp).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "MONEDA ${character.price}",
                        fontSize = if (isTablet) 12.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.Black
                    )
                }
            }
        }
        
        if (character.locked) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 24.dp else 18.dp)
                    .align(Alignment.BottomEnd)
                    .background(Palette.Red)
                    .border(1.5.dp, Palette.Black, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 14.dp else 10.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Palette.Surface)
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 24.dp else 18.dp)
                    .align(Alignment.TopEnd)
                    .background(Palette.Green)
                    .border(1.5.dp, Palette.Black, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 12.dp else 8.dp)
                        .align(Alignment.Center)
                        .background(Palette.Surface)
                        .border(1.dp, Palette.Black, RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
fun StatRow(label: String, filledBlocks: Int, isTablet: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isTablet) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 16.dp else 12.dp)
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
