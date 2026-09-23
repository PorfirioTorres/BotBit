# BotBit

BotBit es un videojuego para Android escrito en Kotlin con Jetpack Compose.
Es un plataformero arcade de un solo personaje jugable en tres modos con
mecánicas distintas entre sí, con un sistema de personajes desbloqueables,
una economía de monedas compartida entre modos, misiones con recompensa y
un motor de juego propio construido sobre Canvas de Compose (no usa un
motor externo como LibGDX ni Unity).

Este documento describe qué es el proyecto, cómo está organizado y qué
tecnologías usa, pensado como referencia para quien se integre al
repositorio.

## Tabla de contenido

- [Qué es este proyecto](#qué-es-este-proyecto)
- [Modos de juego](#modos-de-juego)
- [Arquitectura](#arquitectura)
- [Tecnologías y librerías](#tecnologías-y-librerías)
- [Estructura de carpetas](#estructura-de-carpetas)
- [Persistencia de datos](#persistencia-de-datos)
- [Personajes y física basada en estadísticas](#personajes-y-física-basada-en-estadísticas)
- [Misiones](#misiones)
- [Autenticación y servicios de Google](#autenticación-y-servicios-de-google)
- [Herramientas de verificación](#herramientas-de-verificación)
- [Requisitos para compilar](#requisitos-para-compilar)

## Qué es este proyecto

BotBit gira alrededor de un solo robot controlable, con tres formas de
jugarlo que comparten personaje, monedero y sistema de misiones pero que
tienen mecánicas de física y de entrada completamente distintas entre sí.
La idea central del proyecto es que agregar un modo nuevo no debería
requerir tocar el código de los modos existentes: cada modo es una
implementación independiente de una misma interfaz de juego.

## Modos de juego

### Carrera (Runner)

El modo original del juego. El robot corre automáticamente hacia adelante
sobre un nivel con picos y pozos definidos en un archivo JSON; el jugador
solo controla el salto con un toque. Un solo error termina el intento. Tiene
dos variantes: nivel con longitud y meta fijas, o modo infinito con
generación procedural de obstáculos.

### La Torre

Modo de ascenso vertical inspirado en juegos de plataformas de precisión.
No hay desplazamiento automático: el jugador mantiene presionado para
cargar la fuerza del salto y suelta indicando una dirección (izquierda,
arriba o derecha) para saltar entre plataformas. No existe condición de
derrota: caer solo hace perder altura, no termina la partida. El nivel
está dividido en salas con cámara fija por sala en vez de scroll continuo,
y el progreso se guarda en checkpoints locales.

### Arena

Modo de supervivencia con vista cenital, inspirado en el género de
"survivors". El jugador controla el movimiento del robot con un joystick
virtual (con dos variantes de comportamiento, flotante o fijo) mientras el
robot dispara automáticamente al enemigo más cercano. Los enemigos y los
proyectiles se gestionan con arreglos de tamaño fijo (pooling de objetos)
para evitar asignaciones de memoria durante la partida.

## Arquitectura

El juego se organiza alrededor de una interfaz central, `GameWorld`, que
define el contrato que debe cumplir cualquier modo:

```kotlin
interface GameWorld {
    val kind: GameKind
    val status: GameStatus
    val score: Int
    val progress: Float
    val coins: Int
    fun update(dt: Float)
    fun onInput(event: InputEvent)
    fun retry()
}
```

Cada modo (`World`, `TowerWorld`, `ArenaWorld`) es una implementación
independiente de esta interfaz. La pantalla de juego (`GameScreen`) no sabe
qué modo está corriendo: simplemente actualiza el `GameWorld` con un paso de
tiempo fijo y le pide a un renderer que lo dibuje, y elige los controles en
pantalla (toque simple, botones de carga, joystick) según el `GameKind`. La
entrada del jugador se traduce a un tipo sellado (`InputEvent`) común a los
tres modos antes de llegar a la lógica de juego, así que cada modo solo
interpreta los eventos que le sirven.

Reglas que sigue esta arquitectura, explícitas en el código:

- `update(dt)` siempre recibe el mismo paso de tiempo fijo; no hay delta
  variable dependiente del framerate.
- Ningún modo asigna memoria dentro de `update()`; se usan arreglos y pools
  fijos.
- El estado de física y el estado de animación se manejan por separado.

La navegación entre pantallas (menú, selector de modo, selector de nivel,
inventario, misiones, ajustes, juego) es manual: un estado de tipo sellado
(`Screen`) guardado con `remember { mutableStateOf(...) }` en `MainActivity`,
sin usar `NavHost` de Navigation Compose.

Las capas del código están separadas por paquete:

- `game`: lógica y física de los tres modos, entidades, configuración
  global y carga de niveles desde JSON.
- `ui`: pantallas y componentes de Compose, incluido el renderer del juego.
- `data`: persistencia local (progreso, misiones) y datos de personajes.
- `di`: módulo de inyección de dependencias.
- `utils`: utilidades transversales (analítica, tamaño de pantalla).

## Tecnologías y librerías

- **Lenguaje**: Kotlin 2.4.10
- **UI**: Jetpack Compose, con Compose BOM 2026.08.00 y Material 3
- **Inyección de dependencias**: Hilt 2.60.1
- **Concurrencia**: Kotlin Coroutines (`kotlinx-coroutines-android`,
  `kotlinx-coroutines-play-services`)
- **Persistencia local**: `SharedPreferences` (encapsulado en clases propias,
  sin Room ni DataStore)
- **Autenticación**: Firebase Auth con Credential Manager
  (`androidx-credentials`) y Google Identity (`googleid`) para el inicio de
  sesión con Google
- **Analítica y estabilidad**: Firebase Analytics y Firebase Crashlytics,
  vía el BOM de Firebase 34.18.0
- **Renderizado del juego**: Canvas de Compose (`androidx.compose.ui`),
  sin motor de juego externo
- **Build**: Gradle con Kotlin DSL (`build.gradle.kts`) y catálogo de
  versiones (`libs.versions.toml`)
- **Android Gradle Plugin**: 9.3.3
- **compileSdk / targetSdk**: 37 · **minSdk**: 24
- **Herramientas de verificación** (fuera de la app): scripts en Python 3,
  sin dependencias externas, que simulan la física del juego para validar
  niveles antes de integrarlos

## Estructura de carpetas

```
app/src/main/java/com/bitlogic/botbit/
├── MainActivity.kt              punto de entrada, navegación manual
├── MainApplication.kt           inicialización de Hilt y analítica
├── data/
│   ├── CharacterData.kt          catálogo de personajes y sus estadísticas
│   ├── MissionStore.kt           persistencia de progreso de misiones
│   ├── ProgressStore.kt          monedero, personajes desbloqueados,
│   │                             checkpoints, preferencias
│   └── repository/
│       └── SSORepositoryImpl.kt  inicio de sesión con Google
├── di/
│   └── DataModule.kt             módulo de Hilt
├── game/
│   ├── ArenaWorld.kt             lógica del modo Arena
│   ├── EndlessGenerator.kt       generación procedural del modo infinito
│   ├── Entities.kt               entidades compartidas de juego
│   ├── GameConfig.kt             constantes globales de física
│   ├── GameKind.kt               enum de los tres modos y su disponibilidad
│   ├── GameWorld.kt              interfaz común y eventos de entrada
│   ├── Level.kt                  carga de niveles desde JSON
│   ├── TowerWorld.kt             lógica del modo La Torre
│   ├── World.kt                  lógica del modo Carrera
│   └── missions/
│       ├── Mission.kt            modelo de misión
│       └── MissionManager.kt     lógica de progreso y recompensas
├── ui/
│   ├── Background.kt             fondos con degradado y parallax
│   ├── GameRenderer.kt           dibujo de los tres modos sobre Canvas
│   ├── GameScreen.kt             pantalla de juego y controles por modo
│   ├── LevelSelectScreen.kt      selector de nivel
│   ├── LevelTheme.kt             temas visuales por nivel
│   ├── MenuScreen.kt             menú principal
│   ├── ModeSelectScreen.kt       selector de modo de juego
│   ├── Palette.kt                paleta de colores de la interfaz
│   ├── RenderScratch.kt          buffers reutilizados por el renderer
│   ├── Screens.kt                pantallas de inventario y resultados
│   ├── TermsScreen.kt            términos y condiciones
│   ├── inventory/
│   │   └── InventoryViewModel.kt lógica de compra y selección de personaje
│   ├── login/
│   │   ├── SSOLoginScreen.kt     pantalla de inicio de sesión
│   │   ├── SSOState.kt           estados del inicio de sesión
│   │   └── SSOViewModel.kt       lógica de inicio de sesión
│   ├── missions/
│   │   ├── MissionCard.kt        tarjeta de misión individual
│   │   └── MissionScreen.kt      pantalla de lista de misiones
│   └── theme/
│       ├── Color.kt, Theme.kt, Type.kt   tema de Material 3
└── utils/
    ├── AnalyticsHelper.kt        envoltura sobre Firebase Analytics/Crashlytics
    └── ScreenUtils.kt            utilidades de tamaño de pantalla
```

Los niveles del modo Carrera y el mapa del modo La Torre viven como JSON en
`app/src/main/assets/levels/`.

## Persistencia de datos

Todo el progreso se guarda localmente con `SharedPreferences`, sin base de
datos ni sincronización en la nube:

- **`ProgressStore`**: monedero global (`totalCoins`), personajes
  desbloqueados, personaje seleccionado, mejor puntaje, checkpoints de La
  Torre y preferencias de interfaz (por ejemplo, el modo del joystick).
- **`MissionStore`**: progreso numérico de cada misión y si ya fue
  completada.

No hay todavía una capa de sincronización remota: el inicio de sesión con
Google identifica al usuario para analítica, pero el progreso vive
únicamente en el dispositivo.

## Personajes y física basada en estadísticas

Cada personaje jugable (`CharacterData`) tiene dos estadísticas propias,
salto y velocidad, expresadas como enteros pequeños. El modo Carrera
convierte esas estadísticas en los parámetros reales de física con dos
fórmulas fijas:

```
velocidadDeSalto = 17.9 + salto * 0.3
multiplicadorDeVelocidad = 0.95 + velocidad * 0.03
```

Estas fórmulas se eligieron simulando cada combinación de nivel y personaje
para garantizar que ningún personaje vuelva imposible un nivel existente
(ver la sección de herramientas de verificación).

## Misiones

`MissionManager` mantiene una lista de misiones activas con un tipo
(completar nivel, recolectar monedas, alcanzar puntaje, saltar cierta
cantidad de veces, sobrevivir cierto tiempo en el modo infinito), un
objetivo numérico y una recompensa en monedas y, opcionalmente, un
personaje. El progreso de cada misión se actualiza desde los distintos
modos de juego a través de `updateProgress(tipo, cantidad)`, y al llegar al
objetivo la recompensa se aplica automáticamente sobre `ProgressStore`.

## Autenticación y servicios de Google

El inicio de sesión usa Firebase Auth junto con Credential Manager para
autenticar con una cuenta de Google. Esto requiere un archivo
`google-services.json` propio del proyecto en Firebase, colocado en
`app/google-services.json` (no se versiona en el repositorio por
convención). Sin ese archivo, el proyecto no compila, porque el identificador
de cliente OAuth que usa el inicio de sesión se genera automáticamente a
partir de él.

Además de autenticación, el proyecto usa:

- **Firebase Analytics**, para registrar cambios de pantalla y el resultado
  de cada partida.
- **Firebase Crashlytics**, para el reporte automático de errores no
  controlados.

## Herramientas de verificación

Fuera de la aplicación, el repositorio incluye scripts en Python que
simulan la física del juego de forma independiente al motor real, para
poder validar el diseño de niveles antes de integrarlos:

- **`verify.py` / `stats_matrix.py`**: simulan el modo Carrera contra cada
  nivel y cada personaje para confirmar que el nivel se puede completar sin
  necesidad de abrir la aplicación.
- **`verify_tower.py`**: construye un grafo de alcanzabilidad del modo La
  Torre, donde cada plataforma es un nodo y existe una arista entre dos
  plataformas si alguna combinación de carga y dirección de salto permite
  llegar de una a la otra. Confirma que ninguna plataforma quede aislada y
  que la cima sea alcanzable desde la base.

Las constantes físicas usadas en estos scripts (gravedad, velocidad de
salto mínima y máxima, velocidad lateral, rebote contra paredes) están
copiadas manualmente de `GameConfig.kt` y `TowerWorld.kt`, así que cualquier
cambio a esas constantes en el código Kotlin debe reflejarse también en los
scripts para que la verificación siga siendo válida.

## Requisitos para compilar

- Android Studio con soporte para Kotlin 2.4 y Compose.
- JDK compatible con Android Gradle Plugin 9.3.3.
- Un archivo `google-services.json` válido, descargado desde la consola de
  Firebase para el proyecto correspondiente, colocado en `app/`. La huella
  SHA-1 registrada en ese archivo debe corresponder al keystore con el que
  se firme la compilación (debug o release según el caso), o el inicio de
  sesión con Google fallará aunque el resto de la app compile sin errores.
