package com.example.tavern.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * LIGHT THEME - The Tavern in Daylight
 * Warm, welcoming, medieval tavern atmosphere
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Main brand color (Top bars, important elements)
    primary = WoodPrimary,
    onPrimary = Cream,
    primaryContainer = WoodLight,
    onPrimaryContainer = TextInk,
    
    // Secondary - Accent color (FABs, highlights)
    secondary = GoldAccent,
    onSecondary = WoodDark,
    secondaryContainer = Amber,
    onSecondaryContainer = TextInk,
    
    // Tertiary - Additional accent
    tertiary = Bronze,
    onTertiary = Color.White,
    tertiaryContainer = GoldRich,
    onTertiaryContainer = WoodDark,
    
    // Background - Screen background
    background = Stone,
    onBackground = TextInk,
    
    // Surface - Card backgrounds
    surface = Parchment,
    onSurface = TextInk,
    surfaceVariant = ParchmentDark,
    onSurfaceVariant = TextMuted,
    
    // Additional surfaces
    surfaceTint = GoldAccent,
    inverseSurface = WoodDark,
    inverseOnSurface = Cream,
    
    // Functional colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Outline & dividers
    outline = WoodLight,
    outlineVariant = Color(0xFFD7C2B8),
    scrim = ShadowColor,
)

/**
 * DARK THEME - The Tavern at Night
 * Mysterious, cozy, candlelit atmosphere
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Main brand color
    primary = CherryWood,
    onPrimary = LightParchmentText,
    primaryContainer = MahoganyPrimary,
    onPrimaryContainer = MoonGold,
    
    // Secondary - Accent color
    secondary = MoonGold,
    onSecondary = MahoganyDark,
    secondaryContainer = BronzeGlow,
    onSecondaryContainer = LightParchmentText,
    
    // Tertiary - Additional accent
    tertiary = CopperShine,
    onTertiary = MahoganyDark,
    tertiaryContainer = BronzeGlow,
    onTertiaryContainer = LightParchmentText,
    
    // Background - Screen background
    background = TavernNight,
    onBackground = LightParchmentText,
    
    // Surface - Card backgrounds
    surface = CandleLight,
    onSurface = LightParchmentText,
    surfaceVariant = DarkParchment,
    onSurfaceVariant = MutedGold,
    
    // Additional surfaces
    surfaceTint = MoonGold,
    inverseSurface = Parchment,
    inverseOnSurface = TextInk,
    
    // Functional colors
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Outline & dividers
    outline = ShadowBrown,
    outlineVariant = DarkWood,
    scrim = OverlayDark,
)

/**
 * Main Theme Composable
 * Automatically switches between light and dark themes
 */
@Composable
fun TavernTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we'll use custom tavern colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Preview Light Theme
 */
@Composable
fun LightTavernTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Preview Dark Theme
 */
@Composable
fun DarkTavernTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
