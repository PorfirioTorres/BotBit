package com.bitlogic.botbit.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    navigationIconContentColor = Palette.Ink,
                    containerColor = Palette.Bg,
                    titleContentColor = Palette.Ink
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        // El HTML trae estilos claros y oscuros. Se le pone
                        // data-theme para que siga el ajuste de la app y no
                        // el del telefono.
                        val tema = if (Palette.isDark) "dark" else "light"
                        val html = context.assets.open("terms.html")
                            .bufferedReader().use { it.readText() }
                            .replaceFirst("<html", "<html data-theme=\"$tema\"")
                        loadDataWithBaseURL(
                            "file:///android_asset/", html, "text/html", "utf-8", null
                        )
                    }
                }
            )
        }
    }
}
