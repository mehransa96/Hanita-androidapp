package com.hanita.core

import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Assessments.Kind
import com.hanita.core.gentleman.Advice
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.MensHealth
import com.hanita.core.gentleman.Toolkit
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** GentleMan 4.1: deeper results for the existing self-checks. */
class GentlemanDepthTest {

    private fun pick(kind: Kind, q: Int, value: Int): Int =
        Assessments.scale(kind).optionsFor(q).indexOfFirst { it.value == value }.also { assertTrue(it >= 0) }

    private fun allValue(kind: Kind, value: Int): List<Int> =
        Assessments.scale(kind).questions.indices.map { pick(kind, it, value) }

    @Test fun everyBandHasStepsAndEveryScaleExplainsItself() {
        Kind.values().forEach { kind ->
            val s = Assessments.scale(kind)
            assertTrue("about for ${kind.code}", kind.about.isNotBlank())
            if (!s.isProfile) {
                s.bands.forEach { assertTrue("steps for ${kind.code} ${it.title}", it.steps.isNotEmpty()) }
                assertTrue(s.meaningfulChange >= 1)
                assertTrue("norm for ${kind.code}", !s.norm.isNullOrBlank())
            }
            s.subscales.forEach { sub ->
                assertTrue(sub.items.isNotEmpty())
                assertTrue(sub.items.all { it in s.questions.indices && s.questions[it].scored })
            }
        }
    }

    @Test fun phq9ClosingQuestionIsAskedButNotScored() {
        val k = Kind.DEPRESSION
        val s = Assessments.scale(k)
        assertEquals(10, s.questions.size)
        assertFalse(s.questions.last().scored)
        assertEquals(27, s.maxScore)
        val calm = allValue(k, 0).toMutableList().also { it[9] = pick(k, 9, 3) }
        assertEquals(0, Assessments.score(k, calm))
        val f = Assessments.flags(k, calm)
        assertTrue(f.any { it.seekHelp && it.title.contains("زندگی روزمره") })
        assertEquals(8, Assessments.scale(Kind.ANXIETY).questions.size)
        assertEquals(21, Assessments.scale(Kind.ANXIETY).maxScore)
    }

    @Test fun phqAlgorithmAndCoreScreens() {
        val k = Kind.DEPRESSION
        // Five symptoms on more than half the days, including low interest.
        val major = allValue(k, 0).toMutableList().also { a -> listOf(0, 2, 3, 4, 6).forEach { a[it] = pick(k, it, 2) } }
        assertTrue(Assessments.flags(k, major).any { it.title.contains("افسردگی اساسی") })
        // Only the two core items, "several days" and "more than half": PHQ-2 = 3.
        val core = allValue(k, 0).toMutableList().also { it[0] = pick(k, 0, 1); it[1] = pick(k, 1, 2) }
        val coreFlags = Assessments.flags(k, core)
        assertTrue(coreFlags.any { it.title.contains("چند نشانه") || it.title.contains("PHQ-2") })
        assertTrue(Assessments.flags(k, allValue(k, 0)).isEmpty())
        val gad = Kind.ANXIETY
        val gadCore = allValue(gad, 0).toMutableList().also { it[0] = pick(gad, 0, 2); it[1] = pick(gad, 1, 1) }
        assertTrue(Assessments.flags(gad, gadCore).any { it.title.contains("GAD-2") })
    }

    @Test fun who5FlagsAnyVeryLowItem() {
        val k = Kind.WELL_BEING
        val good = List(5) { 1 } // "most of the time" everywhere = 80
        assertEquals(80, Assessments.score(k, good))
        assertTrue(Assessments.flags(k, good).isEmpty())
        val oneLow = good.toMutableList().also { it[3] = 4 } // "sometimes" on sleep
        assertTrue(Assessments.flags(k, oneLow).single().text.contains("خواب"))
    }

    @Test fun subscoresFollowTheItems() {
        val k = Kind.STRESS
        // "Never" everywhere: helplessness 0 of 24, low efficacy 16 of 16 (all four reversed).
        val subs = Assessments.subscores(k, allValue(k, 0))
        assertEquals(2, subs.size)
        assertEquals(0, subs[0].score); assertEquals(24, subs[0].max); assertEquals(0, subs[0].percent)
        assertEquals(16, subs[1].score); assertEquals(100, subs[1].percent)
        val rses = Assessments.subscores(Kind.SELF_ESTEEM, List(10) { 0 }) // strongly agree everywhere
        assertEquals(15, rses[0].score); assertEquals(0, rses[1].score)
        assertTrue(Assessments.subscores(Kind.PERSONALITY, List(50) { 0 }).isEmpty())
        assertEquals(3, Assessments.subscores(Kind.DEPRESSION, allValue(Kind.DEPRESSION, 0)).size)
    }

    @Test fun insightsPickTheWeightiestAnswers() {
        val k = Kind.SELF_ESTEEM
        val s = Assessments.scale(k)
        val worst = s.questions.map { if (it.reversed) 0 else 3 }
        val ins = Assessments.insights(k, worst)
        assertEquals(3, ins.concerns.size)
        assertTrue(ins.strengths.isEmpty())
        val best = s.questions.map { if (it.reversed) 3 else 0 }
        assertEquals(3, Assessments.insights(k, best).strengths.size)
        assertTrue(Assessments.insights(k, best).concerns.isEmpty())
        // Symptom scales: a high answer is a concern.
        val d = Kind.DEPRESSION
        val one = allValue(d, 0).toMutableList().also { it[3] = pick(d, 3, 3) }
        assertEquals(Assessments.scale(d).questions[3].text, Assessments.insights(d, one).concerns.single().question)
    }

    @Test fun changeUsesTheMeaningfulDifference() {
        val up = Assessments.change(Kind.DEPRESSION, 14, 8)
        assertEquals(Assessments.Direction.BETTER, up.direction); assertTrue(up.meaningful)
        val small = Assessments.change(Kind.DEPRESSION, 10, 12)
        assertEquals(Assessments.Direction.WORSE, small.direction); assertFalse(small.meaningful)
        val who = Assessments.change(Kind.WELL_BEING, 40, 52)
        assertEquals(Assessments.Direction.BETTER, who.direction); assertTrue(who.meaningful)
        assertEquals(Assessments.Direction.SAME, Assessments.change(Kind.STRESS, 20, 20).direction)
        assertTrue(up.text.contains("۶"))
    }

    @Test fun diener7RangesAndBrsMean() {
        val k = Kind.LIFE_SATISFACTION
        val titles = listOf(35, 30, 25, 20, 19, 14, 9).map { Assessments.interpret(k, it).title }
        assertEquals(7, titles.toSet().size)
        assertEquals(Assessments.Level.HIGH, Assessments.interpret(k, 26).level)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(k, 15).level)
        assertEquals(3.0, Assessments.resilienceMean(18), 0.001)
        assertEquals(Assessments.Level.VERY_LOW, Assessments.interpret(Kind.DEPRESSION, 20).level)
        assertTrue(Assessments.interpret(Kind.DEPRESSION, 22).title.contains("شدید"))
    }

    @Test fun screeningsExplainHowAndCoverRiskAndAge() {
        for (age in MensHealth.MIN_AGE..MensHealth.MAX_AGE) {
            MensHealth.screeningsFor(age, smoker = true, overweight = true).forEach { assertTrue("how for ${it.id}", it.how.isNotBlank()) }
        }
        fun ids(age: Int, smoker: Boolean = false) = MensHealth.screeningsFor(age, smoker, false).map { it.id }.toSet()
        assertTrue("tobacco" in ids(30, smoker = true) && "tobacco" !in ids(30))
        assertTrue("falls" in ids(65) && "falls" !in ids(64))
        assertTrue("colon76" in ids(80) && "colon76" !in ids(75))
        assertTrue(MensHealth.screeningsFor(80, false, false).first { it.id == "colon76" }.discuss)
    }

    @Test fun bodyAddsObesityGradesWaistSizeAndTarget() {
        assertEquals(MensHealth.BmiClass.OBESE_2, MensHealth.body(120.0, 180.0)!!.bmiClass)
        assertEquals(MensHealth.BmiClass.OBESE_3, MensHealth.body(135.0, 180.0)!!.bmiClass)
        assertEquals(MensHealth.WaistSize.INCREASED, MensHealth.body(80.0, 180.0, 95.0)!!.waistSize)
        assertEquals(MensHealth.WaistSize.HIGH, MensHealth.body(80.0, 180.0, 102.0)!!.waistSize)
        assertEquals(MensHealth.WaistSize.NORMAL, MensHealth.body(80.0, 180.0, 93.9)!!.waistSize)
        // 180 cm: normal range 60..81 kg.
        assertEquals(19, MensHealth.body(100.0, 180.0)!!.kgToNormal)
        assertEquals(-5, MensHealth.body(55.0, 180.0)!!.kgToNormal)
        assertEquals(0, MensHealth.body(75.0, 180.0)!!.kgToNormal)
    }

    @Test fun everyPillarHasMoreChallengesAndMistakes() {
        GentlemanGuide.PILLARS.forEach { p ->
            assertTrue(p.id, p.challenges.size >= 6)
            assertEquals(p.id, p.challenges.size, p.challenges.toSet().size)
            assertTrue(p.id, Advice.forPillar(p.id)!!.mistakes.size >= 5)
        }
        assertEquals(Habits.PRESETS.size, Habits.PRESETS.map { it.id }.toSet().size)
        assertTrue(Habits.PRESETS.size >= 28)
        Habits.PRESETS.forEach { h ->
            val pillar = GentlemanGuide.pillar(h.pillarId)!!
            // A habit is never shown to someone its pillar is not for.
            if (pillar.audience != GentlemanGuide.Audience.ALL) assertEquals(h.id, pillar.audience, h.audience)
        }
        Toolkit.QUESTIONS.forEach { assertTrue(it.id, it.questions.size >= 14) }
        assertTrue(Toolkit.BREATHING.size >= 4)
    }

    @Test fun everyPartnerPhaseOffersMoreChoice() {
        PartnerDay.Phase.values().forEach { phase ->
            val info = PartnerContent.phaseInfo(phase, fertileShared = true)
            assertTrue(phase.name, info.tips.size >= 5)
            assertTrue(phase.name, info.messages.size >= 5)
            assertEquals(phase.name, info.messages.size, info.messages.toSet().size)
        }
    }
}
