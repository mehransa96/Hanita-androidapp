package com.hanita.partner.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hanita.partner.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/** Muted red for period days on the cycle bar (same in both modes). */
val PeriodRed = Color(0xFFE5484D)

/**
 * Graphite and one amber accent: a calm, minimal, "dashboard" look. Dark is the signature
 * mode; the light mode keeps the same structure with WCAG AA contrast.
 */
private val Dark = darkColorScheme(
    primary = Color(0xFFF5A524),
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF2B2111),
    onPrimaryContainer = Color(0xFFFFD58A),
    secondary = Color(0xFF7CA4C9),
    onSecondary = Color(0xFF0A1A2A),
    secondaryContainer = Color(0xFF1C2733),
    onSecondaryContainer = Color(0xFFCFE0F1),
    background = Color(0xFF0E1014),
    onBackground = Color(0xFFECEFF3),
    surface = Color(0xFF171A20),
    onSurface = Color(0xFFECEFF3),
    surfaceVariant = Color(0xFF20242C),
    onSurfaceVariant = Color(0xFF9AA3AF),
    surfaceContainer = Color(0xFF171A20),
    outline = Color(0xFF3A404B),
    outlineVariant = Color(0xFF2A2F38),
    error = Color(0xFFFF6B6B)
)

private val Light = lightColorScheme(
    primary = Color(0xFF9A5B00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEFD2),
    onPrimaryContainer = Color(0xFF3D2600),
    secondary = Color(0xFF3B6A93),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EDF7),
    onSecondaryContainer = Color(0xFF0F2A42),
    background = Color(0xFFF3F4F6),
    onBackground = Color(0xFF111418),
    surface = Color.White,
    onSurface = Color(0xFF111418),
    surfaceVariant = Color(0xFFECEEF1),
    onSurfaceVariant = Color(0xFF545C67),
    surfaceContainer = Color.White,
    outline = Color(0xFFC5CAD1),
    outlineVariant = Color(0xFFE1E4E8),
    error = Color(0xFFC62828)
)

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

/** Persian, right-to-left, graphite theme that follows the system dark mode. */
@Composable
fun PartnerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = persianTypography(),
        shapes = TightShapes
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
