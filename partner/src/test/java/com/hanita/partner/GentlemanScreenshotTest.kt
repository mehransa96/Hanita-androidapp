package com.hanita.partner

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.Status
import com.hanita.partner.data.GrowthRepository
import com.hanita.partner.data.JournalEntity
import com.hanita.partner.ui.AppActions
import com.hanita.partner.ui.AssessmentScreen
import com.hanita.partner.ui.BreathingScreen
import com.hanita.partner.ui.BudgetScreen
import com.hanita.partner.ui.GrowthScreen
import com.hanita.partner.ui.JournalScreen
import com.hanita.partner.ui.PartnerApp
import com.hanita.partner.ui.PartnerScreen
import com.hanita.partner.ui.PartnerTheme
import com.hanita.partner.ui.ThemeController
import com.hanita.partner.ui.HealthCheckScreen
import com.hanita.partner.ui.BodyScreen
import com.hanita.partner.ui.ChecklistScreen
import com.hanita.core.gentleman.ThemePresets
import com.hanita.partner.ui.PathScreen
import com.hanita.partner.ui.PillarScreen
import com.hanita.partner.ui.SettingsScreen
import com.hanita.partner.ui.SoftStartScreen
import com.hanita.partner.ui.TodayScreen
import com.hanita.partner.ui.ToolsScreen
import com.hanita.partner.ui.WelcomeScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/**
 * Renders the GentleMan screens (dark is the signature look, plus light, a 360dp phone and a
 * large system font) to PNGs under build/outputs/roborazzi, so the UI can be reviewed without a
 * device. Run:
 *   ./gradlew :partner:testDebugUnitTest -Pscreenshots -Proborazzi.test.record=true --tests com.hanita.partner.GentlemanScreenshotTest
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h2400dp-night-mdpi")
class GentlemanScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val app: Application get() = ApplicationProvider.getApplicationContext()
    private val today = LocalDate.now()
    private val none = PaddingValues()
    private val actions = AppActions({}, {}, {}, {}, {}, {}, {}, {})

    @After fun tearDown() = GrowthRepository.resetForTests()

    /** A user who has been using the app for a few weeks. */
    private fun seeded(status: Status): Pair<PartnerStore, GrowthRepository> {
        GrowthRepository.resetForTests()
        val store = PartnerStore(app)
        store.userName = "علی"
        store.status = status
        val repo = GrowthRepository.get(app)
        runBlocking {
            repo.prepare(status)
            Habits.preset("read")?.let { repo.addPreset(it) }
            val habits = repo.habits.first()
            habits.forEachIndexed { i, h ->
                for (d in 0L..20L) if ((d + i) % 3 != 0L) repo.setHabitDone(h.id, today.minusDays(d), true)
            }
            for (d in 1L..9L) repo.setChallengeDone(today.minusDays(d), true, null)
            repo.saveJournal(JournalEntity(today.minusDays(1).toEpochDay(), 4, "با پدرم قدم زدیم", "کار پروژه تمام شد", "", "فردا زودتر بخوابم"))
            repo.saveJournal(JournalEntity(today.minusDays(2).toEpochDay(), 3, "ورزش کردم", "", "", ""))
            repo.setMood(today, 4, null)
            repo.saveAssessment(Assessments.Kind.SELF_ESTEEM, listOf(2, 2, 1, 2, 1, 1, 2, 1, 1, 2))
            repo.saveAssessment(Assessments.Kind.SELF_ESTEEM, listOf(3, 2, 2, 3, 1, 1, 3, 1, 1, 3))
            repo.saveAssessment(Assessments.Kind.WELL_BEING, listOf(3, 3, 2, 3, 2))
            repo.addGoal("دویدن ۵ کیلومتر تا عید", "اگر شنبه و سه‌شنبه ساعت ۷ شد، آنگاه ۳۰ دقیقه می‌دوم.", null)
        }
        return store to repo
    }

    private fun shoot(name: String, themes: ThemeController? = null, content: @Composable () -> Unit) {
        compose.setContent { PartnerTheme(themes) { content() } }
        repeat(15) {
            Thread.sleep(80)
            compose.mainClock.advanceTimeBy(400)
            compose.waitForIdle()
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Config(qualifiers = "w411dp-h891dp-night-mdpi")
    @Test fun welcome() = shoot("01_welcome") { WelcomeScreen { _, _, _ -> } }

    // The whole app with its bottom bar, at a real phone size.
    @Config(qualifiers = "w411dp-h891dp-night-mdpi")
    @Test fun appMarried() {
        seeded(Status.MARRIED)
        shoot("02_app_today_married") { PartnerApp(null) {} }
    }

    @Config(qualifiers = "w411dp-h891dp-notnight-mdpi")
    @Test fun appSingleLight() {
        seeded(Status.SINGLE)
        shoot("02b_app_today_single_light") { PartnerApp(null) {} }
    }

    // Smallest common phone with a large system font.
    @Config(qualifiers = "w360dp-h740dp-night-mdpi", fontScale = 1.3f)
    @Test fun appSmallPhoneLargeFont() {
        seeded(Status.MARRIED)
        shoot("02c_app_today_360_font130") { PartnerApp(null) {} }
    }

    @Test fun today() {
        val (store, repo) = seeded(Status.MARRIED)
        shoot("03_today") { TodayScreen(none, store, repo, 0, Status.MARRIED, today, actions) }
    }

    @Test fun partnerNotLinked() {
        val (store, _) = seeded(Status.MARRIED)
        shoot("04_partner_unlinked") { PartnerScreen(none, store, 0, actions) }
    }

    @Test fun path() = shoot("05_path") { PathScreen(none, Status.MARRIED, actions) }

    @Test fun pillar() = shoot("06_pillar_esteem") { PillarScreen(none, Status.SINGLE, "esteem", actions) }

    @Test fun growth() {
        val (_, repo) = seeded(Status.SINGLE)
        shoot("07_growth") { GrowthScreen(none, repo, Status.SINGLE, today, actions) }
    }

    @Config(qualifiers = "w411dp-h2400dp-notnight-mdpi")
    @Test fun growthLight() {
        val (_, repo) = seeded(Status.MARRIED)
        shoot("07b_growth_light") { GrowthScreen(none, repo, Status.MARRIED, today, actions) }
    }

    @Test fun assessmentQuestion() {
        val (_, repo) = seeded(Status.SINGLE)
        shoot("08_assessment_rses") { AssessmentScreen(none, repo, Assessments.Kind.SELF_ESTEEM.code, actions) }
    }

    @Test fun journal() {
        val (_, repo) = seeded(Status.SINGLE)
        shoot("09_journal") { JournalScreen(none, repo, today.minusDays(1).toEpochDay(), today, actions) }
    }

    @Test fun tools() = shoot("10_tools") { ToolsScreen(none, Status.SINGLE, actions) }

    @Test fun breathing() = shoot("11_breathing") { BreathingScreen(none, "sigh", actions) }

    @Test fun budget() = shoot("12_budget") { BudgetScreen(none, actions) }

    @Test fun softStart() = shoot("13_soft_start") { SoftStartScreen(none, actions) }

    @Test fun settings() {
        val (store, _) = seeded(Status.MARRIED)
        shoot("14_settings", ThemeController("graphite", ThemePresets.Mode.DARK)) {
            SettingsScreen(none, store, 0, Status.MARRIED, actions, {}, {})
        }
    }

    // ---- 4.0: themes, new self-checks and men's health tools ----

    @Config(qualifiers = "w411dp-h891dp-notnight-mdpi")
    @Test fun themesNavyLight() {
        seeded(Status.MARRIED)
        shoot("15_theme_navy_light", ThemeController("navy", ThemePresets.Mode.LIGHT)) { PartnerApp(null) {} }
    }

    @Config(qualifiers = "w411dp-h891dp-night-mdpi")
    @Test fun themesForestDark() {
        seeded(Status.SINGLE)
        shoot("16_theme_forest_dark", ThemeController("forest", ThemePresets.Mode.DARK)) { PartnerApp(null) {} }
    }

    @Config(qualifiers = "w411dp-h891dp-night-mdpi")
    @Test fun themesCopperGrowth() {
        val (_, repo) = seeded(Status.MARRIED)
        shoot("17_theme_copper_growth", ThemeController("copper", ThemePresets.Mode.DARK)) {
            GrowthScreen(none, repo, Status.MARRIED, today, actions)
        }
    }

    @Test fun assessmentPhq9() {
        val (_, repo) = seeded(Status.SINGLE)
        shoot("18_assessment_phq9") { AssessmentScreen(none, repo, Assessments.Kind.DEPRESSION.code, actions) }
    }

    @Test fun pillarMensHealth() = shoot("19_pillar_menhealth") { PillarScreen(none, Status.SINGLE, "menhealth", actions) }

    @Test fun healthCheck() = shoot("20_health_check") { HealthCheckScreen(none, actions) }

    @Test fun body() = shoot("21_body") { BodyScreen(none, actions) }

    @Test fun workout() = shoot("22_workout") { ChecklistScreen(none, "workout", actions) }
}
