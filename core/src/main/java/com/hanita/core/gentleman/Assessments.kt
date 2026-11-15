package com.hanita.core.gentleman

/**
 * Two short, validated self-checks the user can repeat to see change over time:
 *  - the Rosenberg Self-Esteem Scale (10 items, 0..30), and
 *  - the WHO-5 Well-Being Index (5 items, 0..100).
 *
 * They are for self-awareness only, never a diagnosis. A low WHO-5 score points the user to a
 * professional, which is what the WHO-5 literature recommends.
 */
object Assessments {

    enum class Kind(val code: String, val title: String, val subtitle: String, val sourceId: String) {
        SELF_ESTEEM("rses", "آزمون عزت نفس", "مقیاس ۱۰ سؤالی روزنبرگ", "rosenberg"),
        WELL_BEING("who5", "آزمون بهزیستی", "شاخص ۵ سؤالی سازمان جهانی بهداشت", "who5");

        companion object {
            fun fromCode(code: String?): Kind? = values().firstOrNull { it.code == code }
        }
    }

    data class Question(val text: String, val reversed: Boolean = false)

    data class Scale(
        val kind: Kind,
        val intro: String,
        /** Answer labels from the highest value to the lowest. */
        val options: List<String>,
        val questions: List<Question>,
        val maxScore: Int
    ) {
        /** Value of an option index (0 = first label) before reverse scoring. */
        fun optionValue(index: Int): Int = options.size - 1 - index
    }

    enum class Level { HIGH, MIDDLE, LOW, VERY_LOW }

    data class Result(val level: Level, val title: String, val text: String, val seekHelp: Boolean)

    val SELF_ESTEEM = Scale(
        Kind.SELF_ESTEEM,
        "هر جمله را بخوان و بگو چقدر با آن موافقی. جواب درست یا غلط وجود ندارد؛ اولین جوابی که به ذهنت می‌رسد معمولاً دقیق‌تر است.",
        listOf("کاملاً موافقم", "موافقم", "مخالفم", "کاملاً مخالفم"),
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
        maxScore = 30
    )

    val WELL_BEING = Scale(
        Kind.WELL_BEING,
        "در دو هفته گذشته، هر جمله چقدر درباره‌ات درست بوده است؟",
        listOf("همیشه", "بیشتر اوقات", "بیش از نیمی از اوقات", "کمتر از نیمی از اوقات", "گاهی", "هرگز"),
        listOf(
            Question("شاد و سرحال بوده‌ام."),
            Question("آرام و آسوده بوده‌ام."),
            Question("پرانرژی و فعال بوده‌ام."),
            Question("صبح‌ها سرحال و استراحت‌کرده بیدار شده‌ام."),
            Question("زندگی روزمره‌ام پر از چیزهایی بوده که برایم جالب‌اند.")
        ),
        maxScore = 100
    )

    fun scale(kind: Kind): Scale = when (kind) {
        Kind.SELF_ESTEEM -> SELF_ESTEEM
        Kind.WELL_BEING -> WELL_BEING
    }

    /**
     * Total score from the chosen option index of each question (0 = the first label).
     * Returns null unless every question is answered with a valid option.
     */
    fun score(kind: Kind, answers: List<Int>): Int? {
        val s = scale(kind)
        if (answers.size != s.questions.size || answers.any { it !in s.options.indices }) return null
        val raw = answers.mapIndexed { i, a ->
            val v = s.optionValue(a)
            if (s.questions[i].reversed) s.options.size - 1 - v else v
        }.sum()
        return when (kind) {
            Kind.SELF_ESTEEM -> raw
            Kind.WELL_BEING -> raw * 4
        }
    }

    fun interpret(kind: Kind, score: Int): Result = when (kind) {
        Kind.SELF_ESTEEM -> when {
            score > 25 -> Result(
                Level.HIGH, "عزت نفس بالا",
                "نگاهت به خودت محکم و مثبت است. برای پایدار ماندنش، به ارزش‌هایت وفادار بمان و به دیگران هم کمک کن رشد کنند.",
                seekHelp = false
            )
            score >= 15 -> Result(
                Level.MIDDLE, "عزت نفس در محدوده معمول",
                "پایه خوبی داری. تمرین‌های محور «عزت نفس» و دفترچه «سه اتفاق خوب» می‌توانند آن را محکم‌تر کنند.",
                seekHelp = false
            )
            else -> Result(
                Level.LOW, "عزت نفس پایین‌تر از معمول",
                "این نتیجه یعنی این روزها با خودت سخت‌گیری. از محور «عزت نفس» و تمرین مهربانی با خود شروع کن. اگر این احساس ماندگار است، صحبت با یک مشاور بسیار کمک می‌کند.",
                seekHelp = true
            )
        }
        Kind.WELL_BEING -> when {
            score > 50 -> Result(
                Level.HIGH, "بهزیستی خوب",
                "در دو هفته گذشته حالت روی‌هم‌رفته خوب بوده است. عادت‌هایی را که به این حال کمک کرده‌اند نگه دار.",
                seekHelp = false
            )
            score > 28 -> Result(
                Level.LOW, "بهزیستی پایین",
                "حالت در دو هفته گذشته پایین‌تر از حد مطلوب بوده است. خواب، تحرک و گفت‌وگو با یک دوست را جدی بگیر و اگر ادامه داشت با یک مشاور صحبت کن.",
                seekHelp = true
            )
            else -> Result(
                Level.VERY_LOW, "بهزیستی خیلی پایین",
                "این نتیجه می‌تواند نشانه افسردگی باشد. لطفاً با یک روان‌شناس یا پزشک صحبت کن؛ کمک گرفتن نشانه قدرت است.",
                seekHelp = true
            )
        }
    }

    /** Where to find help in Iran; shown with every result that recommends it. */
    const val HELP_TEXT =
        "این آزمون جایگزین تشخیص متخصص نیست. برای مشاوره رایگان تلفنی می‌توانی با صدای مشاور بهزیستی (۱۴۸۰) تماس بگیری و در شرایط اضطراری با اورژانس اجتماعی (۱۲۳) یا اورژانس (۱۱۵)."

    /** Answers kept as a compact digit string, e.g. "0123…". */
    fun encodeAnswers(answers: List<Int>): String = answers.joinToString("") { it.coerceIn(0, 9).toString() }

    fun decodeAnswers(text: String?): List<Int> = text.orEmpty().mapNotNull { it.digitToIntOrNull() }
}
