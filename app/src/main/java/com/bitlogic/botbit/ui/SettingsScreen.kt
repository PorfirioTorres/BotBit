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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitlogic.botbit.data.ProgressStore
import com.bitlogic.botbit.utils.SoundManager

/**
 * Pantalla de Ajustes.
 *
 * Nota sobre el scroll: el Column exterior scrollea y NINGUNA tarjeta de
 * adentro lleva verticalScroll. Dos scrolls verticales anidados lanzan
 * IllegalStateException y tiran la app, que es el error que ya nos paso en el
 * selector de modos.
 */
@Composable
fun SettingsScreen(
    store: ProgressStore,
    userEmail: String?,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onCrashTest: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var joystick by remember { mutableStateOf(store.joystickMode) }
    var music by remember { mutableFloatStateOf(store.musicVolume) }
    var sfx by remember { mutableFloatStateOf(store.sfxVolume) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.LightBg)
            .safeDrawingPadding()
            .padding(horizontal = if (isTablet) 32.dp else 18.dp, vertical = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("AJUSTES", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Palette.Black)
            Spacer(Modifier.size(40.dp))
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // ---------- Controles ----------
            Seccion("CONTROLES") {
                Text(
                    "Joystick de la Arena",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Palette.Black
                )
                Spacer(Modifier.height(6.dp))
                Row {
                    Opcion("FLOTANTE", joystick == "FLOTANTE", Modifier.weight(1f)) {
                        joystick = "FLOTANTE"; store.joystickMode = "FLOTANTE"
                    }
                    Spacer(Modifier.width(10.dp))
                    Opcion("FIJO", joystick == "FIJO", Modifier.weight(1f)) {
                        joystick = "FIJO"; store.joystickMode = "FIJO"
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (joystick == "FLOTANTE")
                        "Aparece donde pongas el dedo."
                    else
                        "Siempre en la esquina inferior izquierda.",
                    fontSize = 12.sp, color = Palette.Muted
                )
            }

            // ---------- Audio ----------
            Seccion("AUDIO") {
                VolumenFila("Música", music) {
                    music = it
                    store.musicVolume = it
                    SoundManager.musicVolume = it
                }
                Spacer(Modifier.height(10.dp))
                VolumenFila("Efectos", sfx) {
                    sfx = it
                    store.sfxVolume = it
                    SoundManager.sfxVolume = it
                }
            }

            // ---------- Cuenta ----------
            Seccion("CUENTA") {
                if (userEmail != null) {
                    Text(userEmail, fontSize = 13.sp, color = Palette.Black)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tu progreso se sincroniza con la nube.",
                        fontSize = 12.sp, color = Palette.Muted
                    )
                    Spacer(Modifier.height(10.dp))
                    Opcion("CERRAR SESIÓN", false, Modifier.fillMaxWidth(), onSignOut)
                } else {
                    Text(
                        "Sin sesión. El progreso solo se guarda en este dispositivo " +
                                "y se pierde si desinstalas la app.",
                        fontSize = 12.sp, color = Palette.Muted
                    )
                }
            }

            // ---------- Datos ----------
            Seccion("DATOS") {
                if (!confirmarBorrado) {
                    Opcion("BORRAR PROGRESO LOCAL", false, Modifier.fillMaxWidth()) {
                        confirmarBorrado = true
                    }
                } else {
                    Text(
                        "Esto borra monedas, personajes y récords de este dispositivo. " +
                                "No se puede deshacer.",
                        fontSize = 12.sp, color = Palette.Red
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Opcion("CANCELAR", false, Modifier.weight(1f)) {
                            confirmarBorrado = false
                        }
                        Spacer(Modifier.width(10.dp))
                        Opcion("SÍ, BORRAR", true, Modifier.weight(1f)) {
                            store.clearAll()
                            confirmarBorrado = false
                        }
                    }
                }
            }

            // ---------- Desarrollo ----------
            // La prueba de Crashlytics vivia escondida en el texto de version
            // del menu principal, donde cualquier jugador la tocaba sin querer
            // y la app se cerraba. Aqui sigue disponible para la evidencia de
            // la materia, pero etiquetada y fuera del camino.
            if (onCrashTest != null) {
                Seccion("DESARROLLO") {
                    Text(
                        "Prueba de Crashlytics. Cierra la app a propósito para " +
                                "verificar que el reporte llegue a Firebase.",
                        fontSize = 12.sp, color = Palette.Muted
                    )
                    Spacer(Modifier.height(8.dp))
                    Opcion("FORZAR CRASH DE PRUEBA", false, Modifier.fillMaxWidth(), onCrashTest)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Text(
        titulo,
        fontSize = 11.sp, fontWeight = FontWeight.Black,
        color = Palette.Muted, letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Surface)
            .border(3.dp, Palette.Black, RoundedCornerShape(16.dp))
            .padding(14.dp),
        content = contenido
    )
}

@Composable
private fun Opcion(
    label: String,
    activo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .height(44.dp)
            .clip(shape)
            .background(if (activo) Palette.DarkYellow else Palette.LightBg)
            .border(2.dp, Palette.Black, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Palette.Black)
    }
}

@Composable
private fun VolumenFila(label: String, valor: Float, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = Palette.Black, modifier = Modifier.width(74.dp))
        Slider(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Palette.DarkYellow,
                activeTrackColor = Palette.Black,
                inactiveTrackColor = Palette.Track
            )
        )
        Text(
            "${(valor * 100).toInt()}%",
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Palette.Muted,
            modifier = Modifier.width(40.dp)
        )
    }
}
