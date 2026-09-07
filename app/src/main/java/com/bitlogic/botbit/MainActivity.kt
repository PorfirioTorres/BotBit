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
import com.bitlogic.botbit.game.GameMode
import com.bitlogic.botbit.game.LevelLoader
import com.bitlogic.botbit.game.missions.MissionManager
import com.bitlogic.botbit.game.missions.MissionType
import com.bitlogic.botbit.ui.GameScreen
import com.bitlogic.botbit.ui.MenuScreen
import com.bitlogic.botbit.ui.Palette
import com.bitlogic.botbit.ui.ScreenInventory
import com.bitlogic.botbit.ui.inventory.InventoryViewModel
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
        
        val level = remember { LevelLoader.fromAssets(this, "levels/level_01.json") }
        
        var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
        var best by remember { mutableIntStateOf(store.bestScore) }
        
        var selectedCharacter by remember { mutableStateOf(store.getSelectedCharacter()) }

        Box(Modifier.fillMaxSize().background(Palette.Bg)) {
            when (val current = screen) {
                is Screen.Menu -> MenuScreen(
                    bestScore = best,
                    onPlay = { screen = Screen.Playing(GameMode.LEVEL) },
                    onEndless = { screen = Screen.Playing(GameMode.ENDLESS) },
                    onInventory = { screen = Screen.Inventory },
                    onMissions = { screen = Screen.Missions },
                    onExit = { finish() }
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
                    level = if (current.mode == GameMode.LEVEL) level else null,
                    bestScore = best,
                    selectedCharacter = selectedCharacter,
                    missionManager = missionManager,
                    onRunFinished = { score, coins, completed ->
                        if (score > best) {
                            best = score
                            store.bestScore = score
                        }
                        if (current.mode == GameMode.LEVEL) {
                            store.saveCoins(level.id, coins)
                            if (completed) {
                                store.markLevelCompleted(level.id)
                                missionManager.updateProgress(MissionType.COMPLETE_LEVEL)
                            }
                        }
                    },
                    onMenu = { screen = Screen.Menu }
                )
            }
        }
    }

    private sealed interface Screen {
        object Menu : Screen
        object Inventory : Screen
        object Missions : Screen
        class Playing(val mode: GameMode) : Screen
    }
}
