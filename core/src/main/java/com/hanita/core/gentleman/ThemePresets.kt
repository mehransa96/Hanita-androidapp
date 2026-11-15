package com.hanita.core.gentleman

/**
 * Ready-made colour themes the user can pick in settings. Each has a dark and a light palette,
 * and every text/background pair meets WCAG AA contrast (checked in the tests).
 *
 * Colours are ARGB longs so this file stays free of Android and can be tested on the JVM.
 */
object ThemePresets {

    /** The colours one mode of a theme needs, in Material 3 role names. */
    data class Palette(
        val primary: Long,
        val onPrimary: Long,
        val primaryContainer: Long,
        val onPrimaryContainer: Long,
        val secondary: Long,
        val secondaryContainer: Long,
        val onSecondaryContainer: Long,
        val background: Long,
        val surface: Long,
        val surfaceVariant: Long,
        val onSurface: Long,
        val onSurfaceVariant: Long,
        val outline: Long,
        val outlineVariant: Long,
        val error: Long
    )

    data class Preset(val id: String, val title: String, val subtitle: String, val dark: Palette, val light: Palette)

    /** Follow the phone, or always light / always dark. */
    enum class Mode(val code: String, val title: String) {
        SYSTEM("system", "خودکار"),
        LIGHT("light", "روشن"),
        DARK("dark", "تیره");

        companion object {
            fun fromCode(code: String?): Mode = values().firstOrNull { it.code == code } ?: SYSTEM
        }
    }

    const val DEFAULT_ID = "graphite"

    val ALL: List<Preset> = listOf(
        Preset(
            "graphite", "گرافیت و کهربا", "مینیمال، تیره و گرم؛ تم اصلی",
            dark = Palette(0xFFF5A524, 0xFF1A1200, 0xFF2B2111, 0xFFFFD58A, 0xFF7CA4C9, 0xFF1C2733, 0xFFCFE0F1, 0xFF0E1014, 0xFF171A20, 0xFF20242C, 0xFFECEFF3, 0xFF9AA3AF, 0xFF3A404B, 0xFF2A2F38, 0xFFFF6B6B),
            light = Palette(0xFF9A5B00, 0xFFFFFFFF, 0xFFFFEFD2, 0xFF3D2600, 0xFF3B6A93, 0xFFE3EDF7, 0xFF0F2A42, 0xFFF3F4F6, 0xFFFFFFFF, 0xFFECEEF1, 0xFF111418, 0xFF545C67, 0xFFC5CAD1, 0xFFE1E4E8, 0xFFC62828)
        ),
        Preset(
            "navy", "سرمه‌ای و طلایی", "کلاسیک و رسمی، مثل کت‌وشلوار سرمه‌ای",
            dark = Palette(0xFFD9B451, 0xFF1C1500, 0xFF2E2714, 0xFFF2DC9C, 0xFF8FB3DD, 0xFF1A2740, 0xFFD3E2F5, 0xFF0A1020, 0xFF111A2E, 0xFF1A2540, 0xFFEAEEF5, 0xFF9AA6BC, 0xFF38445E, 0xFF253049, 0xFFFF6B6B),
            light = Palette(0xFF7A5A00, 0xFFFFFFFF, 0xFFFBEFC9, 0xFF3A2A00, 0xFF1F4E85, 0xFFDFE9F6, 0xFF0F2A4A, 0xFFF1F3F8, 0xFFFFFFFF, 0xFFE8ECF3, 0xFF0F1726, 0xFF4E5A70, 0xFFC2C9D6, 0xFFDFE4EC, 0xFFC62828)
        ),
        Preset(
            "forest", "سبز جنگلی", "آرام و طبیعی، با لمسه‌ای از چوب",
            dark = Palette(0xFF86C99A, 0xFF062712, 0xFF18301F, 0xFFBDE8C9, 0xFFC9B37E, 0xFF2C271A, 0xFFEDE0BF, 0xFF0C120E, 0xFF141C17, 0xFF1D2820, 0xFFE8EFEA, 0xFF97A89C, 0xFF344238, 0xFF243029, 0xFFFF6B6B),
            light = Palette(0xFF2C6A3E, 0xFFFFFFFF, 0xFFDCEFE1, 0xFF0C2A16, 0xFF7A5F23, 0xFFF3EBD8, 0xFF3A2C0A, 0xFFF1F4F1, 0xFFFFFFFF, 0xFFE6ECE7, 0xFF101812, 0xFF4B5A50, 0xFFC0CBC3, 0xFFDDE4DE, 0xFFC62828)
        ),
        Preset(
            "copper", "زغالی و مسی", "گرم و صنعتی، مثل چرم و مس",
            dark = Palette(0xFFE7956A, 0xFF2B1206, 0xFF33231B, 0xFFF6CDB6, 0xFFB8C0C8, 0xFF262A2E, 0xFFDDE2E7, 0xFF121010, 0xFF1B1817, 0xFF262120, 0xFFEFEAE8, 0xFFA89F9B, 0xFF433B38, 0xFF2F2927, 0xFFFF6B6B),
            light = Palette(0xFF9C4A22, 0xFFFFFFFF, 0xFFFBE3D6, 0xFF3D1A08, 0xFF4A5560, 0xFFE7EBEF, 0xFF1E2830, 0xFFF5F2F0, 0xFFFFFFFF, 0xFFEFEAE7, 0xFF191312, 0xFF5C5250, 0xFFD0C6C2, 0xFFE7DFDB, 0xFFC62828)
        ),
        Preset(
            "ocean", "آبی اقیانوسی", "خنک، تمیز و پرانرژی",
            dark = Palette(0xFF62CDE4, 0xFF002A33, 0xFF0F2E37, 0xFFB4ECF8, 0xFFA9B8F0, 0xFF1F2540, 0xFFDCE2FB, 0xFF0A1116, 0xFF111B23, 0xFF19262F, 0xFFE7EFF3, 0xFF93A6B2, 0xFF31424D, 0xFF22313A, 0xFFFF6B6B),
            light = Palette(0xFF0B6780, 0xFFFFFFFF, 0xFFD6F1F8, 0xFF00303C, 0xFF3E4F9A, 0xFFE2E6F8, 0xFF14205A, 0xFFF0F4F6, 0xFFFFFFFF, 0xFFE5ECEF, 0xFF0E171C, 0xFF4A5B64, 0xFFBFCCD2, 0xFFDCE4E8, 0xFFC62828)
        ),
        Preset(
            "burgundy", "شرابی کلاسیک", "باوقار و جسور",
            dark = Palette(0xFFEC8FA2, 0xFF3A0716, 0xFF3A1B23, 0xFFFAC9D3, 0xFFD7B98A, 0xFF2C2519, 0xFFF0DFC2, 0xFF120D0F, 0xFF1C1517, 0xFF271E21, 0xFFF1E9EB, 0xFFAA9A9F, 0xFF45363A, 0xFF302629, 0xFFFF6B6B),
            light = Palette(0xFF8E1F3E, 0xFFFFFFFF, 0xFFF8DDE4, 0xFF3E0717, 0xFF7A5A1F, 0xFFF4EBDB, 0xFF3B2A08, 0xFFF6F1F2, 0xFFFFFFFF, 0xFFF0E8EA, 0xFF1A1113, 0xFF5E4F53, 0xFFD4C5C9, 0xFFEADFE2, 0xFFC62828)
        ),
        Preset(
            "mono", "سیاه و سفید", "بی‌رنگ و کاملاً مینیمال",
            dark = Palette(0xFFF2F2F2, 0xFF111111, 0xFF2A2A2A, 0xFFF2F2F2, 0xFFBDBDBD, 0xFF262626, 0xFFE6E6E6, 0xFF0A0A0A, 0xFF151515, 0xFF1F1F1F, 0xFFF2F2F2, 0xFFA3A3A3, 0xFF3D3D3D, 0xFF2A2A2A, 0xFFFF6B6B),
            light = Palette(0xFF161616, 0xFFFFFFFF, 0xFFE6E6E6, 0xFF111111, 0xFF4A4A4A, 0xFFEDEDED, 0xFF1C1C1C, 0xFFF4F4F4, 0xFFFFFFFF, 0xFFEDEDED, 0xFF111111, 0xFF565656, 0xFFC9C9C9, 0xFFE3E3E3, 0xFFC62828)
        )
    )

    private val byId = ALL.associateBy { it.id }

    /** The theme with this id, or the default one for an unknown or missing id. */
    fun get(id: String?): Preset = byId[id] ?: byId.getValue(DEFAULT_ID)

    /** WCAG contrast ratio of two opaque colours (1..21). */
    fun contrast(a: Long, b: Long): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF).toDouble() / 255.0
            return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
