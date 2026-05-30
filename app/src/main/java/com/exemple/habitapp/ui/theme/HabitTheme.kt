package com.exemple.habitapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Water = Color(0xFF0EA5E9)
val Study = Color(0xFF7C3AED)
val Success = Color(0xFF16A34A)
val Coral = Color(0xFFF97316)
val Warning = Color(0xFFF59E0B)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4353D8),
    secondary = Study,
    tertiary = Success,
    background = Color(0xFFF3F6FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF0F8),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFD3DBE8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AA8FF),
    secondary = Color(0xFFC084FC),
    tertiary = Color(0xFF4ADE80),
    background = Color(0xFF080E1C),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
)

@Composable
fun HabitTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

fun screenGradient(dark: Boolean): List<Color> = if (dark) {
    listOf(Color(0xFF080E1C), Color(0xFF111827), Color(0xFF172033))
} else {
    listOf(Color(0xFFF3F6FB), Color(0xFFE9ECFF), Color.White)
}
