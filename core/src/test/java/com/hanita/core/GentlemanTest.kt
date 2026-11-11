package com.hanita.core

import com.example.utils.JalaliCalendar
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.ImportantDate
import com.hanita.core.gentleman.Progress
import com.hanita.core.gentleman.Status
import com.hanita.core.partner.PartnerDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GentlemanTest {

    private fun jalali(y: Int, m: Int, d: Int): LocalDate = JalaliCalendar.jalaliToGregorian(y, m, d)

    private fun date(month: Int, day: Int, year: Int? = null, before: Int = 1) =
        ImportantDate.create("id$month-$day", "تولد", ImportantDate.Kind.BIRTHDAY, month, day, year, before)

    @Test
    fun nextOccurrenceIsThisYearOrNext() {
        val today = jalali(1405, 8, 20)
        assertEquals(jalali(1405, 9, 30), date(9, 30).nextOccurrence(today))
        assertEquals(jalali(1406, 1, 1), date(1, 1).nextOccurrence(today))
        assertEquals(today, date(8, 20).nextOccurrence(today))
        assertEquals(0, date(8, 20).daysUntil(today))
        assertEquals(1, date(8, 21).daysUntil(today))
    }

    @Test
    fun esfandThirtyFallsBackInCommonYears() {
        val d = date(12, 30)
        val today = jalali(1404, 1, 1)
        val next = d.nextOccurrence(today)
        val j = JalaliCalendar.gregorianToJalali(next)
        assertEquals(12, j.month)
        assertTrue(j.day == 29 || j.day == 30)
    }

    @Test
    fun yearsOnNextOccurrence() {
        val today = jalali(1405, 8, 20)
        assertEquals(5, date(9, 1, year = 1400).yearsOnNext(today))
        assertEquals(6, date(2, 1, year = 1400).yearsOnNext(today))
        assertNull(date(2, 1).yearsOnNext(today))
    }

    @Test
    fun dueTodayOnTheDayAndDaysBefore() {
        val today = jalali(1405, 8, 20)
        val dates = listOf(date(8, 20, before = 0), date(8, 23, before = 3), date(8, 22, before = 3), date(8, 21, before = 0))
        val due = ImportantDate.dueToday(dates, today)
        assertEquals(listOf(0, 3), due.map { it.second })
    }

    @Test
    fun encodeDecodeRoundTripAndClamping() {
        val dates = listOf(
            ImportantDate.create("a", "سالگرد\u001Fازدواج\n", ImportantDate.Kind.ANNIVERSARY, 5, 12, 1398, 3),
            ImportantDate.create("b", "x".repeat(100), ImportantDate.Kind.OTHER, 13, 40, 99, 99)
        )
        val back = ImportantDate.decode(ImportantDate.encode(dates))
        assertEquals(dates, back)
        assertEquals("سالگرد ازدواج", back[0].title)
        assertEquals(12, back[1].month)
        assertEquals(30, back[1].day)
        assertNull(back[1].year)
        assertEquals(ImportantDate.MAX_TITLE, back[1].title.length)
        assertEquals(emptyList<ImportantDate>(), ImportantDate.decode("garbage"))
        assertEquals(emptyList<ImportantDate>(), ImportantDate.decode(null))
    }

    @Test
    fun streakCountsConsecutiveDays() {
        val today = LocalDate.of(2026, 11, 11)
        val d = today.toEpochDay()
        assertEquals(3, Progress.streak(setOf(d, d - 1, d - 2, d - 4), today))
        assertEquals(2, Progress.streak(setOf(d - 1, d - 2), today))
        assertEquals(0, Progress.streak(setOf(d - 2), today))
    }

    @Test
    fun pillarsFollowStatus() {
        val single = GentlemanGuide.pillarsFor(Status.SINGLE).map { it.id }
        val married = GentlemanGuide.pillarsFor(Status.MARRIED).map { it.id }
        assertTrue("dating" in single && "choosing" in single)
        assertFalse("marriage" in single || "cycle" in single)
        assertTrue("marriage" in married && "conflict" in married && "cycle" in married)
        assertFalse("dating" in married)
        assertTrue("manners" in single && "manners" in married)
    }

    @Test
    fun dailyContentRotatesAndFitsStatus() {
        val start = LocalDate.of(2026, 11, 11)
        for (status in Status.values()) {
            val seen = mutableSetOf<String>()
            for (i in 0 until 30) {
                val day = start.plusDays(i.toLong())
                val (pillar, principle) = GentlemanGuide.principleOfDay(status, day)
                assertTrue(pillar.fits(status))
                seen += principle.title
                val (cPillar, _) = GentlemanGuide.challengeOfDay(status, day)
                assertTrue(cPillar.fits(status))
                assertNotEquals(pillar.id, cPillar.id)
            }
            assertTrue(seen.size >= 25)
            assertNotEquals(
                GentlemanGuide.principleOfDay(status, start).first.id,
                GentlemanGuide.principleOfDay(status, start.plusDays(1)).first.id
            )
        }
    }

    @Test
    fun everyPhaseHasRelationshipGuidance() {
        for (phase in PartnerDay.Phase.values()) {
            val g = GentlemanGuide.phaseGuide(phase)
            assertTrue(g.doThis.isNotEmpty() && g.avoid.isNotEmpty() && g.conflict.isNotBlank())
        }
    }
}
