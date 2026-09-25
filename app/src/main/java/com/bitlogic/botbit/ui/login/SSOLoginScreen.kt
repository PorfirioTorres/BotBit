package com.bitlogic.botbit.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitlogic.botbit.ui.Palette

/**
 * Pantalla de inicio de sesion.
 *
 * Dos cambios de fondo respecto a la version anterior:
 *
 * 1. YA NO ES UN MURO. Antes, sin cuenta de Google no se podia entrar al juego.
 *    Si Play Services fallaba, si el emulador no tenia cuenta o si no habia
 *    internet, BotBit entero quedaba inalcanzable. Ahora se puede entrar sin
 *    cuenta y vincularla despues.
 *
 * 2. Usa la identidad del juego (amarillo, negro, trazo grueso) en vez del azul
 *    marino generico de Material, que no se parecia a ninguna otra pantalla.
 */
@Composable
fun SSOLoginScreen(
    viewModel: SSOViewModel = viewModel(),
    onShowTerms: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.ssoState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkExistingSession(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.LightBg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        RobotLogo()

        Spacer(Modifier.height(18.dp))

        Text(
            text = "BotBit",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black,
            letterSpacing = (-1).sp
        )
        Text(
            text = "BITLOGIC STUDIO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Muted,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(40.dp))

        when (val s = state) {
            is SSOState.CheckingExistingSession -> {
                CircularProgressIndicator(color = Palette.DarkYellow)
                Spacer(Modifier.height(12.dp))
                Text("Buscando tu cuenta…", fontSize = 13.sp, color = Palette.Muted)
            }

            is SSOState.Authenticated -> {
                Text(
                    "Sesión iniciada como ${s.user.displayName ?: s.user.email ?: "jugador"}",
                    fontSize = 14.sp,
                    color = Palette.Black,
                    textAlign = TextAlign.Center
                )
            }

            is SSOState.Guest -> {
                Text(
                    "Entrando sin cuenta…",
                    fontSize = 14.sp,
                    color = Palette.Muted
                )
            }

            is SSOState.Idle, is SSOState.Error -> {
                if (s is SSOState.Error) {
                    AvisoError(s.message)
                    Spacer(Modifier.height(18.dp))
                }

                BotonLogin(
                    label = "INICIAR SESIÓN CON GOOGLE",
                    primary = true,
                    onClick = { viewModel.initiateInteractiveSSO(context) }
                )

                Spacer(Modifier.height(12.dp))

                BotonLogin(
                    label = "JUGAR SIN CUENTA",
                    primary = false,
                    onClick = { viewModel.continueAsGuest() }
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Sin cuenta, tu progreso se guarda solo en este " +
                            "dispositivo. Puedes vincular tu cuenta después desde Ajustes.",
                    fontSize = 11.sp,
                    color = Palette.Muted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Ver términos y condiciones",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.Blue,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onShowTerms() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Spacer(Modifier.height(28.dp))
    }
}

/**
 * Aviso de error. Sin cuenta disponible no es lo mismo que un fallo real, asi
 * que se muestra como informacion y no como alarma roja: en ese caso el
 * usuario solo tiene que usar el boton de abajo.
 */
@Composable
private fun AvisoError(mensaje: String) {
    val esFaltaDeCuenta = mensaje.contains("No hay cuentas", ignoreCase = true)
    val borde = if (esFaltaDeCuenta) Palette.Track else Palette.Red

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Surface)
            .border(2.dp, borde, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = if (esFaltaDeCuenta) "SIN CUENTAS EN EL DISPOSITIVO" else "NO SE PUDO INICIAR SESIÓN",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (esFaltaDeCuenta) Palette.Muted else Palette.Red,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = mensaje,
            fontSize = 13.sp,
            color = Palette.Black,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun BotonLogin(label: String, primary: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(if (primary) Palette.DarkYellow else Palette.Surface)
            .border(3.dp, Palette.Black, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Palette.Black,
            letterSpacing = 0.8.sp
        )
    }
}

/** El mismo robot del juego, dibujado con formas. */
@Composable
private fun RobotLogo() {
    Canvas(Modifier.size(104.dp)) {
        val s = size.minDimension * 0.72f
        val left = (size.width - s) / 2f
        val top = (size.height - s) / 2f + s * 0.10f
        val stroke = s * 0.085f

        // Antenas
        drawRect(Palette.Ink, Offset(left + s * 0.15f, top - s * 0.26f), Size(s * 0.06f, s * 0.26f))
        drawCircle(Palette.Red, s * 0.08f, Offset(left + s * 0.18f, top - s * 0.28f))
        drawRect(Palette.Ink, Offset(left + s * 0.79f, top - s * 0.26f), Size(s * 0.06f, s * 0.26f))
        drawCircle(Palette.Blue, s * 0.08f, Offset(left + s * 0.82f, top - s * 0.28f))

        // Cuerpo
        val r = androidx.compose.ui.geometry.CornerRadius(s * 0.22f, s * 0.22f)
        drawRoundRect(Palette.DarkYellow, Offset(left, top), Size(s, s), r)
        drawRoundRect(Palette.Ink, Offset(left, top), Size(s, s), r, style = Stroke(stroke))

        // Ojos
        drawRect(Palette.Surface, Offset(left + s * 0.20f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.22f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))
        drawRect(Palette.Surface, Offset(left + s * 0.65f, top + s * 0.30f), Size(s * 0.15f, s * 0.12f))
        drawRect(Palette.Ink, Offset(left + s * 0.67f, top + s * 0.32f), Size(s * 0.11f, s * 0.08f))

        // Boca
        drawRect(Palette.Surface, Offset(left + s * 0.35f, top + s * 0.60f), Size(s * 0.30f, s * 0.06f))
        drawRect(Palette.Ink, Offset(left + s * 0.36f, top + s * 0.61f), Size(s * 0.28f, s * 0.04f))
    }
}
