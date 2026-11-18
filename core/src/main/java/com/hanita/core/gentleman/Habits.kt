package com.hanita.core.gentleman

import java.time.LocalDate

/**
 * Suggested daily habits, each tied to a pillar and to the evidence behind it, plus the maths
 * for streaks and completion rates. The user can also add habits of his own.
 */
object Habits {

    data class Preset(
        val id: String,
        val title: String,
        val pillarId: String,
        /** One line on why, in the words of the source. */
        val why: String,
        val audience: GentlemanGuide.Audience = GentlemanGuide.Audience.ALL
    ) {
        fun fits(status: Status): Boolean = when (audience) {
            GentlemanGuide.Audience.ALL -> true
            GentlemanGuide.Audience.SINGLE -> status == Status.SINGLE
            GentlemanGuide.Audience.MARRIED -> status == Status.MARRIED
        }
    }

    val PRESETS: List<Preset> = listOf(
        Preset("sleep7", "دست‌کم ۷ ساعت خواب", "health", "حداقل خواب توصیه‌شده برای بزرگسالان (AASM)."),
        Preset("move30", "۳۰ دقیقه پیاده‌روی تند یا ورزش", "health", "پنج روز در هفته، همان ۱۵۰ دقیقه توصیه سازمان جهانی بهداشت است."),
        Preset("veggies", "دست‌کم پنج وعده میوه و سبزی", "health", "توصیه سازمان جهانی بهداشت برای رژیم سالم."),
        Preset("gratitude", "نوشتن سه اتفاق خوب امروز", "esteem", "در کارآزمایی Seligman، شادی را تا شش ماه بالا برد."),
        Preset("breathe", "۵ دقیقه تنفس آرام", "calm", "پنج دقیقه تنفس با بازدم طولانی، خلق را بهتر و تنش را کم کرد (Balban, 2023)."),
        Preset("read", "۲۰ دقیقه مطالعه", "mind", "یادگیری روزانه، پایه رشد فردی."),
        Preset("deepwork", "۶۰ دقیقه کار عمیق بدون گوشی", "work", "بازه بی‌وقفه برای مهم‌ترین کار (Newport)."),
        Preset("spending", "ثبت خرج‌های امروز", "money", "بدون دانستن خرج‌ها، بودجه‌ای در کار نیست."),
        Preset("connect", "پیام یا تماس با یک دوست یا خانواده", "friends", "روابط اجتماعی قوی با سلامت و طول عمر بیشتر همراه است."),
        Preset("skincare", "شست‌وشو و مرطوب‌کننده صبح و شب", "style", "روال ساده مراقبت پوست مردان (AAD)."),
        Preset("strength", "تمرین قدرتی (دو تا سه بار در هفته)", "fitness", "سازمان جهانی بهداشت دست‌کم دو روز تمرین قدرتی در هفته توصیه می‌کند."),
        Preset("phonefree", "گوشی بیرون از اتاق خواب", "digital", "صفحه نمایش پیش از خواب، خواب را دیرتر و سبک‌تر می‌کند."),
        Preset("learnskill", "۳۰ دقیقه کار روی یک مهارت شغلی", "career", "تمرین آگاهانه و پیوسته، پایه مهارت کمیاب است (Dweck؛ Newport)."),
        Preset("water", "هشت لیوان آب", "health", "نوشیدن کافی آب به تمرکز، انرژی و کار کلیه کمک می‌کند؛ ادرار کم‌رنگ نشانه خوبی است."),
        Preset("nosugar", "بدون نوشیدنی شیرین", "health", "سازمان جهانی بهداشت کم کردن قند افزوده را توصیه می‌کند؛ نوشابه بزرگ‌ترین منبع آن است."),
        Preset("steps", "۸۰۰۰ قدم", "fitness", "در یک فراتحلیل بزرگ، حدود ۸ هزار قدم در روز یا بیشتر با خطر مرگ کمتر همراه بود (Paluch 2022)."),
        Preset("stretch", "۱۰ دقیقه کشش و تحرک مفاصل", "fitness", "انعطاف و دامنه حرکت، کمردرد و آسیب ورزشی را کم می‌کند (ACSM)."),
        Preset("top3", "نوشتن سه کار مهم فردا", "work", "برنامه‌ریزی شب قبل، شروع روز را از واکنش به پیام‌ها به کار مهم تبدیل می‌کند."),
        Preset("winddown", "بدون صفحه نمایش ۶۰ دقیقه پیش از خواب", "digital", "نور و محرک صفحه‌ها، خواب را دیرتر و سبک‌تر می‌کند."),
        Preset("mindful", "۱۰ دقیقه مراقبه ذهن‌آگاهی", "calm", "تمرین منظم ذهن‌آگاهی اضطراب و استرس را کم می‌کند (NCCIH)."),
        Preset("kindact", "یک کار خوب برای کسی", "manners", "مهربانی کوچک روزانه، هم حال دیگران را بهتر می‌کند هم حال خودت را."),
        Preset("listen", "یک گفت‌وگو با گوش دادن کامل", "talk", "گوش دادن فعال، پایه رابطه‌های بهتر است (Rogers)."),
        Preset("selfkind", "یک جمله مهربان به خودت", "esteem", "مهربانی با خود به جای سرزنش، با سلامت روان بهتر همراه است (Neff)."),
        Preset("family", "تماس با پدر، مادر یا خواهر و برادر", "friends", "پیوند خانوادگی منظم، از منابع اصلی حمایت در سختی‌هاست."),
        Preset(
            "kidtime", "۱۵ دقیقه بازی بی‌گوشی با بچه", "father",
            "وقت کامل و بی‌حواس‌پرتی با پدر، به رشد عاطفی کودک کمک می‌کند (AAP).", GentlemanGuide.Audience.MARRIED
        ),
        Preset(
            "partnertalk", "۲۰ دقیقه گفت‌وگوی بدون گوشی با همسر", "marriage",
            "گفت‌وگوی روزانه درباره روزِ هم، از توصیه‌های گاتمن.", GentlemanGuide.Audience.MARRIED
        ),
        Preset(
            "appreciate", "یک قدردانی مشخص از همسر", "marriage",
            "تحسین و قدردانی، پادزهر تحقیر در رابطه است (گاتمن).", GentlemanGuide.Audience.MARRIED
        ),
        Preset(
            "newperson", "یک گفت‌وگوی کوتاه با یک آدم تازه", "presence",
            "تمرین تدریجی، اضطراب اجتماعی را کم و اعتمادبه‌نفس را بیشتر می‌کند.", GentlemanGuide.Audience.SINGLE
        )
    )

    fun presetsFor(status: Status): List<Preset> = PRESETS.filter { it.fits(status) }

    fun preset(id: String): Preset? = PRESETS.firstOrNull { it.id == id }

    /** The habits a new user starts with, so the growth screen is never empty. */
    fun starterIds(status: Status): List<String> =
        if (status == Status.MARRIED) listOf("sleep7", "move30", "partnertalk") else listOf("sleep7", "move30", "gratitude")

    /** Share of the last [days] days (including today) that were done, 0..100. */
    fun completionPercent(done: Set<Long>, today: LocalDate, days: Int = 7): Int {
        if (days <= 0) return 0
        val end = today.toEpochDay()
        val count = (0 until days).count { (end - it) in done }
        return count * 100 / days
    }

    /** The longest run of consecutive done days ever. */
    fun bestStreak(done: Set<Long>): Int {
        var best = 0
        for (day in done) {
            if ((day - 1) in done) continue
            var length = 1
            while ((day + length) in done) length++
            if (length > best) best = length
        }
        return best
    }

    /** Done/not-done for each of the last [days] days, oldest first (for the week strip). */
    fun lastDays(done: Set<Long>, today: LocalDate, days: Int = 7): List<Boolean> {
        val end = today.toEpochDay()
        return (days - 1 downTo 0).map { (end - it) in done }
    }

    const val MAX_TITLE = 60
    const val MAX_HABITS = 12
}
