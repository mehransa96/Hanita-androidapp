package com.hanita.core.gentleman

import com.example.utils.JalaliCalendar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Whether the user is single or married; it decides which guidance and features are shown. */
enum class Status(val code: String) {
    SINGLE("single"),
    MARRIED("married");

    companion object {
        fun fromCode(code: String?): Status? = values().firstOrNull { it.code == code }
    }
}

/**
 * A yearly date the user wants to remember (a birthday, an anniversary, an occasion), kept in
 * the Jalali calendar so it repeats on the same Persian date every year.
 */
data class ImportantDate(
    val id: String,
    val title: String,
    val kind: Kind,
    /** Jalali month 1..12. */
    val month: Int,
    /** Jalali day 1..31. */
    val day: Int,
    /** The Jalali year it first happened (for "the 5th anniversary"); null when unknown. */
    val year: Int? = null,
    /** Remind this many days before as well as on the day itself (0 = only on the day). */
    val remindDaysBefore: Int = 1
) {
    enum class Kind(val code: String, val label: String) {
        BIRTHDAY("birthday", "تولد"),
        ANNIVERSARY("anniversary", "سالگرد"),
        OCCASION("occasion", "مناسبت"),
        OTHER("other", "یادآوری");

        companion object {
            fun fromCode(code: String): Kind = values().firstOrNull { it.code == code } ?: OTHER
        }
    }

    /** The next time this date comes, today included. Esfand 30 falls on Esfand 29 in common years. */
    fun nextOccurrence(today: LocalDate): LocalDate {
        val jy = JalaliCalendar.gregorianToJalali(today).year
        val thisYear = onYear(jy)
        return if (thisYear.isBefore(today)) onYear(jy + 1) else thisYear
    }

    fun daysUntil(today: LocalDate): Int = ChronoUnit.DAYS.between(today, nextOccurrence(today)).toInt()

    /** How many years it will be on its next occurrence (e.g. 5 for the 5th anniversary), or null. */
    fun yearsOnNext(today: LocalDate): Int? {
        val origin = year ?: return null
        val n = JalaliCalendar.gregorianToJalali(nextOccurrence(today)).year - origin
        return n.takeIf { it > 0 }
    }

    private fun onYear(jy: Int): LocalDate {
        val last = com.example.utils.JalaliDate(jy, month, 1).lengthOfMonth
        return JalaliCalendar.jalaliToGregorian(jy, month, day.coerceAtMost(last))
    }

    companion object {
        const val MAX_TITLE = 40
        const val MAX_DATES = 60
        val REMIND_CHOICES = listOf(0, 1, 3, 7)
        private const val FIELD = '\u001F'
        private const val RECORD = '\u001E'

        fun create(
            id: String,
            title: String,
            kind: Kind,
            month: Int,
            day: Int,
            year: Int?,
            remindDaysBefore: Int
        ): ImportantDate {
            val m = month.coerceIn(1, 12)
            val maxDay = if (m <= 6) 31 else 30
            return ImportantDate(
                id = id,
                title = title.replace(FIELD, ' ').replace(RECORD, ' ').replace('\n', ' ').trim().take(MAX_TITLE),
                kind = kind,
                month = m,
                day = day.coerceIn(1, maxDay),
                year = year?.takeIf { it in 1300..1500 },
                remindDaysBefore = remindDaysBefore.coerceIn(0, 30)
            )
        }

        fun encode(dates: List<ImportantDate>): String = dates.take(MAX_DATES).joinToString(RECORD.toString()) {
            listOf(it.id, it.kind.code, it.month, it.day, it.year ?: "", it.remindDaysBefore, it.title)
                .joinToString(FIELD.toString())
        }

        /** Reads what [encode] wrote; damaged records are skipped. */
        fun decode(text: String?): List<ImportantDate> {
            if (text.isNullOrEmpty()) return emptyList()
            return text.split(RECORD).mapNotNull { record ->
                val f = record.split(FIELD)
                if (f.size != 7) return@mapNotNull null
                val month = f[2].toIntOrNull() ?: return@mapNotNull null
                val day = f[3].toIntOrNull() ?: return@mapNotNull null
                val before = f[5].toIntOrNull() ?: return@mapNotNull null
                if (f[0].isBlank() || f[6].isBlank()) return@mapNotNull null
                create(f[0], f[6], Kind.fromCode(f[1]), month, day, f[4].toIntOrNull(), before)
            }
        }

        /** Dates sorted by how soon they come. */
        fun upcoming(dates: List<ImportantDate>, today: LocalDate): List<ImportantDate> =
            dates.sortedWith(compareBy({ it.daysUntil(today) }, { it.title }))

        /** Dates to remind today: on the day, or exactly [remindDaysBefore] days before. */
        fun dueToday(dates: List<ImportantDate>, today: LocalDate): List<Pair<ImportantDate, Int>> =
            dates.mapNotNull { date ->
                val left = date.daysUntil(today)
                if (left == 0 || (date.remindDaysBefore > 0 && left == date.remindDaysBefore)) date to left else null
            }.sortedBy { it.second }

        /** Ready-made yearly occasions on fixed Jalali dates. */
        val SUGGESTED_OCCASIONS: List<Triple<String, Int, Int>> = listOf(
            Triple("نوروز", 1, 1),
            Triple("شب یلدا", 9, 30),
            Triple("ولنتاین", 11, 25),
            Triple("سپندارمذگان (روز عشق)", 11, 29)
        )
    }
}

/** Daily-challenge streaks, counted from the days the user marked as done. */
object Progress {
    /** Consecutive done days ending today, or ending yesterday when today is not done yet. */
    fun streak(doneEpochDays: Set<Long>, today: LocalDate): Int {
        var day = today.toEpochDay()
        if (day !in doneEpochDays) day -= 1
        var count = 0
        while (day in doneEpochDays) {
            count++
            day--
        }
        return count
    }
}
