package com.azkari2027.app.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.azkari2027.app.data.AzkarFontFamily

val ObsidianBg = Color(0xFF070D0B)
val ObsidianSurface = Color(0xFF0F1B17)
val ObsidianSurfaceHigh = Color(0xFF162721)
val MedinaGold = Color(0xFFE0B956)
val MedinaGoldDim = Color(0xFFA68432)
val EmeraldAccent = Color(0xFF10B981)
val IvoryText = Color(0xFFF5F2EA)
val MutedSage = Color(0xFF93ACA2)

val ParchmentBg = Color(0xFFFAF7F0)
val ParchmentSurface = Color(0xFFFFFFFF)
val ParchmentSurfaceHigh = Color(0xFFF1EBDC)
val DeepEmerald = Color(0xFF0D5C46)
val AntiqueGold = Color(0xFFB8860B)
val CharcoalInk = Color(0xFF18221F)

private val DarkIslamicColors = darkColorScheme(
    primary = MedinaGold,
    onPrimary = Color(0xFF1A1405),
    primaryContainer = Color(0xFF2B230E),
    onPrimaryContainer = MedinaGold,
    secondary = EmeraldAccent,
    onSecondary = Color(0xFF041D14),
    secondaryContainer = Color(0xFF0F2E24),
    onSecondaryContainer = Color(0xFF8CF0C9),
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = MutedSage,
    outline = MedinaGold.copy(alpha = 0.25f)
)

private val LightIslamicColors = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F2E7),
    onPrimaryContainer = DeepEmerald,
    secondary = AntiqueGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7ECCD),
    onSecondaryContainer = Color(0xFF5C4200),
    background = ParchmentBg,
    onBackground = CharcoalInk,
    surface = ParchmentSurface,
    onSurface = CharcoalInk,
    surfaceVariant = ParchmentSurfaceHigh,
    onSurfaceVariant = Color(0xFF53665E),
    outline = AntiqueGold.copy(alpha = 0.3f)
)

@Composable
fun Azkari2027Theme(
    darkTheme: Boolean,
    fontChoice: AzkarFontFamily,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val customFontFamily = remember(fontChoice) {
        val path = fontChoice.assetPath
        if (path == null) {
            FontFamily.Default
        } else {
            runCatching { FontFamily(Typeface.createFromAsset(context.assets, path)) }
                .getOrDefault(FontFamily.Default)
        }
    }

    val baseTypo = Typography()
    val islamicTypography = remember(customFontFamily) {
        Typography(
            displayLarge = baseTypo.displayLarge.copy(fontFamily = customFontFamily),
            displayMedium = baseTypo.displayMedium.copy(fontFamily = customFontFamily),
            displaySmall = baseTypo.displaySmall.copy(fontFamily = customFontFamily),
            headlineLarge = baseTypo.headlineLarge.copy(fontFamily = customFontFamily),
            headlineMedium = baseTypo.headlineMedium.copy(fontFamily = customFontFamily),
            headlineSmall = baseTypo.headlineSmall.copy(fontFamily = customFontFamily),
            titleLarge = baseTypo.titleLarge.copy(fontFamily = customFontFamily),
            titleMedium = baseTypo.titleMedium.copy(fontFamily = customFontFamily),
            titleSmall = baseTypo.titleSmall.copy(fontFamily = customFontFamily),
            bodyLarge = baseTypo.bodyLarge.copy(fontFamily = customFontFamily),
            bodyMedium = baseTypo.bodyMedium.copy(fontFamily = customFontFamily),
            bodySmall = baseTypo.bodySmall.copy(fontFamily = customFontFamily),
            labelLarge = baseTypo.labelLarge.copy(fontFamily = customFontFamily),
            labelMedium = baseTypo.labelMedium.copy(fontFamily = customFontFamily),
            labelSmall = baseTypo.labelSmall.copy(fontFamily = customFontFamily)
        )
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkIslamicColors else LightIslamicColors,
        typography = islamicTypography,
        content = content
    )
}
