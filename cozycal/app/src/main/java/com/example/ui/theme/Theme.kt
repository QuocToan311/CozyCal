package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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
    primary = PastelCoral,
    secondary = PastelMint,
    tertiary = PastelLavender,
    background = Color(0xFF2C2A29),
    surface = Color(0xFF383534),
    onPrimary = Color(0xFF2C2A29),
    onSecondary = Color(0xFF2C2A29),
    onTertiary = Color(0xFF2C2A29),
    onBackground = CreamBackground,
    onSurface = CreamBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CozyBrown,
    secondary = PastelCoral,
    tertiary = PastelMint,
    background = CreamBackground,
    surface = CreamSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamicColor by default to preserve the gorgeous cozy pastel theme
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
