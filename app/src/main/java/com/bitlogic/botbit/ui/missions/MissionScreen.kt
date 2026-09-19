package com.bitlogic.botbit.ui.missions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.missions.MissionManager
import com.bitlogic.botbit.ui.Palette
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    missionManager: MissionManager,
    onBack: () -> Unit
) {
    val missions by missionManager.missions.collectAsState()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .padding(16.dp)
    ) {
        // Header con botón de actualizar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "MISIONES",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Palette.Ink
            )
            
            // BOTÓN ACTUALIZAR (genera nuevas misiones)
            Button(
                onClick = { 
                    missionManager.refreshMissions()
                    refreshTrigger++
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.Blue
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "ACTUALIZAR",
                    color = Palette.Surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Lista de misiones con Swipe to Dismiss
        if (missions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay misiones disponibles\nToca 'Actualizar' para generar nuevas",
                    color = Palette.Muted,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = missions,
                    key = { it.mission.id }
                ) { mission ->
                    val offset by animateDpAsState(
                        targetValue = 0.dp,
                        label = "missionAnimation"
                    )
                    
                    SwipeToDismissBox(
                        state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart || 
                                    dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                    // ELIMINAR MISIÓN CON SWIPE
                                    missionManager.deleteMission(mission.mission.id)true
                                } else {
                                    false
                                }
                            }
                        ),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Palette.Red)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar misión",
                                    tint = Palette.Surface
                                )
                            }
                        },
                        content = {
                            Box(
                                modifier = Modifier.offset(y = offset)
                            ) {
                                MissionCard(
                                    mission = mission,
                                    onComplete = { 
                                        if (!mission.completed) {
                                            missionManager.completeMission(mission.mission.id)
                                        }
                                    },
                                    onReset = { 
                                        if (mission.completed) {
                                            missionManager.resetMission(mission.mission.id)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
