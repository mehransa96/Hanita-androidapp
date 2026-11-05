package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode(
    val code: String,
    val titleFa: String,
    val subtitleFa: String
) {
    SYSTEM(
        code = "SYSTEM",
        titleFa = "پیروی از سیستم 📱",
        subtitleFa = "تنظیم خودکار پوسته روشن یا تاریک بر اساس حالت دستگاه"
    ),
    LIGHT(
        code = "LIGHT",
        titleFa = "حالت روشن (روز) ☀️",
        subtitleFa = "ظاهر درخشان، سفید و شفاف متناسب با روشنایی روز"
    ),
    DARK(
        code = "DARK",
        titleFa = "حالت تاریک (شب) 🌙",
        subtitleFa = "کاهش خستگی چشم و صرفه‌جویی در باتری با پس‌زمینه تیره"
    );

    companion object {
        fun fromCode(code: String?): ThemeMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class ThemeState(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val palette: String = "COSMIC_BLOSSOM",
    val fontCode: String = "CLASSIC",
    val isDarkEffective: Boolean = true
)

interface ThemeController {
    val isDark: Boolean
    val currentMode: ThemeMode
    val currentPalette: String
    fun setMode(mode: ThemeMode)
    fun setPalette(palette: String)
    fun toggleMode()
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    object : ThemeController {
        override val isDark: Boolean = true
        override val currentMode: ThemeMode = ThemeMode.SYSTEM
        override val currentPalette: String = "COSMIC_BLOSSOM"
        override fun setMode(mode: ThemeMode) {}
        override fun setPalette(palette: String) {}
        override fun toggleMode() {}
    }
}

val LocalThemeState = staticCompositionLocalOf {
    ThemeState()
}

/**
 * Convenient object to access theme controller and state anywhere in Compose.
 */
object ThemeProvider {
    val controller: ThemeController
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeController.current

    val state: ThemeState
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeState.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeState.current.isDarkEffective
}
