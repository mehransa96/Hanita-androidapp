package com.example.data.repository

import com.example.R
import com.example.data.model.CycleMusic
import com.example.data.model.CyclePhaseType

object CycleMusicRepository {

    val allTracks: List<CycleMusic> = listOf(
        // ================= PERIOD / MENSTRUAL PHASE =================
        CycleMusic(
            id = "period_piano_calm",
            title = "پیانو مینیمال تسکین درد",
            subtitle = "نوای ملایم کاهش اسپاسم رحمی",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 24,
            resId = R.raw.period_piano_calm,
            categoryBadge = "تسکین درد لگن",
            benefitDescription = "نوای پیانو با هارمونی‌های عمیق F-maj7 حس آرامش ایجاد می‌کند و می‌تواند تحمل گرفتگی‌ها را آسان‌تر کند.",
            frequencyLabel = "پیانو F-maj7"
        ),
        CycleMusic(
            id = "period_rain_nature",
            title = "طبیعت آرام و باران شبانه",
            subtitle = "آوای یکنواخت باران و امواج دریا",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 24,
            resId = R.raw.period_rain_nature,
            categoryBadge = "صدای باران و دریا",
            benefitDescription = "صدای باران ملایم و امواج دریا ذهن را از کانون درد منحرف کرده و امواج آلفای مغزی را فعال می‌سازد.",
            frequencyLabel = "نویز طبیعی آرامش"
        ),
        CycleMusic(
            id = "period_frequency_528",
            title = "فرکانس معجزه‌آسا ۵۲۸ هرتز",
            subtitle = "ارتعاش ترمیم سلولی و تعادل انرژی",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 24,
            resId = R.raw.period_frequency_528,
            categoryBadge = "فرکانس ۵۲۸Hz سلفژ",
            benefitDescription = "فرکانس کهن ۵۲۸ هرتز همراه با ضربان تتای ۴ هرتز برای تن‌آرامی و استراحت عمیق.",
            frequencyLabel = "۵۲۸ Hz + ۴ Hz Theta"
        ),
        CycleMusic(
            id = "period_ambient_rest",
            title = "نوای خواب عمیق و استراحت",
            subtitle = "پد صوتی گرم برای استراحت مطلق",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 24,
            resId = R.raw.period_ambient_rest,
            categoryBadge = "خواب و استراحت عمیق",
            benefitDescription = "نوای گرم بم برای رفع خستگی ناشی از افت آهن و ایجاد آرامش جسمی و روحی در طول استراحت روزانه.",
            frequencyLabel = "امبینت گرم E-minor"
        ),

        // ================= FOLLICULAR PHASE =================
        CycleMusic(
            id = "follicular_morning_joy",
            title = "طراوت صبحگاهی و آواز پرندگان",
            subtitle = "آوای نشاط‌آور گیتار و نور خورشید",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 24,
            resId = R.raw.follicular_morning_joy,
            categoryBadge = "انرژی صبحگاهی",
            benefitDescription = "همگام با بالا رفتن سطح استروژن، این ملودی شاداب انگیزه بیداری و نشاط روزانه را افزایش می‌دهد.",
            frequencyLabel = "گام شاداب G-Major"
        ),
        CycleMusic(
            id = "follicular_acoustic_start",
            title = "شروع دوباره و پویایی ذهن",
            subtitle = "ریتم آکوستیک برای اهداف نو و برنامه‌ریزی",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 24,
            resId = R.raw.follicular_acoustic_start,
            categoryBadge = "انگیزه شروع دوباره",
            benefitDescription = "ریتم زنده و الهام‌بخش برای برنامه‌ریزی هفتگی، ورزش سبک و احیای انرژی‌های نو در بدن.",
            frequencyLabel = "آکوستیک D-Major"
        ),
        CycleMusic(
            id = "follicular_positive_vibes",
            title = "ارتعاش مثبت و تمرکز خلاق",
            subtitle = "نوای کالیمبا و زنگ‌های پنتاتونیک",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 24,
            resId = R.raw.follicular_positive_vibes,
            categoryBadge = "خلاقیت و انگیزه",
            benefitDescription = "تقویت شفافیت و تمرکز ذهنی، جریان ایده‌پردازی و پاکسازی کرختی روزهای قاعدگی.",
            frequencyLabel = "پنتاتونیک کالیمبا"
        ),
        CycleMusic(
            id = "follicular_gentle_breeze",
            title = "نسیم ملایم بهاری",
            subtitle = "فلوت لطیف و حس امیدواری و رهایی",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 24,
            resId = R.raw.follicular_gentle_breeze,
            categoryBadge = "طراوت و آرامش درون",
            benefitDescription = "نوای لطیف فلوت و باد ملایم برای هماهنگی با روند رو به رشد هورمون‌های زنانه و حس سبکی.",
            frequencyLabel = "فلوت و نسیم بهاری"
        ),

        // ================= OVULATION PHASE =================
        CycleMusic(
            id = "ovulation_high_energy",
            title = "اوج انرژی و ریتم سرزندگی",
            subtitle = "بیت ۱۲۰ BPM برای تحرک و انگیزه بالا",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 24,
            resId = R.raw.ovulation_high_energy,
            categoryBadge = "اوج انرژی و دنس",
            benefitDescription = "در اوج ترشح استروژن و LH، این ریتم شاداب انرژی شما را برای ورزش، رقص و فعالیت آزاد می‌سازد.",
            frequencyLabel = "۱۲۰ BPM الکتروپاپ"
        ),
        CycleMusic(
            id = "ovulation_empowered_beat",
            title = "قدرت درون و اعتماد‌به‌نفس",
            subtitle = "بیس محرک و بیت قدرتمند مدرن",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 24,
            resId = R.raw.ovulation_empowered_beat,
            categoryBadge = "اعتماد‌به‌نفس و قدرت",
            benefitDescription = "تقویت حس جذابیت، قدرت حضور در اجتماع و احساس خودباوری در درخشان‌ترین روزهای چرخه.",
            frequencyLabel = "بیس فانک محرک"
        ),
        CycleMusic(
            id = "ovulation_pure_joy",
            title = "جشن سرزندگی و اندورفین",
            subtitle = "آرپژهای درخشان و پرتحرک ماژور",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 24,
            resId = R.raw.ovulation_pure_joy,
            categoryBadge = "ترشح اندورفین شادی",
            benefitDescription = "حس شادی و سرزندگی برای لذت بردن از لحظات روزمره.",
            frequencyLabel = "آرپژ سرزنده E-Major"
        ),
        CycleMusic(
            id = "ovulation_sunshine_groove",
            title = "رقص نور و آفتاب درخشان",
            subtitle = "ریتم هماهنگ و پرشور برای اوج چرخه",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 24,
            resId = R.raw.ovulation_sunshine_groove,
            categoryBadge = "نشاط و شادابی",
            benefitDescription = "حس هماهنگی کامل هورمونی، ارتباطات صمیمانه و درخشش درونی در دوره تخمک‌گذاری.",
            frequencyLabel = "ریتم شاد تابستانه"
        ),

        // ================= LUTEAL PHASE & PMS =================
        CycleMusic(
            id = "luteal_lofi_chill",
            title = "بیت‌های لوفای تسکین PMS",
            subtitle = "پیانو الکتریک گرم و ریتم دنج آرامش",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 24,
            resId = R.raw.luteal_lofi_chill,
            categoryBadge = "ضد اضطراب PMS",
            benefitDescription = "آکوردهای جاز لوفای به همراه صدای نوستالژیک گرامافون، به آرام‌تر شدن حال‌وهوای روزهای قبل از پریود کمک می‌کنند.",
            frequencyLabel = "Lo-Fi ۷۰ BPM"
        ),
        CycleMusic(
            id = "luteal_frequency_432",
            title = "فرکانس ۴۳۲ هرتز تعادل اعصاب",
            subtitle = "نوای هماهنگ کیهانی و کاهش کورتیزول",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 24,
            resId = R.raw.luteal_frequency_432,
            categoryBadge = "فرکانس آرامش ۴۳۲Hz",
            benefitDescription = "موسیقی آرام با کوک ۴۳۲ هرتز برای کم کردن تنش و آرامش ذهن.",
            frequencyLabel = "۴۳۲ Hz + ۶ Hz Alpha"
        ),
        CycleMusic(
            id = "luteal_warm_acoustic",
            title = "آوای گرم سازهای زهی",
            subtitle = "هارمونی محافظ ویولنسل برای رهایی از تنش",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 24,
            resId = R.raw.luteal_warm_acoustic,
            categoryBadge = "تسکین تنش و استرس",
            benefitDescription = "مانند یک پتوی نرم و نوشیدنی گرم گیاهی، حس گرما و آرامش در روزهای خسته اواخر چرخه می‌دهد.",
            frequencyLabel = "هارمونی زهی لگاتو"
        ),
        CycleMusic(
            id = "luteal_pink_noise_zen",
            title = "نویز صورتی و الگوی تنفس ۴-۷-۸",
            subtitle = "تثبیت تنفس آرام و کاهش تپش قلب",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 24,
            resId = R.raw.luteal_pink_noise_zen,
            categoryBadge = "تنفس ضد اضطراب",
            benefitDescription = "الگوی تنفس هدایت‌شده همراه با نویز صورتی برای کاهش تنش و کمک به خواب راحت‌تر.",
            frequencyLabel = "نویز صورتی + تنفس ۴-۷-۸"
        )
    )

    fun getTracksForPhase(phase: CyclePhaseType): List<CycleMusic> {
        return allTracks.filter { it.phase == phase }
    }

    fun getTrackById(id: String): CycleMusic? {
        return allTracks.find { it.id == id }
    }
}
