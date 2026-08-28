package com.bitlogic.pokebit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bitlogic.pokebit.data.ProgressStore
import com.bitlogic.pokebit.game.GameMode
import com.bitlogic.pokebit.game.LevelData
import com.bitlogic.pokebit.game.LevelLoader
import com.bitlogic.pokebit.ui.GameScreen
import com.bitlogic.pokebit.ui.MenuScreen
import com.bitlogic.pokebit.ui.Palette

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa real. El bloqueo vertical va en el manifest.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val store = ProgressStore(this)
        val level = LevelLoader.fromAssets(this, "levels/level_01.json")

        setContent {
            PokeBitApp(store = store, level = level, onExit = { finish() })
        }
    }
}

private sealed interface Screen {
    object Menu : Screen
    class Playing(val mode: GameMode) : Screen
}

@Composable
private fun PokeBitApp(store: ProgressStore, level: LevelData, onExit: () -> Unit) {

    var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var best by remember { mutableIntStateOf(store.bestScore) }

    Box(Modifier.fillMaxSize().background(Palette.Bg)) {
        when (val current = screen) {
            is Screen.Menu -> MenuScreen(
                bestScore = best,
                onPlay = { screen = Screen.Playing(GameMode.LEVEL) },
                onEndless = { screen = Screen.Playing(GameMode.ENDLESS) },
                onInventory = {screen = Screen.Playing(GameMode.INVENTORY)},
                onExit = onExit
            )

            is Screen.Playing -> GameScreen(
                mode = current.mode,
                level = if (current.mode == GameMode.LEVEL) level else null,
                bestScore = best,
                onRunFinished = { score, pokeballs, completed ->
                    if (score > best) {
                        best = score
                        store.bestScore = score
                    }
                    if (current.mode == GameMode.LEVEL) {
                        store.savePokeballs(level.id, pokeballs)
                        if (completed) store.markLevelCompleted(level.id)
                    }
                },
                onMenu = { screen = Screen.Menu }
            )
        }
    }
}
