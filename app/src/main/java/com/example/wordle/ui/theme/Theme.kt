package com.example.wordle.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class WordleColors(
    val correct: Color = Color.Unspecified,
    val present: Color = Color.Unspecified,
    val absent: Color = Color.Unspecified
)

val LocalWordleColors = staticCompositionLocalOf { WordleColors() }

private val WordleLightColors = WordleColors(
    correct = TileCorrect,
    present = TilePresent,
    absent = TileAbsent
)

private val WordleDarkColors = WordleColors(
    correct = Color(0xFF538D4E),
    present = Color(0xFFB59F3B),
    absent = Color(0xFF3A3A3C)
)

private val WordleHighContrastColors = WordleColors(
    correct = TileCorrectHighContrast,
    present = TilePresentHighContrast,
    absent = Color(0xFF3A3A3C)
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color.Black,
    secondary = PurpleGrey80,
    onSecondary = Color.Black,
    tertiary = Pink80,
    onTertiary = Color.Black,
    background = Color(0xFF121213),
    onBackground = Color.White,
    surface = Color(0xFF121213),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF272729),
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = WordlePrimaryAction,
    onPrimary = Color.White,
    secondary = TilePresent,
    onSecondary = Color.White,
    tertiary = TileAbsent,
    onTertiary = Color.White,
    background = WordleBackground,
    onBackground = WordleTextPrimary,
    surface = WordleSurface,
    onSurface = WordleTextPrimary,
    surfaceVariant = Color(0xFFE3E3E1),
    onSurfaceVariant = WordleTextPrimary
)

// High Contrast / Colorblind friendly color scheme for standard Material components
private val HighContrastColorScheme = lightColorScheme(
    primary = TileCorrectHighContrast,
    onPrimary = Color.White,
    secondary = TilePresentHighContrast,
    onSecondary = Color.White,
    tertiary = Color(0xFF787C7E),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black

)

@Composable
fun WordleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    // Dynamic color is disabled to keep Wordle branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val wordleColors = when {
        highContrast -> WordleHighContrastColors
        darkTheme -> WordleDarkColors
        else -> WordleLightColors
    }

    CompositionLocalProvider(LocalWordleColors provides wordleColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object WordleTheme {
    val colors: WordleColors
        @Composable
        get() = LocalWordleColors.current
}
