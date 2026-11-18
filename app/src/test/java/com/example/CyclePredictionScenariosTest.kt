package com.example

import com.example.domain.CyclePredictor
import com.example.domain.CyclePredictor.Phase
import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The cases a user actually goes through: "my last period started on X and my cycle is N days".
 * Every future period, ovulation, fertile window and phase must follow from that, on every screen
 * (dashboard via [CyclePredictor.dayStatus], calendar via the same function for each day, and the
 * lists via [CyclePredictor.upcomingPeriodStarts] / [CyclePredictor.nextOvulation]).
 */
class CyclePredictionScenariosTest {

    private fun period(start: LocalDate, days: Int) = (0 until days).map { start.plusDays(it.toLong()) }

    /** Period starts the calendar would colour, read day by day from dayStatus like CalendarScreen does. */
    private fun calendarStarts(p: CyclePredictor.Prediction, today: LocalDate, days: Int): List<LocalDate> {
        val starts = mutableListOf<LocalDate>()
        var previous = false
        for (i in 1..days) {
            val date = today.plusDays(i.toLong())
            val s = CyclePredictor.dayStatus(date, p, loggedBleedingToday = false, today = today)
            if (s.isPeriod && !previous) starts += date
            previous = s.isPeriod
        }
        return starts
    }

    @Test
    fun `last period plus 28 days predicts every next period 28 days apart`() {
        val last = LocalDate.of(2026, 11, 1)
        val today = LocalDate.of(2026, 11, 18)
        val p = CyclePredictor.predict(listOf(last), 28, today)
        assertEquals(28, p.averageCycleLength)
        assertEquals(LocalDate.of(2026, 11, 29), p.predictedNextStart)
        assertEquals(11, p.daysRemaining)
        val expected = listOf(
            LocalDate.of(2026, 11, 29), LocalDate.of(2026, 12, 27),
            LocalDate.of(2027, 1, 24), LocalDate.of(2027, 2, 21)
        )
        assertEquals(expected, CyclePredictor.upcomingPeriodStarts(p, today, 4))
        assertEquals(expected, calendarStarts(p, today, 115))
        val todayStatus = CyclePredictor.dayStatus(today, p, loggedBleedingToday = false)
        assertEquals(18, todayStatus.cycleDay)
        assertEquals(11, todayStatus.daysUntilNextPeriod)
        assertEquals(LocalDate.of(2026, 11, 29), todayStatus.predictedPeriodStart)
    }

    @Test
    fun `every cycle length from 21 to 45 projects consistently`() {
        val last = LocalDate.of(2026, 1, 25)
        for (length in CyclePredictor.MIN_CYCLE_LENGTH..CyclePredictor.MAX_CYCLE_LENGTH) {
            val today = last.plusDays(3)
            val p = CyclePredictor.predict(period(last, 5), length, today)
            assertEquals("length $length", length, p.averageCycleLength)
            assertEquals(last.plusDays(length.toLong()), p.predictedNextStart)

            val starts = CyclePredictor.upcomingPeriodStarts(p, today, 5)
            starts.forEachIndexed { k, s -> assertEquals(last.plusDays((k + 1L) * length), s) }
            assertEquals("calendar, length $length", starts, calendarStarts(p, today, 5 * length + 1))

            // Ovulation: cycle day (length - 14), never before day 10; fertile window around it.
            val ovulationDay = CyclePredictor.ovulationDay(length)
            assertEquals(maxOf(length - 14, 10), ovulationDay)
            val ovulation = CyclePredictor.nextOvulation(p, today)!!
            assertEquals(last.plusDays(ovulationDay - 1L), ovulation)
            val (from, to) = CyclePredictor.fertileWindow(ovulation)
            var d = from
            while (!d.isAfter(to)) {
                assertTrue("fertile $d, length $length", CyclePredictor.dayStatus(d, p, false, today).isFertile)
                d = d.plusDays(1)
            }
            assertFalse(CyclePredictor.dayStatus(from.minusDays(1), p, false, today).isFertile)
            assertFalse(CyclePredictor.dayStatus(to.plusDays(1), p, false, today).isFertile)
            assertEquals(Phase.OVULATION, CyclePredictor.dayStatus(ovulation, p, false, today).phase)

            // Phases cover the whole cycle in order, and the last day before the period is PMS/luteal.
            val phases = (1..length).map { CyclePredictor.phaseForCycleDay(it, length, 5) }
            assertEquals(Phase.MENSTRUAL, phases.first())
            assertEquals(Phase.LUTEAL, phases.last())
            assertEquals(phases.sortedBy { it.ordinal }, phases)
            val lastDay = CyclePredictor.dayStatus(starts.first().minusDays(1), p, false, today)
            assertEquals(length, lastDay.cycleDay)
            assertEquals(1, lastDay.daysUntilNextPeriod)
            assertTrue(lastDay.isPms)
        }
    }

    @Test
    fun `month, year and leap day boundaries`() {
        // December -> January.
        val dec = LocalDate.of(2026, 12, 20)
        val p1 = CyclePredictor.predict(listOf(dec), 28, dec.plusDays(1))
        assertEquals(LocalDate.of(2027, 1, 17), p1.predictedNextStart)
        // Through 29 February of a leap year.
        val feb = LocalDate.of(2028, 2, 10)
        val p2 = CyclePredictor.predict(listOf(feb), 28, feb)
        assertEquals(LocalDate.of(2028, 3, 9), p2.predictedNextStart)
        assertEquals(20, CyclePredictor.cycleDayFor(LocalDate.of(2028, 2, 29), feb, 28))
        // Through Nowruz: 20 Esfand 1404 + 28 days = 19 Farvardin 1405.
        val esfand = JalaliCalendar.jalaliToGregorian(1404, 12, 20)
        val p3 = CyclePredictor.predict(listOf(esfand), 28, esfand.plusDays(2))
        assertEquals(JalaliDate(1405, 1, 19), JalaliCalendar.gregorianToJalali(p3.predictedNextStart))
        // A leap Esfand (1403 has 30 days): 25 Esfand 1403 + 28 = 23 Farvardin 1404.
        val leap = JalaliCalendar.jalaliToGregorian(1403, 12, 25)
        val p4 = CyclePredictor.predict(listOf(leap), 28, leap)
        assertEquals(JalaliDate(1404, 1, 23), JalaliCalendar.gregorianToJalali(p4.predictedNextStart))
    }

    @Test
    fun `daylight saving changes do not shift date based predictions`() {
        // Dates are calendar days (LocalDate), so a 23- or 25-hour day never moves a prediction.
        val beforeDst = LocalDate.of(2026, 3, 20) // Europe springs forward on 29 March 2026.
        val p = CyclePredictor.predict(listOf(beforeDst), 28, beforeDst.plusDays(1))
        assertEquals(LocalDate.of(2026, 4, 17), p.predictedNextStart)
        val autumn = LocalDate.of(2026, 10, 10) // ... and falls back on 25 October 2026.
        assertEquals(LocalDate.of(2026, 11, 7), CyclePredictor.predict(listOf(autumn), 28, autumn).predictedNextStart)
    }

    @Test
    fun `a corrected or duplicate start date does not change a 28 day cycle`() {
        // The user first saved the wrong day (today), then the real first day 16 days earlier.
        val real = LocalDate.of(2026, 10, 25)
        val wrong = real.plusDays(16)
        val today = wrong
        val p = CyclePredictor.predict(period(real, 5) + wrong, 28, today)
        assertEquals("a 16-day gap is not a cycle", 28, p.averageCycleLength)
        assertEquals(0, p.cyclesUsed)
        assertFalse(p.hasEnoughData)
    }

    @Test
    fun `a missed log or spotting does not skew the average`() {
        val s1 = LocalDate.of(2026, 1, 1)
        val s2 = s1.plusDays(28)
        val s3 = s2.plusDays(56) // one period was not logged
        val s4 = s3.plusDays(28)
        val today = s4.plusDays(2)
        val p = CyclePredictor.predict(period(s1, 5) + period(s2, 5) + period(s3, 5) + period(s4, 5), 30, today)
        assertEquals(28, p.averageCycleLength)
        assertEquals(2, p.cyclesUsed)
        assertEquals(s4.plusDays(28), p.predictedNextStart)
        // Spotting 17 days after a period start is not a new 17-day cycle in the average.
        val spotting = s4.plusDays(17)
        val p2 = CyclePredictor.predict(period(s1, 5) + period(s2, 5) + spotting, 28, spotting)
        assertEquals(28, p2.averageCycleLength)
    }

    @Test
    fun `real cycles override the entered length once one typical cycle is logged`() {
        val s1 = LocalDate.of(2026, 5, 1)
        val s2 = s1.plusDays(32)
        val p = CyclePredictor.predict(period(s1, 5) + period(s2, 5), 28, s2.plusDays(3))
        assertTrue(p.hasEnoughData)
        assertEquals(32, p.averageCycleLength)
        assertEquals(s2.plusDays(32), p.predictedNextStart)
    }

    @Test
    fun `late period forecasts start tomorrow everywhere`() {
        val last = LocalDate.of(2026, 10, 10)
        val today = LocalDate.of(2026, 11, 18) // due 7 Nov, 11 days late
        val p = CyclePredictor.predict(listOf(last), 28, today)
        assertTrue(p.isLate)
        val status = CyclePredictor.dayStatus(today, p, false)
        assertTrue(status.isLate)
        assertEquals(11, status.daysLate)
        val tomorrow = today.plusDays(1)
        assertEquals(tomorrow, CyclePredictor.upcomingPeriodStarts(p, today, 1).single())
        assertEquals(listOf(tomorrow, tomorrow.plusDays(28), tomorrow.plusDays(56)), calendarStarts(p, today, 70))
        // Ovulation comes after the expected period, not before it.
        val ovulation = CyclePredictor.nextOvulation(p, today)
        assertNotNull(ovulation)
        assertEquals(tomorrow.plusDays(13), ovulation)
        assertTrue(CyclePredictor.dayStatus(ovulation!!, p, false, today).isFertile)
    }

    @Test
    fun `period due today is expected from tomorrow in the forecast`() {
        val last = LocalDate.of(2026, 10, 21)
        val today = last.plusDays(28)
        val p = CyclePredictor.predict(listOf(last), 28, today)
        assertEquals(0, p.daysRemaining)
        val status = CyclePredictor.dayStatus(today, p, false)
        assertEquals(0, status.daysUntilNextPeriod)
        assertFalse(status.isLate)
        assertEquals(today.plusDays(1), CyclePredictor.upcomingPeriodStarts(p, today, 1).single())
    }

    @Test
    fun `a very old last period falls back to the plain projection consistently`() {
        val last = LocalDate.of(2026, 6, 1)
        val today = LocalDate.of(2026, 11, 10) // cycle day 23 of the projection
        val p = CyclePredictor.predict(listOf(last), 28, today)
        val starts = CyclePredictor.upcomingPeriodStarts(p, today, 3)
        assertEquals(starts, calendarStarts(p, today, 3 * 28 + 1))
        starts.forEach { assertEquals(0L, ChronoUnit.DAYS.between(last, it) % 28) }
        val status = CyclePredictor.dayStatus(today, p, false)
        assertEquals(starts.first(), status.predictedPeriodStart)
        val ovulation = CyclePredictor.nextOvulation(p, today)!!
        assertEquals(14, CyclePredictor.cycleDayFor(ovulation, last, 28))
    }

    @Test
    fun `logging the predicted period moves the next prediction`() {
        val first = LocalDate.of(2026, 9, 1)
        val second = LocalDate.of(2026, 9, 30) // 29 days later
        val today = second.plusDays(4)
        val p = CyclePredictor.predict(period(first, 5) + period(second, 5), 28, today)
        assertEquals(second, p.lastPeriodStart)
        assertEquals(29, p.averageCycleLength)
        assertEquals(second.plusDays(29), CyclePredictor.upcomingPeriodStarts(p, today, 1).single())
        assertEquals(5, CyclePredictor.dayStatus(today, p, true).cycleDay)
    }
}
