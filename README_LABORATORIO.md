# Guía de Laboratorio: Implementación de Jetpack Compose, Hilt, Animaciones y Validaciones en Android

Este laboratorio está diseñado para guiar paso a paso a los estudiantes en la creación de un módulo de **Gestión de Eventos** utilizando **Jetpack Compose**, **Hilt (Inyección de Dependencias)**, **Navegación con NavHost**, **Animaciones (`animateContentSize`, `animateDpAsState`)**, **Swipe to Dismiss**, **Validaciones en tiempo real (`LaunchedEffect`)** y **Accesibilidad con TalkBack**.

---

## 🎯 Objetivos del Laboratorio

1. Configurar Hilt e inyección de dependencias en una aplicación Android con Jetpack Compose.
2. Crear un componente de **Tarjeta Plegable (`EventCard`)** expandible/colapsable.
3. Construir una **lista interactiva con Swipe to Dismiss** para eliminar elementos.
4. Aplicar animaciones de entrada con `animateDpAsState`.
5. Implementar navegación interna con **Compose `NavHost`** entre Lista y Formulario.
6. Construir un **Formulario reactivo con validaciones** en tiempo real mediante `LaunchedEffect`.
7. Aplicar buenas prácticas de **Accesibilidad TalkBack** (`contentDescription`, `semantics`).

---

## 📋 Prerrequisitos

- Android Studio Iguana / Jellyfish o superior.
- JDK 17 o 21 configurado.
- Kotlin 1.9+ / Gradle 8+.

---

## 🛠️ Paso 1: Configurar Dependencias en Gradle

### 1.1 Modificar `gradle/libs.versions.toml`
Añade las dependencias para Compose y Hilt en el archivo del catálogo de versiones:

```toml
[versions]
# ... otras versiones ...
hilt = "2.51.1"
hiltCompose = "1.2.0"
composeBom = "2024.05.00"

[libraries]
# Compose BOM
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version = "2.7.7" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltCompose" }

[plugins]
# Hilt Plugin
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### 1.2 Modificar `build.gradle.kts` (Proyecto Raíz)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt.android) apply false
}
```

### 1.3 Modificar `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    // ...
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling-preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}
```

---

## 🚀 Paso 2: Inicializar Hilt en la Aplicación

### 2.1 Crear `MainApplication.kt`
En el paquete `com.example.appclase`:

```kotlin
package com.example.appclase

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application()
```

### 2.2 Registrar en `AndroidManifest.xml`
Asegúrate de agregar `android:name=".MainApplication"` en la etiqueta `<application>`:

```xml
<application
    android:name=".MainApplication"
    ... >
</application>
```

---

## 📦 Paso 3: Modelo y Repositorio de Eventos

### 3.1 Modelo de Datos (`model/Event.kt`)
```kotlin
package com.example.appclase.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val isAdded: Boolean = false
)
```

### 3.2 Repositorio de Eventos (`repository/EventRepository.kt`)
```kotlin
package com.example.appclase.repository

import com.example.appclase.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor() {
    private val _events = MutableStateFlow<List<Event>>(
        listOf(
            Event("1", "Taller de Jetpack Compose", "Aprende UI declarativa", "2026-09-10"),
            Event("2", "Conferencia Hilt & DI", "Arquitectura limpia en Android", "2026-09-15")
        )
    )
    val events: StateFlow<List<Event>> = _events

    fun addEvent(title: String, description: String, date: String) {
        val newEvent = Event(
            id = System.currentTimeMillis().toString(),
            title = title,
            description = description,
            date = date,
            isAdded = true
        )
        _events.value = _events.value + newEvent
    }

    fun deleteEvent(eventId: String) {
        _events.value = _events.value.filterNot { it.id == eventId }
    }
}
```

---

## 🧠 Paso 4: ViewModel con Hilt (`viewmodel/EventViewModel.kt`)

```kotlin
package com.example.appclase.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appclase.model.Event
import com.example.appclase.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    val events: StateFlow<List<Event>> = repository.events

    fun addEvent(title: String, description: String, date: String) {
        repository.addEvent(title, description, date)
    }

    fun deleteEvent(eventId: String) {
        repository.deleteEvent(eventId)
    }
}
```

---

## 🎴 Paso 5: Tarjeta Plegable (`ui/components/EventCard.kt`)

Implementa la tarjeta plegable con `animateContentSize()` y accesibilidad TalkBack:

```kotlin
package com.example.appclase.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.appclase.model.Event

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .semantics {
                contentDescription = if (isExpanded) {
                    "Tarjeta de evento ${event.title}, desplegada."
                } else {
                    "Tarjeta de evento ${event.title}, colapsada. Toca para desplegar."
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.semantics {
                        contentDescription = if (isExpanded) "Colapsar detalles de ${event.title}" else "Expandir detalles de ${event.title}"
                    }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Fecha: ${event.date}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}
```

---

## 🖐️ Paso 6: Elemento con Swipe to Dismiss y Animación de Entrada (`ui/components/EventItem.kt`)

```kotlin
package com.example.appclase.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.appclase.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(
    event: Event,
    onDelete: (String) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete(event.id)
                true
            } else {
                false
            }
        }
    )

    // Animación al agregar eventos
    val offset by animateDpAsState(
        targetValue = if (event.isAdded) 0.dp else 100.dp,
        label = "offsetAnimation"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar evento",
                    tint = Color.White
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .offset(y = offset)
                    .semantics {
                        contentDescription = "Evento: ${event.title}. Desliza a los lados para eliminar."
                    }
            ) {
                EventCard(event = event)
            }
        }
    )
}
```

---

## 📝 Paso 7: Formulario con Validación y `LaunchedEffect` (`ui/screens/EventFormScreen.kt`)

```kotlin
package com.example.appclase.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    onSaveEvent: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    // Validación usando LaunchedEffect al cambiar campos o enviar
    LaunchedEffect(title, description, isSubmitted) {
        if (isSubmitted) {
            titleError = if (title.isBlank()) "¡Título requerido!" else null
            descriptionError = if (description.isBlank()) "¡Descripción requerida!" else null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Evento") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Regresar a la lista de eventos" }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del Evento") },
                isError = titleError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo para ingresar el título del evento" }
            )
            if (title.isEmpty() && isSubmitted) {
                Text(
                    text = "¡Título requerido!",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                isError = descriptionError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo para ingresar la descripción del evento" }
            )
            if (description.isEmpty() && isSubmitted) {
                Text(
                    text = "¡Descripción requerida!",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo para ingresar la fecha del evento" }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSubmitted = true
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSaveEvent(title, description, if (date.isBlank()) "2026-09-03" else date)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Guardar evento nuevo" }
            ) {
                Text("Guardar Evento")
            }
        }
    }
}
```

---

## 🗺️ Paso 8: Configurar NavHost y HiltViewModel (`ui/navigation/EventsNavHost.kt`)

```kotlin
package com.example.appclase.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appclase.ui.screens.EventFormScreen
import com.example.appclase.ui.screens.EventListScreen
import com.example.appclase.viewmodel.EventViewModel

@Composable
fun EventsNavHost(
    viewModel: EventViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val events by viewModel.events.collectAsState()

    NavHost(navController = navController, startDestination = "event_list") {
        composable("event_list") {
            EventListScreen(
                events = events,
                onAddClick = { navController.navigate("event_form") },
                onDeleteEvent = { viewModel.deleteEvent(it) }
            )
        }
        composable("event_form") {
            EventFormScreen(
                onSaveEvent = { title, desc, date ->
                    viewModel.addEvent(title, desc, date)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 🧩 Paso 9: Integración en Fragmento (`EventsFragment.kt`)

```kotlin
package com.example.appclase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.appclase.ui.navigation.EventsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EventsNavHost()
            }
        }
    }
}
```

---

## ♿ Paso 10: Validación de Accesibilidad con TalkBack

1. En el dispositivo o emulador Android, dirígete a `Ajustes > Accesibilidad > TalkBack` y actívalo.
2. Navega por la aplicación comprobando que cada elemento interactivo lea en voz alta su `contentDescription` descriptiva.
3. Verifica que la tarjeta lea claramente su estado (desplegada o colapsada) y que los botones tengan etiquetas claras de acción ("Expandir detalles", "Guardar evento nuevo").

---

## ✅ Criterios de Evaluación del Laboratorio

| Criterio | Puntos |
| :--- | :--- |
| **Inyección de Dependencias Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, `hiltViewModel()`) | 20% |
| **Tarjeta Plegable** con `animateContentSize` y expansión/colapso | 20% |
| **Transición NavHost** entre lista y formulario | 15% |
| **Swipe to Dismiss y Animación** `animateDpAsState` | 15% |
| **Validaciones del Formulario** con `LaunchedEffect` y mensajes de error | 15% |
| **Accesibilidad TalkBack** (`contentDescription`, `semantics`) | 15% |
