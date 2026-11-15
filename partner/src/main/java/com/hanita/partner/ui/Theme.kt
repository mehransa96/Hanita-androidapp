package com.hanita.partner.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hanita.core.gentleman.ThemePresets
import com.hanita.partner.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/** Muted red for period days on the cycle bar (same in both modes). */
val PeriodRed = Color(0xFFE5484D)

/** Turns one palette of a ready-made theme into a Material colour scheme. */
private fun ThemePresets.Palette.toScheme(dark: Boolean): ColorScheme {
    fun c(v: Long) = Color(v)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c(primary),
        onPrimary = c(onPrimary),
        primaryContainer = c(primaryContainer),
        onPrimaryContainer = c(onPrimaryContainer),
        secondary = c(secondary),
        onSecondary = c(if (dark) background else 0xFFFFFFFF),
        secondaryContainer = c(secondaryContainer),
        onSecondaryContainer = c(onSecondaryContainer),
        background = c(background),
        onBackground = c(onSurface),
        surface = c(surface),
        onSurface = c(onSurface),
        surfaceVariant = c(surfaceVariant),
        onSurfaceVariant = c(onSurfaceVariant),
        surfaceContainer = c(surface),
        surfaceContainerLow = c(surface),
        surfaceContainerHigh = c(surfaceVariant),
        surfaceContainerHighest = c(surfaceVariant),
        outline = c(outline),
        outlineVariant = c(outlineVariant),
        error = c(error),
        errorContainer = c(if (dark) 0xFF3B1517 else 0xFFFDE8E8),
        onErrorContainer = c(if (dark) 0xFFFFD9D6 else 0xFF5F1012)
    )
}

/**
 * The user's choice of theme and light/dark mode. Changes apply at once and are saved by [save].
 */
@Stable
class ThemeController(id: String, mode: ThemePresets.Mode, private val save: (String, ThemePresets.Mode) -> Unit = { _, _ -> }) {
    var id: String by mutableStateOf(ThemePresets.get(id).id)
        private set
    var mode: ThemePresets.Mode by mutableStateOf(mode)
        private set

    fun choose(newId: String) {
        id = ThemePresets.get(newId).id
        save(id, mode)
    }

    fun choose(newMode: ThemePresets.Mode) {
        mode = newMode
        save(id, mode)
    }

    @Composable
    fun isDark(): Boolean = when (mode) {
        ThemePresets.Mode.SYSTEM -> isSystemInDarkTheme()
        ThemePresets.Mode.LIGHT -> false
        ThemePresets.Mode.DARK -> true
    }
}

/** The theme controller of the running app; null in previews and tests (default theme). */
val LocalThemeController = staticCompositionLocalOf<ThemeController?> { null }

private fun persianTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Vazirmatn),
        displayMedium = base.displayMedium.copy(fontFamily = Vazirmatn),
        displaySmall = base.displaySmall.copy(fontFamily = Vazirmatn),
        headlineLarge = base.headlineLarge.copy(fontFamily = Vazirmatn),
        headlineMedium = base.headlineMedium.copy(fontFamily = Vazirmatn),
        headlineSmall = base.headlineSmall.copy(fontFamily = Vazirmatn),
        titleLarge = base.titleLarge.copy(fontFamily = Vazirmatn),
        titleMedium = base.titleMedium.copy(fontFamily = Vazirmatn),
        titleSmall = base.titleSmall.copy(fontFamily = Vazirmatn),
        bodyLarge = base.bodyLarge.copy(fontFamily = Vazirmatn),
        bodyMedium = base.bodyMedium.copy(fontFamily = Vazirmatn),
        bodySmall = base.bodySmall.copy(fontFamily = Vazirmatn),
        labelLarge = base.labelLarge.copy(fontFamily = Vazirmatn),
        labelMedium = base.labelMedium.copy(fontFamily = Vazirmatn),
        labelSmall = base.labelSmall.copy(fontFamily = Vazirmatn)
    )
}

private val TightShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp)
)

/**
 * Persian, right-to-left theme. Uses the user's chosen ready-made theme and mode when a
 * [controller] is given, otherwise the default graphite theme following the system.
 */
@Composable
fun PartnerTheme(controller: ThemeController? = null, content: @Composable () -> Unit) {
    val preset = ThemePresets.get(controller?.id)
    val dark = controller?.isDark() ?: isSystemInDarkTheme()
    val scheme = remember(preset.id, dark) { (if (dark) preset.dark else preset.light).toScheme(dark) }
    MaterialTheme(
        colorScheme = scheme,
        typography = persianTypography(),
        shapes = TightShapes
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalThemeController provides controller) {
            content()
        }
    }
}
