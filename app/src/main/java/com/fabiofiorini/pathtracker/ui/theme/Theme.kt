package com.fabiofiorini.pathtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Red,
    secondary = Orange,
    tertiary = Yellow,
    background = Dark,
    surface = Dark,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Dark,
    onBackground = White,
    onSurface = White,
    surfaceVariant = SurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Red,
    secondary = Orange,
    tertiary = Yellow,
    background = LightGrey,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Dark,
    onBackground = Dark,
    onSurface = Dark,
    surfaceVariant = LightGrey
)

@Composable
fun PathTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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
