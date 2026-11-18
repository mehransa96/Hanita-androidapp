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
        val discuss: Boolean = false,
        /** What the test involves and how to prepare, in plain words. */
        val how: String = ""
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
                    "فشار خون بالا معمولاً بی‌علامت است و مهم‌ترین عامل سکته و بیماری قلب است.",
                    how = "پنج دقیقه نشسته استراحت کن، ۳۰ دقیقه پیش از آن چای، قهوه و سیگار نخور و بازو را هم‌سطح قلب بگذار. اگر بالا بود، با اندازه‌گیری در خانه یا هولتر تأیید می‌شود."
                )
            )
            add(
                Screening(
                    "weight", "وزن، BMI و دور کمر", "هر سال در چکاپ",
                    "اضافه‌وزن و چربی شکمی پیش‌درآمد دیابت، فشار خون و بیماری قلب‌اند و با برنامه رفتاری بهتر می‌شوند.",
                    how = "ابزار «BMI و دور کمر» همین برنامه را پر کن. اگر BMI سی یا بیشتر است، از پزشک برنامه کاهش وزن ساختاریافته بخواه."
                )
            )
            add(
                Screening(
                    "mood", "غربالگری افسردگی و اضطراب", "هر سال یا هر وقت حالت چند هفته پایین بود",
                    "تشخیص زودهنگام، درمان را کوتاه‌تر و مؤثرتر می‌کند. آزمون‌های PHQ-9 و GAD-7 همین برنامه نقطه شروع خوبی است.",
                    how = "آزمون‌های افسردگی و اضطراب بخش «خودشناسی» را بزن و نتیجه را به پزشک نشان بده."
                )
            )
            add(
                Screening(
                    "alcohol", "بررسی مصرف الکل", "هر سال در چکاپ",
                    "مصرف پرخطر الکل با فشار خون، بیماری کبد، چند سرطان و حادثه همراه است و مشاوره کوتاه پزشک آن را کم می‌کند.",
                    how = "پزشک با چند سؤال کوتاه (مثل AUDIT-C) می‌پرسد چند وقت یک‌بار و چقدر می‌نوشی. صادقانه جواب بده؛ هدف کمک است."
                )
            )
            if (smoker) add(
                Screening(
                    "tobacco", "ترک سیگار و قلیان", "در هر ویزیت",
                    "ترک دخانیات در هر سنی خطر سکته، سرطان و بیماری ریه را کم می‌کند و ترکیب مشاوره و دارو شانس موفقیت را چند برابر می‌کند.",
                    how = "از پزشک درباره داروهای کمکی (مثل جایگزین نیکوتین) بپرس و با خط ترک دخانیات تماس بگیر."
                )
            )
            if (age <= 79) add(
                Screening(
                    "hcv", "آزمایش هپاتیت C", "دست‌کم یک بار در بزرگسالی",
                    "هپاتیت C سال‌ها بی‌علامت می‌ماند و حالا درمان قطعی دارد.",
                    how = "یک آزمایش خون ساده (آنتی‌بادی HCV)؛ اگر مثبت بود، آزمایش تأییدی انجام می‌شود."
                )
            )
            if (age <= 65) add(
                Screening(
                    "hiv", "آزمایش HIV", "دست‌کم یک بار؛ با رفتار پرخطر، منظم‌تر",
                    "تشخیص و درمان زودهنگام، عمر طبیعی و پیشگیری از انتقال را ممکن می‌کند.",
                    how = "آزمایش خون ساده و محرمانه؛ در مراکز مشاوره بیماری‌های رفتاری رایگان انجام می‌شود."
                )
            )
            if (age in 35..70 && overweight) add(
                Screening(
                    "sugar", "آزمایش قند خون (پیش‌دیابت و دیابت)", "هر ۳ سال",
                    "اضافه‌وزن خطر دیابت نوع ۲ را بالا می‌برد و پیش‌دیابت با تغییر سبک زندگی برگشت‌پذیر است.",
                    how = "قند ناشتا (۸ تا ۱۲ ساعت ناشتا، فقط آب) یا HbA1c که ناشتایی نمی‌خواهد. قند ناشتای ۱۰۰ تا ۱۲۵ پیش‌دیابت و ۱۲۶ به بالا دیابت است."
                )
            )
            if (age in 40..75) add(
                Screening(
                    "lipids", "چربی خون و ارزیابی خطر قلبی", "هر ۴ تا ۶ سال، یا طبق نظر پزشک",
                    "پزشک با کلسترول، فشار، قند و سیگار خطر ده‌ساله سکته و حمله قلبی را می‌سنجد و درباره داروی استاتین تصمیم می‌گیرد.",
                    how = "آزمایش خون (کلسترول تام، LDL، HDL، تری‌گلیسرید)؛ معمولاً ۹ تا ۱۲ ساعت ناشتایی بهتر است. اگر خطر ده‌ساله ۱۰٪ یا بیشتر بود، استاتین معمولاً توصیه می‌شود."
                )
            )
            if (age in 45..75) add(
                Screening(
                    "colon", "غربالگری سرطان روده بزرگ", "آزمایش مدفوع هر سال یا کولونوسکوپی هر ۱۰ سال",
                    "سرطان روده اگر زود پیدا شود، بسیار قابل‌درمان است و پولیپ‌ها پیش از سرطان شدن برداشته می‌شوند.",
                    how = "آزمایش خون مخفی مدفوع (FIT) در خانه انجام می‌شود؛ اگر مثبت بود، کولونوسکوپی لازم است. کولونوسکوپی یک روز آماده‌سازی روده می‌خواهد. سابقه خانوادگی، شروع زودتر را لازم می‌کند."
                )
            )
            if (age in 76..85) add(
                Screening(
                    "colon76", "ادامه غربالگری روده بزرگ", "تصمیم فردی با پزشک",
                    "بعد از ۷۵ سالگی، سود غربالگری به سلامت کلی و سابقه غربالگری‌های قبلی بستگی دارد.",
                    discuss = true,
                    how = "اگر قبلاً غربالگری نشده‌ای و سلامت کلی‌ات خوب است، سود بیشتری دارد."
                )
            )
            if (age in 55..69) add(
                Screening(
                    "psa", "آزمایش PSA پروستات", "پس از گفت‌وگو با پزشک درباره سود و زیانش",
                    "برای این سن، تصمیم فردی است؛ پزشک درباره سود، مثبت کاذب و درمان‌های بی‌مورد توضیح می‌دهد.",
                    discuss = true,
                    how = "آزمایش خون ساده. ۴۸ ساعت پیش از آن دوچرخه‌سواری طولانی و انزال نداشته باش، چون PSA را موقتاً بالا می‌برند. سابقه پدر یا برادر، اهمیت آن را بیشتر می‌کند."
                )
            )
            if (age in 50..80 && smoker) add(
                Screening(
                    "lung", "سی‌تی‌اسکن کم‌دوز ریه", "هر سال",
                    "برای سیگاری‌ها یا کسانی که کمتر از ۱۵ سال است ترک کرده‌اند و سابقه طولانی مصرف دارند، مرگ از سرطان ریه را کم می‌کند.",
                    how = "سی‌تی‌اسکن چند دقیقه‌ای بدون تزریق. برای کسانی است که دست‌کم ۲۰ «پاکت‌سال» کشیده‌اند (مثلاً روزی یک پاکت به مدت ۲۰ سال)."
                )
            )
            if (age in 65..75 && smoker) add(
                Screening(
                    "aaa", "سونوگرافی آئورت شکمی", "یک بار",
                    "مردانی که سیگار کشیده‌اند در خطر گشادی خطرناک آئورت هستند که با یک سونوگرافی ساده پیدا می‌شود.",
                    how = "سونوگرافی شکم چندمنظوره و بی‌درد؛ بهتر است چند ساعت ناشتا باشی."
                )
            )
            if (age >= 65) add(
                Screening(
                    "falls", "پیشگیری از زمین خوردن", "هر سال بررسی؛ ورزش تعادلی چند بار در هفته",
                    "زمین خوردن مهم‌ترین علت آسیب جدی در سالمندی است و ورزش تعادلی و قدرتی خطرش را کم می‌کند.",
                    how = "از پزشک بخواه داروهایی را که سرگیجه می‌آورند مرور کند و بینایی‌ات را بسنجد. تمرین تعادل (مثل تای‌چی) و تمرین قدرتی پا بهترین پیشگیری‌اند."
                )
            )
        }
    }

    enum class BmiClass(val title: String, val advice: String) {
        UNDER("کم‌وزن", "با پزشک یا متخصص تغذیه درباره علت و برنامه افزایش وزن سالم مشورت کن؛ کم‌وزنی گاهی نشانه بیماری تیروئید، گوارش یا استرس است."),
        NORMAL("وزن طبیعی", "عالی است. با تحرک منظم و غذای ساده همین را نگه دار و دور کمرت را هم زیر نظر داشته باش."),
        OVER("اضافه‌وزن", "کم کردن ۵ تا ۱۰ درصد وزن، فشار خون و قند را بهتر می‌کند. تحرک روزانه، غذای کمتر فرآوری‌شده و نوشیدنی‌های بی‌قند را شروع کن."),
        OBESE("چاقی درجه ۱", "خطر دیابت، فشار خون و بیماری قلب بالاتر است. با پزشک درباره برنامه کاهش وزن و آزمایش قند و چربی صحبت کن."),
        OBESE_2("چاقی درجه ۲", "خطر بیماری‌های قلبی و متابولیک به‌طور جدی بالاست. یک برنامه درمانی ساختاریافته زیر نظر پزشک (تغذیه، تحرک و در صورت نیاز دارو) لازم است."),
        OBESE_3("چاقی درجه ۳", "این شدیدترین رده چاقی است. حتماً با پزشک درباره همه گزینه‌های درمان، از جمله داروهای جدید و جراحی، صحبت کن.")
    }

    enum class WaistRisk(val title: String, val advice: String) {
        HEALTHY("سالم", "دور کمرت کمتر از نصف قدت است؛ این بهترین محدوده است."),
        INCREASED("خطر بیشتر", "چربی شکمی خطر بیماری قلب و دیابت را بالا می‌برد. هدف: دور کمر کمتر از نصف قد."),
        HIGH("خطر زیاد", "چربی شکمی در حد پرخطر است. با پزشک درباره آزمایش قند، چربی و فشار خون صحبت کن.")
    }

    /** The WHO cut-offs for men's waist circumference in centimetres. */
    enum class WaistSize(val title: String, val advice: String) {
        NORMAL("کمتر از ۹۴ سانتی‌متر", "دور کمرت زیر آستانه خطر سازمان جهانی بهداشت برای مردان است."),
        INCREASED("۹۴ تا ۱۰۱ سانتی‌متر", "سازمان جهانی بهداشت این را «خطر بیشتر» برای بیماری‌های قلبی و متابولیک می‌داند."),
        HIGH("۱۰۲ سانتی‌متر یا بیشتر", "سازمان جهانی بهداشت این را «خطر بسیار بیشتر» می‌داند؛ کم کردن دور کمر اولویت سلامت توست.")
    }

    data class BodyResult(
        val bmi: Double,
        val bmiClass: BmiClass,
        val waistRatio: Double?,
        val waistRisk: WaistRisk?,
        val waistSize: WaistSize? = null,
        /** Kilograms to lose (negative: to gain) to reach the edge of the normal BMI range; 0 inside it. */
        val kgToNormal: Int = 0
    )

    /**
     * BMI from [weightKg] and [heightCm], plus the waist-to-height ratio and the WHO waist
     * category when [waistCm] is given. Returns null for values outside a plausible adult range.
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
            bmi < 35.0 -> BmiClass.OBESE
            bmi < 40.0 -> BmiClass.OBESE_2
            else -> BmiClass.OBESE_3
        }
        val ratio = waistCm?.let { round2(it / heightCm) }
        val risk = ratio?.let {
            when {
                it < 0.5 -> WaistRisk.HEALTHY
                it < 0.6 -> WaistRisk.INCREASED
                else -> WaistRisk.HIGH
            }
        }
        val size = waistCm?.let {
            when {
                it < 94.0 -> WaistSize.NORMAL
                it < 102.0 -> WaistSize.INCREASED
                else -> WaistSize.HIGH
            }
        }
        val (low, high) = healthyWeight(heightCm)
        val kg = when {
            weightKg > high -> (weightKg - high).roundToInt()
            weightKg < low -> -(low - weightKg).roundToInt()
            else -> 0
        }
        return BodyResult(bmi, cls, ratio, risk, size, kg)
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
