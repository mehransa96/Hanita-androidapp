package com.example.data.repository

import com.example.R
import com.example.data.model.CycleMusic
import com.example.data.model.CyclePhaseType
import com.example.data.model.MusicIntent

object CycleMusicRepository {

    val allTracks: List<CycleMusic> = listOf(
        // ================= PERIOD / MENSTRUAL PHASE =================
        CycleMusic(
            id = "period_piano_calm",
            title = "پیانو مینیمال تسکین درد",
            subtitle = "پیانوی آرام فا ماژور با پد گرم",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 64,
            resId = R.raw.period_piano_calm,
            categoryBadge = "تسکین درد لگن",
            benefitDescription = "آرپژهای نرم پیانو با تمپوی ۶۰ (هم‌آهنگ با تنفس آرام) برای استراحت در روزهای قاعدگی؛ همراه خوبی برای کیسه آب گرم و دراز کشیدن.",
            frequencyLabel = "پیانو F-maj7",
            intent = MusicIntent.PAIN_RELIEF,
            bpm = 60,
            howToListen = "دراز بکشید و کیسه آب گرم را روی شکم بگذارید؛ با هر آرپژ بازدم را کمی طولانی‌تر کنید."
        ),
        CycleMusic(
            id = "period_rain_nature",
            title = "طبیعت آرام و باران شبانه",
            subtitle = "باران ملایم روی برگ‌ها با پد دوردست",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 64,
            resId = R.raw.period_rain_nature,
            categoryBadge = "صدای باران و دریا",
            benefitDescription = "صدای یکنواخت باران به بسیاری از افراد کمک می‌کند ذهن را از درد و فکرهای مزاحم دور کنند و راحت‌تر به خواب بروند.",
            frequencyLabel = "نویز طبیعی آرامش",
            intent = MusicIntent.SLEEP,
            bpm = null,
            howToListen = "در رختخواب با صدای کم؛ حالت تکرار را روشن بگذارید تا تا زمان خواب ادامه پیدا کند."
        ),
        CycleMusic(
            id = "period_frequency_528",
            title = "فرکانس معجزه‌آسا ۵۲۸ هرتز",
            subtitle = "تُن ۵۲۸ هرتز با ضربان دوگوش ۴ هرتز",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 64,
            resId = R.raw.period_frequency_528,
            categoryBadge = "فرکانس ۵۲۸Hz سلفژ",
            benefitDescription = "تُن آرام ۵۲۸ هرتز روی درون آکورد دو، با اختلاف ۴ هرتز بین گوش چپ و راست (با هدفون) و کاسه‌های آوازی؛ برای تن‌آرامی و استراحت عمیق.",
            frequencyLabel = "۵۲۸ Hz • دوگوش ۴ Hz",
            intent = MusicIntent.CALM,
            bpm = null,
            howToListen = "با هدفون و صدای کم، چشم‌ها بسته؛ ۱۰ دقیقه تکرار کافی است.",
            needsHeadphones = true
        ),
        CycleMusic(
            id = "period_ambient_rest",
            title = "نوای خواب عمیق و استراحت",
            subtitle = "پد صوتی گرم برای استراحت مطلق",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 64,
            resId = R.raw.period_ambient_rest,
            categoryBadge = "خواب و استراحت عمیق",
            benefitDescription = "پدهای امبینت آهسته در می‌مینور با نت‌های شیشه‌ای دوردست؛ برای چرت کوتاه و استراحت جسمی در روزهای پرخستگی.",
            frequencyLabel = "امبینت گرم E-minor",
            intent = MusicIntent.SLEEP,
            bpm = null,
            howToListen = "برای چرت کوتاه بعدازظهر یا قبل از خواب شب."
        ),
        CycleMusic(
            id = "period_harp_lullaby",
            title = "لالایی چنگ و کیسه آب گرم",
            subtitle = "چنگ و پد گرم در ر ماژور، میزان سه‌ضربی",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 53,
            resId = R.raw.period_harp_lullaby,
            categoryBadge = "لالایی خواب",
            benefitDescription = "ریتم سه‌ضربی آهسته (۵۴ ضرب) و ملودی ساده لالایی، ذهن را از درد دور می‌کند و برای به خواب رفتن در شب‌های اول پریود ساخته شده است.",
            frequencyLabel = "چنگ ۵۴ BPM • ۳/۴",
            intent = MusicIntent.SLEEP,
            bpm = 54,
            howToListen = "در رختخواب، چراغ‌ها خاموش و صدا کم؛ حالت تکرار روشن."
        ),
        CycleMusic(
            id = "period_cello_comfort",
            title = "آغوش ویولنسل",
            subtitle = "ملودی آرام ویولنسل روی پیانو و سازهای زهی در سل مینور",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 64,
            resId = R.raw.period_cello_comfort,
            categoryBadge = "همدلی و آرامش احساسی",
            benefitDescription = "برای روزهایی که بدن خسته و دل نازک است؛ ملودی بلند ویولنسل اجازه می‌دهد احساسات بدون قضاوت جاری شوند و آرام بگیرند.",
            frequencyLabel = "ویولنسل ۵۶ BPM",
            intent = MusicIntent.CALM,
            bpm = 56,
            howToListen = "نشسته یا دراز کشیده، همراه نوشتن چند خط در دفترچه احساسات."
        ),
        CycleMusic(
            id = "period_brown_noise_sleep",
            title = "نویز قهوه‌ای خواب عمیق",
            subtitle = "نویز بم و گرم با پد بسیار ملایم",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 72,
            resId = R.raw.period_brown_noise_sleep,
            categoryBadge = "خواب و پوشش صدا",
            benefitDescription = "نویز قهوه‌ای از نویز سفید بم‌تر و نرم‌تر است و صداهای مزاحم محیط را می‌پوشاند؛ برای خواب پیوسته‌تر در شب‌هایی که درد یا گرفتگی بیدارتان می‌کند.",
            frequencyLabel = "نویز قهوه‌ای",
            intent = MusicIntent.SLEEP,
            bpm = null,
            howToListen = "صدا را کم کنید (هم‌اندازه صدای باران آرام) و گوشی را کمی دورتر از سر بگذارید."
        ),
        CycleMusic(
            id = "period_belly_breath",
            title = "تنفس شکمی ضد گرفتگی",
            subtitle = "دم ۵٫۵ ثانیه، بازدم ۵٫۵ ثانیه با صدای راهنما",
            phase = CyclePhaseType.PERIOD,
            durationSeconds = 77,
            resId = R.raw.period_belly_breath,
            categoryBadge = "تنفس دیافراگمی",
            benefitDescription = "تنفس آهسته حدود ۵٫۵ بار در دقیقه (تنفس هم‌آهنگ) به فعال شدن سیستم آرام‌سازی بدن کمک می‌کند و همراه گرما می‌تواند شدت احساس گرفتگی را کمتر کند. صدای راهنما با دم بالا می‌رود و با بازدم پایین می‌آید.",
            frequencyLabel = "تنفس ۵٫۵ در دقیقه",
            intent = MusicIntent.BREATHING,
            bpm = null,
            howToListen = "یک دست روی شکم؛ با بالا رفتن صدا شکم را پر کنید و با پایین آمدنش آرام خالی کنید. ۷ دور."
        ),

        // ================= FOLLICULAR PHASE =================
        CycleMusic(
            id = "follicular_morning_joy",
            title = "طراوت صبحگاهی و آواز پرندگان",
            subtitle = "گیتار فینگراستایل سل ماژور و آواز پرندگان",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 60,
            resId = R.raw.follicular_morning_joy,
            categoryBadge = "انرژی صبحگاهی",
            benefitDescription = "گیتار آکوستیک ۹۶ BPM با آواز پرندگان صبح؛ همراه انرژی رو به افزایش روزهای بعد از قاعدگی برای شروع یک روز سرحال.",
            frequencyLabel = "گام شاداب G-Major",
            intent = MusicIntent.MOVEMENT,
            bpm = 96,
            howToListen = "صبح، هنگام آماده شدن یا پیاده‌روی سبک بیرون از خانه."
        ),
        CycleMusic(
            id = "follicular_acoustic_start",
            title = "شروع دوباره و پویایی ذهن",
            subtitle = "گیتار ریتم، فلوت و پرکاشن سبک",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 57,
            resId = R.raw.follicular_acoustic_start,
            categoryBadge = "انگیزه شروع دوباره",
            benefitDescription = "استروم گیتار ر ماژور ۱۰۰ BPM با شیکر و فلوت؛ مناسب برنامه‌ریزی هفتگی، پیاده‌روی و ورزش سبک.",
            frequencyLabel = "آکوستیک D-Major",
            intent = MusicIntent.MOVEMENT,
            bpm = 100,
            howToListen = "همراه پیاده‌روی تند (حدود ۱۰۰ قدم در دقیقه) یا برنامه‌ریزی هفته."
        ),
        CycleMusic(
            id = "follicular_positive_vibes",
            title = "ارتعاش مثبت و تمرکز خلاق",
            subtitle = "نوای کالیمبا و زنگ‌های پنتاتونیک",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 60,
            resId = R.raw.follicular_positive_vibes,
            categoryBadge = "خلاقیت و انگیزه",
            benefitDescription = "ملودی کالیمبا در گام پنتاتونیک دو با پد نرم؛ برای کار خلاقانه، نوشتن و تمرکز آرام.",
            frequencyLabel = "پنتاتونیک کالیمبا",
            intent = MusicIntent.FOCUS,
            bpm = 80,
            howToListen = "پس‌زمینه کار خلاقانه، نوشتن یا مطالعه با صدای کم."
        ),
        CycleMusic(
            id = "follicular_gentle_breeze",
            title = "نسیم ملایم بهاری",
            subtitle = "فلوت لطیف و حس امیدواری و رهایی",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 60,
            resId = R.raw.follicular_gentle_breeze,
            categoryBadge = "طراوت و آرامش درون",
            benefitDescription = "ملودی فلوت روی سازهای زهی و نسیم ملایم در فا ماژور؛ حس سبکی و امید روزهای فاز فولیکولار.",
            frequencyLabel = "فلوت و نسیم بهاری",
            intent = MusicIntent.CALM,
            bpm = 72,
            howToListen = "استراحت میان روز با یک لیوان آب و چند نفس عمیق."
        ),
        CycleMusic(
            id = "follicular_focus_piano",
            title = "پیانوی تمرکز عمیق",
            subtitle = "اوستیناتوی مینیمال پیانو در لا مینور",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 65,
            resId = R.raw.follicular_focus_piano,
            categoryBadge = "مطالعه و کار عمیق",
            benefitDescription = "الگوی تکرارشونده و بدون کلام، حواس‌پرتی کمتری نسبت به آهنگ‌های با کلام دارد؛ برای ساعت‌های تمرکز بالای فاز فولیکولار.",
            frequencyLabel = "پیانو ۸۸ BPM",
            intent = MusicIntent.FOCUS,
            bpm = 88,
            howToListen = "با صدای کم پشت کار؛ ۲۵ دقیقه کار و ۵ دقیقه استراحت (روش پومودورو)."
        ),
        CycleMusic(
            id = "follicular_walk_groove",
            title = "گروو پیاده‌روی صبح",
            subtitle = "یوکللی، دست‌زدن و سوت شاد ۱۱۲ ضرب",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 64,
            resId = R.raw.follicular_walk_groove,
            categoryBadge = "پیاده‌روی تند",
            benefitDescription = "تمپوی ۱۱۲ ضرب با گام‌های پیاده‌روی تند هماهنگ است و کمک می‌کند ۳۰ دقیقه فعالیت متوسط روزانه را با حال بهتر کامل کنید.",
            frequencyLabel = "یوکللی ۱۱۲ BPM",
            intent = MusicIntent.MOVEMENT,
            bpm = 112,
            howToListen = "قدم‌ها را با ضرب هماهنگ کنید؛ حالت تکرار برای پیاده‌روی طولانی."
        ),
        CycleMusic(
            id = "follicular_sunrise_strings",
            title = "طلوع و انگیزه",
            subtitle = "سازهای زهی و پیانو با اوج تدریجی",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 63,
            resId = R.raw.follicular_sunrise_strings,
            categoryBadge = "انگیزه و هدف‌گذاری",
            benefitDescription = "قطعه از آرام شروع می‌شود و کم‌کم اوج می‌گیرد؛ مناسب لحظه‌ای که برنامه هفته یا هدف تازه‌ای را می‌نویسید.",
            frequencyLabel = "ارکستری ۷۶ BPM",
            intent = MusicIntent.CONFIDENCE,
            bpm = 76,
            howToListen = "صبح، هنگام نوشتن سه هدف کوچک امروز."
        ),
        CycleMusic(
            id = "follicular_yoga_flow",
            title = "جریان یوگا با هنگ‌درام",
            subtitle = "هنگ‌درام در گام کرد ر با درون ملایم",
            phase = CyclePhaseType.FOLLICULAR,
            durationSeconds = 62,
            resId = R.raw.follicular_yoga_flow,
            categoryBadge = "یوگا و کشش",
            benefitDescription = "ضرب آرام ۷۰ و صدای گرد هنگ‌درام برای حرکات کششی، یوگا و پیلاتس سبک؛ هر دو ضرب را یک دم یا بازدم در نظر بگیرید.",
            frequencyLabel = "هنگ‌درام ۷۰ BPM",
            intent = MusicIntent.MOVEMENT,
            bpm = 70,
            howToListen = "روی مت، با هر عبارت موسیقی یک حرکت؛ حالت تکرار روشن."
        ),

        // ================= OVULATION PHASE =================
        CycleMusic(
            id = "ovulation_high_energy",
            title = "اوج انرژی و ریتم سرزندگی",
            subtitle = "الکتروپاپ ۱۲۰ BPM با بیس و سینت پرانرژی",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 64,
            resId = R.raw.ovulation_high_energy,
            categoryBadge = "اوج انرژی و دنس",
            benefitDescription = "ریتم چهارضرب ۱۲۰ BPM (تمپوی رایج دویدن و ایروبیک) برای روزهایی که انرژی‌تان در اوج است؛ ورزش، رقص و کارهای پرتحرک.",
            frequencyLabel = "۱۲۰ BPM الکتروپاپ",
            intent = MusicIntent.MOVEMENT,
            bpm = 120,
            howToListen = "دویدن سبک، ایروبیک یا دوچرخه؛ ۱۲۰ ضرب با ریتم قدم‌های تند هماهنگ است."
        ),
        CycleMusic(
            id = "ovulation_empowered_beat",
            title = "قدرت درون و اعتماد‌به‌نفس",
            subtitle = "فانک ۱۰۰ BPM با بیس گرووی و پیانوی الکتریک",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 62,
            resId = R.raw.ovulation_empowered_beat,
            categoryBadge = "اعتماد‌به‌نفس و قدرت",
            benefitDescription = "گروو فانک در می‌مینور با ریف برنجی؛ برای آماده شدن، جلسه‌ها و روزهایی که می‌خواهید با اعتمادبه‌نفس حاضر شوید.",
            frequencyLabel = "بیس فانک محرک",
            intent = MusicIntent.CONFIDENCE,
            bpm = 100,
            howToListen = "قبل از جلسه، مصاحبه یا هر کاری که می‌خواهید با اعتمادبه‌نفس انجامش دهید."
        ),
        CycleMusic(
            id = "ovulation_pure_joy",
            title = "جشن سرزندگی و اندورفین",
            subtitle = "آرپژ درخشان می ماژور ۱۲۰ BPM",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 64,
            resId = R.raw.ovulation_pure_joy,
            categoryBadge = "ترشح اندورفین شادی",
            benefitDescription = "آرپژهای براق، پد و ملودی زنگوله‌ای در می ماژور؛ برای حال خوب و انرژی مثبت روزهای تخمک‌گذاری.",
            frequencyLabel = "آرپژ سرزنده E-Major",
            intent = MusicIntent.JOY,
            bpm = 120,
            howToListen = "رقص در خانه یا کارهای روزمره با حال خوب."
        ),
        CycleMusic(
            id = "ovulation_sunshine_groove",
            title = "رقص نور و آفتاب درخشان",
            subtitle = "گروو تابستانی ۱۱۰ BPM با ماریمبا و استیل‌درام",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 69,
            resId = R.raw.ovulation_sunshine_groove,
            categoryBadge = "نشاط و شادابی",
            benefitDescription = "ماریمبا، استیل‌درام و شیکر در دو ماژور؛ حس آفتابی و اجتماعی برای وقت گذراندن با دوستان و همسر.",
            frequencyLabel = "ریتم شاد تابستانه",
            intent = MusicIntent.JOY,
            bpm = 110,
            howToListen = "وقت گذراندن با دوستان و همسر، آشپزی یا رانندگی."
        ),
        CycleMusic(
            id = "ovulation_hiit_128",
            title = "تمرین اینتروال ۱۲۸",
            subtitle = "هاوس پرانرژی ۱۲۸ ضرب با بیلدآپ",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 64,
            resId = R.raw.ovulation_hiit_128,
            categoryBadge = "HIIT و تمرین شدید",
            benefitDescription = "۱۲۸ ضرب در دقیقه، تمپوی رایج کلاس‌های ایروبیک و اینتروال است؛ بیلدآپ‌ها شروع هر ست تمرین را مشخص می‌کنند.",
            frequencyLabel = "هاوس ۱۲۸ BPM",
            intent = MusicIntent.MOVEMENT,
            bpm = 128,
            howToListen = "بعد از گرم کردن؛ با هر بیلدآپ ۲۰ ثانیه تمرین سنگین و بعد استراحت فعال."
        ),
        CycleMusic(
            id = "ovulation_persian_68",
            title = "شش‌وهشت شادی",
            subtitle = "دف، تنبک و سنتور در حال‌وهوای دستگاه شور",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 65,
            resId = R.raw.ovulation_persian_68,
            categoryBadge = "رقص و شادی ایرانی",
            benefitDescription = "ریتم شش‌وهشت با دف و سنتور در فواصل ربع‌پرده‌ای شور؛ برای رقص، دورهمی و روزهایی که انرژی اجتماعی‌تان در اوج است.",
            frequencyLabel = "شش‌وهشت • دستگاه شور",
            intent = MusicIntent.JOY,
            bpm = 100,
            howToListen = "با صدای بلندتر برای رقص یا دورهمی؛ روی بلندگو بهتر است."
        ),
        CycleMusic(
            id = "ovulation_confidence_anthem",
            title = "سرود قدرت درون",
            subtitle = "پیانو، درام بزرگ و زهی‌های حماسی ۹۲ ضرب",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 63,
            resId = R.raw.ovulation_confidence_anthem,
            categoryBadge = "اعتمادبه‌نفس",
            benefitDescription = "ضرب‌های سنگین و ملودی بالارونده حس قدرت و آمادگی می‌دهد؛ برای قبل از ارائه، مصاحبه یا تمرین قدرتی.",
            frequencyLabel = "پاپ حماسی ۹۲ BPM",
            intent = MusicIntent.CONFIDENCE,
            bpm = 92,
            howToListen = "دو دقیقه قبل از کار مهم، با ایستادن صاف و نفس عمیق."
        ),
        CycleMusic(
            id = "ovulation_social_bossa",
            title = "بوسانوای عصر دونفره",
            subtitle = "گیتار بوسانوا، فلوت و ریم‌شات نرم",
            phase = CyclePhaseType.OVULATION,
            durationSeconds = 73,
            resId = R.raw.ovulation_social_bossa,
            categoryBadge = "معاشرت و حال خوب",
            benefitDescription = "هارمونی جازی و ریتم نرم بوسانوا فضای گرم و صمیمی برای شام دونفره، گفت‌وگو و وقت گذراندن با دوستان می‌سازد.",
            frequencyLabel = "بوسانوا ۱۳۲ BPM",
            intent = MusicIntent.JOY,
            bpm = 132,
            howToListen = "پس‌زمینه شام یا گفت‌وگو با صدای کم."
        ),

        // ================= LUTEAL PHASE & PMS =================
        CycleMusic(
            id = "luteal_lofi_chill",
            title = "بیت‌های لوفای تسکین PMS",
            subtitle = "پیانو الکتریک گرم و ریتم دنج آرامش",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 66,
            resId = R.raw.luteal_lofi_chill,
            categoryBadge = "ضد اضطراب PMS",
            benefitDescription = "آکوردهای جاز پیانوی الکتریک ۷۲ BPM با درام سوینگ و خش‌خش صفحه گرامافون؛ برای آرام‌تر شدن حال‌وهوای روزهای قبل از پریود.",
            frequencyLabel = "Lo-Fi ۷۰ BPM",
            intent = MusicIntent.CALM,
            bpm = 72,
            howToListen = "عصرها هنگام مطالعه سبک یا استراحت؛ صدای کم تا متوسط."
        ),
        CycleMusic(
            id = "luteal_frequency_432",
            title = "فرکانس ۴۳۲ هرتز تعادل اعصاب",
            subtitle = "کوک ۴۳۲ هرتز با ضربان دوگوش ۶ هرتز",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 64,
            resId = R.raw.luteal_frequency_432,
            categoryBadge = "فرکانس آرامش ۴۳۲Hz",
            benefitDescription = "پد و کاسه آوازی با کوک A=432 در لا مینور و اختلاف ملایم ۶ هرتز بین دو گوش (با هدفون)؛ برای کم کردن تنش عصرگاهی.",
            frequencyLabel = "A=432 • دوگوش ۶ Hz",
            intent = MusicIntent.CALM,
            bpm = null,
            howToListen = "با هدفون و صدای کم، نشسته یا دراز کشیده؛ ۱۰ تا ۱۵ دقیقه.",
            needsHeadphones = true
        ),
        CycleMusic(
            id = "luteal_warm_acoustic",
            title = "آوای گرم سازهای زهی",
            subtitle = "ویولنسل، سازهای زهی و گیتار در سی‌بمل ماژور",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 64,
            resId = R.raw.luteal_warm_acoustic,
            categoryBadge = "تسکین تنش و استرس",
            benefitDescription = "مانند یک پتوی نرم و نوشیدنی گرم گیاهی، حس گرما و آرامش در روزهای خسته اواخر چرخه می‌دهد.",
            frequencyLabel = "هارمونی زهی لگاتو",
            intent = MusicIntent.CALM,
            bpm = 60,
            howToListen = "کنار یک نوشیدنی گرم بدون کافئین، شب‌های پرتنش آخر چرخه."
        ),
        CycleMusic(
            id = "luteal_pink_noise_zen",
            title = "نویز صورتی و الگوی تنفس ۴-۷-۸",
            subtitle = "دم ۴ ثانیه، حبس ۷ ثانیه، بازدم ۸ ثانیه",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 76,
            resId = R.raw.luteal_pink_noise_zen,
            categoryBadge = "تنفس ضد اضطراب",
            benefitDescription = "نویز صورتی با دم بالا می‌آید، در حبس ثابت می‌ماند و با بازدم آرام می‌شود؛ زنگ‌های نرم شروع هر مرحله را اعلام می‌کنند. برای کاهش تنش و خواب راحت‌تر.",
            frequencyLabel = "نویز صورتی + تنفس ۴-۷-۸",
            intent = MusicIntent.BREATHING,
            bpm = null,
            howToListen = "با صدای زنگ اول دم، با زنگ دوم حبس و با زنگ سوم بازدم؛ ۴ دور پشت سر هم."
        ),
        CycleMusic(
            id = "luteal_rain_piano",
            title = "پیانو و باران عصرگاهی",
            subtitle = "پیانوی آرام دو ماژور با باران نرم",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 60,
            resId = R.raw.luteal_rain_piano,
            categoryBadge = "آرام شدن قبل از خواب",
            benefitDescription = "ترکیب پیانوی آهسته و صدای باران، ذهن پرمشغله شب‌های پیش از پریود را آرام می‌کند و روال خواب منظم را آسان‌تر می‌سازد.",
            frequencyLabel = "پیانو ۶۴ BPM + باران",
            intent = MusicIntent.SLEEP,
            bpm = 64,
            howToListen = "یک ساعت قبل از خواب، نور کم و بدون صفحه نمایش."
        ),
        CycleMusic(
            id = "luteal_box_breathing",
            title = "تنفس جعبه‌ای ۴-۴-۴-۴",
            subtitle = "دم ۴، مکث ۴، بازدم ۴، مکث ۴ با زنگ راهنما",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 80,
            resId = R.raw.luteal_box_breathing,
            categoryBadge = "ضد اضطراب و تحریک‌پذیری",
            benefitDescription = "تنفس جعبه‌ای روشی ساده و پرکاربرد برای کنترل اضطراب و تنش ناگهانی است؛ چهار زنگ متفاوت شروع هر مرحله را اعلام می‌کنند.",
            frequencyLabel = "تنفس جعبه‌ای",
            intent = MusicIntent.BREATHING,
            bpm = null,
            howToListen = "با زنگ اول دم، زنگ دوم مکث، زنگ سوم بازدم و زنگ چهارم مکث. ۵ دور."
        ),
        CycleMusic(
            id = "luteal_ney_reflection",
            title = "نی و تأمل",
            subtitle = "بداهه نی در حال‌وهوای همایون روی درون",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 78,
            resId = R.raw.luteal_ney_reflection,
            categoryBadge = "تأمل و سبکی ذهن",
            benefitDescription = "صدای نفس‌دار نی و درون ثابت، فضایی برای مکث، نوشتن احساسات و رها کردن تنش‌های روز می‌سازد.",
            frequencyLabel = "نی • دستگاه همایون",
            intent = MusicIntent.CALM,
            bpm = null,
            howToListen = "نشسته، چشم‌ها بسته یا همراه نوشتن در دفترچه."
        ),
        CycleMusic(
            id = "luteal_night_crickets",
            title = "شب تابستان و جیرجیرک",
            subtitle = "جیرجیرک‌ها، نسیم شب و پد آرام",
            phase = CyclePhaseType.LUTEAL,
            durationSeconds = 72,
            resId = R.raw.luteal_night_crickets,
            categoryBadge = "خواب شبانه",
            benefitDescription = "صداهای طبیعی و یکنواخت شب، جایگزین افکار تکراری قبل از خواب می‌شود؛ مخصوص بی‌خوابی‌های فاز لوتئال.",
            frequencyLabel = "آوای شب",
            intent = MusicIntent.SLEEP,
            bpm = null,
            howToListen = "در رختخواب با صدای کم؛ حالت تکرار روشن."
        )
    )

    fun getTracksForPhase(phase: CyclePhaseType): List<CycleMusic> {
        return allTracks.filter { it.phase == phase }
    }

    fun getTrackById(id: String): CycleMusic? {
        return allTracks.find { it.id == id }
    }

    /** Tracks for an optional phase and optional intention, in library order. */
    fun filter(phase: CyclePhaseType?, intent: MusicIntent?): List<CycleMusic> =
        allTracks.filter { (phase == null || it.phase == phase) && (intent == null || it.intent == intent) }

    /** Intentions that have at least one track for the phase (all phases when null), in enum order. */
    fun intentsFor(phase: CyclePhaseType?): List<MusicIntent> =
        MusicIntent.values().filter { i -> allTracks.any { it.intent == i && (phase == null || it.phase == phase) } }
}
