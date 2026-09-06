package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PetrolPrimaryDark,
    secondary = CitrusSecondaryDark,
    tertiary = AmberTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PetrolPrimary,
    onPrimary = Color.White,
    primaryContainer = PetrolPrimaryContainer,
    onPrimaryContainer = OnPetrolPrimaryContainer,
    secondary = CitrusSecondary,
    onSecondary = Color.White,
    secondaryContainer = CitrusSecondaryContainer,
    onSecondaryContainer = OnCitrusSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = OnAmberTertiaryContainer,
    background = PaperBackground,
    onBackground = InkOnSurface,
    surface = CardSurface,
    onSurface = InkOnSurface,
    surfaceVariant = SubtleSurfaceVariant,
    onSurfaceVariant = MutedOnVariant,
    outline = Color(0xFFB9C4C1),
    outlineVariant = SoftBorderOutline,
    error = ErrorRed,
    errorContainer = ErrorRedContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Keep the tailored "Steam & Shine" palette by default rather than the device's dynamic color
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

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content
  )
}
