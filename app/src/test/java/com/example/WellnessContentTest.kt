package com.example

import com.example.domain.CyclePredictor
import com.example.domain.DailyInsights
import com.example.domain.SymptomGuide
import com.example.ui.screens.PHASE_GROUP
import com.example.ui.screens.getEducationalArticles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WellnessContentTest {

    @Test
    fun `daily insight picks the right state`() {
        fun state(isLate: Boolean = false, isPeriod: Boolean = false, isFertile: Boolean = false, isPms: Boolean = false,
                  phase: CyclePredictor.Phase = CyclePredictor.Phase.FOLLICULAR, day: Int = 8, hasData: Boolean = true) =
            DailyInsights.stateOf(hasData, isLate, isPeriod, isFertile, isPms, phase, day)

        assertEquals(DailyInsights.State.NO_DATA, state(hasData = false, isPeriod = true))
        assertEquals(DailyInsights.State.LATE, state(isLate = true, isPms = true))
        assertEquals(DailyInsights.State.PERIOD_EARLY, state(isPeriod = true, day = 2))
        assertEquals(DailyInsights.State.PERIOD_LATE, state(isPeriod = true, day = 4))
        assertEquals(DailyInsights.State.FERTILE, state(isFertile = true))
        assertEquals(DailyInsights.State.PMS, state(isPms = true, phase = CyclePredictor.Phase.LUTEAL, day = 25))
        assertEquals(DailyInsights.State.LUTEAL, state(phase = CyclePredictor.Phase.LUTEAL, day = 18))
        assertEquals(DailyInsights.State.FOLLICULAR, state())
    }

    @Test
    fun `every state gives three tips that change from day to day`() {
        DailyInsights.State.values().forEach { st ->
            val today = DailyInsights.build(st, 3, daysLate = 4)
            assertEquals(3, today.tips.size)
            assertTrue(today.headline.isNotBlank())
            today.tips.forEach { assertTrue(it.text.isNotBlank()) }
            val tomorrow = DailyInsights.build(st, 4, daysLate = 4)
            assertNotEquals("tips for $st should rotate", today.tips, tomorrow.tips)
        }
        assertTrue(DailyInsights.build(DailyInsights.State.LATE, 33, daysLate = 5).headline.contains("۵"))
        assertNotNull(DailyInsights.build(DailyInsights.State.PERIOD_EARLY, 1).caution)
    }

    @Test
    fun `every logger symptom has self care and a doctor note`() {
        val loggerSymptoms = listOf(
            "درد شکم", "کمردرد", "نفخ", "درد لگن", "گرفتگی عضلات", "سردرد", "خستگی", "سرگیجه", "بی‌خوابی",
            "افت تمرکز", "حساسیت سینه", "جوش صورت", "گرگرفتگی", "ورم و احتباس آب", "حالت تهوع", "هوس غذایی",
            "پرخوری", "بی‌اشتهایی", "یبوست"
        )
        loggerSymptoms.forEach { name ->
            val guide = SymptomGuide.forSymptom(name)
            assertNotNull("missing guide for $name", guide)
            assertTrue(guide!!.selfCare.isNotBlank() && guide.seeDoctor.isNotBlank())
        }
        // older stored name from the first app versions still resolves
        assertEquals(SymptomGuide.forSymptom("درد شکم"), SymptomGuide.forSymptom("دل‌پیچه و درد شکم"))
    }

    @Test
    fun `phase articles are complete and sourced`() {
        val articles = getEducationalArticles()
        val phases = articles.filter { it.group == PHASE_GROUP }
        assertEquals(4, phases.size)
        phases.forEach {
            assertTrue(it.title, it.hormones.isNotBlank() && it.bodyAndMood.isNotBlank())
            assertTrue(it.title, it.selfCare.size >= 3)
            assertTrue(it.title, it.seeDoctor.isNotBlank() && it.sources.isNotBlank())
        }
        assertTrue("topic articles expected", articles.size >= 9)
        assertTrue(articles.all { it.sources.isNotBlank() })
    }
}
