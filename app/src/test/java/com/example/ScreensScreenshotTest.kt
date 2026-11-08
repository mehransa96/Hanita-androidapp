package com.example

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChatBotScreen
import com.example.ui.screens.CycleHistoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HealthScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SymptomChartsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MahitaViewModel
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/**
 * Renders every main screen (light and dark, with realistic data) to PNG files under
 * build/outputs/roborazzi, so the UI can be reviewed without a device.
 * Run: ./gradlew testDebugUnitTest -Pscreenshots -Proborazzi.test.record=true --tests com.example.ScreensScreenshotTest
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h3000dp-mdpi")
class ScreensScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun viewModelWithData(): MahitaViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MahitaViewModel(app)
        vm.login("مریم", 29, null)
        val today = LocalDate.now()
        // Three past periods (two complete cycles) and today's log.
        vm.savePeriodRange(today.minusDays(66), today.minusDays(62), "MEDIUM", "")
        vm.savePeriodRange(today.minusDays(37), today.minusDays(33), "HEAVY", "")
        vm.savePeriodRange(today.minusDays(8), today.minusDays(4), "MEDIUM", "")
        vm.logQuickMood("آرامش")
        vm.incrementWater()
        vm.incrementWater()
        return vm
    }

    private fun shoot(name: String, dark: Boolean, content: @Composable (MahitaViewModel) -> Unit) {
        val vm = viewModelWithData()
        compose.setContent {
            MyApplicationTheme(themeMode = if (dark) "DARK" else "LIGHT") {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    content(vm)
                }
            }
        }
        // Let Room and the ViewModel flows settle.
        repeat(20) {
            Thread.sleep(100)
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/${name}_${if (dark) "dark" else "light"}.png")
    }

    private val noop: () -> Unit = {}

    @Test fun login() = shoot("01_login", false) { LoginScreen(onLoginSuccess = { _, _, _ -> }) }

    @Test fun dashboardLight() = shoot("02_dashboard", false) { vm ->
        DashboardScreen(vm, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop)
    }

    @Test fun dashboardDark() = shoot("02_dashboard", true) { vm ->
        DashboardScreen(vm, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop, noop)
    }

    @Test fun log() = shoot("03_log", false) { vm -> LogScreen(vm, noop) }
    @Test fun calendar() = shoot("04_calendar", false) { vm -> CalendarScreen(vm, noop) }
    @Test fun calendarDark() = shoot("04_calendar", true) { vm -> CalendarScreen(vm, noop) }
    @Test fun reminders() = shoot("05_reminders", false) { vm -> RemindersScreen(vm, noop) }
    @Test fun health() = shoot("06_health", true) { vm -> HealthScreen(vm, noop) }
    @Test fun chat() = shoot("07_chat", false) { vm -> ChatBotScreen(vm, noop) }
    @Test fun charts() = shoot("08_charts", false) { vm -> SymptomChartsScreen(vm, noop) }
    @Test fun history() = shoot("09_history", false) { vm -> CycleHistoryScreen(vm, noop) }
}
