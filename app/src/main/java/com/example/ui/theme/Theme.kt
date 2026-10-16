package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.AppBrandingTheme

val LocalAppBranding = staticCompositionLocalOf { AppBrandingTheme.LUXURY_GOLD }

fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun MyApplicationTheme(
    brandingTheme: AppBrandingTheme = AppBrandingTheme.LUXURY_GOLD,
    content: @Composable () -> Unit
) {
    val primaryColor = parseHexColor(brandingTheme.primaryColorHex, LuxuryGold)
    val secondaryColor = parseHexColor(brandingTheme.secondaryColorHex, LuxuryGoldDark)
    val backgroundColor = parseHexColor(brandingTheme.backgroundColorHex, DarkBackground)
    val cardColor = parseHexColor(brandingTheme.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(brandingTheme.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(brandingTheme.textMutedColorHex, TextMuted)

    val isLight = brandingTheme.themePresetId == "modern_light"

    val dynamicColorScheme: ColorScheme = if (isLight) {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.12f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.White,
            background = backgroundColor,
            onBackground = textPrimary,
            surface = cardColor,
            onSurface = textPrimary,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = textMuted,
            outline = Color(0xFFCBD5E1)
        )
    } else {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = if (isLight) Color.White else TextDark,
            primaryContainer = primaryColor.copy(alpha = 0.18f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = TextDark,
            secondaryContainer = DarkElevated,
            onSecondaryContainer = textPrimary,
            background = backgroundColor,
            onBackground = textPrimary,
            surface = cardColor,
            onSurface = textPrimary,
            surfaceVariant = DarkElevated,
            onSurfaceVariant = textMuted,
            outline = primaryColor.copy(alpha = 0.45f)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val activity = view.context.findActivity()
                activity?.window?.let { window ->
                    window.statusBarColor = backgroundColor.toArgb()
                    window.navigationBarColor = backgroundColor.toArgb()
                    val insets = WindowCompat.getInsetsController(window, view)
                    insets.isAppearanceLightStatusBars = isLight
                    insets.isAppearanceLightNavigationBars = isLight
                }
            } catch (e: Throwable) {
                // Gracefully ignore window controller styling exceptions
            }
        }
    }

    CompositionLocalProvider(LocalAppBranding provides brandingTheme) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
