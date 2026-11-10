package com.example.ui.theme

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

/**
 * Hanita's mobile design tokens (phones only, portrait first).
 *
 * - Spacing follows a 4dp grid; screens use a 16dp side gutter.
 * - Every tappable element gets at least a 48dp touch area (Material / WCAG 2.5.8).
 * - Motion uses Material "emphasized" easing; enter 300ms, exit 200ms (exit is faster than enter).
 */
object HanitaSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Horizontal page gutter on phones. */
    val gutter = 16.dp

    /** Minimum touch target for anything tappable. */
    val touchTarget = 48.dp
}

/** Rounded shapes used by Material components (cards, dialogs, buttons, text fields). */
val HanitaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object HanitaMotion {
    /** Material 3 emphasized decelerate: elements entering the screen. */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Material 3 emphasized accelerate: elements leaving the screen. */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val ENTER_MS = 300
    const val EXIT_MS = 200
    const val FADE_MS = 150

    fun <T> enter() = tween<T>(ENTER_MS, easing = EmphasizedDecelerate)
    fun <T> exit() = tween<T>(EXIT_MS, easing = EmphasizedAccelerate)
}

/**
 * Light haptic ticks for confirmations (a tab change, +1 glass of water, a logged mood).
 * Uses the View API so it follows the user's system "touch feedback" setting.
 */
class HanitaHaptics(private val view: View) {
    /** A short tick for selections and small steps. */
    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** A firmer confirmation for a completed action (data saved). */
    fun confirm() {
        val type = if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.VIRTUAL_KEY
        view.performHapticFeedback(type)
    }
}

@Composable
fun rememberHanitaHaptics(): HanitaHaptics {
    val view = LocalView.current
    return remember(view) { HanitaHaptics(view) }
}
