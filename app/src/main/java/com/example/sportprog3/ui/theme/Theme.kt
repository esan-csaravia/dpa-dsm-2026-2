package com.example.sportprog3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = Pitch,
    tertiary = Lime,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    outline = Line
)

@Composable
fun SportProG3Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}