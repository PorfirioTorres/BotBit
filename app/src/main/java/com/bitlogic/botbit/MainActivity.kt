package com.bitlogic.botbit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bitlogic.botbit.data.CloudStore
import com.bitlogic.botbit.data.MissionStore
import com.bitlogic.botbit.data.ProgressStore
import com.bitlogic.botbit.game.GameKind
import com.bitlogic.botbit.game.GameMode
import com.bitlogic.botbit.game.LevelData
import com.bitlogic.botbit.game.LevelLoader
import com.bitlogic.botbit.game.missions.MissionManager
import com.bitlogic.botbit.game.missions.MissionType
import com.bitlogic.botbit.ui.GameScreen
import com.bitlogic.botbit.ui.LevelSelectScreen
import com.bitlogic.botbit.ui.MenuScreen
import com.bitlogic.botbit.ui.ModeSelectScreen
import com.bitlogic.botbit.ui.Palette
import com.bitlogic.botbit.ui.ScreenInventory
import com.bitlogic.botbit.ui.SettingsScreen
import com.bitlogic.botbit.ui.TermsScreen
import com.bitlogic.botbit.ui.ThemeMode
import com.bitlogic.botbit.ui.inventory.InventoryViewModel
import com.bitlogic.botbit.ui.login.SSOLoginScreen
import com.bitlogic.botbit.ui.login.SSOState
import com.bitlogic.botbit.ui.login.SSOViewModel
import com.bitlogic.botbit.ui.missions.MissionScreen
import com.bitlogic.botbit.utils.AnalyticsHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Se fija el tema ANTES del primer frame para que no parpadee en claro
        // al abrir la app en modo oscuro.
        Palette.isDark = when (ThemeMode.from(ProgressStore(this).themeMode)) {
            ThemeMode.CLARO -> false
            ThemeMode.OSCURO -> true
            ThemeMode.SISTEMA -> (resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        setContent {
            BotBitApp()
        }
    }

    @Composable
    private fun BotBitApp() {
        val store = remember { ProgressStore(this) }
        val cloud = remember { CloudStore(store) }
        val missionStore = remember { MissionStore(this) }
        val missionManager = remember { MissionManager(missionStore, store) }
        val inventoryViewModel: InventoryViewModel = hiltViewModel()
        val ssoViewModel: SSOViewModel = hiltViewModel()
        
        val ssoState by ssoViewModel.ssoState.collectAsState()

        LaunchedEffect(ssoState) {
            if (ssoState is SSOState.Authenticated) {
                cloud.syncOnLogin()
            }
        }
        
        val levels = remember {
            listOf("level_01.json", "level_02.json", "level_03.json").mapNotNull { file ->
                runCatching { LevelLoader.fromAssets(this, "levels/$file") }.getOrNull()
            }
        }

        val towerLevel = remember {
            runCatching { LevelLoader.fromAssets(this, "levels/tower_map.json") }.getOrNull()
        }
        
        var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
        var best by remember { mutableIntStateOf(store.bestScore) }
        
        var selectedCharacter by remember { mutableStateOf(store.getSelectedCharacter()) }

        // ---- Tema claro / oscuro ----
        var themeMode by remember { mutableStateOf(ThemeMode.from(store.themeMode)) }
        val systemDark = isSystemInDarkTheme()
        val dark = when (themeMode) {
            ThemeMode.CLARO -> false
            ThemeMode.OSCURO -> true
            ThemeMode.SISTEMA -> systemDark
        }
        // Palette.isDark es estado de Compose: cambiarlo repinta toda la app.
        SideEffect { Palette.isDark = dark }

        // Los componentes de Material (iconos, sliders, barra de Terminos)
        // toman sus colores de aqui, no de Palette.
        val colorScheme = if (dark) {
            darkColorScheme(
                primary = Palette.DarkYellow, onPrimary = Palette.OnAccent,
                background = Palette.Dark.Bg, onBackground = Palette.Dark.Ink,
                surface = Palette.Dark.Surface, onSurface = Palette.Dark.Ink
            )
        } else {
            lightColorScheme(
                primary = Palette.DarkYellow, onPrimary = Palette.OnAccent,
                background = Palette.Light.Bg, onBackground = Palette.Light.Ink,
                surface = Palette.Light.Surface, onSurface = Palette.Light.Ink
            )
        }
        val contentColor = if (dark) Palette.Dark.Ink else Palette.Light.Ink

        LaunchedEffect(screen) {
            AnalyticsHelper.logScreenView(screen.javaClass.simpleName)
        }

        MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(Modifier.fillMaxSize().background(Palette.Bg)) {
            if (!ssoState.canPlay && screen != Screen.Terms) {
                SSOLoginScreen(
                    viewModel = ssoViewModel,
                    onShowTerms = { screen = Screen.Terms }
                )
            } else {
                when (val current = screen) {
                    is Screen.Terms -> TermsScreen(onBack = { 
                        if (ssoState.canPlay) screen = Screen.Menu
                        else screen = Screen.Menu
                    })

                    is Screen.Menu -> MenuScreen(
                        onPlay = { screen = Screen.ModeSelect },
                        onInventory = { screen = Screen.Inventory },
                        onMissions = { screen = Screen.Missions },
                        onTerms = { screen = Screen.Terms },
                        onSettings = { screen = Screen.Settings },
                        onExit = { finish() }
                    )

                    is Screen.ModeSelect -> ModeSelectScreen(
                        onSelect = { kind, mode ->
                            screen = if (kind == GameKind.RUNNER && mode == GameMode.LEVEL) {
                                Screen.LevelSelect
                            } else if (kind == GameKind.TOWER) {
                                Screen.Playing(kind, mode, towerLevel)
                            } else {
                                Screen.Playing(kind, mode, null)
                            }
                        },
                        onBack = { screen = Screen.Menu }
                    )

                    is Screen.LevelSelect -> LevelSelectScreen(
                        levels = levels,
                        isUnlocked = { i -> i == 0 || store.isLevelCompleted(levels[i - 1].id) },
                        coinsFor = { lv -> store.coinsFor(lv.id) },
                        bestScoreFor = { lv -> store.bestScoreForLevel(lv.id) },
                        isCompleted = { lv -> store.isLevelCompleted(lv.id) },
                        onSelect = { lv -> screen = Screen.Playing(GameKind.RUNNER, GameMode.LEVEL, lv) },
                        onBack = { screen = Screen.ModeSelect }
                    )

                    is Screen.Inventory -> ScreenInventory(
                        onBack = { 
                            selectedCharacter = store.getSelectedCharacter()
                            screen = Screen.Menu 
                        },
                        viewModel = inventoryViewModel
                    )

                    is Screen.Missions -> MissionScreen(
                        missionManager = missionManager,
                        onBack = { screen = Screen.Menu }
                    )

                    is Screen.Settings -> SettingsScreen(
                        store = store,
                        userEmail = FirebaseAuth.getInstance().currentUser?.email,
                        onSignOut = {
                            // Antes solo se cerraba sesion en Firebase y el
                            // SSOViewModel seguia en Authenticated: el menu
                            // seguia abierto pero la nube ya no recibia nada.
                            ssoViewModel.signOut(this@MainActivity)
                            screen = Screen.Menu
                        },
                        onBack = { screen = Screen.Menu },
                        themeMode = themeMode,
                        onThemeModeChange = { modo ->
                            themeMode = modo
                            store.themeMode = modo.name
                        },
                        onCrashTest = { throw RuntimeException("Crash de prueba - BotBit") }
                    )

                    is Screen.Playing -> GameScreen(
                        kind = current.kind,
                        mode = current.mode,
                        level = current.level,
                        store = store,
                        bestScore = best,
                        selectedCharacter = selectedCharacter,
                        missionManager = missionManager,
                        onRunFinished = { score, coins, completed ->
                            AnalyticsHelper.logGameOver(score, coins, if (completed) "completed" else "dead")
                            
                            // 1. Sumar monedas al monedero global (independiente del modo)
                            store.addCoins(coins)
                            
                            // 2. Guardar mejor puntaje por tipo de juego
                            store.saveBestScore(current.kind, score)
                            val played = current.level
                            if (played != null) {
                                store.saveBestScoreForLevel(played.id, score)
                                store.saveCoins(played.id, coins)
                                if (completed) {
                                    store.markLevelCompleted(played.id)
                                    missionManager.updateProgress(MissionType.COMPLETE_LEVEL)
                                }
                            }

                            // 3. Misiones nuevas
                            if (completed || score > 0) {
                                missionManager.setProgress(MissionType.COINS_IN_ONE_RUN, coins)
                            }
                            store.totalDistance += score
                            missionManager.setProgress(MissionType.TOTAL_DISTANCE, store.totalDistance)

                            // 4. Sincronización en la nube
                            cloud.push()
                        },
                        onMenu = { screen = Screen.Menu }
                    )
                }
            }
        }
        }
        }
    }

    private sealed interface Screen {
        object Menu : Screen
        object Inventory : Screen
        object Missions : Screen
        object ModeSelect : Screen
        object LevelSelect : Screen
        object Terms : Screen
        object Settings : Screen
        class Playing(val kind: GameKind, val mode: GameMode, val level: LevelData?) : Screen
    }
}
