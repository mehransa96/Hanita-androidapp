package com.hanita.core.gentleman

import kotlin.math.roundToInt

/**
 * Men's preventive health: which screenings fit an age and risk (after the USPSTF A and B
 * recommendations) and simple body measures (WHO BMI classes, NICE waist-to-height ratio).
 *
 * Educational only: local guidelines differ and a doctor decides what is right for a person.
 */
object MensHealth {

    data class Screening(
        val id: String,
        val title: String,
        /** How often or when, in plain words. */
        val schedule: String,
        /** Why it matters, one line. */
        val why: String,
        /** True when the guideline asks for a shared decision with the doctor first. */
        val discuss: Boolean = false
    )

    const val MIN_AGE = 18
    const val MAX_AGE = 90

    /**
     * Screenings for a man of [age]. [smoker] means he smokes now or quit within 15 years
     * (with a long smoking history); [overweight] means a BMI of 25 or more.
     */
    fun screeningsFor(age: Int, smoker: Boolean, overweight: Boolean): List<Screening> {
        if (age < MIN_AGE) return emptyList()
        return buildList {
            add(
                Screening(
                    "bp", "اندازه‌گیری فشار خون",
                    if (age >= 40) "هر سال" else "هر ۳ تا ۵ سال (اگر بالای ۱۳۰/۸۵ بود، هر سال)",
                    "فشار خون بالا معمولاً بی‌علامت است و مهم‌ترین عامل سکته و بیماری قلب است."
                )
            )
            add(
                Screening(
                    "mood", "غربالگری افسردگی و اضطراب", "هر سال یا هر وقت حالت چند هفته پایین بود",
                    "تشخیص زودهنگام، درمان را کوتاه‌تر و مؤثرتر می‌کند. آزمون‌های PHQ-9 و GAD-7 همین برنامه نقطه شروع خوبی است."
                )
            )
            if (age <= 79) add(
                Screening(
                    "hcv", "آزمایش هپاتیت C", "دست‌کم یک بار در بزرگسالی",
                    "هپاتیت C سال‌ها بی‌علامت می‌ماند و حالا درمان قطعی دارد."
                )
            )
            if (age <= 65) add(
                Screening(
                    "hiv", "آزمایش HIV", "دست‌کم یک بار؛ با رفتار پرخطر، منظم‌تر",
                    "تشخیص و درمان زودهنگام، عمر طبیعی و پیشگیری از انتقال را ممکن می‌کند."
                )
            )
            if (age in 35..70 && overweight) add(
                Screening(
                    "sugar", "آزمایش قند خون (پیش‌دیابت و دیابت)", "هر ۳ سال",
                    "اضافه‌وزن خطر دیابت نوع ۲ را بالا می‌برد و پیش‌دیابت با تغییر سبک زندگی برگشت‌پذیر است."
                )
            )
            if (age in 40..75) add(
                Screening(
                    "lipids", "چربی خون و ارزیابی خطر قلبی", "هر ۴ تا ۶ سال، یا طبق نظر پزشک",
                    "پزشک با کلسترول، فشار، قند و سیگار خطر ده‌ساله سکته و حمله قلبی را می‌سنجد و درباره داروی استاتین تصمیم می‌گیرد."
                )
            )
            if (age in 45..75) add(
                Screening(
                    "colon", "غربالگری سرطان روده بزرگ", "آزمایش مدفوع هر سال یا کولونوسکوپی هر ۱۰ سال",
                    "سرطان روده اگر زود پیدا شود، بسیار قابل‌درمان است و پولیپ‌ها پیش از سرطان شدن برداشته می‌شوند."
                )
            )
            if (age in 55..69) add(
                Screening(
                    "psa", "آزمایش PSA پروستات", "پس از گفت‌وگو با پزشک درباره سود و زیانش",
                    "برای این سن، تصمیم فردی است؛ پزشک درباره سود، مثبت کاذب و درمان‌های بی‌مورد توضیح می‌دهد.",
                    discuss = true
                )
            )
            if (age in 50..80 && smoker) add(
                Screening(
                    "lung", "سی‌تی‌اسکن کم‌دوز ریه", "هر سال",
                    "برای سیگاری‌ها یا کسانی که کمتر از ۱۵ سال است ترک کرده‌اند و سابقه طولانی مصرف دارند، مرگ از سرطان ریه را کم می‌کند."
                )
            )
            if (age in 65..75 && smoker) add(
                Screening(
                    "aaa", "سونوگرافی آئورت شکمی", "یک بار",
                    "مردانی که سیگار کشیده‌اند در خطر گشادی خطرناک آئورت هستند که با یک سونوگرافی ساده پیدا می‌شود."
                )
            )
        }
    }

    enum class BmiClass(val title: String, val advice: String) {
        UNDER("کم‌وزن", "با پزشک یا متخصص تغذیه درباره علت و برنامه افزایش وزن سالم مشورت کن."),
        NORMAL("وزن طبیعی", "عالی است. با تحرک منظم و غذای ساده همین را نگه دار."),
        OVER("اضافه‌وزن", "کم کردن ۵ تا ۱۰ درصد وزن، فشار خون و قند را بهتر می‌کند. تحرک روزانه و غذای کمتر فرآوری‌شده را شروع کن."),
        OBESE("چاقی", "خطر دیابت، فشار خون و بیماری قلب بالاتر است. با پزشک درباره برنامه کاهش وزن و آزمایش قند و چربی صحبت کن.")
    }

    enum class WaistRisk(val title: String, val advice: String) {
        HEALTHY("سالم", "دور کمرت کمتر از نصف قدت است؛ این بهترین محدوده است."),
        INCREASED("خطر بیشتر", "چربی شکمی خطر بیماری قلب و دیابت را بالا می‌برد. هدف: دور کمر کمتر از نصف قد."),
        HIGH("خطر زیاد", "چربی شکمی در حد پرخطر است. با پزشک درباره آزمایش قند، چربی و فشار خون صحبت کن.")
    }

    data class BodyResult(val bmi: Double, val bmiClass: BmiClass, val waistRatio: Double?, val waistRisk: WaistRisk?)

    /**
     * BMI from [weightKg] and [heightCm], plus the waist-to-height ratio when [waistCm] is given.
     * Returns null for values outside a plausible adult range.
     */
    fun body(weightKg: Double, heightCm: Double, waistCm: Double? = null): BodyResult? {
        if (heightCm !in 120.0..230.0 || weightKg !in 30.0..300.0) return null
        if (waistCm != null && waistCm !in 40.0..200.0) return null
        val m = heightCm / 100.0
        val bmi = round1(weightKg / (m * m))
        val cls = when {
            bmi < 18.5 -> BmiClass.UNDER
            bmi < 25.0 -> BmiClass.NORMAL
            bmi < 30.0 -> BmiClass.OVER
            else -> BmiClass.OBESE
        }
        val ratio = waistCm?.let { round2(it / heightCm) }
        val risk = ratio?.let {
            when {
                it < 0.5 -> WaistRisk.HEALTHY
                it < 0.6 -> WaistRisk.INCREASED
                else -> WaistRisk.HIGH
            }
        }
        return BodyResult(bmi, cls, ratio, risk)
    }

    /** The weight range (kg) with a normal BMI (18.5–24.9) for [heightCm]. */
    fun healthyWeight(heightCm: Double): Pair<Int, Int> {
        val m = heightCm / 100.0
        return (18.5 * m * m).roundToInt() to (24.9 * m * m).roundToInt()
    }

    private fun round1(v: Double) = (v * 10).roundToInt() / 10.0
    private fun round2(v: Double) = (v * 100).roundToInt() / 100.0

    const val DISCLAIMER =
        "این فهرست بر پایه توصیه‌های کارگروه خدمات پیشگیری آمریکا (USPSTF) و سازمان جهانی بهداشت است و آموزشی است. " +
            "راهنماهای ایران و شرایط تو ممکن است فرق داشته باشد؛ تصمیم نهایی با پزشک خانواده است."
}
