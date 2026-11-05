package com.example

import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PeriodCalendarUnitTest {

    @Test
    fun testJalaliDateConversionAndComparison() {
        val gregorianDate = LocalDate.of(2026, 5, 15)
        val jalaliDate = JalaliCalendar.gregorianToJalali(gregorianDate)
        
        assertNotNull(jalaliDate)
        assertTrue(jalaliDate.year >= 1405)
        assertTrue(jalaliDate.month in 1..12)
        assertTrue(jalaliDate.day in 1..31)

        val backToGregorian = jalaliDate.toLocalDate()
        assertEquals(gregorianDate, backToGregorian)
    }

    @Test
    fun testPeriodRangeDuration() {
        val start = LocalDate.of(2026, 5, 10)
        val end = LocalDate.of(2026, 5, 15)

        val durationDays = ChronoUnit.DAYS.between(start, end).toInt() + 1
        assertEquals(6, durationDays)

        val startJalali = JalaliCalendar.gregorianToJalali(start)
        val endJalali = JalaliCalendar.gregorianToJalali(end)
        assertTrue(startJalali <= endJalali)
    }
}
