package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

private val CosmicBlossomColorScheme = darkColorScheme(
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
    surfaceVariant = Color(0xFF161226),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2A1F5A),
    outlineVariant = Color(0xFF2A1F5A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF0D0B1A)
)

private val SweetCandyColorScheme = lightColorScheme(
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
    surfaceVariant = Color.White,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFF1F5F9),
    outlineVariant = Color(0xFFF1F5F9),
    error = Color(0xFFFF4C4C),
    onError = Color.White
)

private val MintOrchidColorScheme = darkColorScheme(
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
    surfaceVariant = Color(0xFF0C2427),
    onSurfaceVariant = Color(0xFF5EEAD4),
    outline = Color(0xFF134E4A),
    outlineVariant = Color(0xFF134E4A),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF061517)
)

private val SoftLavenderColorScheme = darkColorScheme(
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
    surfaceVariant = Color(0xFF1B192A),
    onSurfaceVariant = Color(0xFFC4B5FD),
    outline = Color(0xFF312E81),
    outlineVariant = Color(0xFF312E81),
    error = Color(0xFFFF4C4C),
    onError = Color(0xFF0F0E17)
)

@Composable
fun MyApplicationTheme(
    themeCode: String = "COSMIC_BLOSSOM",
    fontCode: String = "CLASSIC",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeCode) {
        "SWEET_CANDY" -> SweetCandyColorScheme
        "MINT_ORCHID" -> MintOrchidColorScheme
        "SOFT_LAVENDER" -> SoftLavenderColorScheme
        else -> CosmicBlossomColorScheme
    }

    val isLight = themeCode == "SWEET_CANDY"

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
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypographyForFont(fontCode),
        content = content
    )
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
