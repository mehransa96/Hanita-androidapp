package com.hanita.core

import com.hanita.core.gentleman.Advice
import com.hanita.core.gentleman.Assessments
import com.hanita.core.gentleman.Assessments.Kind
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.Habits
import com.hanita.core.gentleman.MensHealth
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Status
import com.hanita.core.gentleman.ThemePresets
import com.hanita.core.gentleman.Toolkit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** GentleMan 4.0: the new self-checks, advice, men's health tools and themes. */
class GentlemanFourTest {

    /** Index of the option whose value is [value] for question [q]. */
    private fun pick(kind: Kind, q: Int, value: Int): Int =
        Assessments.scale(kind).optionsFor(q).indexOfFirst { it.value == value }.also { assertTrue(it >= 0) }

    private fun allValue(kind: Kind, value: Int): List<Int> =
        Assessments.scale(kind).questions.indices.map { pick(kind, it, value) }

    @Test fun everyScaleIsWellFormedAndSourced() {
        assertEquals(Kind.values().size, Assessments.ALL_SCALES.size)
        Kind.values().forEach { kind ->
            val s = Assessments.scale(kind)
            assertEquals(kind, s.kind)
            assertNotNull("source for ${kind.code}", Sources.get(kind.sourceId))
            assertTrue(s.questions.isNotEmpty())
            assertTrue("answers fit in one digit", s.questions.indices.all { s.optionsFor(it).size in 2..10 })
            if (!s.isProfile) {
                assertTrue(s.maxScore > s.minScore)
                assertTrue("bands for ${kind.code}", s.bands.isNotEmpty())
                // The lowest band starts at or below the minimum, so every score has a meaning.
                assertTrue(s.bands.minOf { it.min } <= s.minScore)
                for (score in s.minScore..s.maxScore) Assessments.interpret(kind, score)
            }
        }
        assertEquals(Kind.values().size, Kind.values().map { it.code }.toSet().size)
    }

    @Test fun publishedRangesAreReproduced() {
        val expected = mapOf(
            Kind.WELL_BEING to (0 to 100), Kind.DEPRESSION to (0 to 27), Kind.ANXIETY to (0 to 21),
            Kind.STRESS to (0 to 40), Kind.SELF_ESTEEM to (0 to 30), Kind.RESILIENCE to (6 to 30),
            Kind.SELF_EFFICACY to (10 to 40), Kind.LIFE_SATISFACTION to (5 to 35),
            Kind.LONELINESS to (3 to 9), Kind.RELATIONSHIP to (0 to 21)
        )
        expected.forEach { (kind, range) ->
            val s = Assessments.scale(kind)
            assertEquals(kind.code, range.first, s.minScore)
            assertEquals(kind.code, range.second, s.maxScore)
        }
    }

    @Test fun phq9CutOffsAndItemNine() {
        val k = Kind.DEPRESSION
        assertEquals(0, Assessments.score(k, allValue(k, 0)))
        assertEquals(27, Assessments.score(k, allValue(k, 3)))
        assertEquals(Assessments.Level.HIGH, Assessments.interpret(k, 4).level)
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(k, 5).level)
        assertFalse(Assessments.interpret(k, 9).seekHelp)
        assertTrue(Assessments.interpret(k, 10).seekHelp)
        assertEquals(Assessments.Level.VERY_LOW, Assessments.interpret(k, 20).level)
        val calm = allValue(k, 0)
        assertFalse(Assessments.needsUrgentHelp(k, calm))
        val itemNine = calm.toMutableList().also { it[8] = pick(k, 8, 1) }
        assertEquals(1, Assessments.score(k, itemNine))
        assertTrue("any thought of self-harm needs help whatever the total", Assessments.needsUrgentHelp(k, itemNine))
        assertFalse(Assessments.needsUrgentHelp(Kind.ANXIETY, itemNine))
    }

    @Test fun gad7AndPssReverseItems() {
        assertFalse(Assessments.interpret(Kind.ANXIETY, 9).seekHelp)
        assertTrue(Assessments.interpret(Kind.ANXIETY, 10).seekHelp)
        assertEquals(21, Assessments.score(Kind.ANXIETY, allValue(Kind.ANXIETY, 3)))
        val k = Kind.STRESS
        // Answering "never" everywhere: the six negative items give 0, the four reversed give 4 each.
        assertEquals(16, Assessments.score(k, allValue(k, 0)))
        assertEquals(24, Assessments.score(k, allValue(k, 4)))
        assertEquals(Assessments.Level.LOW, Assessments.interpret(k, 27).level)
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(k, 14).level)
    }

    @Test fun who5StillScoresAsBefore() {
        val k = Kind.WELL_BEING
        assertEquals(100, Assessments.score(k, List(5) { 0 }))
        assertEquals(52, Assessments.score(k, listOf(2, 2, 3, 3, 2)))
    }

    @Test fun personalityProfile() {
        val k = Kind.PERSONALITY
        assertEquals(50, Assessments.PERSONALITY.questions.size)
        Assessments.Trait.values().forEach { t -> assertEquals(10, Assessments.PERSONALITY.questions.count { it.trait == t }) }
        // "Completely true" everywhere, with the IPIP-50 keying: E 5+/5-, A 6+/4-, C 6+/4-,
        // emotional stability 2+/8-, intellect 7+/3-.
        val profile = Assessments.profile(allValue(k, 5))!!
        assertEquals(5, profile.size)
        val expected = mapOf(
            Assessments.Trait.EXTRAVERSION to 30, Assessments.Trait.AGREEABLENESS to 34,
            Assessments.Trait.CONSCIENTIOUSNESS to 34, Assessments.Trait.STABILITY to 18, Assessments.Trait.OPENNESS to 38
        )
        profile.forEach { assertEquals(it.trait.name, expected.getValue(it.trait), it.score); assertEquals(10, it.min); assertEquals(50, it.max) }
        val ideal = Assessments.PERSONALITY.questions.indices.map { i ->
            pick(k, i, if (Assessments.PERSONALITY.questions[i].reversed) 1 else 5)
        }
        Assessments.profile(ideal)!!.forEach { assertEquals(50, it.score); assertEquals(100, it.percent); assertEquals("خیلی بالا", it.levelTitle) }
        assertNull(Assessments.profile(listOf(0, 1)))
        assertEquals(0, Assessments.score(k, ideal))
        // Results saved from the 20-item Mini-IPIP still show a profile (each factor 4..20).
        val legacy = Assessments.profile(List(20) { 0 })!!
        legacy.filter { it.trait != Assessments.Trait.OPENNESS }.forEach { assertEquals(12, it.score); assertEquals(50, it.percent) }
        assertEquals(8, legacy.first { it.trait == Assessments.Trait.OPENNESS }.score)
    }

    @Test fun resilienceEfficacySatisfactionLonelinessCouples() {
        assertEquals(18, Assessments.score(Kind.RESILIENCE, allValue(Kind.RESILIENCE, 3)))
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(Kind.RESILIENCE, 18).level)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(Kind.RESILIENCE, 17).level)
        assertEquals(40, Assessments.score(Kind.SELF_EFFICACY, allValue(Kind.SELF_EFFICACY, 4)))
        assertEquals(35, Assessments.score(Kind.LIFE_SATISFACTION, allValue(Kind.LIFE_SATISFACTION, 7)))
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(Kind.LIFE_SATISFACTION, 20).level)
        assertEquals(Assessments.Level.VERY_LOW, Assessments.interpret(Kind.LIFE_SATISFACTION, 9).level)
        assertEquals(Assessments.Level.HIGH, Assessments.interpret(Kind.LONELINESS, 5).level)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(Kind.LONELINESS, 6).level)
        // CSI-4: item 1 runs 0..6, items 2-4 run 0..5.
        val csi = Kind.RELATIONSHIP
        assertEquals(7, Assessments.scale(csi).optionsFor(0).size)
        assertEquals(6, Assessments.scale(csi).optionsFor(1).size)
        assertEquals(Assessments.Level.LOW, Assessments.interpret(csi, 13).level)
        assertTrue(Assessments.interpret(csi, 13).seekHelp)
        assertEquals(Assessments.Level.MIDDLE, Assessments.interpret(csi, 14).level)
        assertFalse(csi.fits(Status.SINGLE))
        assertTrue(Kind.forStatus(Status.MARRIED).contains(csi))
        assertNull(Assessments.score(csi, listOf(6, 6, 0, 0)))
    }

    @Test fun everyPillarHasAdviceSourcesAndIcon() {
        val ids = GentlemanGuide.PILLARS.map { it.id }.toSet()
        assertEquals(23, ids.size)
        assertEquals(ids, Advice.pillarIds)
        GentlemanGuide.PILLARS.forEach { p ->
            val a = Advice.forPillar(p.id)!!
            assertTrue(p.id, a.mistakes.size >= 3 && a.firstStep.isNotBlank())
            assertTrue(p.id, Sources.forPillar(p.id).isNotEmpty())
        }
        listOf("career", "leader", "menhealth", "fitness", "digital").forEach {
            assertTrue(GentlemanGuide.pillar(it)!!.fits(Status.SINGLE))
        }
        Habits.PRESETS.forEach { assertNotNull(it.id, GentlemanGuide.pillar(it.pillarId)) }
        assertEquals(Toolkit.CHECKLISTS.size, Toolkit.CHECKLISTS.map { it.id }.toSet().size)
        assertTrue(Toolkit.CHECKLISTS.any { it.id == "workout" } && Toolkit.CHECKLISTS.any { it.id == "negotiate" })
    }

    @Test fun screeningsFollowAgeAndRisk() {
        fun ids(age: Int, smoker: Boolean = false, overweight: Boolean = false) =
            MensHealth.screeningsFor(age, smoker, overweight).map { it.id }.toSet()
        assertTrue(ids(17).isEmpty())
        assertEquals(setOf("bp", "weight", "mood", "alcohol", "hcv", "hiv"), ids(25))
        assertTrue("sugar" in ids(40, overweight = true) && "sugar" !in ids(40))
        assertTrue("lipids" in ids(40) && "lipids" !in ids(39))
        assertTrue("colon" in ids(45) && "colon" !in ids(44) && "colon" !in ids(76))
        assertTrue("psa" in ids(60) && "psa" !in ids(70))
        assertTrue(MensHealth.screeningsFor(60, false, false).first { it.id == "psa" }.discuss)
        assertTrue("lung" in ids(55, smoker = true) && "lung" !in ids(55))
        assertTrue("aaa" in ids(70, smoker = true) && "aaa" !in ids(70))
        assertTrue("hiv" !in ids(66) && "hcv" !in ids(80))
    }

    @Test fun bodyMeasures() {
        val r = MensHealth.body(80.0, 180.0, 85.0)!!
        assertEquals(24.7, r.bmi, 0.001)
        assertEquals(MensHealth.BmiClass.NORMAL, r.bmiClass)
        assertEquals(0.47, r.waistRatio!!, 0.001)
        assertEquals(MensHealth.WaistRisk.HEALTHY, r.waistRisk)
        assertEquals(MensHealth.BmiClass.OVER, MensHealth.body(90.0, 180.0)!!.bmiClass)
        assertEquals(MensHealth.BmiClass.OBESE, MensHealth.body(100.0, 180.0, 110.0)!!.bmiClass)
        assertEquals(MensHealth.WaistRisk.HIGH, MensHealth.body(100.0, 180.0, 110.0)!!.waistRisk)
        assertEquals(MensHealth.WaistRisk.INCREASED, MensHealth.body(80.0, 180.0, 95.0)!!.waistRisk)
        assertNull(MensHealth.body(80.0, 18.0))
        assertNull(MensHealth.body(5.0, 180.0))
        assertEquals(60 to 81, MensHealth.healthyWeight(180.0))
    }

    @Test fun themesAreDistinctAndReadable() {
        assertTrue(ThemePresets.ALL.size >= 6)
        assertEquals(ThemePresets.ALL.size, ThemePresets.ALL.map { it.id }.toSet().size)
        assertEquals(ThemePresets.DEFAULT_ID, ThemePresets.get("missing").id)
        assertEquals(ThemePresets.Mode.SYSTEM, ThemePresets.Mode.fromCode(null))
        assertEquals(ThemePresets.Mode.DARK, ThemePresets.Mode.fromCode("dark"))
        ThemePresets.ALL.forEach { preset ->
            listOf(preset.dark, preset.light).forEach { p ->
                val name = preset.id
                fun aa(fg: Long, bg: Long, min: Double = 4.5) =
                    assertTrue("$name ${ThemePresets.contrast(fg, bg)}", ThemePresets.contrast(fg, bg) >= min)
                aa(p.onPrimary, p.primary)
                aa(p.onPrimaryContainer, p.primaryContainer)
                aa(p.onSecondaryContainer, p.secondaryContainer)
                aa(p.onSurface, p.surface, 7.0)
                aa(p.onSurface, p.background, 7.0)
                aa(p.onSurfaceVariant, p.surface)
                aa(p.onSurfaceVariant, p.surfaceVariant)
                aa(p.primary, p.surface, 3.0)
                aa(p.secondary, p.surface, 3.0)
            }
            assertTrue(ThemePresets.contrast(preset.dark.background, preset.light.background) > 10)
        }
        assertEquals(21.0, ThemePresets.contrast(0xFF000000, 0xFFFFFFFF), 0.01)
    }
}
