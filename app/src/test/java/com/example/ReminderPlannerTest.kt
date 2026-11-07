package com.example

import com.example.domain.CyclePredictor
import com.example.domain.ReminderPlanner
import com.example.domain.ReminderPlanner.PhaseKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderPlannerTest {

    private val nine = LocalTime.of(9, 0)
    private fun settings(daysBeforePeriod: Int = 1, daysBeforeOvulation: Int = 1) =
        ReminderPlanner.Settings(true, true, daysBeforePeriod, daysBeforeOvulation, nine)

    private fun prediction(lastStart: LocalDate, length: Int = 28, today: LocalDate) =
        CyclePredictor.predict((0 until 5).map { lastStart.plusDays(it.toLong()) }, length, today)

    @Test
    fun `no period logged means no reminders`() {
        val p = CyclePredictor.predict(emptyList(), 28, LocalDate.of(2026, 1, 1))
        assertEquals(ReminderPlanner.Plan.EMPTY, ReminderPlanner.plan(p, settings(), LocalDateTime.of(2026, 1, 1, 8, 0)))
    }

    @Test
    fun `period reminder fires the chosen days before the predicted start`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 10, 12, 0)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(daysBeforePeriod = 2), now)
        assertEquals(LocalDate.of(2026, 1, 29), plan.period!!.eventDate)
        assertEquals(LocalDateTime.of(2026, 1, 27, 9, 0), plan.period!!.fireAt)
    }

    @Test
    fun `ovulation reminder never skips a whole cycle`() {
        // Ovulation (cycle day 14) is on Jan 14. On Jan 13 at 12:00 the "1 day before" time
        // (Jan 13 09:00) has passed, so the next one is the following cycle's ovulation (Feb 11),
        // not the one two cycles later (the old bug).
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 13, 12, 0)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(), now)
        assertEquals(LocalDate.of(2026, 2, 11), plan.ovulation!!.eventDate)
        assertEquals(LocalDateTime.of(2026, 2, 10, 9, 0), plan.ovulation!!.fireAt)
    }

    @Test
    fun `ovulation date matches the prediction engine`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 2, 8, 0)
        val p = prediction(last, length = 32, today = now.toLocalDate())
        val plan = ReminderPlanner.plan(p, settings(daysBeforeOvulation = 0), now)
        val ovulationDay = plan.ovulation!!.eventDate
        val status = CyclePredictor.dayStatus(ovulationDay, p, loggedBleedingToday = false, today = now.toLocalDate())
        assertEquals(CyclePredictor.Phase.OVULATION, status.phase)
        assertTrue(status.isFertile)
    }

    @Test
    fun `late period only plans the late check-in`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 30, 8, 0) // expected Jan 29, one day late
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(), now)
        assertNull(plan.period)
        assertNull(plan.ovulation)
        assertTrue(plan.phases.isEmpty())
        assertEquals(LocalDateTime.of(2026, 2, 1, 9, 0), plan.lateCheck!!.fireAt)
    }

    @Test
    fun `due today is not late and keeps the same-day reminder`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 29, 7, 0)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(daysBeforePeriod = 0), now)
        assertEquals(LocalDateTime.of(2026, 1, 29, 9, 0), plan.period!!.fireAt)
    }

    @Test
    fun `disabled reminders are not planned`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 5, 8, 0)
        val off = ReminderPlanner.Settings(false, false, 1, 1, nine)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), off, now)
        assertNull(plan.period)
        assertNull(plan.ovulation)
        assertNull(plan.lateCheck)
        assertTrue(plan.phases.isEmpty())
    }

    @Test
    fun `every phase reminder is in the future`() {
        val last = LocalDate.of(2026, 1, 1)
        val now = LocalDateTime.of(2026, 1, 20, 10, 0)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(), now)
        assertEquals(PhaseKind.entries.toSet(), plan.phases.map { it.kind }.toSet())
        plan.phases.forEach { assertTrue(it.fireAt.isAfter(now)) }
        assertEquals(LocalDateTime.of(2026, 1, 29, 9, 0), plan.phases.first { it.kind == PhaseKind.MENSTRUAL }.fireAt)
    }

    @Test
    fun `long gap without data falls back to the projection`() {
        val last = LocalDate.of(2025, 6, 1)
        val now = LocalDateTime.of(2026, 1, 10, 8, 0)
        val plan = ReminderPlanner.plan(prediction(last, today = now.toLocalDate()), settings(), now)
        assertNotNull(plan.period)
        assertTrue(plan.period!!.fireAt.isAfter(now))
    }
}
