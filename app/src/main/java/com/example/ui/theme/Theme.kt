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

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = SecurityGreen,
    tertiary = AlertAmber,
    background = ObsidianBg,
    surface = CyberCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AccentCyan,
    secondary = SecurityGreen,
    tertiary = AlertAmber,
    background = Color(0xFFFAFBFD),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1A24),
    onSurface = Color(0xFF1A1A24)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark vault theme for cohesive visual experience
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the custom designed cybersecurity palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
