package com.example.data.model

import androidx.annotation.RawRes

enum class CyclePhaseType(
    val key: String,
    val persianTitle: String,
    val emoji: String,
    val shortSubtitle: String,
    val description: String
) {
    PERIOD(
        key = "period",
        persianTitle = "دوره قاعدگی",
        emoji = "🩸",
        shortSubtitle = "تسکین دردهای عضلانی و انقباضات رحمی",
        description = "بدن شما در این فاز نیاز به بازسازی، استراحت عمیق و تسکین اسپاسم‌های شکمی دارد. موسیقی‌های آرام این فاز با پیانوی ملایم می‌توانند به آرامش و استراحت بهتر کمک کنند (جایگزین مسکن یا درمان نیستند)."
    ),
    FOLLICULAR(
        key = "follicular",
        persianTitle = "فاز فولیکولار",
        emoji = "🌱",
        shortSubtitle = "طراوت، انرژی نو و شروع دوباره",
        description = "با افزایش سطح استروژن، انرژی و خلاقیت شما در حال شکوفایی است. قطعات آکوستیک شاداب و کالیمبای نشاط‌آور این فاز انگیزه‌بخش شروع پروژه‌های تازه است."
    ),
    OVULATION(
        key = "ovulation",
        persianTitle = "فاز تخمک‌گذاری",
        emoji = "🌸",
        shortSubtitle = "اوج اعتماد‌به‌نفس، سرزندگی و انگیزه",
        description = "شما در اوج ترشح هورمون‌های نشاط و جذابیت هستید. ریتم‌های پرانرژی و بیت‌های شاداب این بخش انرژی مضاعفی برای ورزش و فعالیت‌های پرتحرک به شما می‌بخشند."
    ),
    LUTEAL(
        key = "luteal",
        persianTitle = "فاز لوتئال و PMS",
        emoji = "🌙",
        shortSubtitle = "کاهش استرس، تعادل خلق‌وخو و رفع تنش",
        description = "پیش از شروع قاعدگی، نوسانات پروژسترون می‌تواند خلق‌وخو را تحریک کند. بیت‌های دنج لوفای می‌توانند به آرام شدن ذهن و کاهش تنش روزانه کمک کنند."
    )
}

/**
 * What the listener wants right now. Every phase offers several intentions, so she can pick by how she
 * feels today (a sleepy period day, a focused follicular morning, an anxious PMS evening ...).
 */
enum class MusicIntent(val label: String, val emoji: String) {
    CALM("آرامش", "🌿"),
    SLEEP("خواب", "😴"),
    PAIN_RELIEF("تسکین درد", "🫶"),
    BREATHING("تنفس هدایت‌شده", "🌬️"),
    FOCUS("تمرکز و خلاقیت", "🎯"),
    MOVEMENT("حرکت و ورزش", "⚡"),
    JOY("شادی و رقص", "💃"),
    CONFIDENCE("اعتمادبه‌نفس", "✨")
}

data class CycleMusic(
    val id: String,
    val title: String,
    val subtitle: String,
    val phase: CyclePhaseType,
    val durationSeconds: Int,
    @RawRes val resId: Int,
    val categoryBadge: String,
    val benefitDescription: String,
    val frequencyLabel: String,
    val intent: MusicIntent = MusicIntent.CALM,
    /** Tempo in beats per minute, null for beatless soundscapes. */
    val bpm: Int? = null,
    /** When and how to listen, e.g. "در رختخواب، با صدای کم". */
    val howToListen: String = "",
    /** True when the effect needs stereo headphones (binaural pieces). */
    val needsHeadphones: Boolean = false
)
