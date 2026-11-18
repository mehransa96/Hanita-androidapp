package com.hanita.core.gentleman

/**
 * Validated self-checks the user can repeat to see change over time. Every one is a published
 * instrument that is free to use (public domain or free for non-commercial use with citation),
 * scored exactly as its authors describe, with the cut-offs from its own literature:
 *
 *  - Mood:   WHO-5 well-being, PHQ-9 depression, GAD-7 anxiety, PSS-10 perceived stress.
 *  - Self:   Rosenberg self-esteem, IPIP-50 Big Five personality, Brief Resilience Scale,
 *            General Self-Efficacy, Satisfaction With Life.
 *  - Bonds:  UCLA 3-item loneliness, Couples Satisfaction Index (CSI-4, married only).
 *
 * Beyond the total, each result explains itself: the parts of the scale (sub-scores), the answers
 * that weighed most, warning signs the authors define (PHQ-2, GAD-2, WHO-5 items, the PHQ-9
 * algorithm), what is typical in the general population, concrete next steps for the band, and
 * whether a change since the last time is large enough to be real.
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
        val audience: GentlemanGuide.Audience = GentlemanGuide.Audience.ALL,
        /** What the test measures and how to read it, shown before the first question. */
        val about: String = ""
    ) {
        WELL_BEING(
            "who5", "آزمون بهزیستی", "شاخص ۵ سؤالی سازمان جهانی بهداشت (WHO-5)", "who5", Group.MOOD, 1,
            about = "حال خوب، آرامش، انرژی، خواب و علاقه به زندگی را در دو هفته گذشته می‌سنجد. نمره از ۰ تا ۱۰۰ است و هرچه بیشتر، بهتر. یکی از پراستفاده‌ترین سنجه‌های حال روانی در دنیاست."
        ),
        DEPRESSION(
            "phq9", "غربالگری افسردگی", "پرسشنامه سلامت بیمار (PHQ-9)", "phq9", Group.MOOD, 2,
            about = "نُه نشانه اصلی افسردگی را در دو هفته گذشته می‌سنجد و یک سؤال پایانی می‌پرسد این نشانه‌ها چقدر کار و زندگی‌ات را سخت کرده‌اند. نمره ۰ تا ۲۷ است و هرچه کمتر، بهتر."
        ),
        ANXIETY(
            "gad7", "غربالگری اضطراب", "مقیاس اضطراب فراگیر (GAD-7)", "gad7", Group.MOOD, 2,
            about = "هفت نشانه اضطراب و نگرانی را در دو هفته گذشته می‌سنجد، به‌اضافه یک سؤال درباره اثرش بر زندگی‌ات. نمره ۰ تا ۲۱ است و هرچه کمتر، بهتر."
        ),
        STRESS(
            "pss10", "آزمون استرس", "مقیاس استرس ادراک‌شده کوهن (PSS-10)", "pss", Group.MOOD, 3,
            about = "می‌سنجد زندگی‌ات در یک ماه گذشته چقدر غیرقابل‌پیش‌بینی، خارج از کنترل و پرفشار به نظرت رسیده است. نمره ۰ تا ۴۰ است و جدا از کل، دو بخش «درماندگی» و «احساس توانمندی» را هم نشان می‌دهد."
        ),
        SELF_ESTEEM(
            "rses", "آزمون عزت نفس", "مقیاس ۱۰ سؤالی روزنبرگ", "rosenberg", Group.SELF, 2,
            about = "نگاه کلی‌ات به ارزش خودت را می‌سنجد؛ هم نگاه مثبت به خود و هم میزان خودسرزنشی. نمره ۰ تا ۳۰ است و هرچه بیشتر، بهتر."
        ),
        PERSONALITY(
            "ipip", "آزمون شخصیت پنج‌عاملی", "IPIP-50، پنجاه سؤال درباره پنج ویژگی اصلی", "ipip50", Group.SELF, 8,
            about = "پنج ویژگی اصلی شخصیت را، هر کدام با ده سؤال، می‌سنجد: برون‌گرایی، همدلی، نظم، ثبات هیجانی و ذهن باز. شخصیت خوب یا بد ندارد؛ نتیجه نقاط قوت و نکته‌های مراقبتی هر ویژگی را در کار و رابطه نشان می‌دهد."
        ),
        RESILIENCE(
            "brs", "آزمون تاب‌آوری", "مقیاس کوتاه تاب‌آوری (BRS)", "brs", Group.SELF, 1,
            about = "می‌سنجد پس از سختی‌ها و فشارها چقدر زود دوباره سرپا می‌شوی. سازندگان، میانگین پاسخ‌ها (۱ تا ۵) را تفسیر می‌کنند؛ برنامه هر دو را نشان می‌دهد."
        ),
        SELF_EFFICACY(
            "gse", "آزمون خودکارآمدی", "مقیاس عمومی خودکارآمدی شوارتزر (GSE)", "gse", Group.SELF, 2,
            about = "باورت به توانایی خودت در حل مشکل و کنار آمدن با موقعیت‌های سخت و غیرمنتظره را می‌سنجد. نمره ۱۰ تا ۴۰ است و هرچه بیشتر، بهتر."
        ),
        LIFE_SATISFACTION(
            "swls", "رضایت از زندگی", "مقیاس رضایت از زندگی دینر (SWLS)", "swls", Group.SELF, 1,
            about = "قضاوت کلی خودت از زندگی‌ات را می‌سنجد. نمره ۵ تا ۳۵ است و با هفت بازه رسمی سازنده آزمون تفسیر می‌شود."
        ),
        LONELINESS(
            "ucla3", "آزمون تنهایی", "مقیاس سه‌سؤالی تنهایی UCLA", "ucla3", Group.BOND, 1,
            about = "احساس نداشتن همدم، کنار گذاشته شدن و جدا افتادن از دیگران را می‌سنجد. نمره ۳ تا ۹ است و هرچه کمتر، بهتر."
        ),
        RELATIONSHIP(
            "csi4", "رضایت از رابطه زناشویی", "شاخص رضایت زوجین (CSI-4)", "csi4", Group.BOND, 1,
            GentlemanGuide.Audience.MARRIED,
            about = "رضایت کلی‌ات از رابطه با همسرت را می‌سنجد. نمره ۰ تا ۲۱ است و نمره زیر ۱۳٫۵ نشانه نارضایتی قابل‌توجه است."
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

    /** The five factors of the Big Five (neuroticism is shown the other way round, as stability). */
    enum class Trait(
        val title: String,
        val high: String,
        val low: String,
        val tip: String,
        /** What a high score tends to bring, and what to watch for. */
        val highStrength: String,
        val highWatch: String,
        /** What a low score tends to bring, and what to watch for. */
        val lowStrength: String,
        val lowWatch: String,
        /** How the trait shows at work and in a relationship, whatever the score. */
        val atWork: String,
        val inLove: String
    ) {
        EXTRAVERSION(
            "برون‌گرایی",
            "از جمع و گفت‌وگو انرژی می‌گیری، راحت شروع‌کننده ارتباطی و در گروه دیده می‌شوی.",
            "درون‌گراتری؛ تنهایی شارژت می‌کند و رابطه‌های کم ولی عمیق را ترجیح می‌دهی.",
            "درون‌گرا یا برون‌گرا بودن خوب یا بد نیست. اگر درون‌گرایی، بعد از جمع‌های شلوغ برای خودت وقت شارژ بگذار؛ اگر برون‌گرایی، تمرین کن بیشتر گوش بدهی.",
            "شبکه‌سازی، رهبری جمع، انرژی دادن به تیم و راحتی در مذاکره و ارائه.",
            "ممکن است زیاد حرف بزنی، کمتر گوش بدهی یا برای فرار از تنهایی، برنامه‌ات را بیش از حد پر کنی.",
            "تمرکز عمیق، گوش دادن خوب، فکر کردن پیش از حرف زدن و دوستی‌های کم ولی محکم.",
            "ممکن است فرصت‌ها را به خاطر دیده نشدن از دست بدهی یا دیگران سکوتت را بی‌علاقگی بدانند؛ گاهی پیش‌قدم شو.",
            "برون‌گراها در فروش، مدیریت و کار تیمی می‌درخشند؛ درون‌گراها در تحلیل، نوشتن و کار عمیق. محیط و ساعت کاری‌ات را با سطح انرژی‌ات هماهنگ کن.",
            "اگر با همسرت در این ویژگی فرق دارید، درباره مهمانی و وقت تنهایی از قبل توافق کنید تا هیچ‌کدام خسته یا تنها نشوید."
        ),
        AGREEABLENESS(
            "مهربانی و همدلی",
            "همدل، مهربان و اهل همکاری هستی و احساس دیگران برایت مهم است.",
            "رک و مستقلی و راحت‌تر مخالفت می‌کنی؛ گاهی ممکن است سرد به نظر برسی.",
            "همدلی بالا اگر با «نه گفتن» همراه نباشد خسته‌ات می‌کند؛ همدلی پایین را با پرسیدن «حالت چطور است؟» و گوش دادن کامل تقویت کن.",
            "اعتمادسازی، کار تیمی، آرام کردن اختلاف‌ها و دوستی‌های پایدار.",
            "ممکن است «نه» گفتن برایت سخت باشد، از خواسته‌هایت بگذری و بعد دلخور شوی؛ مرزهایت را روشن کن.",
            "صراحت، استقلال رأی، دفاع از حق خودت و تصمیم‌گیری سخت بدون نگرانی از خوشایند دیگران.",
            "ممکن است بی‌ملاحظه یا سرد به نظر برسی؛ پیش از انتقاد، اول نیت خوبت را بگو و احساس طرف را بپرس.",
            "همدل‌ها در کارهای خدماتی، آموزش و تیم‌سازی موفق‌اند؛ رک‌ها در مذاکره، کنترل کیفیت و تصمیم‌های دشوار. هر دو باید یاد بگیرند انتقاد را محترمانه بگویند.",
            "همدلی بالا رابطه را گرم نگه می‌دارد، به شرط آنکه نیازهای خودت را هم بگویی. اگر رک‌تری، لحن نرم و قدردانی روزانه را عمداً تمرین کن."
        ),
        CONSCIENTIOUSNESS(
            "نظم و وجدان کاری",
            "منظم، قابل‌اعتماد و اهل برنامه‌ای و کارها را به موقع تمام می‌کنی.",
            "انعطاف‌پذیر و خودجوشی، ولی ممکن است کارها عقب بیفتد یا نظم را سخت نگه داری.",
            "وجدان کاری یکی از قوی‌ترین پیش‌بینی‌کننده‌های موفقیت شغلی است. برای تقویتش از برنامه «اگر… آنگاه…» و فهرست کارهای روزانه استفاده کن.",
            "قابل‌اعتماد بودن، تمام کردن کارها، مدیریت پول و سلامت بهتر و پیشرفت شغلی پایدار.",
            "ممکن است کمال‌گرا شوی، استراحت را عقب بیندازی یا از بی‌نظمی دیگران زود عصبانی شوی.",
            "انعطاف، خلاقیت در لحظه، راحتی با تغییر و فشار کمتر از برنامه‌های خشک.",
            "ممکن است کارها و قول‌ها عقب بیفتد؛ کارهای مهم را در تقویم بنویس، برایشان یادآور بگذار و به جای اراده به سیستم تکیه کن.",
            "در بیشتر شغل‌ها، این ویژگی قوی‌ترین پیش‌بینی‌کننده عملکرد در میان پنج ویژگی است. اگر پایین است، ابزارهایی مثل فهرست کار و بلوک کار عمیق جبرانش می‌کنند.",
            "در زندگی مشترک، تقسیم روشن کارهای خانه و پول، اختلاف‌های ناشی از تفاوت در نظم را بسیار کم می‌کند."
        ),
        STABILITY(
            "ثبات هیجانی",
            "معمولاً آرامی، زود از کوره در نمی‌روی و فشار را خوب تحمل می‌کنی.",
            "احساساتت قوی است و نگرانی یا ناراحتی زودتر سراغت می‌آید.",
            "اگر ثبات هیجانی‌ات پایین‌تر است، تنفس آرام، ورزش منظم، خواب کافی و نوشتن احساسات کمک بزرگی است؛ اگر آزارت می‌دهد، با یک مشاور حرف بزن.",
            "آرامش زیر فشار، تصمیم‌گیری با سر سرد و تکیه‌گاه بودن برای دیگران در بحران.",
            "ممکن است نشانه‌های خستگی یا ناراحتی خودت یا دیگران را دیر ببینی؛ آرامش را با بی‌تفاوتی اشتباه نگیر.",
            "حساسیت بالا به خطر و احساسات، که با مدیریت درست به دقت و همدلی تبدیل می‌شود.",
            "نگرانی، دلخوری یا خشم ممکن است زود شعله بگیرد؛ مکث شش‌ثانیه‌ای، تنفس آرام، ورزش و خواب منظم اثر زیادی دارند و مشاوره (به‌ویژه CBT) بسیار کمک می‌کند.",
            "در کارهای پرفشار، ثبات هیجانی مزیت است. اگر پایین‌تر است، کارهای پراسترس را در ساعت‌های پرانرژی‌ات بگذار و بین آن‌ها استراحت کوتاه داشته باش.",
            "در دعوا، کسی که زودتر برانگیخته می‌شود باید حق «وقت استراحت ۲۰ دقیقه‌ای» داشته باشد؛ گاتمن نشان داد با ضربان بالا، گفت‌وگو فقط بدتر می‌شود."
        ),
        OPENNESS(
            "ذهن باز و خلاقیت",
            "کنجکاو و خلاقی و از ایده‌ها، هنر و یادگیری چیزهای تازه لذت می‌بری.",
            "عمل‌گرا و واقع‌بینی و روش‌های آزموده را به ایده‌های انتزاعی ترجیح می‌دهی.",
            "برای تقویت ذهن باز، هر ماه یک کتاب یا مهارت بیرون از حوزه همیشگی‌ات را امتحان کن.",
            "یادگیری سریع، حل خلاقانه مسئله، راحتی با ایده‌های تازه و آدم‌های متفاوت.",
            "ممکن است از کارهای تکراری زود خسته شوی یا ایده‌های زیادی را نیمه‌کاره رها کنی؛ هر بار فقط یکی را تا آخر ببر.",
            "واقع‌بینی، تمرکز بر آنچه جواب می‌دهد و ثبات در روش‌های آزموده.",
            "ممکن است فرصت‌های تازه یا راه‌های بهتر را از دست بدهی؛ گاهی آگاهانه یک روش جدید را برای مدت کوتاه امتحان کن.",
            "ذهن باز در کارهای خلاق، پژوهش و راه‌اندازی کسب‌وکار کمک می‌کند؛ عمل‌گرایی در اجرا، عملیات و کارهایی که دقت و تکرار می‌خواهند.",
            "تجربه‌های تازه مشترک (سفر، کلاس، غذای جدید) رابطه را زنده نگه می‌دارد؛ پژوهش‌های آرون نشان داد کار تازه دونفره، نزدیکی را بیشتر می‌کند."
        )
    }

    data class Question(
        val text: String,
        /** Scored the other way round (agreeing lowers the score). */
        val reversed: Boolean = false,
        /** For the personality test: the factor this item measures. */
        val trait: Trait? = null,
        /** Answers for this question only, when they differ from the scale's. */
        val options: List<Option>? = null,
        /** False for an item the authors ask but do not add to the total (PHQ-9 item 10). */
        val scored: Boolean = true
    )

    /**
     * A score range and what it means; [min] is the lowest total that falls in this band.
     * [steps] are concrete things to do now, most useful first.
     */
    data class Band(
        val min: Int,
        val level: Level,
        val title: String,
        val text: String,
        val seekHelp: Boolean = false,
        val steps: List<String> = emptyList()
    )

    /**
     * A named part of a scale, e.g. the PHQ-2 core of the PHQ-9. [items] are question indexes;
     * [higherIsWorse] follows the scale. [low] and [high] say what each end means.
     */
    data class Subscale(val title: String, val items: List<Int>, val low: String, val high: String)

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
        val higherIsWorse: Boolean = false,
        /** Parts of the scale shown under the total. */
        val subscales: List<Subscale> = emptyList(),
        /**
         * The smallest change between two results that is more than day-to-day noise: the
         * published minimal important difference where there is one, else half a standard
         * deviation of the scale in population samples (Norman 2003).
         */
        val meaningfulChange: Int = 1,
        /** What is typical in the general population, in one line, when the literature says. */
        val norm: String? = null
    ) {
        fun optionsFor(question: Int): List<Option> = questions[question].options ?: options

        private fun range(question: Int): IntRange {
            val values = optionsFor(question).map { it.value }
            return values.min()..values.max()
        }

        private val scoredItems: List<Int> get() = questions.indices.filter { questions[it].scored }

        val minScore: Int get() = scoredItems.sumOf { range(it).first } * multiplier
        val maxScore: Int get() = scoredItems.sumOf { range(it).last } * multiplier

        /** True for the personality test, whose result is a profile of traits, not one score. */
        val isProfile: Boolean get() = questions.any { it.trait != null }

        /** Value of the chosen option of [question], after reverse scoring. */
        internal fun scored(question: Int, optionIndex: Int): Int {
            val v = optionsFor(question)[optionIndex].value
            val r = range(question)
            return if (questions[question].reversed) r.first + r.last - v else v
        }

        /**
         * How unfavourable an answer is, from 0.0 (best possible) to 1.0 (worst possible),
         * whichever way the scale runs.
         */
        internal fun burden(question: Int, optionIndex: Int): Double {
            val r = range(question)
            val span = (r.last - r.first).coerceAtLeast(1).toDouble()
            val position = (scored(question, optionIndex) - r.first) / span
            return if (higherIsWorse) position else 1.0 - position
        }

        internal fun minOf(items: List<Int>): Int = items.sumOf { range(it).first }
        internal fun maxOf(items: List<Int>): Int = items.sumOf { range(it).last }

        fun valid(answers: List<Int>): Boolean =
            answers.size == questions.size && answers.withIndex().all { (i, a) -> a in optionsFor(i).indices }
    }

    /** HIGH is always the most favourable band and VERY_LOW the least, whatever the scale. */
    enum class Level { HIGH, MIDDLE, LOW, VERY_LOW }

    data class Result(
        val level: Level,
        val title: String,
        val text: String,
        val seekHelp: Boolean,
        val steps: List<String> = emptyList()
    )

    /** One part of a result: its score, range and what it means for this user. */
    data class SubScore(val title: String, val score: Int, val min: Int, val max: Int, val text: String) {
        /** Position between the lowest and highest possible score, 0..100. */
        val percent: Int get() = ((score - min) * 100 / (max - min).coerceAtLeast(1)).coerceIn(0, 100)
    }

    /** One answer worth looking at: the question and the answer the user chose. */
    data class ItemNote(val question: String, val answer: String)

    /** The answers that pulled the result down most, and the ones that held it up. */
    data class Insights(val concerns: List<ItemNote>, val strengths: List<ItemNote>)

    /** A warning sign defined by the authors of the test, with what to do about it. */
    data class Flag(val title: String, val text: String, val seekHelp: Boolean)

    enum class Direction { BETTER, WORSE, SAME }

    data class Change(val delta: Int, val direction: Direction, val meaningful: Boolean, val text: String)

    data class TraitScore(val trait: Trait, val score: Int, val max: Int, val min: Int) {
        val percent: Int get() = (score - min) * 100 / (max - min)

        /** Five steps from very low to very high, as words. */
        val levelTitle: String get() = when {
            percent >= 80 -> "خیلی بالا"
            percent >= 60 -> "بالا"
            percent > 40 -> "میانه"
            percent > 20 -> "پایین"
            else -> "خیلی پایین"
        }

        val text: String get() = when {
            percent >= 60 -> trait.high
            percent <= 40 -> trait.low
            else -> "در میانه‌ای؛ بسته به موقعیت، هر دو سوی این ویژگی را نشان می‌دهی."
        }

        /** What this score tends to bring. */
        val strength: String get() = when {
            percent >= 60 -> trait.highStrength
            percent <= 40 -> trait.lowStrength
            else -> "انعطاف: می‌توانی بسته به موقعیت، از هر دو سوی این ویژگی استفاده کنی."
        }

        /** What to watch for with this score. */
        val watch: String get() = when {
            percent >= 60 -> trait.highWatch
            percent <= 40 -> trait.lowWatch
            else -> "حواست باشد در موقعیت‌های پرفشار به کدام سو می‌روی و آیا همان سو به کارت می‌آید."
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

    /** The authors' closing question of the PHQ-9 and GAD-7: asked, but not added to the total. */
    private val DIFFICULTY_OPTIONS = options("اصلاً سخت نبوده" to 0, "کمی سخت بوده" to 1, "خیلی سخت بوده" to 2, "بی‌نهایت سخت بوده" to 3)
    private const val DIFFICULTY_TEXT =
        "این مشکلات (اگر داشته‌ای) انجام کار، رسیدگی به امور خانه یا کنار آمدن با دیگران را چقدر برایت سخت کرده است؟"

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
            Band(
                76, Level.HIGH, "بهزیستی خیلی خوب",
                "در دو هفته گذشته بیشتر وقت‌ها حالت خوب، آرام و پرانرژی بوده است.",
                steps = listOf(
                    "بنویس کدام عادت‌ها (خواب، ورزش، آدم‌ها، کار) بیشترین سهم را در این حال داشته‌اند تا در روزهای سخت به آن‌ها برگردی.",
                    "یک ماه دیگر دوباره آزمون را بزن تا ببینی حالت پایدار است."
                )
            ),
            Band(
                51, Level.HIGH, "بهزیستی خوب",
                "در دو هفته گذشته حالت روی‌هم‌رفته خوب بوده است. عادت‌هایی را که به این حال کمک کرده‌اند نگه دار.",
                steps = listOf(
                    "پایین‌ترین جواب‌هایت را در بخش «پاسخ‌هایی که بیشتر اثر داشتند» ببین؛ همان‌جا بیشترین جای رشد است.",
                    "خواب منظم و دست‌کم ۱۵۰ دقیقه تحرک در هفته، دو پایه اصلی حفظ این حال‌اند."
                )
            ),
            Band(
                29, Level.LOW, "بهزیستی پایین",
                "حالت در دو هفته گذشته پایین‌تر از حد مطلوب بوده است (زیر ۵۰). سازمان جهانی بهداشت در این حالت بررسی بیشتر از نظر افسردگی را پیشنهاد می‌کند.",
                seekHelp = true,
                steps = listOf(
                    "آزمون افسردگی (PHQ-9) را همین حالا بزن تا تصویر دقیق‌تری داشته باشی.",
                    "این هفته سه کار کوچک لذت‌بخش (پیاده‌روی در روز، دیدار با یک دوست، سرگرمی قدیمی) را در تقویمت بگذار و انجام بده.",
                    "اگر تا دو هفته دیگر بهتر نشد، با یک روان‌شناس یا پزشک خانواده صحبت کن."
                )
            ),
            Band(
                0, Level.VERY_LOW, "بهزیستی خیلی پایین",
                "نمره ۲۸ یا کمتر در پژوهش‌ها با احتمال زیاد افسردگی همراه است. لطفاً با یک روان‌شناس یا پزشک صحبت کن؛ کمک گرفتن نشانه قدرت است.",
                seekHelp = true,
                steps = listOf(
                    "همین هفته با یک روان‌شناس، روان‌پزشک یا پزشک خانواده وقت بگیر یا با صدای مشاور (۱۴۸۰) تماس بگیر.",
                    "به یک نفر که به او اعتماد داری بگو این روزها حالت خوب نیست.",
                    "آزمون افسردگی (PHQ-9) را هم بزن و نتیجه‌اش را به متخصص نشان بده."
                )
            )
        ),
        multiplier = 4,
        meaningfulChange = 10,
        norm = "در پژوهش‌ها، تغییر ۱۰ امتیازی تغییر واقعی حال به حساب می‌آید و نمره زیر ۵۰ نشانه بهزیستی پایین است."
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
            Question("فکر اینکه بهتر است بمیری یا به نوعی به خودت آسیب بزنی"),
            Question(DIFFICULTY_TEXT, options = DIFFICULTY_OPTIONS, scored = false)
        ),
        listOf(
            Band(
                0, Level.HIGH, "نشانه‌ای از افسردگی دیده نمی‌شود",
                "در دو هفته گذشته نشانه قابل‌توجهی نداشته‌ای. خواب، تحرک و رابطه‌های خوب را ادامه بده.",
                steps = listOf(
                    "عادت‌های محافظ را نگه دار: خواب منظم، ورزش، نور روز صبح و دیدار با دوستان.",
                    "اگر اتفاق سختی در راه است (تغییر کار، فقدان، جدایی)، چند هفته بعد دوباره آزمون را بزن."
                )
            ),
            Band(
                5, Level.MIDDLE, "نشانه‌های خفیف",
                "چند نشانه خفیف داری. در این بازه معمولاً «مراقبت فعال» توصیه می‌شود: خودت قدم بردار و دو هفته دیگر دوباره بسنج.",
                steps = listOf(
                    "ساعت خواب و بیداری‌ات را ثابت کن و صبح‌ها ۱۵ دقیقه زیر نور روز راه برو.",
                    "هر روز یک کار کوچک لذت‌بخش یا مفید را برنامه‌ریزی کن و انجام بده (فعال‌سازی رفتاری).",
                    "دو هفته دیگر دوباره آزمون را بزن؛ اگر نمره بالا رفت، با متخصص صحبت کن."
                )
            ),
            Band(
                10, Level.LOW, "نشانه‌های متوسط",
                "نشانه‌ها در حدی است که سازندگان آزمون ارزیابی متخصص را پیشنهاد می‌کنند. افسردگی شایع و درمان‌پذیر است و روان‌درمانی (مثل CBT) اثر ثابت‌شده دارد.",
                seekHelp = true,
                steps = listOf(
                    "در دو هفته آینده با یک روان‌شناس یا پزشک خانواده وقت بگیر و این نتیجه را نشانش بده.",
                    "تا آن زمان روزانه تحرک (حتی ۲۰ دقیقه پیاده‌روی) و ساعت خواب ثابت را حفظ کن.",
                    "الکل و ماندن طولانی در شبکه‌های اجتماعی را کم کن؛ هر دو خلق را پایین‌تر می‌برند."
                )
            ),
            Band(
                15, Level.VERY_LOW, "نشانه‌های نسبتاً شدید",
                "نشانه‌ها نسبتاً شدید است. لطفاً هر چه زودتر با یک روان‌شناس یا روان‌پزشک صحبت کن؛ در این بازه معمولاً درمان فعال (روان‌درمانی و گاهی دارو) توصیه می‌شود.",
                seekHelp = true,
                steps = listOf(
                    "همین هفته با روان‌پزشک یا روان‌شناس وقت بگیر؛ اگر سخت است، از پزشک خانواده شروع کن.",
                    "به یک نفر نزدیک بگو حالت خوب نیست تا در پیگیری کنارت باشد.",
                    "تا شروع درمان، کارهای روزانه را کوچک کن ولی رها نکن: خواب، غذا، کمی تحرک."
                )
            ),
            Band(
                20, Level.VERY_LOW, "نشانه‌های شدید",
                "نشانه‌ها شدید است. این نمره به معنای ضعف تو نیست؛ نشان می‌دهد بدنت و ذهنت به کمک تخصصی نیاز دارند و درمان واقعاً جواب می‌دهد.",
                seekHelp = true,
                steps = listOf(
                    "همین امروز یا فردا با روان‌پزشک یا پزشک تماس بگیر؛ در صورت نیاز با صدای مشاور (۱۴۸۰) شروع کن.",
                    "اگر فکر آسیب به خودت داری، با اورژانس اجتماعی (۱۲۳) یا اورژانس (۱۱۵) تماس بگیر.",
                    "تنها نمان؛ از یک نفر نزدیک بخواه این روزها بیشتر کنارت باشد."
                )
            )
        ),
        higherIsWorse = true,
        subscales = listOf(
            Subscale(
                "هسته افسردگی (کم‌علاقگی و غم، PHQ-2)", listOf(0, 1),
                "لذت بردن و خلقت در این دو هفته حفظ شده است.",
                "کم‌علاقگی یا غم، دو نشانه اصلی افسردگی، در این دو هفته پررنگ بوده‌اند."
            ),
            Subscale(
                "نشانه‌های جسمی (خواب، انرژی، اشتها، تحرک)", listOf(2, 3, 4, 7),
                "خواب، انرژی و اشتهایت روی‌هم‌رفته سر جایش است.",
                "بدنت درگیر است: خواب، انرژی یا اشتها به هم ریخته. این نشانه‌ها گاهی علت جسمی هم دارند؛ چکاپ پزشکی (تیروئید، کم‌خونی، ویتامین D) را فراموش نکن."
            ),
            Subscale(
                "نشانه‌های فکری (ارزش خود، تمرکز، فکر آسیب)", listOf(5, 6, 8),
                "نگاهت به خودت و تمرکزت روی‌هم‌رفته حفظ شده است.",
                "خودسرزنشی، مشکل تمرکز یا فکرهای تیره پررنگ شده‌اند؛ این‌ها با روان‌درمانی شناختی به‌خوبی بهتر می‌شوند."
            )
        ),
        meaningfulChange = 5,
        norm = "میانگین در جمعیت عمومی حدود ۳ است (Kocalevent 2013) و کاهش ۵ امتیازی، بهبود واقعی به حساب می‌آید."
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
            Question("ترس از اینکه اتفاق وحشتناکی بیفتد"),
            Question(DIFFICULTY_TEXT, options = DIFFICULTY_OPTIONS, scored = false)
        ),
        listOf(
            Band(
                0, Level.HIGH, "اضطراب کم",
                "نشانه قابل‌توجهی از اضطراب نداری. تمرین تنفس و ورزش را برای روزهای پرفشار نگه دار.",
                steps = listOf(
                    "یکی از تمرین‌های تنفس جعبه ابزار را یاد بگیر تا در روز پرفشار آماده باشد.",
                    "ورزش منظم، مؤثرترین پیشگیری غیردارویی از اضطراب است."
                )
            ),
            Band(
                5, Level.MIDDLE, "اضطراب خفیف",
                "کمی اضطراب داری که برای بسیاری از آدم‌ها در دوره‌های پرفشار معمول است. با چند تغییر ساده معمولاً کمتر می‌شود.",
                steps = listOf(
                    "روزی ۵ دقیقه تنفس آرام (آه چرخه‌ای یا ۶ نفس در دقیقه) انجام بده.",
                    "کافئین را بعد از ظهر قطع کن و خواب را به ۷ ساعت برسان.",
                    "روزی ۱۵ دقیقه «وقت نگرانی» بگذار: نگرانی‌ها را بنویس و بیرون از آن زمان، به آن وقت موکولشان کن."
                )
            ),
            Band(
                10, Level.LOW, "اضطراب متوسط",
                "اضطراب در حدی است که سازندگان آزمون ارزیابی متخصص را پیشنهاد می‌کنند. درمان‌های کوتاه‌مدت مثل CBT برای اضطراب بسیار مؤثرند.",
                seekHelp = true,
                steps = listOf(
                    "با یک روان‌شناس (ترجیحاً با رویکرد CBT) وقت بگیر؛ معمولاً چند جلسه هم تفاوت محسوسی می‌دهد.",
                    "تمرین تنفس را دو بار در روز، نه فقط هنگام اضطراب، انجام بده.",
                    "از موقعیت‌هایی که از ترس از آن‌ها دوری می‌کنی فهرست بنویس؛ دوری کردن اضطراب را ماندگارتر می‌کند."
                )
            ),
            Band(
                15, Level.VERY_LOW, "اضطراب شدید",
                "اضطراب شدید است. لطفاً با یک روان‌شناس یا روان‌پزشک صحبت کن؛ لازم نیست تنهایی با آن کنار بیایی و درمان مؤثر وجود دارد.",
                seekHelp = true,
                steps = listOf(
                    "همین هفته با روان‌پزشک یا روان‌شناس وقت بگیر.",
                    "اگر تپش قلب، تنگی نفس یا درد قفسه سینه داری، یک بار هم پزشک عمومی را ببین تا علت جسمی کنار گذاشته شود.",
                    "در لحظه‌های اوج، تنفس با بازدم طولانی را انجام بده و به خودت یادآوری کن موج اضطراب معمولاً در چند دقیقه فروکش می‌کند."
                )
            )
        ),
        higherIsWorse = true,
        subscales = listOf(
            Subscale(
                "هسته اضطراب (عصبی بودن و کنترل نگرانی، GAD-2)", listOf(0, 1),
                "دو نشانه اصلی اضطراب کم است.",
                "عصبی بودن یا ناتوانی در کنترل نگرانی، دو نشانه اصلی اضطراب، پررنگ است."
            ),
            Subscale(
                "نگرانی و ترس", listOf(2, 6),
                "نگرانی‌ات در حد معمول است.",
                "ذهنت زیاد درگیر نگرانی و پیش‌بینی بدترین اتفاق است؛ نوشتن نگرانی‌ها و «وقت نگرانی» کمک می‌کند."
            ),
            Subscale(
                "تنش بدنی و تحریک‌پذیری", listOf(3, 4, 5),
                "بدنت روی‌هم‌رفته آرام است.",
                "بدنت در حالت آماده‌باش است: بی‌قراری، تنش یا زودرنجی. ورزش هوازی و تنفس آرام، مستقیم روی همین بخش اثر دارند."
            )
        ),
        meaningfulChange = 4,
        norm = "میانگین در جمعیت عمومی حدود ۳ است (Löwe 2008) و کاهش ۴ امتیازی، بهبود واقعی به حساب می‌آید."
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
            Band(
                0, Level.HIGH, "استرس کم",
                "استرست در یک ماه گذشته کم بوده است. همین تعادل را با خواب، ورزش و وقت آزاد حفظ کن.",
                steps = listOf(
                    "بنویس چه چیزی این ماه را آرام نگه داشت تا در ماه‌های شلوغ‌تر از آن محافظت کنی.",
                    "یک تمرین تنفس را یاد بگیر تا برای روزهای پرفشار آماده باشی."
                )
            ),
            Band(
                14, Level.MIDDLE, "استرس متوسط",
                "استرس متوسطی داری که برای بیشتر آدم‌ها معمول است. نگاه کن کدام بخش (درماندگی یا کمبود احساس توانمندی) بیشتر سهم دارد.",
                steps = listOf(
                    "فشارهایت را در دو ستون بنویس: «در کنترل من» و «خارج از کنترل من». روی ستون اول برنامه بریز و ستون دوم را رها کن.",
                    "کارهای بزرگ را به قدم‌های ۲۵ دقیقه‌ای بشکن.",
                    "سه بار در هفته ورزش هوازی و روزی ۵ دقیقه تنفس آرام را در برنامه‌ات بگذار."
                )
            ),
            Band(
                27, Level.LOW, "استرس زیاد",
                "استرس زیادی را تحمل می‌کنی که اگر ادامه یابد روی خواب، خلق، فشار خون و رابطه‌ها اثر می‌گذارد.",
                seekHelp = true,
                steps = listOf(
                    "همین هفته یک یا دو تعهد را لغو، واگذار یا عقب بینداز؛ بار باید کم شود، نه فقط تحمل بیشتر.",
                    "از یک نفر کمک عملی بخواه (در کار، خانه یا پول).",
                    "اگر خواب، خلق یا رابطه‌هایت به هم ریخته، با یک مشاور صحبت کن و آزمون‌های افسردگی و اضطراب را هم بزن."
                )
            )
        ),
        higherIsWorse = true,
        subscales = listOf(
            Subscale(
                "احساس درماندگی", listOf(0, 1, 2, 5, 8, 9),
                "کمتر احساس می‌کنی اوضاع از دستت خارج شده است.",
                "اغلب احساس کرده‌ای کارها از کنترلت خارج است و فشارها روی هم انباشته شده‌اند."
            ),
            Subscale(
                "کمبود احساس توانمندی", listOf(3, 4, 6, 7),
                "به توانایی‌ات در کنار آمدن با مشکلات اعتماد داری؛ این بزرگ‌ترین سپر در برابر استرس است.",
                "کمتر احساس کرده‌ای از پس مشکلات برمی‌آیی. موفقیت‌های کوچک و پشت‌سرهم، این حس را برمی‌گرداند."
            )
        ),
        meaningfulChange = 3,
        norm = "میانگین مردان در نمونه‌های بزرگ آمریکایی حدود ۱۵ بوده است (Cohen 2012)."
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
            Band(
                26, Level.HIGH, "عزت نفس بالا",
                "نگاهت به خودت محکم و مثبت است. عزت نفس سالم یعنی ارزش خودت را بدانی بدون اینکه خودت را برتر از دیگران ببینی.",
                steps = listOf(
                    "به ارزش‌هایت وفادار بمان؛ عزت نفس پایدار از هماهنگی رفتار و ارزش‌ها می‌آید.",
                    "به یک نفر کمک کن مهارتی را یاد بگیرد؛ این هم به او کمک می‌کند هم به حس ارزشمندی تو."
                )
            ),
            Band(
                15, Level.MIDDLE, "عزت نفس در محدوده معمول",
                "پایه خوبی داری. بیشتر آدم‌ها در همین محدوده‌اند. نگاه کن کدام بخش (نگاه مثبت یا خودسرزنشی) پایین‌تر است.",
                steps = listOf(
                    "هر شب سه کار خوبی را که امروز انجام دادی (هرچقدر کوچک) در دفترچه بنویس.",
                    "وقتی خودت را سرزنش می‌کنی، بپرس: «به دوستم در همین وضعیت چه می‌گفتم؟» و همان را به خودت بگو.",
                    "یک قول کوچک به خودت بده و انجامش بده؛ اعتماد به خود از قول‌های نگه‌داشته ساخته می‌شود."
                )
            ),
            Band(
                0, Level.LOW, "عزت نفس پایین‌تر از معمول",
                "این نتیجه یعنی این روزها با خودت سخت‌گیری. عزت نفس پایین قابل‌تغییر است و اغلب با افسردگی یا اضطراب همراه است.",
                seekHelp = true,
                steps = listOf(
                    "از محور «عزت نفس» و تمرین مهربانی با خود شروع کن.",
                    "آزمون‌های افسردگی و اضطراب را هم بزن تا تصویر کامل‌تری داشته باشی.",
                    "اگر این احساس ماندگار است، صحبت با یک مشاور بسیار کمک می‌کند؛ CBT برای تغییر باورهای منفی درباره خود مؤثر است."
                )
            )
        ),
        subscales = listOf(
            Subscale(
                "نگاه مثبت به خود", listOf(0, 2, 3, 6, 9),
                "کمتر ویژگی‌های خوبت را می‌بینی؛ تمرین «سه کار خوب امروز» کمک می‌کند.",
                "ویژگی‌ها و ارزش خودت را می‌بینی و قبول داری."
            ),
            Subscale(
                "دوری از خودسرزنشی", listOf(1, 4, 5, 7, 8),
                "صدای منتقد درونت بلند است و زیاد خودت را سرزنش می‌کنی.",
                "کمتر خودت را تحقیر یا سرزنش می‌کنی."
            )
        ),
        meaningfulChange = 3,
        norm = "میانگین جهانی در پژوهش ۵۳ کشور حدود ۲۰ تا ۲۲ از ۳۰ بوده است (Schmitt و Allik 2005)."
    )

    /** The 50-item IPIP Big-Five Factor Markers, in the published order and keying. */
    val PERSONALITY = Scale(
        Kind.PERSONALITY,
        "هر جمله را بخوان و بگو چقدر درباره‌ات درست است؛ خودت را همان‌طور که معمولاً هستی توصیف کن، نه آن‌طور که دوست داری باشی یا در آینده می‌خواهی باشی. هیچ ویژگی‌ای خوب یا بد نیست.",
        TRUE5,
        listOf(
            Question("در جمع‌ها معمولاً شور و حال مجلسم.", trait = Trait.EXTRAVERSION),
            Question("نگرانی چندانی برای دیگران ندارم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("همیشه آماده و مهیا هستم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("زود استرس می‌گیرم.", reversed = true, trait = Trait.STABILITY),
            Question("دایره واژگان گسترده‌ای دارم.", trait = Trait.OPENNESS),
            Question("زیاد حرف نمی‌زنم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("به آدم‌ها علاقه‌مندم.", trait = Trait.AGREEABLENESS),
            Question("وسایلم را این‌طرف و آن‌طرف رها می‌کنم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("بیشتر وقت‌ها آرامم.", trait = Trait.STABILITY),
            Question("فهمیدن ایده‌های انتزاعی برایم سخت است.", reversed = true, trait = Trait.OPENNESS),
            Question("کنار آدم‌ها احساس راحتی می‌کنم.", trait = Trait.EXTRAVERSION),
            Question("به دیگران توهین می‌کنم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("به جزئیات توجه می‌کنم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("نگران چیزهای مختلف هستم.", reversed = true, trait = Trait.STABILITY),
            Question("تخیل قوی و زنده‌ای دارم.", trait = Trait.OPENNESS),
            Question("ترجیح می‌دهم در حاشیه بمانم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("با احساسات دیگران همدردی می‌کنم.", trait = Trait.AGREEABLENESS),
            Question("کارها را به هم می‌ریزم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("به‌ندرت احساس غم و دلگرفتگی می‌کنم.", trait = Trait.STABILITY),
            Question("به ایده‌های انتزاعی علاقه‌ای ندارم.", reversed = true, trait = Trait.OPENNESS),
            Question("شروع‌کننده گفت‌وگوها هستم.", trait = Trait.EXTRAVERSION),
            Question("به مشکلات دیگران علاقه‌ای ندارم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("کارهای روزمره را بی‌معطلی انجام می‌دهم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("زود به هم می‌ریزم.", reversed = true, trait = Trait.STABILITY),
            Question("ایده‌های عالی به ذهنم می‌رسد.", trait = Trait.OPENNESS),
            Question("حرف زیادی برای گفتن ندارم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("دل نازکی دارم.", trait = Trait.AGREEABLENESS),
            Question("اغلب یادم می‌رود وسایل را سر جایشان برگردانم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("زود ناراحت می‌شوم.", reversed = true, trait = Trait.STABILITY),
            Question("قدرت تخیل خوبی ندارم.", reversed = true, trait = Trait.OPENNESS),
            Question("در مهمانی‌ها با آدم‌های مختلف زیادی حرف می‌زنم.", trait = Trait.EXTRAVERSION),
            Question("واقعاً به دیگران علاقه‌ای ندارم.", reversed = true, trait = Trait.AGREEABLENESS),
            Question("نظم و ترتیب را دوست دارم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("حالم زیاد عوض می‌شود.", reversed = true, trait = Trait.STABILITY),
            Question("چیزها را زود می‌فهمم.", trait = Trait.OPENNESS),
            Question("دوست ندارم توجه دیگران را به خودم جلب کنم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("برای دیگران وقت می‌گذارم.", trait = Trait.AGREEABLENESS),
            Question("از زیر مسئولیت‌هایم شانه خالی می‌کنم.", reversed = true, trait = Trait.CONSCIENTIOUSNESS),
            Question("نوسان خلق زیادی دارم.", reversed = true, trait = Trait.STABILITY),
            Question("از واژه‌های دشوار و دقیق استفاده می‌کنم.", trait = Trait.OPENNESS),
            Question("مرکز توجه بودن برایم اشکالی ندارد.", trait = Trait.EXTRAVERSION),
            Question("احساسات دیگران را حس می‌کنم.", trait = Trait.AGREEABLENESS),
            Question("طبق برنامه پیش می‌روم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("زود کلافه و عصبانی می‌شوم.", reversed = true, trait = Trait.STABILITY),
            Question("برای فکر کردن درباره چیزها وقت می‌گذارم.", trait = Trait.OPENNESS),
            Question("پیش غریبه‌ها ساکتم.", reversed = true, trait = Trait.EXTRAVERSION),
            Question("کاری می‌کنم که آدم‌ها کنارم احساس راحتی کنند.", trait = Trait.AGREEABLENESS),
            Question("در کارم دقیق و سخت‌گیرم.", trait = Trait.CONSCIENTIOUSNESS),
            Question("اغلب دلگرفته و غمگینم.", reversed = true, trait = Trait.STABILITY),
            Question("پر از ایده‌ام.", trait = Trait.OPENNESS)
        )
    )

    /**
     * The 20-item Mini-IPIP used before 4.1. Kept only so personality results saved then still
     * show their profile; new tests use [PERSONALITY].
     */
    internal val LEGACY_PERSONALITY = Scale(
        Kind.PERSONALITY,
        PERSONALITY.intro,
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
            Band(
                26, Level.HIGH, "تاب‌آوری بالا",
                "میانگین پاسخ‌هایت بالای ۴٫۳ است: پس از سختی‌ها زود دوباره سرپا می‌شوی.",
                steps = listOf(
                    "این توانایی را با کمک به دیگرانی که در سختی‌اند به کار بگیر.",
                    "عادت‌هایی که پشتیبان تاب‌آوری‌اند (خواب، ورزش، رابطه‌های حمایتگر) را در روزهای خوب هم نگه دار."
                )
            ),
            Band(
                18, Level.MIDDLE, "تاب‌آوری معمول",
                "میانگین پاسخ‌هایت بین ۳ تا ۴٫۳ است: مثل بیشتر آدم‌ها، پس از سختی‌ها با کمی زمان سرپا می‌شوی.",
                steps = listOf(
                    "پس از هر اتفاق سخت، بنویس چه چیزی کمکت کرد سرپا شوی؛ این فهرست جعبه‌ابزار تاب‌آوری توست.",
                    "رابطه با دو سه نفر که در سختی می‌توانی رویشان حساب کنی را زنده نگه دار.",
                    "به جای «چرا این برای من اتفاق افتاد؟» بپرس «قدم بعدی کوچک من چیست؟»."
                )
            ),
            Band(
                0, Level.LOW, "تاب‌آوری پایین‌تر",
                "میانگین پاسخ‌هایت زیر ۳ است: این روزها برگشتن از سختی‌ها برایت دشوار است. تاب‌آوری مهارت است و یاد گرفته می‌شود.",
                seekHelp = true,
                steps = listOf(
                    "تمرین‌های محور «آرامش و استرس» را روزانه انجام بده.",
                    "با یک دوست مورد اعتماد درباره سختی اخیرت حرف بزن؛ پنهان کردن، بازیابی را کند می‌کند.",
                    "مشاور می‌تواند مهارت‌های کنار آمدن (حل مسئله، بازسازی فکر، پذیرش) را یادت بدهد."
                )
            )
        ),
        meaningfulChange = 2,
        norm = "میانگین پاسخ‌ها در نمونه‌های سازندگان حدود ۳٫۷ از ۵ بوده است (Smith 2008)."
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
            Band(
                33, Level.HIGH, "خودکارآمدی بالا",
                "به توانایی‌ات در حل مشکلات اعتماد داری و از میانگین جهانی بالاتری.",
                steps = listOf(
                    "از این اعتماد برای یک هدف بزرگ‌تر از معمول استفاده کن.",
                    "حواست باشد اعتماد بالا، تو را از برنامه‌ریزی و کمک گرفتن بی‌نیاز نشان ندهد."
                )
            ),
            Band(
                25, Level.MIDDLE, "خودکارآمدی در محدوده معمول",
                "باور خوبی به توانایی‌هایت داری و نزدیک میانگین جهانی هستی.",
                steps = listOf(
                    "هدف‌هایت را به قدم‌های کوچک و قابل‌انجام بشکن؛ هر موفقیت کوچک باور را قوی‌تر می‌کند.",
                    "موفقیت‌هایت را بنویس؛ ذهن، شکست‌ها را بهتر از موفقیت‌ها به یاد می‌آورد."
                )
            ),
            Band(
                10, Level.LOW, "خودکارآمدی پایین‌تر از معمول",
                "این روزها به توانایی‌ات کمتر اعتماد داری. بندورا نشان داد باور به توانایی با تجربه موفقیت، دیدن الگو و تشویق ساخته می‌شود.",
                steps = listOf(
                    "یک هدف خیلی کوچک و مطمئن برای این هفته انتخاب کن و انجامش بده.",
                    "از کسی که مهارتی را که می‌خواهی دارد بپرس چطور شروع کرد.",
                    "اگر این حس با غم یا بی‌انگیزگی همراه است، آزمون افسردگی را هم بزن."
                )
            )
        ),
        meaningfulChange = 3,
        norm = "میانگین جهانی در ۲۵ کشور حدود ۲۹ بوده است (Scholz 2002)."
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
        // Diener's seven published ranges.
        listOf(
            Band(
                31, Level.HIGH, "بسیار راضی",
                "زندگی‌ات را بسیار دوست داری و حس می‌کنی خوب پیش می‌رود. این به معنای نبود مشکل نیست، بلکه یعنی منابع زیادی برای رضایت داری.",
                steps = listOf(
                    "رضایت با رشد هم‌خوان است؛ برای خودت یک هدف تازه و معنادار بگذار.",
                    "بخشی از وقت یا منابعت را به کسانی بده که در شرایط سخت‌ترند."
                )
            ),
            Band(
                26, Level.HIGH, "راضی",
                "بیشتر بخش‌های زندگی‌ات (کار، رابطه، سلامت، وقت آزاد) خوب پیش می‌رود.",
                steps = listOf(
                    "ببین کدام بخش زندگی‌ات کمی عقب‌تر است و برایش یک قدم کوچک بنویس.",
                    "قدردانی منظم (دفترچه سه اتفاق خوب) این رضایت را پایدارتر می‌کند."
                )
            ),
            Band(
                21, Level.MIDDLE, "کمی راضی",
                "زندگی‌ات روی‌هم‌رفته خوب است ولی بخش‌هایی هست که می‌خواهی بهتر شود. بیشتر مردم در کشورهای توسعه‌یافته در همین حدود هستند.",
                steps = listOf(
                    "از چهار بخش کار، رابطه، سلامت و پول، کدام بیشترین اثر را دارد؟ همان را انتخاب کن.",
                    "برایش یک برنامه «اگر… آنگاه…» بنویس، مثلاً: «اگر شنبه شد، یک ساعت روی رزومه‌ام کار می‌کنم»."
                )
            ),
            Band(
                20, Level.MIDDLE, "میانه",
                "نه راضی هستی نه ناراضی؛ زندگی‌ات ترکیبی از بخش‌های خوب و بخش‌هایی است که باید بهتر شوند.",
                steps = listOf(
                    "دو بخشی را که بیشترین نارضایتی را دارند بنویس و برای یکی همین هفته قدم بردار.",
                    "گاهی رضایت با تغییر انتظارها هم بهتر می‌شود؛ ببین کدام انتظارت از مقایسه با دیگران آمده است."
                )
            ),
            Band(
                15, Level.LOW, "کمی ناراضی",
                "در چند بخش مهم زندگی مشکل کوچک ولی مهم داری، یا در یک بخش مشکل بزرگ. گاهی هم نتیجه یک اتفاق اخیر است.",
                steps = listOf(
                    "اگر علت، اتفاقی اخیر است (از دست دادن کار، جدایی)، به خودت زمان بده و از دیگران کمک بخواه.",
                    "اگر نارضایتی قدیمی است، یک بخش را انتخاب کن و تغییر کوچک ولی واقعی بساز.",
                    "اگر بی‌انگیزگی یا غم هم داری، آزمون افسردگی را بزن."
                )
            ),
            Band(
                10, Level.LOW, "ناراضی",
                "از زندگی‌ات به‌طور قابل‌توجهی ناراضی هستی. سازنده آزمون می‌گوید در این بازه معمولاً چند بخش زندگی خوب پیش نمی‌رود و تغییر لازم است.",
                seekHelp = true,
                steps = listOf(
                    "با یک مشاور یا آدم باتجربه مورد اعتماد درباره اینکه از کجا شروع کنی حرف بزن.",
                    "آزمون افسردگی را بزن؛ نارضایتی ماندگار گاهی نشانه افسردگی است.",
                    "یک تغییر کوچک ولی واقعی در یک بخش (ورزش، رابطه، کار) همین هفته شروع کن."
                )
            ),
            Band(
                5, Level.VERY_LOW, "بسیار ناراضی",
                "این روزها از زندگی‌ات خیلی ناراضی هستی. این نمره اغلب با یک اتفاق سخت یا افسردگی همراه است و کمک گرفتن واقعاً تفاوت ایجاد می‌کند.",
                seekHelp = true,
                steps = listOf(
                    "همین هفته با یک مشاور یا روان‌شناس صحبت کن یا با صدای مشاور (۱۴۸۰) تماس بگیر.",
                    "آزمون افسردگی را هم بزن و نتیجه را به متخصص نشان بده.",
                    "به یک نفر نزدیک بگو حالت چطور است؛ تنها نماندن مهم‌ترین قدم اول است."
                )
            )
        ),
        subscales = listOf(
            Subscale(
                "رضایت از امروز", listOf(0, 1, 2),
                "از شرایط فعلی زندگی‌ات راضی نیستی.",
                "از شرایط فعلی زندگی‌ات راضی هستی."
            ),
            Subscale(
                "نگاه به گذشته", listOf(3, 4),
                "حس می‌کنی هنوز به چیزهای مهمی که می‌خواستی نرسیده‌ای یا مسیرت را عوض می‌کردی.",
                "از مسیری که تا امروز آمده‌ای راضی هستی."
            )
        ),
        meaningfulChange = 3,
        norm = "بیشتر مردم در کشورهای توسعه‌یافته نمره ۲۰ تا ۲۴ دارند (Pavot و Diener 2008)."
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
            Band(
                0, Level.HIGH, "احساس تنهایی کم",
                "رابطه‌هایت حس پیوند و همراهی به تو می‌دهند. آن‌ها را با دیدار و تماس منظم زنده نگه دار.",
                steps = listOf(
                    "هفته‌ای یک دیدار حضوری با دوست یا خانواده را در تقویمت ثابت کن.",
                    "حواست به دوستی باشد که مدتی است از او خبر نداری؛ شاید او تنهاست."
                )
            ),
            Band(
                6, Level.LOW, "احساس تنهایی",
                "این روزها احساس تنهایی می‌کنی؛ حسی رایج که پژوهش هولت‌لانستاد نشان داد اثرش بر سلامت هم‌اندازه عوامل خطر شناخته‌شده است.",
                steps = listOf(
                    "همین هفته با یک دوست قرار حضوری بگذار؛ پیام و تماس خوب است، ولی دیدار اثر بیشتری دارد.",
                    "به یک جمع منظم بپیوند (باشگاه، کلاس، گروه کوه، کار داوطلبانه)؛ تکرار دیدار، دوستی می‌سازد.",
                    "زمان شبکه‌های اجتماعی را به حدود ۳۰ دقیقه در روز کم کن؛ این کار تنهایی را کمتر کرده است."
                )
            ),
            Band(
                8, Level.VERY_LOW, "احساس تنهایی زیاد",
                "تنهایی زیادی را تحمل می‌کنی. این حس درمان‌پذیر است و لازم نیست با آن تنها بمانی.",
                seekHelp = true,
                steps = listOf(
                    "یک قدم کوچک و مطمئن بردار: به یک آشنای قدیمی پیام بده و قرار کوتاهی بگذار.",
                    "پیوستن به یک گروه با فعالیت مشترک، آسان‌ترین راه شروع رابطه تازه است.",
                    "صحبت با یک مشاور کمک می‌کند؛ گاهی تنهایی با افسردگی همراه است و آزمون افسردگی را هم بزن."
                )
            )
        ),
        higherIsWorse = true,
        meaningfulChange = 1,
        norm = "در نمونه‌های بزرگ، نمره ۶ و بالاتر معمولاً «احساس تنهایی» در نظر گرفته می‌شود (Steptoe 2013)."
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
            Band(
                17, Level.HIGH, "رضایت بالا از رابطه",
                "رابطه‌تان گرم و رضایت‌بخش است.",
                steps = listOf(
                    "قدردانی مشخص روزانه، گفت‌وگوی بی‌گوشی و قرار دونفره منظم را نگه دار.",
                    "رابطه خوب هم نیاز به نگهداری دارد؛ هر چند ماه یک کار تازه دونفره امتحان کنید."
                )
            ),
            Band(
                14, Level.MIDDLE, "رضایت خوب",
                "رابطه‌تان روی‌هم‌رفته رضایت‌بخش است و بالای نقطه برش نارضایتی قرار دارد.",
                steps = listOf(
                    "ابزار «سؤال‌های گفت‌وگوی دونفره» را امتحان کن تا نزدیک‌تر شوید.",
                    "از همسرت بپرس «چه کاری از من باعث می‌شود بیشتر احساس دوست‌داشته‌شدن کنی؟» و همان را انجام بده."
                )
            ),
            Band(
                0, Level.LOW, "نارضایتی قابل‌توجه",
                "نمره‌ات زیر نقطه برش نارضایتی زوجین (۱۳٫۵) است. این به معنای پایان رابطه نیست؛ نشان می‌دهد رابطه به توجه جدی نیاز دارد.",
                seekHelp = true,
                steps = listOf(
                    "محور «مدیریت اختلاف» و ابزار «شروع نرم» را ببین و در گفت‌وگوی بعدی از آن استفاده کن.",
                    "مشاوره زوج را جدی بگیر؛ پژوهش‌ها نشان می‌دهد هرچه زودتر شروع شود، اثرش بیشتر است.",
                    "اگر ترس، تحقیر یا خشونت در رابطه هست، اول به امنیت خودت و همسرت فکر کن و کمک تخصصی بگیر."
                )
            )
        ),
        meaningfulChange = 2,
        norm = "سازندگان، نمره زیر ۱۳٫۵ را نشانه نارضایتی قابل‌توجه زوجین می‌دانند (Funk و Rogge 2007)."
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
        return answers.withIndex().filter { s.questions[it.index].scored }.sumOf { (i, a) -> s.scored(i, a) } * s.multiplier
    }

    /**
     * The five factor scores of the personality test, or null if incomplete. Accepts the
     * current 50-item test (each factor 10..50) and results saved from the 20-item test before.
     */
    fun profile(answers: List<Int>): List<TraitScore>? {
        val s = when (answers.size) {
            PERSONALITY.questions.size -> PERSONALITY
            LEGACY_PERSONALITY.questions.size -> LEGACY_PERSONALITY
            else -> return null
        }
        if (!s.valid(answers)) return null
        return Trait.values().map { trait ->
            val items = s.questions.indices.filter { s.questions[it].trait == trait }
            TraitScore(trait, items.sumOf { s.scored(it, answers[it]) }, max = items.size * 5, min = items.size)
        }
    }

    fun interpret(kind: Kind, score: Int): Result {
        val s = scale(kind)
        if (s.bands.isEmpty()) {
            return Result(
                Level.MIDDLE, "نیم‌رخ شخصیت تو",
                "پنج ویژگی اصلی شخصیتت را در زیر ببین. درصد هر ویژگی جای تو را بین کمترین و بیشترین نمره ممکن نشان می‌دهد. شخصیت خوب یا بد ندارد؛ شناختنش کمک می‌کند نقاط قوتت را بهتر به کار ببری.",
                false
            )
        }
        val band = s.bands.filter { score >= it.min }.maxByOrNull { it.min } ?: s.bands.minBy { it.min }
        return Result(band.level, band.title, band.text, band.seekHelp, band.steps)
    }

    /** Scores of the parts of the scale, or an empty list when it has none or answers are incomplete. */
    fun subscores(kind: Kind, answers: List<Int>): List<SubScore> {
        val s = scale(kind)
        if (!s.valid(answers) || s.isProfile) return emptyList()
        return s.subscales.map { sub ->
            val score = sub.items.sumOf { s.scored(it, answers[it]) }
            val min = s.minOf(sub.items)
            val max = s.maxOf(sub.items)
            val position = (score - min).toDouble() / (max - min).coerceAtLeast(1)
            // The text for the end the user is nearer; for symptom scales, the high end is worse.
            val text = if (position >= 0.5) sub.high else sub.low
            SubScore(sub.title, score, min, max, text)
        }
    }

    /**
     * The answers that weighed most: up to [limit] that pulled the result towards the worse end
     * (in the worse third of their range) and up to [limit] that held it up (in the best third).
     */
    fun insights(kind: Kind, answers: List<Int>, limit: Int = 3): Insights {
        val s = scale(kind)
        if (!s.valid(answers) || s.isProfile) return Insights(emptyList(), emptyList())
        val scored = s.questions.indices.filter { s.questions[it].scored }
        val burdens = scored.associateWith { s.burden(it, answers[it]) }
        fun note(i: Int) = ItemNote(s.questions[i].text, s.optionsFor(i)[answers[i]].label)
        val concerns = scored.filter { burdens.getValue(it) >= 2.0 / 3 }
            .sortedByDescending { burdens.getValue(it) }.take(limit).map(::note)
        val strengths = scored.filter { burdens.getValue(it) <= 1.0 / 3 }
            .sortedBy { burdens.getValue(it) }.take(limit).map(::note)
        return Insights(concerns, strengths)
    }

    /** Index of the PHQ-9 item on thoughts of death or self-harm. */
    private const val PHQ_SELF_HARM = 8

    /**
     * Warning signs the authors define, beyond the total:
     *  - PHQ-9: the PHQ-2 core (items 1–2, 3 or more), the diagnostic algorithm of the PHQ
     *    (five or more symptoms on more than half the days, one of them a core symptom; item 9
     *    counts at any frequency) and the closing question on how hard life has become.
     *  - GAD-7: the GAD-2 core (items 1–2, 3 or more) and the closing question.
     *  - WHO-5: any item answered "sometimes" or "never", which the WHO also treats as a sign.
     */
    fun flags(kind: Kind, answers: List<Int>): List<Flag> {
        val s = scale(kind)
        if (!s.valid(answers)) return emptyList()
        fun v(i: Int) = s.scored(i, answers[i])
        return buildList {
            when (kind) {
                Kind.DEPRESSION -> {
                    val core = v(0) + v(1)
                    val present = (0..PHQ_SELF_HARM).count { i -> if (i == PHQ_SELF_HARM) v(i) >= 1 else v(i) >= 2 }
                    val coreOften = v(0) >= 2 || v(1) >= 2
                    if (present >= 5 && coreOften) {
                        add(
                            Flag(
                                "الگوی پاسخ‌ها با افسردگی اساسی هم‌خوان است",
                                "دست‌کم پنج نشانه، بیشتر از نیمی از روزها و از جمله کم‌علاقگی یا غم، داشته‌ای. سازندگان PHQ این الگو را نشانه احتمال افسردگی اساسی می‌دانند که باید متخصص بررسی کند.",
                                true
                            )
                        )
                    } else if (present >= 2 && coreOften) {
                        add(
                            Flag(
                                "چند نشانه افسردگی با هم دیده می‌شود",
                                "دو تا چهار نشانه، بیشتر از نیمی از روزها و از جمله کم‌علاقگی یا غم، داشته‌ای. اگر چند هفته ادامه داشته، با یک متخصص مشورت کن.",
                                true
                            )
                        )
                    } else if (core >= 3) {
                        add(
                            Flag(
                                "غربالگری دوسؤالی (PHQ-2) مثبت است",
                                "جمع دو سؤال اول (کم‌علاقگی و غم) ۳ یا بیشتر است؛ در غربالگری‌های پزشکی، این نقطه شروع بررسی دقیق‌تر است.",
                                false
                            )
                        )
                    }
                    difficultyFlag(s, answers)?.let(::add)
                }
                Kind.ANXIETY -> {
                    if (v(0) + v(1) >= 3) {
                        add(
                            Flag(
                                "غربالگری دوسؤالی (GAD-2) مثبت است",
                                "جمع دو سؤال اول (عصبی بودن و کنترل نگرانی) ۳ یا بیشتر است؛ در غربالگری‌های پزشکی، این نقطه شروع بررسی دقیق‌تر است.",
                                false
                            )
                        )
                    }
                    difficultyFlag(s, answers)?.let(::add)
                }
                Kind.WELL_BEING -> {
                    val low = s.questions.indices.filter { v(it) <= 1 }
                    if (low.isNotEmpty()) {
                        add(
                            Flag(
                                "دست‌کم یک بخش از حالت خیلی پایین است",
                                "به ${low.size} جمله «گاهی» یا «هرگز» جواب دادی (${low.joinToString("، ") { WELL_BEING_SHORT[it] }}). حتی وقتی نمره کل خوب است، سازمان جهانی بهداشت این را نشانه‌ای برای توجه بیشتر می‌داند.",
                                false
                            )
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private val WELL_BEING_SHORT = listOf("شادی", "آرامش", "انرژی", "خواب و سرحالی", "علاقه به زندگی")

    private fun difficultyFlag(s: Scale, answers: List<Int>): Flag? {
        val i = s.questions.indexOfFirst { !it.scored }
        if (i < 0) return null
        val v = s.scored(i, answers[i])
        return when {
            v >= 2 -> Flag(
                "این نشانه‌ها زندگی روزمره‌ات را سخت کرده‌اند",
                "گفتی این مشکلات کار، خانه یا رابطه‌هایت را «${s.optionsFor(i)[answers[i]].label}». وقتی نشانه‌ها کارکرد روزانه را مختل می‌کنند، کمک تخصصی حتی با نمره متوسط هم ارزشمند است.",
                true
            )
            v == 1 -> Flag(
                "اثر کم بر زندگی روزمره",
                "گفتی این مشکلات زندگی روزمره‌ات را «کمی سخت» کرده‌اند. حواست به آن باشد و اگر بیشتر شد، کمک بگیر.",
                false
            )
            else -> null
        }
    }

    /**
     * How [current] compares with the [previous] result of the same test: better, worse or the
     * same, and whether the difference reaches the scale's [Scale.meaningfulChange].
     */
    fun change(kind: Kind, previous: Int, current: Int): Change {
        val s = scale(kind)
        val delta = current - previous
        val better = if (s.higherIsWorse) delta < 0 else delta > 0
        val size = kotlin.math.abs(delta)
        val meaningful = size >= s.meaningfulChange
        val direction = when {
            delta == 0 -> Direction.SAME
            better -> Direction.BETTER
            else -> Direction.WORSE
        }
        val amount = toPersian(size)
        val text = when {
            direction == Direction.SAME -> "نمره‌ات با آزمون قبلی یکی است."
            !meaningful -> "نمره‌ات $amount امتیاز ${if (better) "بهتر" else "پایین‌تر"} از آزمون قبلی است؛ این تغییر کوچک و در حد نوسان طبیعی است."
            better -> "نمره‌ات $amount امتیاز بهتر از آزمون قبلی است؛ این تغییر آن‌قدر بزرگ است که بهبود واقعی به حساب می‌آید."
            else -> "نمره‌ات $amount امتیاز بدتر از آزمون قبلی است؛ این تغییر واقعی است و ارزش دارد به علتش فکر کنی."
        }
        return Change(delta, direction, meaningful, text)
    }

    private fun toPersian(n: Int): String = n.toString().map { c -> if (c in '0'..'9') '۰' + (c - '0') else c }.joinToString("")

    /** The resilience score as the authors report it: the mean answer, 1.00..5.00. */
    fun resilienceMean(score: Int): Double = score / RESILIENCE.questions.size.toDouble()

    /**
     * True when an answer needs an immediate, caring response no matter the total: any thought
     * of death or self-harm on PHQ-9 item 9.
     */
    fun needsUrgentHelp(kind: Kind, answers: List<Int>): Boolean {
        if (kind != Kind.DEPRESSION) return false
        val s = scale(kind)
        val a = answers.getOrNull(PHQ_SELF_HARM) ?: return false
        return a in s.optionsFor(PHQ_SELF_HARM).indices && s.scored(PHQ_SELF_HARM, a) > 0
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

    /** How to get the most accurate result, shown before the first question. */
    const val HOW_TO =
        "در جای آرام و بدون عجله جواب بده، به اولین پاسخ صادقانه‌ات اعتماد کن و به بازه زمانی‌ای که آزمون می‌پرسد فکر کن. برای دیدن روند، هر آزمون را با فاصله (مثلاً هر دو تا چهار هفته) تکرار کن."

    /** Answers kept as a compact digit string, e.g. "0123…". */
    fun encodeAnswers(answers: List<Int>): String = answers.joinToString("") { it.coerceIn(0, 9).toString() }

    fun decodeAnswers(text: String?): List<Int> = text.orEmpty().mapNotNull { it.digitToIntOrNull() }
}
