package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityDeepPurple,
    secondary = HighDensitySecondary,
    onSecondary = HighDensityDeepPurple,
    tertiary = HighDensityWarningText,
    background = HighDensityBg,
    onBackground = HighDensityTextPrimary,
    surface = HighDensityCardBg,
    onSurface = HighDensityTextPrimary,
    surfaceContainer = HighDensityItemBg,
    outline = HighDensityBorder
  )

private val LightColorScheme = DarkColorScheme // Always enforce the dark High Density theme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // High density is a dark dashboard theme
  dynamicColor: Boolean = false, // Disable device wallpaper styling to project branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
