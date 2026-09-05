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
    primary = OceanPrimaryDark,
    secondary = OceanSecondaryDark,
    tertiary = OceanTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OceanPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OnOceanPrimaryContainer,
    secondary = OceanSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = OceanSecondaryContainer,
    onSecondaryContainer = OnOceanSecondaryContainer,
    tertiary = OceanTertiary,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = OceanTertiaryContainer,
    onTertiaryContainer = OnOceanTertiaryContainer,
    background = SoftComfortBackground,
    onBackground = DeepSlateOnSurface,
    surface = CrispCardWhite,
    onSurface = DeepSlateOnSurface,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = MutedSlateOnVariant,
    outline = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
    outlineVariant = SoftBorderOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Keep consistent tailored comfortable white palette by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
