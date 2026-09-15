package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val InxernalColorScheme = darkColorScheme(
  primary = InxernalAccent,
  onPrimary = InxernalText,
  primaryContainer = InxernalAccentContainer,
  onPrimaryContainer = InxernalAccentLight,
  secondary = InxernalAccentLight,
  onSecondary = InxernalBg,
  background = InxernalBg,
  onBackground = InxernalText,
  surface = InxernalSurface,
  onSurface = InxernalText,
  surfaceVariant = InxernalSurfaceVariant,
  onSurfaceVariant = InxernalTextDim,
  outline = InxernalBorder,
  error = InxernalError
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = InxernalColorScheme,
    typography = Typography,
    content = content
  )
}
