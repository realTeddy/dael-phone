package me.tewodros.dael.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Palette {
    val bg = Color(0xFF1A1033)
    val surface = Color(0xFF2A1B4D)
    val coral = Color(0xFFFF7A59)
    val sun = Color(0xFFFFC857)
    val mint = Color(0xFF4ADE80)
    val sky = Color(0xFF60A5FA)
    val grape = Color(0xFFA78BFA)
    val pink = Color(0xFFF472B6)
    val red = Color(0xFFEF4444)
    val tiles = listOf(coral, sun, mint, sky, grape, pink)
}

@Composable
fun DaelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.coral,
            secondary = Palette.sun,
            background = Palette.bg,
            surface = Palette.surface,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
        content = content,
    )
}
