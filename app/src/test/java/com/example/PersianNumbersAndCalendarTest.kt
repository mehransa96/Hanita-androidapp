package com.example

import com.example.domain.CyclePredictor
import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate
import com.example.utils.MedicineReminderHelper
import com.example.utils.PersianNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PersianNumbersAndCalendarTest {

    @Test
    fun `persian and arabic digits are parsed`() {
        assertEquals(65.5f, PersianNumbers.parseDecimal("۶۵٫۵")!!, 0.001f)
        assertEquals(36.7f, PersianNumbers.parseDecimal("٣٦.٧")!!, 0.001f)
        assertEquals(70f, PersianNumbers.parseDecimal(" 70 ")!!, 0.001f)
        assertEquals(28, PersianNumbers.parseInt("۲۸"))
    }

    @Test
    fun `invalid or out of range numbers are rejected`() {
        assertNull(PersianNumbers.parseDecimal("abc"))
        assertNull(PersianNumbers.parseDecimal(""))
        assertNull(PersianNumbers.parseDecimal("1.2.3"))
        assertNull(PersianNumbers.parseDecimal("500", 25f..250f))
        assertNull(PersianNumbers.parseInt("۵۰", 21..45))
        assertNull(PersianNumbers.parseInt("-3"))
    }

    @Test
    fun `display helpers use persian digits`() {
        assertEquals("۱۴۰۵", PersianNumbers.toPersianDigits(1405))
        assertEquals("۰۸:۰۵", PersianNumbers.formatTime(8, 5))
        assertEquals("۶۵٫۵", PersianNumbers.formatDecimal(65.5f))
        assertEquals("۷۰", PersianNumbers.formatDecimal(70f))
    }

    @Test
    fun `medicine times are validated`() {
        assertEquals(LocalTime.of(21, 30), MedicineReminderHelper.parseTime("۲۱:۳۰"))
        assertNull(MedicineReminderHelper.parseTime("9"))
        assertNull(MedicineReminderHelper.parseTime("25:00"))
        val now = LocalDateTime.of(2026, 1, 1, 22, 0)
        assertEquals(LocalDateTime.of(2026, 1, 2, 21, 30), MedicineReminderHelper.nextOccurrence(LocalTime.of(21, 30), now))
        assertEquals(LocalDateTime.of(2026, 1, 1, 23, 0), MedicineReminderHelper.nextOccurrence(LocalTime.of(23, 0), now))
    }

    @Test
    fun `jalali conversion round-trips for every day of several years`() {
        var date = LocalDate.of(2019, 1, 1)
        val end = LocalDate.of(2031, 12, 31)
        while (!date.isAfter(end)) {
            val j = JalaliCalendar.gregorianToJalali(date)
            assertTrue(j.day in 1..j.lengthOfMonth)
            assertEquals(date, j.toLocalDate())
            date = date.plusDays(1)
        }
    }

    @Test
    fun `known jalali dates`() {
        assertEquals(JalaliDate(1403, 1, 1), JalaliCalendar.gregorianToJalali(LocalDate.of(2024, 3, 20)))
        assertEquals(JalaliDate(1404, 1, 1), JalaliCalendar.gregorianToJalali(LocalDate.of(2025, 3, 21)))
        // 1403 is a leap year: Esfand has 30 days.
        assertEquals(30, JalaliDate(1403, 12, 1).lengthOfMonth)
        assertEquals(29, JalaliDate(1404, 12, 1).lengthOfMonth)
    }

    @Test
    fun `period runs and completed cycles come from the logged days`() {
        fun period(start: LocalDate, days: Int) = (0 until days).map { start.plusDays(it.toLong()) }
        val days = period(LocalDate.of(2026, 1, 1), 5) + period(LocalDate.of(2026, 1, 30), 4) + period(LocalDate.of(2026, 2, 27), 6)
        val runs = CyclePredictor.periodRuns(days)
        assertEquals(3, runs.size)
        assertEquals(LocalDate.of(2026, 1, 5), runs[0].end)
        assertEquals(4, runs[1].lengthDays)
        val cycles = CyclePredictor.completedCycles(days, LocalDate.of(2026, 3, 10))
        assertEquals(listOf(29, 28), cycles.map { it.lengthDays })
        assertEquals(listOf(5, 4), cycles.map { it.periodDays })
    }

    @Test
    fun `future days are not counted as completed cycles`() {
        val days = listOf(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1))
        assertTrue(CyclePredictor.completedCycles(days, LocalDate.of(2026, 1, 15)).isEmpty())
    }
}
