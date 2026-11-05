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
        description = "بدن شما در این فاز نیاز به بازسازی، استراحت عمیق و تسکین اسپاسم‌های شکمی دارد. موسیقی‌های این فاز با فرکانس‌های درمانی ۵۲۸Hz و پیانوی ملایم به کاهش گرفتگی‌ها کمک می‌کنند."
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
        description = "پیش از شروع قاعدگی، نوسانات پروژسترون می‌تواند خلق‌وخو را تحریک کند. بیت‌های دنج لوفای و فرکانس ۴۳۲Hz اعصاب را تسکین داده و اضطراب را از بین می‌برند."
    )
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
    val frequencyLabel: String
)
