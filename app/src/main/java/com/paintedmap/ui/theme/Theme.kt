package com.paintedmap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = BlackBlue,
    onPrimaryContainer = UltraWhiteBlue,

    secondary = WhiteBlue,
    onSecondary = UltraBlackBlue,

    tertiary = WhiteBlue,

    background = UltraBlackBlue,
    onBackground = UltraWhiteBlue,

    surface = BlackBlue,
    onSurface = UltraWhiteBlue,
    surfaceVariant = UltraBlackBlue,
    onSurfaceVariant = UltraWhiteBlue,

    error = ErrorRed,
    onError = Black,

    outline = Grey,
    scrim = Black
)
private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = UltraWhiteBlue,
    onPrimaryContainer = BlackBlue,

    secondary = WhiteBlue,
    onSecondary = UltraBlackBlue,

    tertiary = Blue,

    background = UltraWhiteBlue,
    onBackground = BlackBlue,

    surface = WhiteBlue,
    onSurface = BlackBlue,
        surfaceVariant = UltraWhiteBlue,
    onSurfaceVariant = BlackBlue,

    error = ErrorRed,
    onError = White,

    outline = Grey,
    scrim = Black
)

@Composable
fun PaintedMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}