# Plan de Integración: Firebase, Analytics y Crashlytics para BotBit

Este documento describe el paso a paso para integrar el ecosistema de monitoreo y análisis de Google en el proyecto BotBit.

## 1. Configuración en la Consola de Firebase
1.  **Registro**: Ir a [Firebase Console](https://console.firebase.google.com/) y crear el proyecto "BotBit".
2.  **App Android**: Registrar el paquete `com.bitlogic.botbit`.
3.  **SHA-1**: Obtener la huella digital ejecutando `./gradlew signingReport` y pegarla en la configuración de la app en la consola.
4.  **Descarga**: Colocar el archivo `google-services.json` en la carpeta `app/`.

## 2. Modificación de Archivos Gradle

### build.gradle.kts (Raíz del Proyecto)
Añadir los plugins en el bloque `plugins`:
```kotlin
plugins {
    // ...
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
```

### app/build.gradle.kts (Módulo App)
Aplicar los plugins y declarar las dependencias:
```kotlin
plugins {
    // ...
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Analytics y Crashlytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
}
```

## 3. Implementación de Google Analytics
Crear un objeto helper para centralizar el registro de eventos de juego:
- **Eventos Críticos**: `level_start`, `level_complete`, `game_over`, `character_unlocked`.
- **Propiedades de Usuario**: `favorite_character`, `max_level_reached`.

## 4. Implementación de Crashlytics
- **Reporte Automático**: Captura errores fatales (crashes) sin código adicional.
- **Errores No Fatales**: Usar `FirebaseCrashlytics.getInstance().recordException(e)` en bloques try-catch críticos (ej: carga de JSON).
- **Custom Keys**: Registrar el ID del nivel actual para saber dónde falló la app.

## 5. Pruebas de Verificación
1.  **Forzar Crash**: Añadir un botón temporal que lance una excepción para verificar que aparezca en el panel de Crashlytics.
2.  **DebugView**: Usar la herramienta DebugView de Firebase para ver los eventos de Analytics en tiempo real.

---
**Estado del Plan**: Pendiente de ejecución.
