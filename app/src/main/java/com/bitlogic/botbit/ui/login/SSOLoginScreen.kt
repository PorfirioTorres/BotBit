package com.bitlogic.botbit.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentState = state) {
                is SSOState.CheckingExistingSession -> {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
                is SSOState.Authenticated -> {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Bienvenido, ${currentState.user.displayName}", color = Color.White)
                            Button(onClick = { viewModel.signOut(context) }) {
                                Text("Cerrar Sesión")
                            }
                        }
                    }
                }
                is SSOState.Idle, is SSOState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (currentState is SSOState.Error) {
                            Text(text = currentState.message, color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
                        }
                        Button(
                            onClick = { viewModel.initiateInteractiveSSO(context) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Iniciar sesión con Google SSO")
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            TextButton(onClick = onShowTerms) {
                Text("View Terms & Conditions", color = Color.LightGray)
            }
        }
    }
}
