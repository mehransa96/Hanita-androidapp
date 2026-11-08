package com.example.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Decides WHEN every cycle reminder fires. Pure and unit-tested, and built on the same
 * [CyclePredictor.Prediction] the dashboard and calendar show, so a notification can never
 * announce a different date than the app.
 *
 * Rules:
 *  - Upcoming cycles start at lastPeriodStart + k * averageCycleLength.
 *  - While the period is late (1..[CyclePredictor.MAX_LATE_DAYS] days) the next cycle start is
 *    unknown: only the late check-in is planned, everything else resumes once the period is logged.
 *  - Each reminder is the first occurrence whose fire time is still in the future.
 */
object ReminderPlanner {

    const val LATE_CHECK_DAYS = 3

    data class Settings(
        val periodEnabled: Boolean,
        val ovulationEnabled: Boolean,
        val daysBeforePeriod: Int,
        val daysBeforeOvulation: Int,
        val time: LocalTime
    )

    data class Reminder(val fireAt: LocalDateTime, val eventDate: LocalDate)

    enum class PhaseKind { MENSTRUAL, FOLLICULAR, OVULATION, LUTEAL }

    data class PhaseReminder(val kind: PhaseKind, val fireAt: LocalDateTime)

    data class Plan(
        val period: Reminder?,
        val ovulation: Reminder?,
        val lateCheck: Reminder?,
        val phases: List<PhaseReminder>
    ) {
        companion object {
            val EMPTY = Plan(null, null, null, emptyList())
        }
    }

    fun plan(prediction: CyclePredictor.Prediction, settings: Settings, now: LocalDateTime): Plan {
        val lastStart = prediction.lastPeriodStart ?: return Plan.EMPTY
        val length = prediction.averageCycleLength.toLong()
        val today = now.toLocalDate()
        val expected = lastStart.plusDays(length)
        val overdue = ChronoUnit.DAYS.between(expected, today)
        val isLate = overdue in 1..CyclePredictor.MAX_LATE_DAYS.toLong()

        val lateCheck = if (settings.periodEnabled) {
            val date = expected.plusDays(LATE_CHECK_DAYS.toLong())
            Reminder(date.atTime(settings.time), expected).takeIf { it.fireAt.isAfter(now) }
        } else null

        if (isLate) return Plan(period = null, ovulation = null, lateCheck = lateCheck, phases = emptyList())

        // Cycle starts from the last real period onwards; bounded so the search always ends.
        val cycleStarts = generateSequence(lastStart) { it.plusDays(length) }.take(MAX_CYCLES_AHEAD)

        fun firstFuture(offsetDays: Long, daysBefore: Int): Reminder? = cycleStarts
            .map { start ->
                val event = start.plusDays(offsetDays)
                Reminder(event.minusDays(daysBefore.toLong()).atTime(settings.time), event)
            }
            .firstOrNull { it.fireAt.isAfter(now) }

        val ovulationDay = CyclePredictor.ovulationDay(prediction.averageCycleLength).toLong()

        val period = if (settings.periodEnabled) {
            // The next period of cycle k is the start of cycle k + 1.
            firstFuture(length, settings.daysBeforePeriod.coerceIn(0, 7))
        } else null

        val ovulation = if (settings.ovulationEnabled) {
            firstFuture(ovulationDay - 1, settings.daysBeforeOvulation.coerceIn(0, 7))
        } else null

        val phases = if (settings.periodEnabled) {
            listOf(
                PhaseKind.MENSTRUAL to 1L,
                PhaseKind.FOLLICULAR to (prediction.averagePeriodLength + 1).toLong(),
                PhaseKind.OVULATION to ovulationDay - 1,
                PhaseKind.LUTEAL to ovulationDay + 2
            ).mapNotNull { (kind, cycleDay) ->
                firstFuture(cycleDay - 1, 0)?.let { PhaseReminder(kind, it.fireAt) }
            }
        } else emptyList()

        return Plan(period, ovulation, lateCheck, phases)
    }

    private const val MAX_CYCLES_AHEAD = 400
}
