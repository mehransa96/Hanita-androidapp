package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.CosmicBlossomDarkColorScheme
import com.example.ui.theme.CosmicBlossomLightColorScheme
import com.example.ui.theme.DEFAULT_THEME
import com.example.ui.theme.PeonyBlossomDarkColorScheme
import com.example.ui.theme.PeonyBlossomLightColorScheme
import com.example.ui.theme.SunsetPeachLightColorScheme
import com.example.ui.theme.getApplicationColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeonyThemeTest {

    private fun contrast(a: Color, b: Color): Double {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test fun peonyIsTheDefaultAndTheFallback() {
        assertEquals("PEONY_BLOSSOM", DEFAULT_THEME)
        assertEquals(PeonyBlossomLightColorScheme.primary, getApplicationColorScheme(DEFAULT_THEME, false).primary)
        assertEquals(PeonyBlossomDarkColorScheme.primary, getApplicationColorScheme(DEFAULT_THEME, true).primary)
        assertEquals(PeonyBlossomLightColorScheme.primary, getApplicationColorScheme("UNKNOWN", false).primary)
    }

    @Test fun existingThemesAreUnchanged() {
        assertEquals(CosmicBlossomLightColorScheme.primary, getApplicationColorScheme("COSMIC_BLOSSOM", false).primary)
        assertEquals(CosmicBlossomDarkColorScheme.background, getApplicationColorScheme("COSMIC_BLOSSOM", true).background)
        assertEquals(SunsetPeachLightColorScheme.primary, getApplicationColorScheme("SUNSET_PEACH", false).primary)
    }

    @Test fun peonyTextMeetsWcagAa() {
        for (s in listOf(PeonyBlossomLightColorScheme, PeonyBlossomDarkColorScheme)) {
            val pairs = listOf(
                s.onBackground to s.background, s.onSurface to s.surface, s.onSurfaceVariant to s.surface,
                s.onSurfaceVariant to s.surfaceVariant, s.onPrimary to s.primary, s.primary to s.surface,
                s.primary to s.background, s.secondary to s.surface, s.tertiary to s.surface,
                s.onPrimaryContainer to s.primaryContainer, s.onSecondaryContainer to s.secondaryContainer,
                s.onTertiaryContainer to s.tertiaryContainer, s.error to s.surface
            )
            pairs.forEachIndexed { i, (fg, bg) ->
                assertTrue("pair $i: ${contrast(fg, bg)}", contrast(fg, bg) >= 4.5)
            }
        }
    }
}
