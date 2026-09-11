package com.bitlogic.botbit.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberScreenInfo(): ScreenInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    
    return ScreenInfo(
        width = screenWidth,
        height = screenHeight,
        isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        isTablet = screenWidth >= 600,
        isPhone = screenWidth < 600
    )
}

data class ScreenInfo(
    val width: Int,
    val height: Int,
    val isLandscape: Boolean,
    val isTablet: Boolean,
    val isPhone: Boolean
)
