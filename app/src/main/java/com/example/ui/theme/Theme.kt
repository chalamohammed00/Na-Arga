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

private val DarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  secondary = NeonPurple,
  tertiary = NeonPink,
  background = ObsidianBackground,
  surface = CyberBackgroundCard,
  onPrimary = ObsidianBackground,
  onSecondary = PureWhite,
  onTertiary = PureWhite,
  onBackground = PureWhite,
  onSurface = PureWhite,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for luxury futuristic cyber vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve strict neon styling
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
