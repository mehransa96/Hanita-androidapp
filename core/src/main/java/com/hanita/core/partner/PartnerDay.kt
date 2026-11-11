package com.hanita.core.partner

import com.example.domain.CyclePredictor
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * What the partner app shows for a day, projected from a [PartnerCard] with the same maths as
 * Hanita ([CyclePredictor]). It never says "late": once the expected day has passed without a
 * new card it only says that an update from the owner is awaited.
 */
object PartnerDay {

    enum class Phase {
        /** Bleeding days. */
        PERIOD,
        /** After the period, energy rises. */
        AFTER_PERIOD,
        /** Middle of the cycle (the fertile window when the owner shares it). */
        MID_CYCLE,
        /** Calm days after the middle of the cycle, before PMS. */
        LUTEAL,
        /** The last days before the expected period. */
        PMS,
        /** The period is expected today. */
        PERIOD_DUE,
        /** The expected day has passed and no new card came yet. */
        AWAITING_UPDATE,
        /** The card is too old to project anything useful. */
        STALE,
        /** The owner paused sharing. */
        PAUSED,
        /** The owner has not logged any period yet. */
        NO_DATA
    }

    /** After this many projected cycles without a new card, the data counts as too old. */
    const val MAX_PROJECTED_CYCLES = 3

    data class Today(
        val phase: Phase,
        /** 1-based day of the projected cycle; 0 when unknown. */
        val cycleDay: Int,
        /** Days until the next expected period; null when unknown. */
        val daysUntilPeriod: Int?,
        val nextPeriodStart: LocalDate?,
        /** True on fertile-window days, only when the owner shares them. */
        val fertile: Boolean
    )

    fun prediction(card: PartnerCard, today: LocalDate): CyclePredictor.Prediction? {
        val start = card.lastPeriodStart ?: return null
        val next = start.plusDays(card.cycleLength.toLong())
        val remaining = ChronoUnit.DAYS.between(today, next).toInt()
        return CyclePredictor.Prediction(
            hasEnoughData = true,
            averageCycleLength = card.cycleLength,
            averagePeriodLength = card.periodLength,
            lastPeriodStart = start,
            predictedNextStart = next,
            daysRemaining = remaining,
            cyclesUsed = 0,
            cycleVariationDays = 0,
            isLate = remaining < 0
        )
    }

    /** True when [today] is beyond [MAX_PROJECTED_CYCLES] cycles after the last known period. */
    fun isStale(card: PartnerCard, today: LocalDate): Boolean {
        val start = card.lastPeriodStart ?: return false
        return ChronoUnit.DAYS.between(start, today) >= card.cycleLength.toLong() * MAX_PROJECTED_CYCLES
    }

    fun today(card: PartnerCard?, today: LocalDate): Today {
        if (card == null || card.revoked) return Today(Phase.NO_DATA, 0, null, null, false)
        if (card.paused) return Today(Phase.PAUSED, 0, null, null, false)
        val start = card.lastPeriodStart ?: return Today(Phase.NO_DATA, 0, null, null, false)
        if (today.isBefore(start)) return Today(Phase.NO_DATA, 0, null, null, false)
        if (isStale(card, today)) return Today(Phase.STALE, 0, null, null, false)

        val expected = start.plusDays(card.cycleLength.toLong())
        val overdue = ChronoUnit.DAYS.between(expected, today)
        // The first missed cycle: wait for the owner instead of guessing.
        if (overdue == 0L) return Today(Phase.PERIOD_DUE, card.cycleLength + 1, 0, today, false)
        if (overdue in 1..AWAIT_DAYS) return Today(Phase.AWAITING_UPDATE, 0, null, null, false)

        val prediction = prediction(card, today)!!
        // Beyond the waiting window the plain projection is used (the owner may simply not have
        // sent an update), so the anchor is the projected cycle, not "late".
        val status = CyclePredictor.dayStatus(today, prediction, loggedBleedingToday = false, today = expected.minusDays(1))
        val phase = when {
            status.isPeriod -> Phase.PERIOD
            status.isPms -> Phase.PMS
            status.phase == CyclePredictor.Phase.OVULATION || status.isFertile -> Phase.MID_CYCLE
            status.phase == CyclePredictor.Phase.FOLLICULAR -> Phase.AFTER_PERIOD
            else -> Phase.LUTEAL
        }
        return Today(
            phase = phase,
            cycleDay = status.cycleDay,
            daysUntilPeriod = status.daysUntilNextPeriod,
            nextPeriodStart = status.predictedPeriodStart,
            fertile = card.shareFertile && status.isFertile
        )
    }

    /** Days after the expected start during which the app waits for a new card. */
    const val AWAIT_DAYS = 10L

    enum class Event(val code: String) {
        PERIOD_SOON("soon"),
        PERIOD_START("start"),
        PMS_START("pms"),
        FERTILE_START("fertile");

        companion object {
            fun fromCode(code: String): Event? = values().firstOrNull { it.code == code }
        }
    }

    data class Upcoming(val event: Event, val date: LocalDate, val daysFromToday: Int)

    /**
     * The next date of each enabled event, today included, soonest first. Fertile days are
     * included only when the owner shares them.
     */
    fun upcoming(
        card: PartnerCard?,
        today: LocalDate,
        enabled: Set<Event>,
        daysBeforePeriod: Int
    ): List<Upcoming> {
        if (card == null || !card.hasCycleData || isStale(card, today)) return emptyList()
        val start = card.lastPeriodStart!!
        val length = card.cycleLength
        // While an update is awaited the next dates are unknown: say nothing rather than guess.
        val overdue = ChronoUnit.DAYS.between(start.plusDays(length.toLong()), today)
        if (overdue in 1..AWAIT_DAYS) return emptyList()
        val yesterday = today.minusDays(1)
        fun next(cycleDay: Int): LocalDate =
            CyclePredictor.nextOccurrenceOfCycleDay(start, length, cycleDay, yesterday)

        val out = mutableListOf<Upcoming>()
        fun add(event: Event, date: LocalDate) {
            if (event in enabled) out += Upcoming(event, date, ChronoUnit.DAYS.between(today, date).toInt())
        }
        val before = daysBeforePeriod.coerceIn(1, 7)
        add(Event.PERIOD_START, next(1))
        add(Event.PERIOD_SOON, next(length - before + 1))
        add(Event.PMS_START, next(length - PMS_DAYS + 1))
        if (card.shareFertile) {
            add(Event.FERTILE_START, next(CyclePredictor.ovulationDay(length) - 5))
        }
        return out.sortedBy { it.date }
    }

    /** The events that fall exactly on [date]. */
    fun eventsOn(card: PartnerCard?, date: LocalDate, enabled: Set<Event>, daysBeforePeriod: Int): List<Event> =
        upcoming(card, date, enabled, daysBeforePeriod).filter { it.daysFromToday == 0 }.map { it.event }

    /** Same PMS window as Hanita: the last 5 days before the expected period. */
    const val PMS_DAYS = 5
}
