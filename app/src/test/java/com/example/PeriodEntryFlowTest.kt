package com.example

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.MahitaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * Runs the real ViewModel with the real (Robolectric) database through what a user does:
 * sign up with "last period on X, cycle 28 days", correct the date in settings, then log the next
 * period. The dashboard, calendar and reminders all read [MahitaViewModel.cyclePrediction] and
 * [MahitaViewModel.cycleInfo], so these are the numbers every tab shows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PeriodEntryFlowTest {

    private fun waitUntil(what: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(50)
        }
        throw AssertionError("Timed out waiting for: $what")
    }

    @Test
    fun `sign up, correct the date and log the next period`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MahitaViewModel(app)
        val today = LocalDate.now()

        // 1. Sign-up: the first day of the last period was 10 days ago, the cycle is 28 days.
        val entered = today.minusDays(10)
        vm.register("مریم", 28, entered.toString())
        waitUntil("the entered period is used") {
            vm.cyclePrediction.value.lastPeriodStart == entered && vm.periodRuns.value.size == 1
        }
        var p = vm.cyclePrediction.value
        assertEquals(28, p.averageCycleLength)
        assertEquals(entered.plusDays(28), p.predictedNextStart)
        assertEquals(18, p.daysRemaining)
        waitUntil("today's status follows") { vm.cycleInfo.value.currentDay == 11 }
        assertEquals(18, vm.cycleInfo.value.daysUntilNextPeriod)
        assertEquals(entered.plusDays(28), vm.cycleInfo.value.predictedPeriodStart)

        // 2. Settings: "actually it started 4 days earlier". This corrects the last period instead
        //    of adding a second one, so the 28 days and the forecast stay right.
        val corrected = entered.minusDays(4)
        assertEquals(MahitaViewModel.StartChange.MOVED, vm.setLastPeriodStart(corrected))
        waitUntil("the corrected start is used") {
            vm.cyclePrediction.value.lastPeriodStart == corrected && vm.periodRuns.value.size == 1
        }
        p = vm.cyclePrediction.value
        assertEquals(28, p.averageCycleLength)
        assertFalse(p.hasEnoughData)
        assertEquals(corrected.plusDays(28), p.predictedNextStart)

        // 3. A later correction (still inside the same cycle) moves it again, not a new 21-day cycle.
        val later = corrected.plusDays(6)
        assertEquals(MahitaViewModel.StartChange.MOVED, vm.setLastPeriodStart(later))
        waitUntil("moved forward") { vm.cyclePrediction.value.lastPeriodStart == later && vm.periodRuns.value.size == 1 }
        assertEquals(later.plusDays(28), vm.cyclePrediction.value.predictedNextStart)

        // 4. Changing the cycle length before any full cycle is logged changes the forecast.
        vm.setAverageCycleLength(30)
        waitUntil("30-day cycle") { vm.cyclePrediction.value.averageCycleLength == 30 }
        assertEquals(later.plusDays(30), vm.cyclePrediction.value.predictedNextStart)
        vm.setAverageCycleLength(28)
        waitUntil("back to 28") { vm.cyclePrediction.value.averageCycleLength == 28 }

        // 5. A whole earlier period logged in the calendar 29 days before: the real average is used.
        val earlier = later.minusDays(29)
        vm.savePeriodRange(earlier, earlier.plusDays(4))
        waitUntil("two periods") { vm.periodRuns.value.size == 2 && vm.cyclePrediction.value.hasEnoughData }
        p = vm.cyclePrediction.value
        assertEquals(29, p.averageCycleLength)
        assertEquals(later, p.lastPeriodStart)
        assertEquals(later.plusDays(29), p.predictedNextStart)
        assertTrue(vm.completedCycles.value.single().isTypical)
        assertEquals(29, vm.completedCycles.value.single().lengthDays)
    }

    @Test
    fun `a future date is never saved as a period`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MahitaViewModel(app)
        assertEquals(MahitaViewModel.StartChange.IGNORED, vm.setLastPeriodStart(LocalDate.now().plusDays(3)))
    }
}
