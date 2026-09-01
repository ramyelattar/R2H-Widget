package com.r2h_widget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = VoidBlack,
    secondary = AccentCyan,
    onSecondary = VoidBlack,
    tertiary = ProGold,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = Obsidian,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5B3DBB),
    secondary = Color(0xFF14647C),
    tertiary = Color(0xFF765B00),
)

@Composable
fun R2HWidgetTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    accentHex: String = "#B69CFF",
    content: @Composable () -> Unit
) {
    val themeAccent = runCatching {
        Color(android.graphics.Color.parseColor(accentHex))
    }.getOrDefault(AccentViolet)
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme.copy(primary = themeAccent)
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
