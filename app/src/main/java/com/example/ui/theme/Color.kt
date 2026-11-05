package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Light Theme Colors
val LightBackground = Color(0xFFFCF8F8)
val LightSurface = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFFEC4899)
val LightPrimaryDark = Color(0xFFDB2777)
val LightPrimaryVariant = Color(0xFFFB7185) // Rose-400 for gradient
val LightPrimaryLight = Color(0xFFFDF2F8)
val LightSecondary = Color(0xFFF472B6) // Pink-400
val LightTextDark = Color(0xFF0F172A)
val LightTextMedium = Color(0xFF1E293B)
val LightTextLight = Color(0xFF64748B)
val LightTextLighter = Color(0xFF94A3B8)
val LightBorder = Color(0xFFF1F5F9)
val LightError = Color(0xFFFF4C4C)
val LightIndigo50 = Color(0xFFEEF2FF)
val LightIndigo500 = Color(0xFF6366F1)
val LightAmber50 = Color(0xFFFFFBEB)
val LightAmber500 = Color(0xFFF59E0B)

// Dark Theme Colors
val NightBackground = Color(0xFF0D0B1A) // deep cosmic navy
val NightSurface = Color(0xFF161226)    // dark card surface
val NightPrimary = Color(0xFFE879A0)    // vivid rose-pink
val NightPrimaryDark = Color(0xFFE879A0)
val NightPrimaryVariant = Color(0xFFE879A0)
val NightPrimaryLight = Color(0xFF3D1530) // PrimaryContainer
val NightSecondary = Color(0xFFB59DFF)  // soft lavender
val NightTextDark = Color(0xFFF0E8FF)   // OnBackground
val NightTextMedium = Color(0xFFDDD5F0) // OnSurface
val NightTextLight = Color(0xFF94A3B8)  // OnSurfaceVariant
val NightTextLighter = Color(0xFF64748B) // Outline
val NightBorder = Color(0xFF2A1F5A)    // SecondaryContainer/Border
val NightIndigo50 = Color(0xFF0F3D38)   // TertiaryContainer (ovulation teal container)
val NightIndigo500 = Color(0xFF7FD8C8)  // Tertiary (mint teal)
val NightAmber50 = Color(0xFF3D1530)
val NightAmber500 = Color(0xFFB59DFF) // Soft lavender for auxiliary highlights

val ErrorRed = Color(0xFFFF4C4C) // Keep for backward compatibility without composable if needed

// Dynamic Colors mapping (acting as proxies for existing Sleek usages)
val SleekBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val SleekSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val SleekPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val SleekPrimaryDark: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer

val SleekPrimaryVariant: Color
    @Composable get() = MaterialTheme.colorScheme.error // Re-using error for variant temporarily, or we could just use a custom check

val SleekPrimaryLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

val SleekSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val SleekTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val SleekTextMedium: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

val SleekTextLight: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val SleekTextLighter: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val SleekBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

val Indigo50: Color
    @Composable get() = MaterialTheme.colorScheme.tertiaryContainer

val Indigo500: Color
    @Composable get() = MaterialTheme.colorScheme.tertiary

val Amber50: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) NightAmber50 else LightAmber50

val Amber500: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) NightAmber500 else LightAmber500

