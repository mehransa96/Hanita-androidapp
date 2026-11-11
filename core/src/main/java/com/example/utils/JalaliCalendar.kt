package com.example.utils

import java.time.LocalDate

class JalaliCalendar {

    companion object {
        fun gregorianToJalali(date: LocalDate): JalaliDate {
            return gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
        }

        fun gregorianToJalali(gY: Int, gM: Int, gD: Int): JalaliDate {
            val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
            
            var gy = gY - 1600
            val gm = gM - 1
            val gd = gD - 1
            
            var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
            
            for (i in 0 until gm) {
                gDayNo += gDaysInMonth[i]
            }
            if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
                gDayNo++
            }
            gDayNo += gd
            
            var jDayNo = gDayNo - 79
            
            val jNp = jDayNo / 12053
            jDayNo %= 12053
            
            var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
            
            jDayNo %= 1461
            
            if (jDayNo >= 366) {
                jy += (jDayNo - 1) / 365
                jDayNo = (jDayNo - 1) % 365
            }
            
            var jm = 0
            for (i in 0 until 11) {
                if (jDayNo < jDaysInMonth[i]) {
                    jm = i
                    break
                }
                jDayNo -= jDaysInMonth[i]
                jm = i + 1
            }
            
            return JalaliDate(jy, jm + 1, jDayNo + 1)
        }

        fun jalaliToGregorian(jY: Int, jM: Int, jD: Int): LocalDate {
            var jy = jY - 979
            var jm = jM - 1
            var jd = jD - 1

            var jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4
            for (i in 0 until jm) {
                jDayNo += if (i < 6) 31 else if (i < 11) 30 else 29
            }
            jDayNo += jd

            var gDayNo = jDayNo + 79

            var gy = 1600 + 400 * (gDayNo / 146097)
            gDayNo %= 146097

            var leap = true
            if (gDayNo >= 36525) {
                gDayNo--
                gy += 100 * (gDayNo / 36524)
                gDayNo %= 36524
                if (gDayNo >= 365) {
                    gDayNo++
                } else {
                    leap = false
                }
            }

            gy += 4 * (gDayNo / 1461)
            gDayNo %= 1461

            if (gDayNo >= 366) {
                leap = false
                gDayNo--
                gy += gDayNo / 365
                gDayNo %= 365
            }

            val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            if (leap) gDaysInMonth[1] = 29

            var gm = 0
            for (i in 0..11) {
                if (gDayNo < gDaysInMonth[i]) {
                    gm = i
                    break
                }
                gDayNo -= gDaysInMonth[i]
            }

            return LocalDate.of(gy, gm + 1, gDayNo + 1)
        }
    }
}

data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {
    val monthName: String
        get() = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")[month - 1]

    val lengthOfMonth: Int
        get() = when {
            month <= 6 -> 31
            month <= 11 -> 30
            else -> if (isLeapYear(year)) 30 else 29
        }

    fun isLeapYear(y: Int): Boolean {
        val matches = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        val modulus = y % 33
        return matches.contains(modulus)
    }

    fun firstDayOfWeek(): Int {
        val gregorian = JalaliCalendar.jalaliToGregorian(year, month, 1)
        val dow = gregorian.dayOfWeek.value // Monday = 1, Sunday = 7
        return (dow + 1) % 7 // Convert to Saturday = 0, Sunday = 1... Friday = 6
    }
    
    fun toLocalDate(): LocalDate {
        return JalaliCalendar.jalaliToGregorian(year, month, day)
    }

    override fun compareTo(other: JalaliDate): Int {
        if (this.year != other.year) return this.year - other.year
        if (this.month != other.month) return this.month - other.month
        return this.day - other.day
    }

    fun format(): String {
        return "$year/$month/$day"
    }

    fun formatShort(): String {
        return "${PersianNumbers.toPersianDigits(day)} $monthName"
    }

    fun formatLong(): String {
        return "${PersianNumbers.toPersianDigits(day)} $monthName ${PersianNumbers.toPersianDigits(year)}"
    }
}

/** Jalali dates formatted for display, with Persian digits. */
object PersianDates {
    /** "۱۶ آبان" */
    fun short(date: java.time.LocalDate): String {
        val j = JalaliCalendar.gregorianToJalali(date)
        return "${PersianNumbers.toPersianDigits(j.day)} ${j.monthName}"
    }

    /** "۱۶ آبان ۱۴۰۵" */
    fun long(date: java.time.LocalDate): String {
        val j = JalaliCalendar.gregorianToJalali(date)
        return "${PersianNumbers.toPersianDigits(j.day)} ${j.monthName} ${PersianNumbers.toPersianDigits(j.year)}"
    }

    /** "۱۴۰۵/۰۸/۱۶" */
    fun numeric(date: java.time.LocalDate): String {
        val j = JalaliCalendar.gregorianToJalali(date)
        return PersianNumbers.toPersianDigits("%d/%02d/%02d".format(java.util.Locale.US, j.year, j.month, j.day))
    }

    fun parseOrNull(iso: String): java.time.LocalDate? = runCatching { java.time.LocalDate.parse(iso) }.getOrNull()
}
