package com.hanita.core.gentleman

/**
 * Short, validated self-checks the user can repeat to see change over time. Every one is a
 * published instrument that is free to use (public domain or free for non-commercial use with
 * citation), scored exactly as its authors describe, with the cut-offs from its own literature:
 *
 *  - Mood:   WHO-5 well-being, PHQ-9 depression, GAD-7 anxiety, PSS-10 perceived stress.
 *  - Self:   Rosenberg self-esteem, Mini-IPIP Big Five personality, Brief Resilience Scale,
 *            General Self-Efficacy, Satisfaction With Life.
 *  - Bonds:  UCLA 3-item loneliness, Couples Satisfaction Index (CSI-4, married only).
 *
 * The Persian wording is the app's own faithful translation. Results are for self-awareness
 * only, never a diagnosis; screeners that point to a problem always point to a professional.
 */
object Assessments {

    enum class Group(val title: String, val subtitle: String) {
        MOOD("سلامت روان", "غربالگری کوتاه حال این روزها"),
        SELF("خودشناسی و شخصیت", "ویژگی‌ها، توانایی‌ها و رضایت از زندگی"),
        BOND("رابطه و پیوند", "تنهایی و رابطه با همسر")
    }

    enum class Kind(
        val code: String,
        val title: String,
        val subtitle: String,
        val sourceId: String,
        val group: Group,
        /** Roughly how long it takes, in minutes. */
        val minutes: Int,
        val audience: GentlemanGuide.Audience = GentlemanGuide.Audience.ALL
    ) {
        WELL_BEING("who5", "آزمون بهزیستی", "شاخص ۵ سؤالی سازمان جهانی بهداشت (WHO-5)", "who5", Group.MOOD, 1),
        DEPRESSION("phq9", "غربالگری افسردگی", "پرسشنامه سلامت بیمار (PHQ-9)", "phq9", Group.MOOD, 2),
        ANXIETY("gad7", "غربالگری اضطراب", "مقیاس اضطراب فراگیر (GAD-7)", "gad7", Group.MOOD, 2),
        STRESS("pss10", "آزمون استرس", "مقیاس استرس ادراک‌شده کوهن (PSS-10)", "pss", Group.MOOD, 3),
        SELF_ESTEEM("rses", "آزمون عزت نفس", "مقیاس ۱۰ سؤالی روزنبرگ", "rosenberg", Group.SELF, 2),
        PERSONALITY("ipip", "آزمون شخصیت پنج‌عاملی", "Mini-IPIP، ۲۰ سؤال درباره پنج ویژگی اصلی", "mini-ipip", Group.SELF, 4),
        RESILIENCE("brs", "آزمون تاب‌آوری", "مقیاس کوتاه تاب‌آوری (BRS)", "brs", Group.SELF, 1),
        SELF_EFFICACY("gse", "آزمون خودکارآمدی", "مقیاس عمومی خودکارآمدی شوارتزر (GSE)", "gse", Group.SELF, 2),
        LIFE_SATISFACTION("swls", "رضایت از زندگی", "مقیاس رضایت از زندگی دینر (SWLS)", "swls", Group.SELF, 1),
        LONELINESS("ucla3", "آزمون تنهایی", "مقیاس سه‌سؤالی تنهایی UCLA", "ucla3", Group.BOND, 1),
        RELATIONSHIP(
            "csi4", "رضایت از رابطه زناشویی", "شاخص رضایت زوجین (CSI-4)", "csi4", Group.BOND, 1,
            GentlemanGuide.Audience.MARRIED
        );

        fun fits(status: Status): Boolean = when (audience) {
            GentlemanGuide.Audience.ALL -> true
            GentlemanGuide.Audience.SINGLE -> status == Status.SINGLE
            GentlemanGuide.Audience.MARRIED -> status == Status.MARRIED
        }

        companion object {
            fun fromCode(code: String?): Kind? = values().firstOrNull { it.code == code }
            fun forStatus(status: Status): List<Kind> = values().filter { it.fits(status) }
        }
    }

    /** One answer: what the user sees and the value the authors score it as. */
    data class Option(val label: String, val value: Int)

    /** The five factors of the Mini-IPIP (neuroticism is shown the other way round, as stability). */
    enum class Trait(val title: String, val high: String, val low: String, val tip: String) {
        EXTRAVERSION(
            "برون‌گرایی",
            "از جمع و گفت‌وگو انرژی می‌گیری، راحت شروع‌کننده ارتباطی و در گروه دیده می‌شوی.",
            "درون‌گراتری؛ تنهایی شارژت می‌کند و رابطه‌های کم ولی عمیق را ترجیح می‌دهی.",
            "درون‌گرا یا برون‌گرا بودن خوب یا بد نیست. اگر درون‌گرایی، بعد از جمع‌های شلوغ برای خودت وقت شارژ بگذار؛ اگر برون‌گرایی، تمرین کن بیشتر گوش بدهی."
        ),
        AGREEABLENESS(
            "مهربانی و همدلی",
            "همدل، مهربان و اهل همکاری هستی و احساس دیگران برایت مهم است.",
            "رک و مستقلی و راحت‌تر مخالفت می‌کنی؛ گاهی ممکن است سرد به نظر برسی.",
            "همدلی بالا اگر با «نه گفتن» همراه نباشد خسته‌ات می‌کند؛ همدلی پایین را با پرسیدن «حالت چطور است؟» و گوش دادن کامل تقویت کن."
        ),
        CONSCIENTIOUSNESS(
            "نظم و وجدان کاری",
            "منظم، قابل‌اعتماد و اهل برنامه‌ای و کارها را به موقع تمام می‌کنی.",
            "انعطاف‌پذیر و خودجوشی، ولی ممکن است کارها عقب بیفتد یا نظم را سخت نگه داری.",
            "وجدان کاری یکی از قوی‌ترین پیش‌بینی‌کننده‌های موفقیت شغلی است. برای تقویتش از برنامه «اگر… آنگاه…» و فهرست کارهای روزانه استفاده کن."
        ),
        STABILITY(
            "ثبات هیجانی",
            "معمولاً آرامی، زود از کوره در نمی‌روی و فشار را خوب تحمل می‌کنی.",
            "احساساتت قوی است و نگرانی یا ناراحتی زودتر سراغت می‌آید.",
            "اگر ثبات هیجانی‌ات پایین‌تر است، تنفس آرام، ورزش منظم، خواب کافی و نوشتن احساسات کمک بزرگی است؛ اگر آزارت می‌دهد، با یک مشاور حرف بزن."
        ),
        OPENNESS(
            "ذهن باز و خلاقیت",
            "کنجکاو و خلاقی و از ایده‌ها، هنر و یادگیری چیزهای تازه لذت می‌بری.",
            "عمل‌گرا و واقع‌بینی و روش‌های آزموده را به ایده‌های انتزاعی ترجیح می‌دهی.",
            "برای تقویت ذهن باز، هر ماه یک کتاب یا مهارت بیرون از حوزه همیشگی‌ات را امتحان کن."
        )
    }

    data class Question(
        val text: String,
        /** Scored the other way round (agreeing lowers the score). */
        val reversed: Boolean = false,
        /** For the personality test: the factor this item measures. */
        val trait: Trait? = null,
        /** Answers for this question only, when they differ from the scale's (CSI-4 item 1). */
        val options: List<Option>? = null
    )

    /** A score range and what it means; [min] is the lowest total that falls in this band. */
    data class Band(val min: Int, val level: Level, val title: String, val text: String, val seekHelp: Boolean = false)

    data class Scale(
        val kind: Kind,
        val intro: String,
        /** Answers in the order they are shown. */
        val options: List<Option>,
        val questions: List<Question>,
        /** Score bands; a score falls in the band with the highest [Band.min] it reaches. */
        val bands: List<Band> = emptyList(),
        /** Raw totals are multiplied by this (WHO-5 is reported as 0..100). */
        val multiplier: Int = 1,
        /** For scales where a high score means more symptoms (PHQ-9, GAD-7, PSS-10, UCLA). */
        val higherIsWorse: Boolean = false
    ) {
        fun optionsFor(question: Int): List<Option> = questions[question].options ?: options

        private fun range(question: Int): IntRange {
            val values = optionsFor(question).map { it.value }
            return values.min()..values.max()
        }

        val minScore: Int get() = questions.indices.sumOf { range(it).first } * multiplier
        val maxScore: Int get() = questions.indices.sumOf { range(it).last } * multiplier

        /** True for the personality test, whose result is a profile of traits, not one score. */
        val isProfile: Boolean get() = questions.any { it.trait != null }

        /** Value of the chosen option of [question], after reverse scoring. */
        internal fun scored(question: Int, optionIndex: Int): Int {
            val v = optionsFor(question)[optionIndex].value
            val r = range(question)
            return if (questions[question].reversed) r.first + r.last - v else v
        }

        fun valid(answers: List<Int>): Boolean =
            answers.size == questions.size && answers.withIndex().all { (i, a) -> a in optionsFor(i).indices }
    }

    /** HIGH is always the most favourable band and VERY_LOW the least, whatever the scale. */
    enum class Level { HIGH, MIDDLE, LOW, VERY_LOW }

    data class Result(val level: Level, val title: String, val text: String, val seekHelp: Boolean)

    data class TraitScore(val trait: Trait, val score: Int, val max: Int, val min: Int) {
        val percent: Int get() = (score - min) * 100 / (max - min)
        val text: String get() = when {
            percent >= 60 -> trait.high
            percent <= 40 -> trait.low
            else -> "در میانه‌ای؛ بسته به موقعیت، هر دو سوی این ویژگی را نشان می‌دهی."
        }
    }

    // ------------------------------------------------------------------ answer sets

    private fun options(vararg pairs: Pair<String, Int>) = pairs.map { Option(it.first, it.second) }

    private val AGREE4 = options("کاملاً موافقم" to 3, "موافقم" to 2, "مخالفم" to 1, "کاملاً مخالفم" to 0)
    private val WHO5_OPTIONS = options(
        "همیشه" to 5, "بیشتر اوقات" to 4, "بیش از نیمی از اوقات" to 3,
        "کمتر از نیمی از اوقات" to 2, "گاهی" to 1, "هرگز" to 0
    )
    private val PHQ_OPTIONS = options("اصلاً" to 0, "چند روز" to 1, "بیش از نیمی از روزها" to 2, "تقریباً هر روز" to 3)
    private val PSS_OPTIONS = options("هرگز" to 0, "تقریباً هرگز" to 1, "گاهی" to 2, "نسبتاً زیاد" to 3, "خیلی زیاد" to 4)
    private val TRUE5 = options(
        "کاملاً درباره‌ام درست است" to 5, "درباره‌ام درست است" to 4, "نه درست نه نادرست" to 3,
        "درباره‌ام نادرست است" to 2, "کاملاً نادرست است" to 1
    )
    private val AGREE5 = options("کاملاً موافقم" to 5, "موافقم" to 4, "نظری ندارم" to 3, "مخالفم" to 2, "کاملاً مخالفم" to 1)
    private val GSE_OPTIONS = options("کاملاً درست است" to 4, "نسبتاً درست است" to 3, "به‌سختی درست است" to 2, "اصلاً درست نیست" to 1)
    private val AGREE7 = options(
        "کاملاً موافقم" to 7, "موافقم" to 6, "کمی موافقم" to 5, "نه موافق نه مخالف" to 4,
        "کمی مخالفم" to 3, "مخالفم" to 2, "کاملاً مخالفم" to 1
    )
    private val UCLA_OPTIONS = options("اغلب" to 3, "گاهی" to 2, "به‌ندرت یا هرگز" to 1)
    private val CSI_OPTIONS = options("کاملاً" to 5, "خیلی زیاد" to 4, "تا حد زیادی" to 3, "تا حدی" to 2, "کمی" to 1, "اصلاً" to 0)
    private val CSI_HAPPINESS = options(
        "بی‌نقص" to 6, "فوق‌العاده خوشحال" to 5, "خیلی خوشحال" to 4, "خوشحال" to 3,
        "کمی ناراضی" to 2, "نسبتاً ناراضی" to 1, "خیلی ناراضی" to 0
    )

    // ------------------------------------------------------------------ mood

    val WELL_BEING = Scale(
        Kind.WELL_BEING,
        "در دو هفته گذشته، هر جمله چقدر درباره‌ات درست بوده است؟",
        WHO5_OPTIONS,
        listOf(
            Question("شاد و سرحال بوده‌ام."),
            Question("آرام و آسوده بوده‌ام."),
            Question("پرانرژی و فعال بوده‌ام."),
            Question("صبح‌ها سرحال و استراحت‌کرده بیدار شده‌ام."),
            Question("زندگی روزمره‌ام پر از چیزهایی بوده که برایم جالب‌اند.")
        ),
        listOf(
            Band(51, Level.HIGH, "بهزیستی خوب", "در دو هفته گذشته حالت روی‌هم‌رفته خوب بوده است. عادت‌هایی را که به این حال کمک کرده‌اند نگه دار."),
            Band(
                29, Level.LOW, "بهزیستی پایین",
                "حالت در دو هفته گذشته پایین‌تر از حد مطلوب بوده است. خواب، تحرک و گفت‌وگو با یک دوست را جدی بگیر، آزمون افسردگی (PHQ-9) را هم بزن و اگر ادامه داشت با یک مشاور صحبت کن.",
                seekHelp = true
            ),
            Band(0, Level.VERY_LOW, "بهزیستی خیلی پایین", "این نتیجه می‌تواند نشانه افسردگی باشد. لطفاً با یک روان‌شناس یا پزشک صحبت کن؛ کمک گرفتن نشانه قدرت است.", seekHelp = true)
        ),
        multiplier = 4
    )

    val DEPRESSION = Scale(
        Kind.DEPRESSION,
        "در دو هفته گذشته، هر کدام از این مشکلات چند وقت یک‌بار آزارت داده است؟",
        PHQ_OPTIONS,
        listOf(
            Question("کم‌علاقگی یا لذت نبردن از انجام کارها"),
            Question("احساس غم، افسردگی یا ناامیدی"),
            Question("مشکل در به خواب رفتن یا خواب ماندن، یا خوابیدن بیش از حد"),
            Question("احساس خستگی یا کم‌انرژی بودن"),
            Question("کم‌اشتهایی یا پرخوری"),
            Question("احساس بد درباره خودت؛ اینکه آدم شکست‌خورده‌ای هستی یا خودت یا خانواده‌ات را ناامید کرده‌ای"),
            Question("مشکل در تمرکز، مثلاً هنگام خواندن یا تماشای تلویزیون"),
            Question("آن‌قدر کند حرکت کردن یا حرف زدن که دیگران متوجه شوند، یا برعکس، آن‌قدر بی‌قرار بودن که بیشتر از معمول تکان بخوری"),
            Question("فکر اینکه بهتر است بمیری یا به نوعی به خودت آسیب بزنی")
        ),
        listOf(
            Band(0, Level.HIGH, "نشانه‌ای از افسردگی دیده نمی‌شود", "در دو هفته گذشته نشانه قابل‌توجهی نداشته‌ای. خواب، تحرک و رابطه‌های خوب را ادامه بده."),
            Band(5, Level.MIDDLE, "نشانه‌های خفیف", "چند نشانه خفیف داری. خواب منظم، ورزش، نور روز و حرف زدن با یک دوست را جدی بگیر و دو هفته دیگر دوباره آزمون را بزن."),
            Band(10, Level.LOW, "نشانه‌های متوسط", "نشانه‌ها در حدی است که بهتر است با یک روان‌شناس یا پزشک مشورت کنی. افسردگی شایع و درمان‌پذیر است.", seekHelp = true),
            Band(15, Level.VERY_LOW, "نشانه‌های شدید", "نشانه‌ها شدید است. لطفاً هر چه زودتر با یک روان‌شناس یا روان‌پزشک صحبت کن؛ درمان واقعاً کمک می‌کند.", seekHelp = true)
        ),
        higherIsWorse = true
    )

    val ANXIETY = Scale(
        Kind.ANXIETY,
        "در دو هفته گذشته، هر کدام از این مشکلات چند وقت یک‌بار آزارت داده است؟",
        PHQ_OPTIONS,
        listOf(
            Question("احساس عصبی بودن، اضطراب یا بی‌قراری"),
            Question("نتوانستن در متوقف کردن یا کنترل نگرانی"),
            Question("نگرانی بیش از حد درباره چیزهای مختلف"),
            Question("مشکل در آرام گرفتن"),
            Question("آن‌قدر بی‌قرار بودن که یک‌جا نشستن سخت باشد"),
            Question("زود رنجیدن یا زود عصبانی شدن"),
            Question("ترس از اینکه اتفاق وحشتناکی بیفتد")
        ),
        listOf(
            Band(0, Level.HIGH, "اضطراب کم", "نشانه قابل‌توجهی از اضطراب نداری. تمرین تنفس و ورزش را برای روزهای پرفشار نگه دار."),
            Band(5, Level.MIDDLE, "اضطراب خفیف", "کمی اضطراب داری. تنفس آرام، کم کردن کافئین، خواب کافی و نوشتن نگرانی‌ها کمک می‌کند."),
            Band(10, Level.LOW, "اضطراب متوسط", "اضطراب در حدی است که مشورت با روان‌شناس پیشنهاد می‌شود. درمان‌های کوتاه‌مدت مثل CBT برای اضطراب بسیار مؤثرند.", seekHelp = true),
            Band(15, Level.VERY_LOW, "اضطراب شدید", "اضطراب شدید است. لطفاً با یک روان‌شناس یا روان‌پزشک صحبت کن؛ لازم نیست تنهایی با آن کنار بیایی.", seekHelp = true)
        ),
        higherIsWorse = true
    )

    val STRESS = Scale(
        Kind.STRESS,
        "در یک ماه گذشته، هر کدام از این حالت‌ها چند وقت یک‌بار برایت پیش آمده است؟",
        PSS_OPTIONS,
        listOf(
            Question("به خاطر اتفاقی غیرمنتظره ناراحت شده‌ای؟"),
            Question("احساس کرده‌ای نمی‌توانی چیزهای مهم زندگی‌ات را کنترل کنی؟"),
            Question("احساس عصبی بودن و فشار داشته‌ای؟"),
            Question("به توانایی‌ات در حل مشکلات شخصی‌ات مطمئن بوده‌ای؟", reversed = true),
            Question("احساس کرده‌ای کارها همان‌طور که می‌خواهی پیش می‌روند؟", reversed = true),
            Question("دیده‌ای که نمی‌توانی از پس همه کارهایی که باید انجام دهی برآیی؟"),
            Question("توانسته‌ای دلخوری‌ها و ناراحتی‌های زندگی‌ات را کنترل کنی؟", reversed = true),
            Question("احساس کرده‌ای بر اوضاع مسلطی؟", reversed = true),
            Question("به خاطر چیزهایی که از کنترلت خارج بوده عصبانی شده‌ای؟"),
            Question("احساس کرده‌ای مشکلات آن‌قدر روی هم انباشته شده‌اند که نمی‌توانی بر آن‌ها غلبه کنی؟")
        ),
        listOf(
            Band(0, Level.HIGH, "استرس کم", "استرست در یک ماه گذشته کم بوده است. همین تعادل را با خواب، ورزش و وقت آزاد حفظ کن."),
            Band(14, Level.MIDDLE, "استرس متوسط", "استرس متوسطی داری که برای بیشتر آدم‌ها معمول است. تمرین تنفس، ورزش منظم و تقسیم کارهای بزرگ به قدم‌های کوچک کمک می‌کند."),
            Band(27, Level.LOW, "استرس زیاد", "استرس زیادی را تحمل می‌کنی. ببین کدام فشارها قابل‌کم‌کردن است، از دیگران کمک بخواه و اگر خواب یا خلقت به هم ریخته با یک مشاور صحبت کن.", seekHelp = true)
        ),
        higherIsWorse = true
    )

    // ------------------------------------------------------------------ self

    val SELF_ESTEEM = Scale(
        Kind.SELF_ESTEEM,
        "هر جمله را بخوان و بگو چقدر با آن موافقی. جواب درست یا غلط وجود ندارد؛ اولین جوابی که به ذهنت می‌رسد معمولاً دقیق‌تر است.",
        AGREE4,
        listOf(
            Question("روی‌هم‌رفته از خودم راضی‌ام."),
            Question("گاهی فکر می‌کنم اصلاً به درد نمی‌خورم.", reversed = true),
            Question("احساس می‌کنم ویژگی‌های خوب زیادی دارم."),
            Question("می‌توانم کارها را به خوبیِ بیشتر آدم‌ها انجام دهم."),
            Question("احساس می‌کنم چیز زیادی ندارم که به آن افتخار کنم.", reversed = true),
            Question("گاهی واقعاً احساس بی‌فایدگی می‌کنم.", reversed = true),
            Question("احساس می‌کنم آدم ارزشمندی هستم، دست‌کم هم‌اندازه دیگران."),
            Question("کاش می‌توانستم برای خودم احترام بیشتری قائل باشم.", reversed = true),
            Question("روی‌هم‌رفته فکر می‌کنم آدم شکست‌خورده‌ای هستم.", reversed = true),
            Question("نگاه مثبتی به خودم دارم.")
        ),
        listOf(
            Band(26, Level.HIGH, "عزت نفس بالا", "نگاهت به خودت محکم و مثبت است. برای پایدار ماندنش، به ارزش‌هایت وفادار بمان و به دیگران هم کمک کن رشد کنند."),
            Band(15, Level.MIDDLE, "عزت نفس در محدوده معمول", "پایه خوبی داری. تمرین‌های محور «عزت نفس» و دفترچه «سه اتفاق خوب» می‌توانند آن را محکم‌تر کنند."),
            Band(
                0, Level.LOW, "عزت نفس پایین‌تر از معمول",
                "این نتیجه یعنی این روزها با خودت سخت‌گیری. از محور «عزت نفس» و تمرین مهربانی با خود شروع کن. اگر این احساس ماندگار است، صحبت با یک مشاور بسیار کمک می‌کند.",
                seekHelp = true
            )
        )
    )

    val PERSONALITY = Scale(
        Kind.PERSONALITY,
        "هر جمله را بخوان و بگو چقدر درباره‌ات درست است؛ خودت را همان‌طور که معمولاً هستی توصیف کن، نه آن‌طور که دوست داری باشی. هیچ ویژگی‌ای خوب یا بد نیست.",
        TRUE5,
        listOf(
            Question("در مهمانی‌ها معمولاً شور و مرکز جمعم.", trait = Trait.EXTRAVERSION),
            Question("با احساسات دیگران همدردی می‌کنم.", trait = Trait.AGREEABLENESS),
            Question("کارهای روزمره را بی‌معطلی انجام می‌دهم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("حالم زیاد بالا و پایین می‌شود.", reversed = true, trait = Trait.STABILITY),
            Question("تخیل قوی و زنده‌ای دارم.", trait = Trait.OPENNESS),
            Question("زیاد حرف نمی‌زنم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("به مشکلات دیگران علاقه‌ای ندارم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("اغلب یادم می‌رود وسایل را سر جایشان برگردانم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("بیشتر وقت‌ها آرامم.", trait = Trait.STABILITY),
            Question("به ایده‌های انتزاعی علاقه‌ای ندارم.", reversed = true, trait = Trait.OPENNESS),
            Question("در مهمانی‌ها با آدم‌های مختلف زیادی حرف می‌زنم.", trait = Trait.EXTRAVERSION),
            Question("احساسات دیگران را حس می‌کنم.", trait = Trait.AGREEABLENESS),
            Question("نظم و ترتیب را دوست دارم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("زود ناراحت می‌شوم.", reversed = true, trait = Trait.STABILITY),
            Question("فهمیدن ایده‌های انتزاعی برایم سخت است.", reversed = true, trait = Trait.OPENNESS),
            Question("ترجیح می‌دهم در حاشیه بمانم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("واقعاً به دیگران علاقه‌ای ندارم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("کارها را به هم می‌ریزم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("به‌ندرت احساس غم و دلگرفتگی می‌کنم.", trait = Trait.STABILITY),
            Question("قدرت تخیل خوبی ندارم.", reversed = true, trait = Trait.OPENNESS)
        )
    )

    val RESILIENCE = Scale(
        Kind.RESILIENCE,
        "چقدر با هر جمله موافقی؟",
        AGREE5,
        listOf(
            Question("معمولاً پس از دوره‌های سخت زود دوباره سرپا می‌شوم."),
            Question("گذشتن از اتفاق‌های پراسترس برایم سخت است.", reversed = true),
            Question("بازیابی‌ام پس از یک اتفاق پراسترس زیاد طول نمی‌کشد."),
            Question("وقتی اتفاق بدی می‌افتد، کنار آمدن با آن برایم سخت است.", reversed = true),
            Question("معمولاً روزهای سخت را بدون دردسر زیاد پشت سر می‌گذارم."),
            Question("معمولاً مدت زیادی طول می‌کشد تا از ناکامی‌های زندگی‌ام عبور کنم.", reversed = true)
        ),
        // The authors read the mean (1..5): below 3 low, 3..4.30 normal, above 4.30 high.
        listOf(
            Band(26, Level.HIGH, "تاب‌آوری بالا", "پس از سختی‌ها زود دوباره سرپا می‌شوی. این توانایی را با کمک به دیگران و حفظ عادت‌های سالم نگه دار."),
            Band(18, Level.MIDDLE, "تاب‌آوری معمول", "مثل بیشتر آدم‌ها، پس از سختی‌ها با کمی زمان سرپا می‌شوی. خواب، ورزش و رابطه‌های حمایتگر تاب‌آوری را بیشتر می‌کنند."),
            Band(
                0, Level.LOW, "تاب‌آوری پایین‌تر",
                "این روزها برگشتن از سختی‌ها برایت دشوار است. تمرین‌های محور «آرامش و استرس» و حرف زدن با یک دوست مورد اعتماد کمک می‌کند و مشاور می‌تواند مهارت‌های کنار آمدن را یادت بدهد.",
                seekHelp = true
            )
        )
    )

    val SELF_EFFICACY = Scale(
        Kind.SELF_EFFICACY,
        "هر جمله چقدر درباره‌ات درست است؟",
        GSE_OPTIONS,
        listOf(
            Question("اگر به اندازه کافی تلاش کنم، همیشه می‌توانم مسئله‌های سخت را حل کنم."),
            Question("اگر کسی با من مخالفت کند، می‌توانم راهی برای رسیدن به خواسته‌ام پیدا کنم."),
            Question("پایبند ماندن به هدف‌هایم و رسیدن به آن‌ها برایم آسان است."),
            Question("مطمئنم که می‌توانم با اتفاق‌های غیرمنتظره به‌خوبی کنار بیایم."),
            Question("به لطف توانایی‌هایم، می‌دانم در موقعیت‌های پیش‌بینی‌نشده چه کنم."),
            Question("اگر تلاش لازم را بکنم، بیشتر مشکلات را می‌توانم حل کنم."),
            Question("در برابر سختی‌ها آرام می‌مانم، چون به توانایی‌ام در کنار آمدن اعتماد دارم."),
            Question("وقتی با مشکلی روبه‌رو می‌شوم، معمولاً چند راه‌حل پیدا می‌کنم."),
            Question("وقتی در دردسر می‌افتم، معمولاً راه‌حلی به ذهنم می‌رسد."),
            Question("معمولاً از پس هر اتفاقی که پیش بیاید برمی‌آیم.")
        ),
        // No official cut-offs; the international mean is about 29 (SD ≈ 4–5).
        listOf(
            Band(33, Level.HIGH, "خودکارآمدی بالا", "به توانایی‌ات در حل مشکلات اعتماد داری. از این اعتماد برای هدف‌های بزرگ‌تر و یاد دادن به دیگران استفاده کن."),
            Band(25, Level.MIDDLE, "خودکارآمدی در محدوده معمول", "باور خوبی به توانایی‌هایت داری. موفقیت‌های کوچک و پشت‌سرهم، این باور را قوی‌تر می‌کند؛ هدف‌هایت را به قدم‌های کوچک بشکن."),
            Band(10, Level.LOW, "خودکارآمدی پایین‌تر از معمول", "این روزها به توانایی‌ات کمتر اعتماد داری. با یک هدف کوچک و قابل‌انجام شروع کن، موفقیت‌هایت را بنویس و از آدم‌هایی که الگویت هستند یاد بگیر.")
        )
    )

    val LIFE_SATISFACTION = Scale(
        Kind.LIFE_SATISFACTION,
        "چقدر با هر جمله موافقی؟ صادقانه و بر اساس زندگی خودت جواب بده.",
        AGREE7,
        listOf(
            Question("زندگی‌ام از بیشتر جهات به ایده‌آل من نزدیک است."),
            Question("شرایط زندگی‌ام عالی است."),
            Question("از زندگی‌ام راضی‌ام."),
            Question("تا امروز چیزهای مهمی را که از زندگی می‌خواستم به دست آورده‌ام."),
            Question("اگر می‌توانستم زندگی‌ام را از نو شروع کنم، تقریباً هیچ چیز را تغییر نمی‌دادم.")
        ),
        listOf(
            Band(26, Level.HIGH, "رضایت بالا از زندگی", "زندگی‌ات را دوست داری و حس می‌کنی خوب پیش می‌رود. حواست باشد رضایت با رشد هم‌خوان است؛ هدف تازه‌ای هم برای خودت بگذار."),
            Band(20, Level.MIDDLE, "رضایت متوسط", "بیشتر جنبه‌های زندگی‌ات خوب است ولی بخش‌هایی هست که می‌خواهی بهتر شود. یکی را انتخاب کن و برایش هدف «اگر… آنگاه…» بنویس."),
            Band(10, Level.LOW, "کمی ناراضی", "از چند بخش مهم زندگی‌ات راضی نیستی. بنویس کدام بخش (کار، رابطه، سلامت، پول) بیشترین اثر را دارد و از همان شروع کن."),
            Band(
                5, Level.VERY_LOW, "نارضایتی زیاد",
                "این روزها از زندگی‌ات خیلی ناراضی هستی. اگر این حس ماندگار است یا با غم و بی‌انگیزگی همراه است، با یک مشاور صحبت کن و آزمون افسردگی را هم بزن.",
                seekHelp = true
            )
        )
    )

    // ------------------------------------------------------------------ bonds

    val LONELINESS = Scale(
        Kind.LONELINESS,
        "این سؤال‌ها درباره احساس تو در رابطه با دیگران است. هر کدام چند وقت یک‌بار برایت پیش می‌آید؟",
        UCLA_OPTIONS,
        listOf(
            Question("چقدر احساس می‌کنی همدم و هم‌صحبتی نداری؟"),
            Question("چقدر احساس می‌کنی کنار گذاشته شده‌ای؟"),
            Question("چقدر احساس می‌کنی از دیگران جدا افتاده‌ای؟")
        ),
        listOf(
            Band(0, Level.HIGH, "احساس تنهایی کم", "رابطه‌هایت حس پیوند و همراهی به تو می‌دهند. آن‌ها را با دیدار و تماس منظم زنده نگه دار."),
            Band(6, Level.LOW, "احساس تنهایی", "این روزها احساس تنهایی می‌کنی؛ حسی رایج که با سلامت جسم و روان هم ارتباط دارد. محور «دوستی و روابط اجتماعی» را ببین و همین هفته با یک دوست قرار حضوری بگذار.", seekHelp = false),
            Band(8, Level.VERY_LOW, "احساس تنهایی زیاد", "تنهایی زیادی را تحمل می‌کنی. پیوستن به یک گروه (ورزش، کلاس، کار داوطلبانه) و حرف زدن با یک مشاور می‌تواند این حس را کم کند.", seekHelp = true)
        ),
        higherIsWorse = true
    )

    val RELATIONSHIP = Scale(
        Kind.RELATIONSHIP,
        "این چهار سؤال درباره رابطه‌ات با همسرت است. جوابت فقط روی همین گوشی می‌ماند.",
        CSI_OPTIONS,
        listOf(
            Question("روی‌هم‌رفته، رابطه‌ات با همسرت چقدر خوشحال‌کننده است؟", options = CSI_HAPPINESS),
            Question("با همسرم رابطه‌ای گرم و راحت دارم."),
            Question("رابطه‌ات با همسرت چقدر برایت ارزشمند و دلگرم‌کننده است؟"),
            Question("به‌طور کلی چقدر از رابطه‌ات راضی هستی؟")
        ),
        // The authors' cut-off: scores below 13.5 suggest notable relationship dissatisfaction.
        listOf(
            Band(17, Level.HIGH, "رضایت بالا از رابطه", "رابطه‌تان گرم و رضایت‌بخش است. با قدردانی روزانه، گفت‌وگوی بی‌گوشی و قرار دونفره منظم نگهش دار."),
            Band(14, Level.MIDDLE, "رضایت خوب", "رابطه‌تان روی‌هم‌رفته رضایت‌بخش است. ابزار «سؤال‌های گفت‌وگوی دونفره» را امتحان کن تا نزدیک‌تر شوید."),
            Band(
                0, Level.LOW, "نارضایتی قابل‌توجه",
                "نمره‌ات زیر نقطه برش نارضایتی زوجین است. محور «مدیریت اختلاف» و ابزار «شروع نرم» را ببین و مشاوره زوج را جدی بگیر؛ هرچه زودتر، اثرش بیشتر.",
                seekHelp = true
            )
        )
    )

    val ALL_SCALES: List<Scale> = listOf(
        WELL_BEING, DEPRESSION, ANXIETY, STRESS, SELF_ESTEEM, PERSONALITY,
        RESILIENCE, SELF_EFFICACY, LIFE_SATISFACTION, LONELINESS, RELATIONSHIP
    )

    private val byKind = ALL_SCALES.associateBy { it.kind }

    fun scale(kind: Kind): Scale = byKind.getValue(kind)

    /**
     * Total score from the chosen option index of each question (0 = the first label shown).
     * Returns null unless every question is answered with a valid option. The personality test
     * has no single score and returns 0; use [profile] for it.
     */
    fun score(kind: Kind, answers: List<Int>): Int? {
        val s = scale(kind)
        if (!s.valid(answers)) return null
        if (s.isProfile) return 0
        return answers.withIndex().sumOf { (i, a) -> s.scored(i, a) } * s.multiplier
    }

    /** The five factor scores of the personality test (each 4..20), or null if incomplete. */
    fun profile(answers: List<Int>): List<TraitScore>? {
        val s = PERSONALITY
        if (!s.valid(answers)) return null
        return Trait.values().map { trait ->
            val items = s.questions.indices.filter { s.questions[it].trait == trait }
            TraitScore(trait, items.sumOf { s.scored(it, answers[it]) }, max = items.size * 5, min = items.size)
        }
    }

    fun interpret(kind: Kind, score: Int): Result {
        val s = scale(kind)
        if (s.bands.isEmpty()) {
            return Result(Level.MIDDLE, "نیم‌رخ شخصیت تو", "پنج ویژگی اصلی شخصیتت را در زیر ببین. شخصیت خوب یا بد ندارد؛ شناختنش کمک می‌کند نقاط قوتت را بهتر به کار ببری.", false)
        }
        val band = s.bands.filter { score >= it.min }.maxByOrNull { it.min } ?: s.bands.minBy { it.min }
        return Result(band.level, band.title, band.text, band.seekHelp)
    }

    /**
     * True when an answer needs an immediate, caring response no matter the total: any thought
     * of death or self-harm on PHQ-9 item 9.
     */
    fun needsUrgentHelp(kind: Kind, answers: List<Int>): Boolean {
        if (kind != Kind.DEPRESSION) return false
        val s = scale(kind)
        val last = s.questions.lastIndex
        val a = answers.getOrNull(last) ?: return false
        return a in s.optionsFor(last).indices && s.scored(last, a) > 0
    }

    /** Where to find help in Iran; shown with every result that recommends it. */
    const val HELP_TEXT =
        "این آزمون جایگزین تشخیص متخصص نیست. برای مشاوره رایگان تلفنی می‌توانی با صدای مشاور بهزیستی (۱۴۸۰) تماس بگیری و در شرایط اضطراری با اورژانس اجتماعی (۱۲۳) یا اورژانس (۱۱۵)."

    /** Shown at once when [needsUrgentHelp] is true. */
    const val URGENT_TEXT =
        "گفتی این روزها به مرگ یا آسیب زدن به خودت فکر کرده‌ای. این فکرها درمان‌پذیرند و تو تنها نیستی. " +
            "همین امروز با کسی که به او اعتماد داری حرف بزن و با صدای مشاور بهزیستی (۱۴۸۰) تماس بگیر. " +
            "اگر ممکن است همین حالا به خودت آسیب بزنی، با اورژانس اجتماعی (۱۲۳) یا اورژانس (۱۱۵) تماس بگیر یا به نزدیک‌ترین بیمارستان برو."

    /** One line on how the app treats these tests, for the test list. */
    const val ABOUT =
        "همه آزمون‌ها ابزارهای علمی منتشرشده و رایگان هستند که با همان روش سازندگانشان نمره‌گذاری می‌شوند. ترجمه فارسی از خود برنامه است و نتیجه فقط برای خودشناسی است، نه تشخیص."

    /** Answers kept as a compact digit string, e.g. "0123…". */
    fun encodeAnswers(answers: List<Int>): String = answers.joinToString("") { it.coerceIn(0, 9).toString() }

    fun decodeAnswers(text: String?): List<Int> = text.orEmpty().mapNotNull { it.digitToIntOrNull() }
}
