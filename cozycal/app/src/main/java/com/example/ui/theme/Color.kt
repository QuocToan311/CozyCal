package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val CreamBackground = Color(0xFFFAF6EE)
val CreamSurface = Color(0xFFFFFDF9)
val CozyBrown = Color(0xFF5D4037)
val TextDark = Color(0xFF3D3A36)
val TextMuted = Color(0xFF8C877E)

val PastelMint = Color(0xFFCFEADF)
val PastelPeach = Color(0xFFFFECE6)
val PastelCoral = Color(0xFFFFB3A0)
val PastelLavender = Color(0xFFE5E3F7)
val PastelYellow = Color(0xFFFFF7CE)
val PastelBlue = Color(0xFFD6EDF8)

// Legacy compatibility values
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun safeParseColor(colorHex: String?, fallbackHex: String = "#FFDBCE"): Color {
    if (colorHex.isNullOrBlank()) return Color(android.graphics.Color.parseColor(fallbackHex))
    return try {
        val cleaned = colorHex.trim()
        val formatted = if (cleaned.startsWith("#")) cleaned else "#$cleaned"
        Color(android.graphics.Color.parseColor(formatted))
    } catch (e: Exception) {
        Color(android.graphics.Color.parseColor(fallbackHex))
    }
}


