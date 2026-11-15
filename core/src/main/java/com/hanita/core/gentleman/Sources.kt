package com.hanita.core.gentleman

/**
 * The references the guide's content is based on. Every pillar, test and tool names the
 * sources it follows so the user can see where an idea comes from and read more.
 *
 * Only books, peer-reviewed papers and health organisations are listed. A [url] is given only
 * for stable addresses (DOIs and official pages); the app opens it in the phone's browser.
 */
object Sources {

    data class Source(
        val id: String,
        /** Author or organisation and year, e.g. "Seligman و همکاران (۲۰۰۵)". */
        val author: String,
        /** The book, paper or page title as published. */
        val title: String,
        /** One Persian line on what the app takes from it. */
        val note: String,
        val url: String? = null
    )

    val ALL: List<Source> = listOf(
        Source(
            "branden", "Nathaniel Branden (1994)", "The Six Pillars of Self-Esteem",
            "شش تمرین عزت نفس: زندگی آگاهانه، پذیرش خود، مسئولیت، ابراز وجود، هدفمندی و یکپارچگی."
        ),
        Source(
            "neff", "Kristin Neff (2003)", "Self-compassion: An alternative conceptualization of a healthy attitude toward oneself. Self and Identity, 2(2)",
            "مهربانی با خود به جای سرزنش؛ پایه تمرین‌های «با خودت مهربان باش».",
            "https://doi.org/10.1080/15298860309032"
        ),
        Source(
            "rosenberg", "Morris Rosenberg (1965)", "Society and the Adolescent Self-Image. Princeton University Press",
            "مقیاس ده‌سؤالی عزت نفس روزنبرگ؛ پرکاربردترین سنجه عزت نفس در پژوهش‌ها."
        ),
        Source(
            "who5", "Topp و همکاران (2015)", "The WHO-5 Well-Being Index: A systematic review of the literature. Psychotherapy and Psychosomatics, 84(3)",
            "شاخص پنج‌سؤالی بهزیستی سازمان جهانی بهداشت و نقطه‌های برش آن.",
            "https://doi.org/10.1159/000376585"
        ),
        Source(
            "seligman", "Seligman و همکاران (2005)", "Positive psychology progress: Empirical validation of interventions. American Psychologist, 60(5)",
            "تمرین «سه اتفاق خوب» در دفترچه روزانه که در کارآزمایی، شادی را بالا و افسردگی را پایین برد.",
            "https://doi.org/10.1037/0003-066X.60.5.410"
        ),
        Source(
            "beck", "Judith S. Beck (2020)", "Cognitive Behavior Therapy: Basics and Beyond (3rd ed.)",
            "شناختن فکرهای خودکار منفی و جایگزین کردن آن‌ها با فکر واقع‌بینانه."
        ),
        Source(
            "nccih", "NCCIH, National Institutes of Health", "Meditation and Mindfulness: What You Need To Know",
            "شواهد اثر مراقبه و ذهن‌آگاهی بر استرس و اضطراب.",
            "https://www.nccih.nih.gov/health/meditation-and-mindfulness-what-you-need-to-know"
        ),
        Source(
            "apa-anger", "American Psychological Association", "Control anger before it controls you",
            "شناخت نشانه‌های خشم، آرام‌سازی، حل مسئله و ارتباط بهتر.",
            "https://www.apa.org/topics/anger/control"
        ),
        Source(
            "balban", "Balban و همکاران (2023)", "Brief structured respiration practices enhance mood and reduce physiological arousal. Cell Reports Medicine, 4(1)",
            "تنفس با بازدم طولانی (آه چرخه‌ای) در پنج دقیقه در روز خلق را بهتر و برانگیختگی را کم کرد.",
            "https://doi.org/10.1016/j.xcrm.2022.100895"
        ),
        Source(
            "zaccaro", "Zaccaro و همکاران (2018)", "How breath-control can change your life: A systematic review on psycho-physiological correlates of slow breathing. Frontiers in Human Neuroscience, 12",
            "تنفس آهسته (حدود شش نفس در دقیقه) با آرامش بیشتر و اضطراب کمتر همراه است.",
            "https://doi.org/10.3389/fnhum.2018.00353"
        ),
        Source(
            "carnegie", "Dale Carnegie (1936)", "How to Win Friends and Influence People",
            "به خاطر سپردن اسم‌ها، علاقه واقعی به دیگران و گوش دادن."
        ),
        Source(
            "rogers", "Carl Rogers و Richard Farson (1957)", "Active Listening. Industrial Relations Center, University of Chicago",
            "گوش دادن فعال: شنیدن معنای کامل و بازگو کردن آن به زبان خود."
        ),
        Source(
            "post", "Emily Post Institute (2017)", "Emily Post's Etiquette, 19th edition",
            "آداب معاشرت مبتنی بر ملاحظه، احترام و صداقت."
        ),
        Source(
            "aad", "American Academy of Dermatology", "Skin care tips for men",
            "مراقبت ساده پوست: شست‌وشوی ملایم، مرطوب‌کننده و ضدآفتاب روزانه."
        ),
        Source(
            "who-pa", "World Health Organization (2020)", "WHO guidelines on physical activity and sedentary behaviour",
            "۱۵۰ تا ۳۰۰ دقیقه فعالیت متوسط در هفته و دو جلسه تمرین قدرتی برای بزرگسالان.",
            "https://www.who.int/publications/i/item/9789240015128"
        ),
        Source(
            "sleep", "Watson و همکاران (2015)", "Recommended amount of sleep for a healthy adult: AASM and SRS consensus. Sleep, 38(6)",
            "بزرگسالان برای سلامت به‌طور منظم دست‌کم هفت ساعت خواب در شب لازم دارند.",
            "https://doi.org/10.5665/sleep.4716"
        ),
        Source(
            "who-diet", "World Health Organization", "Healthy diet (fact sheet)",
            "دست‌کم ۴۰۰ گرم میوه و سبزی در روز، نمک کمتر از ۵ گرم و قند آزاد کمتر.",
            "https://www.who.int/news-room/fact-sheets/detail/healthy-diet"
        ),
        Source(
            "warren", "Elizabeth Warren و Amelia Warren Tyagi (2005)", "All Your Worth: The Ultimate Lifetime Money Plan",
            "قاعده بودجه ۵۰/۳۰/۲۰: نیازها، خواسته‌ها و پس‌انداز."
        ),
        Source(
            "cfpb", "Consumer Financial Protection Bureau", "An essential guide to building an emergency fund",
            "ساختن تدریجی صندوق اضطراری برای هزینه‌های پیش‌بینی‌نشده."
        ),
        Source(
            "clear", "James Clear (2018)", "Atomic Habits",
            "تغییرهای کوچک روزانه، سیستم به جای هدف و هویت به جای نتیجه."
        ),
        Source(
            "gollwitzer", "Peter Gollwitzer (1999)", "Implementation intentions: Strong effects of simple plans. American Psychologist, 54(7)",
            "برنامه «اگر… آنگاه…» احتمال انجام هدف را به‌طور معناداری بالا می‌برد.",
            "https://doi.org/10.1037/0003-066X.54.7.493"
        ),
        Source(
            "lally", "Lally و همکاران (2010)", "How are habits formed: Modelling habit formation in the real world. European Journal of Social Psychology, 40(6)",
            "خودکار شدن یک عادت به‌طور میانه حدود ۶۶ روز طول کشید و یک روز جا انداختن اثر چندانی نداشت.",
            "https://doi.org/10.1002/ejsp.674"
        ),
        Source(
            "newport", "Cal Newport (2016)", "Deep Work: Rules for Focused Success in a Distracted World",
            "بازه‌های بی‌وقفه تمرکز برای کار باکیفیت."
        ),
        Source(
            "holt", "Holt-Lunstad و همکاران (2010)", "Social relationships and mortality risk: A meta-analytic review. PLoS Medicine, 7(7)",
            "روابط اجتماعی قوی‌تر با ۵۰٪ احتمال بقای بیشتر همراه بود.",
            "https://doi.org/10.1371/journal.pmed.1000316"
        ),
        Source(
            "aron", "Aron و همکاران (1997)", "The experimental generation of interpersonal closeness. Personality and Social Psychology Bulletin, 23(4)",
            "پرسش‌هایی که به‌تدریج عمیق‌تر می‌شوند، صمیمیت می‌سازند (پایه پرسش‌های گفت‌وگو).",
            "https://doi.org/10.1177/0146167297234003"
        ),
        Source(
            "vanepp", "John Van Epp (2007)", "How to Avoid Falling in Love with a Jerk",
            "مدل RAM: شناختن، اعتماد، تکیه، تعهد و تماس به ترتیب و با سرعت متعادل."
        ),
        Source(
            "gottman", "John Gottman و Nan Silver (2015)", "The Seven Principles for Making Marriage Work (revised ed.)",
            "نقشه عشق، تحسین، پاسخ به «درخواست‌های توجه»، شروع نرم بحث و ترمیم."
        ),
        Source(
            "gottman-levenson", "Gottman و Levenson (2000)", "The timing of divorce: Predicting when a couple will divorce over a 14-year period. Journal of Marriage and Family, 62(3)",
            "انتقاد، تحقیر، حالت دفاعی و قهر سرد، پیش‌بینی‌کننده‌های جدایی‌اند.",
            "https://doi.org/10.1111/j.1741-3737.2000.00737.x"
        ),
        Source(
            "chapman", "Gary Chapman (1992)", "The Five Love Languages",
            "چارچوب محبوب پنج زبان محبت (پشتوانه پژوهشی آن محدود است و فقط برای گفت‌وگو به کار می‌رود)."
        ),
        Source(
            "lewicki", "Lewicki و همکاران (2016)", "An exploration of the structure of effective apologies. Negotiation and Conflict Management Research, 9(2)",
            "شش جزء عذرخواهی؛ پذیرفتن مسئولیت و پیشنهاد جبران مؤثرترین‌ها هستند.",
            "https://doi.org/10.1111/ncmr.12073"
        ),
        Source(
            "acog", "American College of Obstetricians and Gynecologists", "Premenstrual Syndrome (PMS) — FAQ",
            "نشانه‌های پیش از قاعدگی، راه‌های کاهش آن و زمان مراجعه به پزشک.",
            "https://www.acog.org/womens-health/faqs/premenstrual-syndrome"
        ),
        Source(
            "cdc-parenting", "Centers for Disease Control and Prevention", "Positive Parenting Tips",
            "توصیه‌های رشد کودک: وقت باکیفیت، تحسین رفتار خوب و انضباط آرام."
        ),
        Source(
            "aap", "American Academy of Pediatrics (HealthyChildren.org)", "Disciplining your child / The role of fathers",
            "انضباط بدون تنبیه بدنی و اهمیت مشارکت پدر."
        )
    )

    private val byId = ALL.associateBy { it.id }

    fun get(id: String): Source = byId.getValue(id)

    private val PILLAR_SOURCES: Map<String, List<String>> = mapOf(
        "manners" to listOf("post", "carnegie"),
        "talk" to listOf("rogers", "carnegie", "gottman"),
        "style" to listOf("aad", "post"),
        "health" to listOf("who-pa", "sleep", "who-diet"),
        "money" to listOf("warren", "cfpb"),
        "mind" to listOf("beck", "nccih", "seligman"),
        "dating" to listOf("vanepp", "aron"),
        "choosing" to listOf("vanepp", "gottman"),
        "solo" to listOf("who-diet", "holt"),
        "marriage" to listOf("gottman", "chapman"),
        "conflict" to listOf("gottman", "gottman-levenson", "lewicki"),
        "cycle" to listOf("acog"),
        "esteem" to listOf("branden", "neff", "rosenberg"),
        "presence" to listOf("carnegie", "rogers", "branden"),
        "calm" to listOf("apa-anger", "balban", "zaccaro", "nccih"),
        "work" to listOf("clear", "gollwitzer", "lally", "newport"),
        "friends" to listOf("holt", "carnegie"),
        "father" to listOf("cdc-parenting", "aap")
    )

    fun forPillar(pillarId: String): List<Source> = PILLAR_SOURCES[pillarId].orEmpty().map(::get)

    /** Every pillar that has sources, for the tests. */
    internal val pillarIds: Set<String> get() = PILLAR_SOURCES.keys
}
