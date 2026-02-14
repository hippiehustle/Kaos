package com.securescanner.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MatteCyan600,
    onPrimary = Color.White,
    primaryContainer = MatteCyan700,
    onPrimaryContainer = MatteCyan200,

    secondary = MatteCyan400,
    onSecondary = Charcoal900,
    secondaryContainer = Charcoal700,
    onSecondaryContainer = MatteCyan300,

    tertiary = MatteCyan500,
    onTertiary = Charcoal900,
    tertiaryContainer = Charcoal700,
    onTertiaryContainer = MatteCyan200,

    background = Charcoal900,
    onBackground = Charcoal100,

    surface = Charcoal800,
    onSurface = Charcoal100,
    surfaceVariant = Charcoal700,
    onSurfaceVariant = Charcoal300,

    surfaceContainerLowest = Charcoal900,
    surfaceContainerLow = Charcoal850,
    surfaceContainer = Charcoal800,
    surfaceContainerHigh = Charcoal750,
    surfaceContainerHighest = Charcoal700,

    outline = Charcoal600,
    outlineVariant = Charcoal700,

    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = Charcoal100,
    inverseOnSurface = Charcoal900,
    inversePrimary = MatteCyan700,

    scrim = Color.Black,
)

@Composable
fun SecureScannerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SecureScannerTypography,
        shapes = SecureScannerShapes,
        content = content
    )
}
