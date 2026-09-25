package com.bitlogic.botbit.ui.missions

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.game.missions.MissionProgress
import com.bitlogic.botbit.ui.Palette

@Composable
fun MissionCard(
    mission: MissionProgress,
    onClaim: () -> Unit = {},
    onComplete: () -> Unit = {},
    onReset: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Palette.Surface
        ),
        border = BorderStroke(
            if (mission.completed) 2.dp else 1.dp,
            if (mission.claimed) Palette.Muted else if (mission.completed) Palette.Green else Palette.Track
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.mission.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mission.claimed) Palette.Muted else if (mission.completed) Palette.Green else Palette.Ink
                    )
                    
                    val progress = (mission.progress.toFloat() / mission.mission.target).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .background(Palette.Track, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .background(
                                    if (mission.claimed) Palette.Muted else if (mission.completed) Palette.Green else Palette.Yellow,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
                
                if (mission.claimed) {
                    Text(text = "Reclamada", fontSize = 12.sp, color = Palette.Muted, modifier = Modifier.padding(end = 8.dp))
                } else if (mission.completed) {
                    Text(text = "Completada", fontSize = 12.sp, color = Palette.Green, modifier = Modifier.padding(end = 8.dp))
                } else {
                    Text(
                        text = "${mission.progress}/${mission.mission.target}",
                        fontSize = 12.sp,
                        color = Palette.Muted,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        tint = Palette.Muted
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                Text(text = mission.mission.description, fontSize = 14.sp, color = Palette.Muted)
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recompensa:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.Ink
                    )
                    if (mission.mission.rewardCoins > 0) {
                        Text(
                            text = "${mission.mission.rewardCoins} monedas",
                            fontSize = 12.sp,
                            color = Palette.YellowDeep
                        )
                    }
                    if (mission.mission.rewardCharacter != null) {
                        Text(
                            text = "Personaje: ${mission.mission.rewardCharacter}",
                            fontSize = 12.sp,
                            color = Palette.Purple
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Botones de accion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BOTÓN RECLAMAR (si está cumplida y no cobrada)
                    if (mission.claimable) {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.Yellow
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "RECLAMAR +${mission.mission.rewardCoins}",
                                color = Palette.OnAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    } else if (!mission.completed) {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.Green
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "COMPLETAR",
                                color = Palette.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else if (mission.claimed) {
                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.Track
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "REINICIAR",
                                color = Palette.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // BOTON ELIMINAR (siempre visible en expandido)
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.Red
                        ),
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text(
                            text = "X",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
