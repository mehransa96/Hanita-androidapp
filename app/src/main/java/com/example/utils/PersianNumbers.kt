package com.example.utils

/**
 * Number helpers for a Persian UI.
 *
 * Persian keyboards type ۰-۹ (and sometimes Arabic ٠-٩) with "٫" as the decimal separator.
 * Kotlin's toFloatOrNull() rejects those, so every numeric field goes through [parseDecimal].
 */
object PersianNumbers {
    private const val PERSIAN_ZERO = '۰'
    private const val ARABIC_ZERO = '٠'

    /** Converts Persian/Arabic-Indic digits and decimal separators to ASCII. */
    fun normalize(input: String): String {
        val out = StringBuilder(input.length)
        for (c in input.trim()) {
            out.append(
                when (c) {
                    in '۰'..'۹' -> '0' + (c - PERSIAN_ZERO)
                    in '٠'..'٩' -> '0' + (c - ARABIC_ZERO)
                    '٫', ',', '،' -> '.'
                    else -> c
                }
            )
        }
        return out.toString()
    }

    /** Parses a decimal typed with any digits; null when empty, malformed or outside [range]. */
    fun parseDecimal(input: String, range: ClosedFloatingPointRange<Float>? = null): Float? {
        val normalized = normalize(input)
        if (normalized.isEmpty() || !DECIMAL.matches(normalized)) return null
        val value = normalized.toFloatOrNull() ?: return null
        return if (range == null || value in range) value else null
    }

    /** Parses an integer typed with any digits; null when malformed or outside [range]. */
    fun parseInt(input: String, range: IntRange? = null): Int? {
        val normalized = normalize(input)
        if (normalized.isEmpty() || !INTEGER.matches(normalized)) return null
        val value = normalized.toIntOrNull() ?: return null
        return if (range == null || value in range) value else null
    }

    /** Converts ASCII digits to Persian digits for display. */
    fun toPersianDigits(input: String): String {
        val out = StringBuilder(input.length)
        for (c in input) out.append(if (c in '0'..'9') PERSIAN_ZERO + (c - '0') else c)
        return out.toString()
    }

    fun toPersianDigits(value: Int): String = toPersianDigits(value.toString())

    /** "08:05" with Persian digits. */
    fun formatTime(hour: Int, minute: Int): String =
        toPersianDigits("%02d:%02d".format(java.util.Locale.US, hour, minute))

    /** One decimal place at most, Persian digits and decimal separator ("۶۵٫۵"). */
    fun formatDecimal(value: Float): String {
        val rounded = Math.round(value * 10f) / 10f
        val text = if (rounded % 1f == 0f) rounded.toInt().toString()
        else String.format(java.util.Locale.US, "%.1f", rounded)
        return toPersianDigits(text).replace('.', '٫')
    }

    private val DECIMAL = Regex("""\d{1,4}(\.\d{1,3})?""")
    private val INTEGER = Regex("""\d{1,6}""")
}
