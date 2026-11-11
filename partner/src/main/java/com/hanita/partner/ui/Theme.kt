package com.hanita.partner.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
import com.hanita.partner.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val Light = lightColorScheme(
    primary = Color(0xFF1E40AF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FF),
    onPrimaryContainer = Color(0xFF0B1F5C),
    secondary = Color(0xFFBE185D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE7F3),
    onSecondaryContainer = Color(0xFF5B0B2E),
    background = Color(0xFFF5F7FB),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF4B5563),
    outlineVariant = Color(0xFFD9DEE8)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9DB6FF),
    onPrimary = Color(0xFF0B1F5C),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBE4FF),
    secondary = Color(0xFFF9A8D4),
    onSecondary = Color(0xFF5B0B2E),
    secondaryContainer = Color(0xFF831843),
    onSecondaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF334155)
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

/** Persian, right-to-left, calm blue theme that follows the system dark mode. */
@Composable
fun PartnerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = persianTypography()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
