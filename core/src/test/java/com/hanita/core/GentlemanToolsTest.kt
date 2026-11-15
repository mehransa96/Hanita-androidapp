package com.hanita.core

import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Status
import com.hanita.core.gentleman.Toolkit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GentlemanToolsTest {

    @Test
    fun everyPillarHasACategoryAndSources() {
        val ids = GentlemanGuide.PILLARS.map { it.id }
        assertEquals("pillar ids are unique", ids.size, ids.toSet().size)
        for (p in GentlemanGuide.PILLARS) {
            assertTrue("${p.id} needs sources", Sources.forPillar(p.id).isNotEmpty())
            assertTrue(p.principles.size >= 4 && p.challenges.size >= 3)
        }
        // Every category has content for both single and married users.
        for (status in Status.values()) {
            val cats = GentlemanGuide.pillarsFor(status).map { it.category }.toSet()
            assertEquals(GentlemanGuide.Category.values().toSet(), cats)
        }
        assertTrue(Sources.pillarIds.all { GentlemanGuide.pillar(it) != null })
        val sourceIds = Sources.ALL.map { it.id }
        assertEquals(sourceIds.size, sourceIds.toSet().size)
        assertTrue(Sources.ALL.all { it.url == null || it.url!!.startsWith("https://") })
    }

    @Test
    fun rosenbergScoringReversesItems() {
        val s = Assessments.SELF_ESTEEM
        // "Strongly agree" (index 0) to every item: positives give 3, reversed give 0.
        assertEquals(15, Assessments.score(s.kind, List(10) { 0 }))
        // The most positive answer to every item.
        val best = s.questions.map { if (it.reversed) 3 else 0 }
        assertEquals(30, Assessments.score(s.kind, best))
        val worst = s.questions.map { if (it.reversed) 0 else 3 }
        assertEquals(0, Assessments.score(s.kind, worst))
        assertNull(Assessments.score(s.kind, List(9) { 0 }))
        assertNull(Assessments.score(s.kind, List(10) { 4 }))
        assertEquals(Assessments.Level.HIGH, Assessments.interpret(s.kind, 26).level)
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(s.kind, 15).level)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(s.kind, 14).level)
    }

    @Test
    fun who5ScoreIsAPercentage() {
        val k = Assessments.Kind.WELL_BEING
        assertEquals(100, Assessments.score(k, List(5) { 0 }))
        assertEquals(0, Assessments.score(k, List(5) { 5 }))
        assertEquals(52, Assessments.score(k, listOf(2, 2, 3, 3, 2)))
        assertFalse(Assessments.interpret(k, 52).seekHelp)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(k, 50).level)
        assertEquals(Assessments.Level.VERY_LOW, Assessments.interpret(k, 28).level)
        assertEquals(listOf(1, 2, 3), Assessments.decodeAnswers(Assessments.encodeAnswers(listOf(1, 2, 3))))
    }

    @Test
    fun habitMaths() {
        val today = LocalDate.of(2026, 11, 15)
        val d = today.toEpochDay()
        val done = setOf(d, d - 1, d - 3, d - 10, d - 11, d - 12, d - 13)
        assertEquals(42, Habits.completionPercent(done, today, 7))
        assertEquals(4, Habits.bestStreak(done))
        assertEquals(0, Habits.bestStreak(emptySet()))
        assertEquals(listOf(false, false, false, true, false, true, true), Habits.lastDays(done, today, 7))
        for (status in Status.values()) {
            val presets = Habits.presetsFor(status).map { it.id }
            assertTrue(Habits.starterIds(status).all { it in presets })
        }
        assertTrue(Habits.PRESETS.all { GentlemanGuide.pillar(it.pillarId) != null })
    }

    @Test
    fun budgetAddsUp() {
        val b = Toolkit.budget(30_000_001)
        assertEquals(30_000_001, b.needs + b.wants + b.savings)
        assertEquals(15_000_000, b.needs)
        assertEquals(9_000_000, b.wants)
        assertEquals(Toolkit.BudgetSplit(0, 0, 0), Toolkit.budget(-5))
        assertEquals(30L to 60L, Toolkit.emergencyFund(10))
        assertEquals(4, Toolkit.monthsToGoal(100, 10, 25))
        assertEquals(0, Toolkit.monthsToGoal(100, 100, 0))
        assertNull(Toolkit.monthsToGoal(100, 0, 0))
        assertEquals("۲۵٬۰۰۰٬۰۰۰", Toolkit.formatAmount(25_000_000))
        assertEquals("۹۹۹", Toolkit.formatAmount(999))
    }

    @Test
    fun breathingPhasesCycle() {
        val box = Toolkit.BREATHING.first { it.id == "box" }
        assertEquals(16, box.cycleSeconds)
        assertEquals("دم" to 4, box.phaseAt(0).let { it.first.label to it.second })
        assertEquals(Toolkit.Phase.Kind.HOLD, box.phaseAt(5).first.kind)
        assertEquals(Toolkit.Phase.Kind.INHALE, box.phaseAt(16).first.kind)
        assertTrue(Toolkit.BREATHING.all { b -> Sources.ALL.any { it.id == b.sourceId } })
    }

    @Test
    fun wordsForHardMoments() {
        val a = Toolkit.apology("دیر رسیدم", "منتظر بمانی", "شام امشب را من درست کنم", "زودتر راه می‌افتم")
        assertTrue(a.startsWith("متأسفم که دیر رسیدم."))
        assertTrue("responsibility is always named", a.contains("اشتباه از من بود"))
        assertTrue(a.contains("برای جبرانش"))
        val s = Toolkit.softStartup("نگران", "دیر خبر می‌دهی", "زودتر پیام بدهی")
        assertEquals("وقتی دیر خبر می‌دهی، من نگران می‌شوم. خیلی ممنون می‌شوم اگر زودتر پیام بدهی.", s)
        assertEquals(listOf("همیشه"), Toolkit.harshWords("تو همیشه دیر می‌آیی"))
        assertTrue(Toolkit.questionsFor(Status.MARRIED).any { it.id == "couple" })
        assertFalse(Toolkit.questionsFor(Status.SINGLE).any { it.id == "couple" })
        assertEquals("معمولی", Toolkit.moodLabel(3))
        assertEquals("عالی", Toolkit.moodLabel(9))
    }
}
