package com.hanita.core.gentleman

/**
 * The practical tools: breathing patterns, the 50/30/20 budget, an apology builder, a "soft
 * start-up" builder for hard talks, conversation questions and a preparation checklist.
 * Everything here is pure logic so it can be tested without a phone.
 */
object Toolkit {

    // ---- Breathing ----

    data class Phase(val label: String, val seconds: Int, val kind: Kind) {
        enum class Kind { INHALE, HOLD, EXHALE }
    }

    data class Breathing(
        val id: String,
        val title: String,
        val subtitle: String,
        val phases: List<Phase>,
        val sourceId: String
    ) {
        val cycleSeconds: Int get() = phases.sumOf { it.seconds }

        /** Which phase is running [elapsed] seconds into the exercise, and its seconds left. */
        fun phaseAt(elapsed: Int): Pair<Phase, Int> {
            var t = Math.floorMod(elapsed, cycleSeconds)
            for (p in phases) {
                if (t < p.seconds) return p to (p.seconds - t)
                t -= p.seconds
            }
            return phases.last() to phases.last().seconds
        }
    }

    val BREATHING: List<Breathing> = listOf(
        Breathing(
            "sigh", "آه چرخه‌ای", "دو دم پشت‌سرهم از بینی، یک بازدم بلند از دهان",
            listOf(
                Phase("دم از بینی", 2, Phase.Kind.INHALE),
                Phase("دم کوتاه دوم", 1, Phase.Kind.INHALE),
                Phase("بازدم آرام و بلند", 6, Phase.Kind.EXHALE)
            ),
            "balban"
        ),
        Breathing(
            "slow", "تنفس آهسته ۶ در دقیقه", "دم چهار ثانیه، بازدم شش ثانیه",
            listOf(Phase("دم", 4, Phase.Kind.INHALE), Phase("بازدم", 6, Phase.Kind.EXHALE)),
            "zaccaro"
        ),
        Breathing(
            "long", "بازدم طولانی ۴–۸", "دم چهار ثانیه، بازدم آرام هشت ثانیه؛ مناسب پیش از خواب",
            listOf(Phase("دم از بینی", 4, Phase.Kind.INHALE), Phase("بازدم آرام از دهان", 8, Phase.Kind.EXHALE)),
            "zaccaro"
        ),
        Breathing(
            "box", "تنفس مربعی", "دم، مکث، بازدم و مکث؛ هر کدام چهار ثانیه",
            listOf(
                Phase("دم", 4, Phase.Kind.INHALE),
                Phase("نگه دار", 4, Phase.Kind.HOLD),
                Phase("بازدم", 4, Phase.Kind.EXHALE),
                Phase("نگه دار", 4, Phase.Kind.HOLD)
            ),
            "zaccaro"
        )
    )

    // ---- Money ----

    data class BudgetSplit(val needs: Long, val wants: Long, val savings: Long)

    /** The 50/30/20 rule; rounding leftovers go to savings so the parts always add up. */
    fun budget(monthlyIncome: Long): BudgetSplit {
        val income = monthlyIncome.coerceAtLeast(0)
        val needs = income * 50 / 100
        val wants = income * 30 / 100
        return BudgetSplit(needs, wants, income - needs - wants)
    }

    /** Three- and six-month emergency fund targets for the given monthly essential costs. */
    fun emergencyFund(monthlyEssentials: Long): Pair<Long, Long> {
        val m = monthlyEssentials.coerceAtLeast(0)
        return m * 3 to m * 6
    }

    /** Months needed to reach [target] when saving [perMonth]; null if it never gets there. */
    fun monthsToGoal(target: Long, saved: Long, perMonth: Long): Int? {
        val left = target - saved
        if (left <= 0) return 0
        if (perMonth <= 0) return null
        return ((left + perMonth - 1) / perMonth).toInt()
    }

    /** Groups digits in threes with the Persian separator, e.g. 25000000 -> "۲۵٬۰۰۰٬۰۰۰". */
    fun formatAmount(value: Long): String {
        val digits = kotlin.math.abs(value).toString().reversed().chunked(3).joinToString("٬").reversed()
        val sign = if (value < 0) "−" else ""
        return sign + digits.map { c -> if (c in '0'..'9') '۰' + (c - '0') else c }.joinToString("")
    }

    // ---- Words for hard moments ----

    /**
     * An apology with the parts research found most effective: regret, responsibility,
     * explanation (optional, short), an offer to repair, and a promise for next time.
     */
    fun apology(what: String, impact: String, repair: String, next: String): String {
        val lines = mutableListOf<String>()
        val w = what.trim()
        lines += if (w.isEmpty()) "متأسفم." else "متأسفم که $w."
        lines += "اشتباه از من بود و توجیهی برایش ندارم."
        impact.trim().takeIf { it.isNotEmpty() }?.let { lines += "می‌فهمم که باعث شد $it." }
        repair.trim().takeIf { it.isNotEmpty() }?.let { lines += "برای جبرانش می‌خواهم $it." }
        next.trim().takeIf { it.isNotEmpty() }?.let { lines += "از این به بعد $it." }
        lines += "اگر بخواهی، دوست دارم بیشتر درباره‌اش حرف بزنیم."
        return lines.joinToString(" ")
    }

    /**
     * A "soft start-up" for raising a problem without blame: a feeling, a specific situation,
     * and a positive request.
     */
    fun softStartup(feeling: String, situation: String, request: String): String {
        val f = feeling.trim().ifEmpty { "ناراحت" }
        val s = situation.trim()
        val r = request.trim()
        val first = if (s.isEmpty()) "من این روزها کمی $f هستم." else "وقتی $s، من $f می‌شوم."
        val second = if (r.isEmpty()) "می‌شود با هم درباره‌اش حرف بزنیم؟" else "خیلی ممنون می‌شوم اگر $r."
        return "$first $second"
    }

    /** Sentences that blame, to warn about while writing (Gottman's "harsh start-up"). */
    private val HARSH = listOf("همیشه", "هیچ‌وقت", "هیچوقت", "تو اصلاً", "مقصر", "خسته‌ام کردی", "مثل همیشه")

    fun harshWords(text: String): List<String> = HARSH.filter { text.contains(it) }

    // ---- Conversation ----

    data class QuestionSet(val id: String, val title: String, val questions: List<String>, val audience: GentlemanGuide.Audience)

    val QUESTIONS: List<QuestionSet> = listOf(
        QuestionSet(
            "know", "آشنایی بهتر", listOf(
                "اگر می‌توانستی با هر کسی در دنیا شام بخوری، چه کسی را انتخاب می‌کردی؟",
                "یک روز «عالی» برای تو چه شکلی دارد؟",
                "از چه چیزی در زندگی‌ات بیشتر از همه سپاسگزاری؟",
                "اگر فردا با یک توانایی تازه بیدار می‌شدی، دوست داشتی چه باشد؟",
                "بزرگ‌ترین دستاوردی که به آن افتخار می‌کنی چیست؟",
                "چه چیزی در یک دوستی برایت از همه ارزشمندتر است؟",
                "خاطره‌ای که همیشه لبخندت را برمی‌گرداند کدام است؟",
                "اگر می‌دانستی یک سال دیگر زندگی‌ات عوض می‌شود، الان چه چیزی را تغییر می‌دادی؟",
                "بهترین توصیه‌ای که تا حالا گرفته‌ای چه بوده است؟",
                "چه کاری را دوست داری یاد بگیری ولی هنوز شروع نکرده‌ای؟",
                "کدام کتاب، فیلم یا سفر نگاهت را به زندگی عوض کرد؟",
                "در کودکی دوست داشتی چه‌کاره شوی؟",
                "چه چیزی تو را بی‌دلیل خوشحال می‌کند؟",
                "اگر یک سال تعطیلی با حقوق داشتی، با آن چه می‌کردی؟",
                "از کدام اشتباهت بیشترین درس را گرفتی؟"
            ),
            GentlemanGuide.Audience.ALL
        ),
        QuestionSet(
            "values", "ارزش‌ها و آینده (پیش از تعهد)", listOf(
                "خانواده ایده‌آلت در ده سال آینده چه شکلی است؟",
                "پول برایت چه معنایی دارد؟ اهل پس‌انداز هستی یا خرج کردن؟",
                "وقتی ناراحت هستی، دوست داری طرف مقابل چه کار کند؟",
                "کارهای خانه و تصمیم‌های مهم را چطور تقسیم می‌کنی؟",
                "رابطه با خانواده‌هایمان را بعد از ازدواج چطور می‌بینی؟",
                "درباره فرزند و زمانش چه فکری داری؟",
                "سه چیزی که در رابطه هرگز نمی‌پذیری چیست؟",
                "معنای «خانه» برای تو چیست؟",
                "در دعوا معمولاً چه رفتاری داری و دوست داری طرف مقابل چطور رفتار کند؟",
                "دین، باورها و سنت‌ها در زندگی مشترک چه جایی دارند؟",
                "درباره محل زندگی و احتمال مهاجرت چه فکری داری؟",
                "کار و پیشرفت شغلی هر دو نفر را چطور با زندگی مشترک هماهنگ می‌کنی؟",
                "وقت تنهایی و وقت با دوستان بعد از ازدواج برایت چه شکلی است؟",
                "از رابطه پدر و مادرت چه چیزی را می‌خواهی تکرار کنی و چه چیزی را نه؟"
            ),
            GentlemanGuide.Audience.SINGLE
        ),
        QuestionSet(
            "couple", "گفت‌وگوی دونفره", listOf(
                "این هفته کدام لحظه با من برایت از همه بهتر بود؟",
                "چه کاری از من باعث می‌شود بیشتر احساس دوست‌داشته‌شدن کنی؟",
                "این روزها بزرگ‌ترین دغدغه‌ات چیست و چطور می‌توانم کمکت کنم؟",
                "رؤیایی داری که هنوز به من نگفته باشی؟",
                "اگر یک سفر دونفره بی‌محدودیت داشتیم، کجا می‌رفتیم؟",
                "کدام خاطره مشترکمان را بیشتر از همه دوست داری؟",
                "سال آینده دوست داری چه چیزی را با هم شروع کنیم؟",
                "چیزی هست که دوست داشته باشی بیشتر با هم انجام دهیم؟",
                "کی آخرین بار احساس کردی کاملاً درکت کردم؟",
                "یک عادت کوچک من که دوستش داری چیست؟",
                "در این ماه از چه چیزی در رابطه‌مان قدردانی؟",
                "اگر می‌توانستیم یک قانون تازه برای خانه بگذاریم، چه بود؟",
                "پنج سال دیگر خودمان را کجا می‌بینی؟",
                "چه چیزی از روزهای اول آشنایی‌مان دلت را تنگ می‌کند؟"
            ),
            GentlemanGuide.Audience.MARRIED
        )
    )

    fun questionsFor(status: Status): List<QuestionSet> = QUESTIONS.filter {
        when (it.audience) {
            GentlemanGuide.Audience.ALL -> true
            GentlemanGuide.Audience.SINGLE -> status == Status.SINGLE
            GentlemanGuide.Audience.MARRIED -> status == Status.MARRIED
        }
    }

    // ---- Preparation ----

    data class Checklist(val id: String, val title: String, val items: List<String>)

    val CHECKLISTS: List<Checklist> = listOf(
        Checklist(
            "meeting", "پیش از جلسه یا مصاحبه", listOf(
                "هدف این جلسه را در یک جمله بنویس.",
                "سه نکته اصلی که باید بگویی را آماده کن.",
                "یک سؤال خوب برای طرف مقابل آماده کن.",
                "لباس تمیز و اندازه، کفش تمیز.",
                "ده دقیقه زودتر برس.",
                "پیش از ورود، یک دقیقه تنفس آرام.",
                "گوشی را بی‌صدا کن."
            )
        ),
        Checklist(
            "date", "پیش از قرار", listOf(
                "جایی عمومی و آرام انتخاب کن و برنامه را روشن بگو.",
                "سر وقت برس.",
                "دوش، بوی خوش ملایم، لباس مرتب.",
                "دو سه سؤال باز برای آشنایی در ذهن داشته باش.",
                "گوشی را کنار بگذار و واقعاً گوش کن.",
                "به انتخاب‌ها و مرزهایش احترام بگذار.",
                "در پایان، صادقانه و مؤدبانه خداحافظی کن."
            )
        ),
        Checklist(
            "talk", "پیش از گفت‌وگوی سخت", listOf(
                "مطمئن شو هر دو آرام و خسته نیستید.",
                "فقط یک موضوع را انتخاب کن.",
                "با جمله‌ای که با «من» شروع می‌شود آغاز کن.",
                "اول حرفش را خلاصه کن، بعد جواب بده.",
                "اگر تند شد، ۲۰ دقیقه مکث کن و حتماً برگرد.",
                "با یک قدم عملی مشترک تمامش کن."
            )
        ),
        Checklist(
            "negotiate", "پیش از مذاکره حقوق یا قرارداد", listOf(
                "حقوق رایج این شغل را از چند منبع و همکار بپرس.",
                "سه دستاورد کاری‌ات را با عدد و نتیجه بنویس.",
                "عدد هدفت و کمترین عددی که می‌پذیری را از قبل مشخص کن.",
                "بهترین گزینه جایگزینت را بدان: اگر توافق نشد، چه می‌کنی؟",
                "اول بپرس طرف مقابل چه نیازی دارد، بعد پیشنهادت را بگو.",
                "به جز حقوق، بقیه موارد را هم در نظر بگیر: بیمه، آموزش، دورکاری، مرخصی.",
                "پیشنهاد را در لحظه قبول نکن؛ برای فکر کردن زمان بخواه.",
                "توافق نهایی را کتبی بگیر."
            )
        ),
        Checklist(
            "workout", "برنامه قدرتی مبتدی (۳ بار در هفته)", listOf(
                "۵ دقیقه گرم کردن: پیاده‌روی تند، چرخش مفصل‌ها.",
                "اسکات با وزن بدن: ۳ ست × ۱۰ تا ۱۲ تکرار.",
                "شنا (پوش‌آپ)، روی زانو یا دیوار اگر لازم است: ۳ ست × ۸ تا ۱۲.",
                "زیربغل با کش یا بطری آب، یا بارفیکس: ۳ ست × ۸ تا ۱۲.",
                "لانج (هر پا): ۲ ست × ۸ تا ۱۰.",
                "پل باسن: ۳ ست × ۱۲.",
                "پلانک: ۳ بار × ۲۰ تا ۴۰ ثانیه.",
                "۵ دقیقه سرد کردن و کشش.",
                "هفته بعد کمی بیشتر: یک تکرار، یک ست یا کمی وزنه."
            )
        ),
        Checklist(
            "sleep", "بهداشت خواب", listOf(
                "هر روز، حتی آخر هفته، سر یک ساعت بیدار شو.",
                "بعد از ظهر کافئین (قهوه، چای پررنگ، نوشابه) نخور.",
                "سه ساعت پیش از خواب شام سنگین نخور.",
                "یک ساعت پیش از خواب گوشی و صفحه را کنار بگذار.",
                "اتاق را تاریک، خنک و ساکت نگه دار.",
                "اگر ۲۰ دقیقه خوابت نبرد، بلند شو و کاری آرام کن تا خواب‌آلود شوی.",
                "صبح‌ها چند دقیقه نور روز ببین.",
                "اگر خروپف شدید یا قطع تنفس در خواب داری، به پزشک بگو."
            )
        )
    )

    // ---- Mood ----

    val MOODS: List<String> = listOf("خیلی بد", "بد", "معمولی", "خوب", "عالی")

    fun moodLabel(mood: Int): String = MOODS[(mood - 1).coerceIn(0, MOODS.lastIndex)]
}
