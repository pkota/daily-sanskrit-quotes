package com.dailysanskritquotes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dailysanskritquotes.R
import com.dailysanskritquotes.ui.viewmodel.AppColorTheme
import com.dailysanskritquotes.ui.viewmodel.TextSizeOption

// Default color schemes
private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

// Warm color schemes (orange/amber tones)
private val WarmLightColorScheme = lightColorScheme(
    primary = Color(0xFFE07C4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF341100),
    secondary = Color(0xFFD4A373),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFFB08968),
    onTertiary = Color.White,
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFF5DED0),
    onSurfaceVariant = Color(0xFF52443B)
)

private val WarmDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB68C),
    onPrimary = Color(0xFF522300),
    primaryContainer = Color(0xFF733400),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFE6C29A),
    onSecondary = Color(0xFF422C0E),
    secondaryContainer = Color(0xFF5B4123),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = Color(0xFFD4A373),
    onTertiary = Color(0xFF3E2D1C),
    background = Color(0xFF1A1110),
    surface = Color(0xFF1A1110),
    surfaceVariant = Color(0xFF52443B),
    onSurfaceVariant = Color(0xFFD7C3B7)
)


// Cool color schemes (blue tones)
private val CoolLightColorScheme = lightColorScheme(
    primary = Color(0xFF4F8FE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF6B9BD2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E3FF),
    onSecondaryContainer = Color(0xFF0E1D2E),
    tertiary = Color(0xFF5C7BA5),
    onTertiary = Color.White,
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E)
)

private val CoolDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5C8FF),
    onPrimary = Color(0xFF00315E),
    primaryContainer = Color(0xFF004884),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFB0C6E8),
    onSecondary = Color(0xFF1A3048),
    secondaryContainer = Color(0xFF314760),
    onSecondaryContainer = Color(0xFFD4E3FF),
    tertiary = Color(0xFF8EAED4),
    onTertiary = Color(0xFF1E3450),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF)
)

// Forest color schemes (green tones)
private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF4FA06B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC0F0CE),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Color(0xFF6B9B7A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8E8C8),
    onSecondaryContainer = Color(0xFF072016),
    tertiary = Color(0xFF5A8A6E),
    onTertiary = Color.White,
    background = Color(0xFFFCFDF7),
    surface = Color(0xFFFCFDF7),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414941)
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CD8A0),
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFFC0F0CE),
    secondary = Color(0xFF9DCCAC),
    onSecondary = Color(0xFF1C352A),
    secondaryContainer = Color(0xFF334C3F),
    onSecondaryContainer = Color(0xFFB8E8C8),
    tertiary = Color(0xFF7FBE94),
    onTertiary = Color(0xFF1A3728),
    background = Color(0xFF1A1C1A),
    surface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF)
)

val VesperLibre = FontFamily(
    Font(R.font.vesper_libre_regular, FontWeight.Normal),
    Font(R.font.vesper_libre_bold, FontWeight.Bold)
)

fun scaledTypography(scale: Float): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = VesperLibre, fontSize = base.displayLarge.fontSize * scale),
        displayMedium = base.displayMedium.copy(fontFamily = VesperLibre, fontSize = base.displayMedium.fontSize * scale),
        displaySmall = base.displaySmall.copy(fontFamily = VesperLibre, fontSize = base.displaySmall.fontSize * scale),
        headlineLarge = base.headlineLarge.copy(fontFamily = VesperLibre, fontSize = base.headlineLarge.fontSize * scale),
        headlineMedium = base.headlineMedium.copy(fontFamily = VesperLibre, fontSize = base.headlineMedium.fontSize * scale),
        headlineSmall = base.headlineSmall.copy(fontFamily = VesperLibre, fontSize = base.headlineSmall.fontSize * scale),
        titleLarge = base.titleLarge.copy(fontFamily = VesperLibre, fontSize = base.titleLarge.fontSize * scale),
        titleMedium = base.titleMedium.copy(fontFamily = VesperLibre, fontSize = base.titleMedium.fontSize * scale),
        titleSmall = base.titleSmall.copy(fontFamily = VesperLibre, fontSize = base.titleSmall.fontSize * scale),
        bodyLarge = base.bodyLarge.copy(fontFamily = VesperLibre, fontSize = base.bodyLarge.fontSize * scale),
        bodyMedium = base.bodyMedium.copy(fontFamily = VesperLibre, fontSize = base.bodyMedium.fontSize * scale),
        bodySmall = base.bodySmall.copy(fontFamily = VesperLibre, fontSize = base.bodySmall.fontSize * scale),
        labelLarge = base.labelLarge.copy(fontFamily = VesperLibre, fontSize = base.labelLarge.fontSize * scale),
        labelMedium = base.labelMedium.copy(fontFamily = VesperLibre, fontSize = base.labelMedium.fontSize * scale),
        labelSmall = base.labelSmall.copy(fontFamily = VesperLibre, fontSize = base.labelSmall.fontSize * scale),
    )
}

@Composable
fun DailySanskritQuotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    textSizeOption: TextSizeOption = TextSizeOption.MEDIUM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (colorTheme) {
        AppColorTheme.DEFAULT -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        AppColorTheme.WARM -> if (darkTheme) WarmDarkColorScheme else WarmLightColorScheme
        AppColorTheme.COOL -> if (darkTheme) CoolDarkColorScheme else CoolLightColorScheme
        AppColorTheme.FOREST -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
    }

    val typography = scaledTypography(textSizeOption.scale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
