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
    primary = OceanPrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = OceanCard,
    onPrimaryContainer = OceanPrimaryLight,
    secondary = OceanAccentGold,
    onSecondary = Color.Black,
    tertiary = OceanAccentEmerald,
    background = OceanDeep,
    onBackground = TextPrimaryDark,
    surface = OceanSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = OceanCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = OceanCardBorder,
    error = OceanAccentCoral
)

private val LightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    tertiary = Color(0xFF059669),
    background = OceanLightBg,
    onBackground = TextPrimaryLight,
    surface = OceanLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = OceanLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = OceanLightCardBorder,
    error = Color(0xFFDC2626)
)

@Composable
fun OceanMathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // prefer dedicated Ocean branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
        content = content
    )
}
