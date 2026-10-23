package com.paintedmap.ui.theme

import android.widget.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimens {
    private val scale: Float

    @Composable
    get() = (LocalConfiguration.current.screenWidthDp / 360f).coerceIn(0.8f, 1.5f)

    val xs: Dp @Composable get() = (4 * scale).dp
    val sm: Dp @Composable get() = (8 * scale).dp
    val md: Dp @Composable get() = (12 * scale).dp
    val lg: Dp @Composable get() = (16 * scale).dp
    val xl: Dp @Composable get() = (24 * scale).dp

    val cornerSm: Dp = 12.dp
    val cornerMd: Dp = 20.dp
    val cornerLg: Dp = 28.dp

    val statSpacerWeight: Float = 0.15f
    val statContentWeight: Float = 0.7f

    val statTrackThickness: Dp @Composable get() = (10 * scale).dp
    val statIndicatorWidth: Dp @Composable get() = (40 * scale).dp
    val statTrackBlendFraction: Float = 0.3f
    val statLabelAlpha: Float = 0.6f

    val profileBarHeight: Dp @Composable get() = (120 * scale).dp
    val mapGlassHeight: Dp @Composable get() = (200 * scale).dp
    val navigationBarHeight: Dp @Composable get() = (80 * scale).dp
    val searchBarHeight: Dp @Composable get() = (56 * scale).dp
    val regionsHeight: Dp @Composable get() = (72 * scale).dp
    val buttonHeight: Dp @Composable get() = (48 * scale).dp

    val titleLarge: TextUnit @Composable get() = (32 * scale).sp
    val bodyMedium: TextUnit @Composable get() = (14 * scale).sp
    val bodyLarge: TextUnit @Composable get() = (16 * scale).sp
}
