package com.bitlogic.botbit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.bitlogic.botbit.ui.TermsScreen
import com.bitlogic.botbit.ui.inventory.InventoryViewModel
import com.bitlogic.botbit.ui.login.SSOLoginScreen
import com.bitlogic.botbit.ui.login.SSOState
import com.bitlogic.botbit.ui.login.SSOViewModel
import com.bitlogic.botbit.ui.missions.MissionScreen
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

        setContent {
            BotBitApp()
        }
    }

    @Composable
    private fun BotBitApp() {
        val store = remember { ProgressStore(this) }
        val missionStore = remember { MissionStore(this) }
        val missionManager = remember { MissionManager(missionStore) }
        val inventoryViewModel: InventoryViewModel = hiltViewModel()
        val ssoViewModel: SSOViewModel = hiltViewModel()
        
        val ssoState by ssoViewModel.ssoState.collectAsState()
        
        // Los tres niveles. Antes solo se cargaba el primero, asi que los
        // niveles 2 y 3 existian en assets pero nunca se podian jugar.
        val levels = remember {
            listOf("level_01.json", "level_02.json", "level_03.json").mapNotNull { file ->
                runCatching { LevelLoader.fromAssets(this, "levels/$file") }.getOrNull()
            }
        }
        
        var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
        var best by remember { mutableIntStateOf(store.bestScore) }
        
        var selectedCharacter by remember { mutableStateOf(store.getSelectedCharacter()) }

        Box(Modifier.fillMaxSize().background(Palette.Bg)) {
            if (ssoState !is SSOState.Authenticated && screen != Screen.Terms) {
                SSOLoginScreen(
                    viewModel = ssoViewModel,
                    onShowTerms = { screen = Screen.Terms }
                )
            } else {
                when (val current = screen) {
                    is Screen.Terms -> TermsScreen(
                        onBack = { 
                            // If coming from login, go back to login logic is handled by if condition above
                            // If coming from menu, go back to menu.
                            screen = Screen.Menu 
                        }
                    )
                    is Screen.Menu -> MenuScreen(
                    onPlay = { screen = Screen.ModeSelect },
                    onInventory = { screen = Screen.Inventory },
                    onMissions = { screen = Screen.Missions },
                    onTerms = { screen = Screen.Terms },
                    onExit = { finish() }
                )

                    is Screen.ModeSelect -> ModeSelectScreen(
                        onSelect = { kind, mode ->
                            screen = if (kind == GameKind.RUNNER && mode == GameMode.LEVEL) {
                                Screen.LevelSelect
                            } else {
                                Screen.Playing(kind, mode, null)
                            }
                        },
                        onBack = { screen = Screen.Menu }
                    )

                    is Screen.LevelSelect -> LevelSelectScreen(
                    levels = levels,
                    // El primero siempre abierto; cada siguiente pide el anterior completado.
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

                    is Screen.Playing -> GameScreen(
                        mode = current.mode,
                        level = current.level,
                        bestScore = best,
                        selectedCharacter = selectedCharacter,
                        missionManager = missionManager,
                        onRunFinished = { score, coins, completed ->
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
                    },
                        onMenu = { screen = Screen.Menu }
                    )
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
        class Playing(val kind: GameKind, val mode: GameMode, val level: LevelData?) : Screen
    }
}
