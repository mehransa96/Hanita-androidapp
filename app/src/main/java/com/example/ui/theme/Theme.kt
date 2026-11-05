package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

// ==========================================
// 1. Cosmic Blossom (شکوفه کیهانی)
// ==========================================
val CosmicBlossomDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE879A0),
    onPrimary = Color(0xFF0D0B1A),
    primaryContainer = Color(0xFF3D1530),
    onPrimaryContainer = Color(0xFFE879A0),
    secondary = Color(0xFFB59DFF),
    onSecondary = Color(0xFF0D0B1A),
    tertiary = Color(0xFF7FD8C8),
    onTertiary = Color(0xFF0D0B1A),
    tertiaryContainer = Color(0xFF0F3D38),
    onTertiaryContainer = Color(0xFF7FD8C8),
    background = Color(0xFF0D0B1A),
    onBackground = Color(0xFFF0E8FF),
    surface = Color(0xFF161226),
    onSurface = Color(0xFFDDD5F0),
    surfaceVariant = Color(0xFF1F1A35),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2A1F5A),
    outlineVariant = Color(0xFF2A1F5A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF0D0B1A)
)

val CosmicBlossomLightColorScheme = lightColorScheme(
    primary = Color(0xFFD94678),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4EC),
    onPrimaryContainer = Color(0xFF9E1247),
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color.White,
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = Color(0xFFFFF5F8),
    onBackground = Color(0xFF280B18),
    surface = Color.White,
    onSurface = Color(0xFF1F1A24),
    surfaceVariant = Color(0xFFFFF0F5),
    onSurfaceVariant = Color(0xFF6B5E67),
    outline = Color(0xFFFBCFE8),
    outlineVariant = Color(0xFFFCE7F3),
    error = Color(0xFFE11D48),
    onError = Color.White
)

// ==========================================
// 2. Sweet Candy (آبنبات شیرین)
// ==========================================
val SweetCandyLightColorScheme = lightColorScheme(
    primary = Color(0xFFEC4899),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDF2F8),
    onPrimaryContainer = Color(0xFFDB2777),
    secondary = Color(0xFFF472B6),
    onSecondary = Color.White,
    tertiary = Color(0xFF14B8A6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF14B8A6),
    background = Color(0xFFFFF5F7),
    onBackground = Color(0xFF330018),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFFFF0F4),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFF1F5F9),
    outlineVariant = Color(0xFFFCE7F3),
    error = Color(0xFFFF4C4C),
    onError = Color.White
)

val SweetCandyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6),
    onPrimary = Color(0xFF1F0814),
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFBCFE8),
    secondary = Color(0xFFFB7185),
    onSecondary = Color(0xFF1F0814),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF1F0814),
    tertiaryContainer = Color(0xFF115E59),
    onTertiaryContainer = Color(0xFF99F6E4),
    background = Color(0xFF140810),
    onBackground = Color(0xFFFDF2F8),
    surface = Color(0xFF200D1A),
    onSurface = Color(0xFFFCE7F3),
    surfaceVariant = Color(0xFF2E1724),
    onSurfaceVariant = Color(0xFFFDA4AF),
    outline = Color(0xFF501026),
    outlineVariant = Color(0xFF501026),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF1F0814)
)

// ==========================================
// 3. Mint Orchid (ارکیده نعنایی)
// ==========================================
val MintOrchidDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF061517),
    primaryContainer = Color(0xFF0F3D38),
    onPrimaryContainer = Color(0xFF2DD4BF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF061517),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF061517),
    tertiaryContainer = Color(0xFF4C1D37),
    onTertiaryContainer = Color(0xFFF472B6),
    background = Color(0xFF061517),
    onBackground = Color(0xFFE2F9F6),
    surface = Color(0xFF0C2427),
    onSurface = Color(0xFFCCEFEB),
    surfaceVariant = Color(0xFF13363B),
    onSurfaceVariant = Color(0xFF5EEAD4),
    outline = Color(0xFF134E4A),
    outlineVariant = Color(0xFF134E4A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF061517)
)

val MintOrchidLightColorScheme = lightColorScheme(
    primary = Color(0xFF0D9488),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFFDB2777),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF9D174D),
    background = Color(0xFFF0FDF9),
    onBackground = Color(0xFF042F2E),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6FFFA),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF99F6E4),
    outlineVariant = Color(0xFFCCFBF1),
    error = Color(0xFFE11D48),
    onError = Color.White
)

// ==========================================
// 4. Soft Lavender (اسطوخودوس)
// ==========================================
val SoftLavenderDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF0F0E17),
    primaryContainer = Color(0xFF231E3D),
    onPrimaryContainer = Color(0xFFA78BFA),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF0F0E17),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF0F0E17),
    tertiaryContainer = Color(0xFF134E4A),
    onTertiaryContainer = Color(0xFF2DD4BF),
    background = Color(0xFF0F0E17),
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF1B192A),
    onSurface = Color(0xFFEDE9FE),
    surfaceVariant = Color(0xFF27243C),
    onSurfaceVariant = Color(0xFFC4B5FD),
    outline = Color(0xFF312E81),
    outlineVariant = Color(0xFF312E81),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF0F0E17)
)

val SoftLavenderLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF5B21B6),
    secondary = Color(0xFFDB2777),
    onSecondary = Color.White,
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = Color(0xFFF8F6FE),
    onBackground = Color(0xFF1E1B4B),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFDDD6FE),
    outlineVariant = Color(0xFFEDE9FE),
    error = Color(0xFFE11D48),
    onError = Color.White
)

// ==========================================
// 5. Rose Gold (رز گلد لوکس)
// ==========================================
val RoseGoldLightColorScheme = lightColorScheme(
    primary = Color(0xFFBE185D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF1F2),
    onPrimaryContainer = Color(0xFF9F1239),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    tertiary = Color(0xFFFB7185),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFFBE185D),
    background = Color(0xFFFFF8F9),
    onBackground = Color(0xFF4C0519),
    surface = Color.White,
    onSurface = Color(0xFF2D0612),
    surfaceVariant = Color(0xFFFFF1F2),
    onSurfaceVariant = Color(0xFF701A31),
    outline = Color(0xFFFECDD3),
    outlineVariant = Color(0xFFFECDD3),
    error = Color(0xFFE11D48),
    onError = Color.White
)

val RoseGoldDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF43F5E),
    onPrimary = Color(0xFF200511),
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFDA4AF),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF200511),
    tertiary = Color(0xFFFB7185),
    onTertiary = Color(0xFF200511),
    tertiaryContainer = Color(0xFF701A31),
    onTertiaryContainer = Color(0xFFFFE4E6),
    background = Color(0xFF13070C),
    onBackground = Color(0xFFFFF1F2),
    surface = Color(0xFF1E0C14),
    onSurface = Color(0xFFFFE4E6),
    surfaceVariant = Color(0xFF2E1520),
    onSurfaceVariant = Color(0xFFFECDD3),
    outline = Color(0xFF5E0B25),
    outlineVariant = Color(0xFF5E0B25),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF200511)
)

// ==========================================
// 6. Emerald Zen (زمرد ذن)
// ==========================================
val EmeraldZenLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECFDF5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    tertiary = Color(0xFF14B8A6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF0F766E),
    background = Color(0xFFF2FBF6),
    onBackground = Color(0xFF022C22),
    surface = Color.White,
    onSurface = Color(0xFF064E3B),
    surfaceVariant = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFFA7F3D0),
    outlineVariant = Color(0xFFA7F3D0),
    error = Color(0xFFEF4444),
    onError = Color.White
)

val EmeraldZenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF022C22),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF0F766E),
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF04140D),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF092015),
    onSurface = Color(0xFFD1FAE5),
    surfaceVariant = Color(0xFF0F3223),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF0D533A),
    outlineVariant = Color(0xFF0D533A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF022C22)
)

// ==========================================
// 7. Midnight Amethyst (یاقوت شب)
// ==========================================
val MidnightAmethystDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC084FC),
    onPrimary = Color(0xFF180828),
    primaryContainer = Color(0xFF3B0764),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = Color(0xFFF43F5E),
    onSecondary = Color(0xFF180828),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF180828),
    tertiaryContainer = Color(0xFF0C4A6E),
    onTertiaryContainer = Color(0xFFBAE6FD),
    background = Color(0xFF0A0713),
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0xFF140E26),
    onSurface = Color(0xFFF3E8FF),
    surfaceVariant = Color(0xFF20173D),
    onSurfaceVariant = Color(0xFFC084FC),
    outline = Color(0xFF4C1D95),
    outlineVariant = Color(0xFF4C1D95),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF180828)
)

val MidnightAmethystLightColorScheme = lightColorScheme(
    primary = Color(0xFF9333EA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF581C87),
    secondary = Color(0xFFE11D48),
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0369A1),
    background = Color(0xFFFAF7FF),
    onBackground = Color(0xFF2E1065),
    surface = Color.White,
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF6B21A8),
    outline = Color(0xFFD8B4FE),
    outlineVariant = Color(0xFFE9D5FF),
    error = Color(0xFFEF4444),
    onError = Color.White
)

// ==========================================
// 8. Sunset Peach (هلو و غروب)
// ==========================================
val SunsetPeachLightColorScheme = lightColorScheme(
    primary = Color(0xFFEA580C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF7ED),
    onPrimaryContainer = Color(0xFF9A3412),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.White,
    tertiary = Color(0xFFEC4899),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFDF2F8),
    onTertiaryContainer = Color(0xFFBE185D),
    background = Color(0xFFFFFBF7),
    onBackground = Color(0xFF431407),
    surface = Color.White,
    onSurface = Color(0xFF291005),
    surfaceVariant = Color(0xFFFFF7ED),
    onSurfaceVariant = Color(0xFF7C2D12),
    outline = Color(0xFFFED7AA),
    outlineVariant = Color(0xFFFED7AA),
    error = Color(0xFFEF4444),
    onError = Color.White
)

val SunsetPeachDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFB923C),
    onPrimary = Color(0xFF2C0B02),
    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF2C0B02),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF2C0B02),
    tertiaryContainer = Color(0xFF500724),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF140804),
    onBackground = Color(0xFFFFF7ED),
    surface = Color(0xFF220E06),
    onSurface = Color(0xFFFFEDD5),
    surfaceVariant = Color(0xFF2E1308),
    onSurfaceVariant = Color(0xFFFDBA74),
    outline = Color(0xFF67230A),
    outlineVariant = Color(0xFF67230A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF2C0B02)
)

/**
 * Returns the appropriate ColorScheme for the given palette code and dark mode flag.
 */
fun getApplicationColorScheme(paletteCode: String, isDark: Boolean): ColorScheme {
    return if (isDark) {
        when (paletteCode) {
            "SWEET_CANDY" -> SweetCandyDarkColorScheme
            "MINT_ORCHID" -> MintOrchidDarkColorScheme
            "SOFT_LAVENDER" -> SoftLavenderDarkColorScheme
            "ROSE_GOLD" -> RoseGoldDarkColorScheme
            "EMERALD_ZEN" -> EmeraldZenDarkColorScheme
            "MIDNIGHT_AMETHYST" -> MidnightAmethystDarkColorScheme
            "SUNSET_PEACH" -> SunsetPeachDarkColorScheme
            else -> CosmicBlossomDarkColorScheme
        }
    } else {
        when (paletteCode) {
            "SWEET_CANDY" -> SweetCandyLightColorScheme
            "MINT_ORCHID" -> MintOrchidLightColorScheme
            "SOFT_LAVENDER" -> SoftLavenderLightColorScheme
            "ROSE_GOLD" -> RoseGoldLightColorScheme
            "EMERALD_ZEN" -> EmeraldZenLightColorScheme
            "MIDNIGHT_AMETHYST" -> MidnightAmethystLightColorScheme
            "SUNSET_PEACH" -> SunsetPeachLightColorScheme
            else -> CosmicBlossomLightColorScheme
        }
    }
}

/**
 * Primary theme entrypoint supporting dynamic light/dark modes and palette customization.
 */
@Composable
fun MyApplicationTheme(
    themeCode: String = "COSMIC_BLOSSOM",
    themeMode: String = "SYSTEM",
    fontCode: String = "CLASSIC",
    onThemeChanged: ((String) -> Unit)? = null,
    onThemeModeChanged: ((String) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val parsedMode = ThemeMode.fromCode(themeMode)

    val isDark = when (parsedMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDark
    }

    val colorScheme = getApplicationColorScheme(themeCode, isDark)

    val themeController = remember(parsedMode, themeCode, isDark) {
        object : ThemeController {
            override val isDark: Boolean = isDark
            override val currentMode: ThemeMode = parsedMode
            override val currentPalette: String = themeCode
            override fun setMode(mode: ThemeMode) {
                onThemeModeChanged?.invoke(mode.code)
            }
            override fun setPalette(palette: String) {
                onThemeChanged?.invoke(palette)
            }
            override fun toggleMode() {
                val next = when (parsedMode) {
                    ThemeMode.LIGHT -> ThemeMode.DARK
                    ThemeMode.DARK -> ThemeMode.SYSTEM
                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                }
                onThemeModeChanged?.invoke(next.code)
            }
        }
    }

    val themeState = remember(parsedMode, themeCode, fontCode, isDark) {
        ThemeState(
            mode = parsedMode,
            palette = themeCode,
            fontCode = fontCode,
            isDarkEffective = isDark
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is Activity) {
                    break
                }
                context = context.baseContext
            }
            if (context is Activity) {
                val window = context.window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeController provides themeController,
        LocalThemeState provides themeState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypographyForFont(fontCode),
            content = content
        )
    }
}

fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            onClick = onClick
        )
}
