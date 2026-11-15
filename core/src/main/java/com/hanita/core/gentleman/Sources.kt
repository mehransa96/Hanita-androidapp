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
        ),
        // ---- Self-checks added in 4.0 ----
        Source(
            "phq9", "Kroenke, Spitzer و Williams (2001)", "The PHQ-9: Validity of a brief depression severity measure. Journal of General Internal Medicine, 16(9)",
            "پرسشنامه ۹ سؤالی غربالگری افسردگی و نقطه‌های برش ۵، ۱۰، ۱۵ و ۲۰ (آزاد برای استفاده).",
            "https://doi.org/10.1046/j.1525-1497.2001.016009606.x"
        ),
        Source(
            "gad7", "Spitzer و همکاران (2006)", "A brief measure for assessing generalized anxiety disorder: The GAD-7. Archives of Internal Medicine, 166(10)",
            "مقیاس ۷ سؤالی غربالگری اضطراب و نقطه‌های برش ۵، ۱۰ و ۱۵ (آزاد برای استفاده).",
            "https://doi.org/10.1001/archinte.166.10.1092"
        ),
        Source(
            "pss", "Cohen, Kamarck و Mermelstein (1983)", "A global measure of perceived stress. Journal of Health and Social Behavior, 24(4)",
            "مقیاس ۱۰ سؤالی استرس ادراک‌شده؛ چهار سؤال معکوس نمره‌گذاری می‌شوند.",
            "https://doi.org/10.2307/2136404"
        ),
        Source(
            "mini-ipip", "Donnellan و همکاران (2006)", "The Mini-IPIP scales: Tiny-yet-effective measures of the Big Five factors of personality. Psychological Assessment, 18(2)",
            "نسخه ۲۰ سؤالی آزمون پنج‌عاملی شخصیت از بانک عمومی IPIP (مالکیت عمومی).",
            "https://doi.org/10.1037/1040-3590.18.2.192"
        ),
        Source(
            "brs", "Smith و همکاران (2008)", "The brief resilience scale: Assessing the ability to bounce back. International Journal of Behavioral Medicine, 15(3)",
            "مقیاس ۶ سؤالی توانایی برگشتن پس از سختی‌ها.",
            "https://doi.org/10.1080/10705500802222972"
        ),
        Source(
            "gse", "Schwarzer و Jerusalem (1995)", "Generalized Self-Efficacy scale. In: Measures in Health Psychology: A User's Portfolio. NFER-NELSON",
            "مقیاس ۱۰ سؤالی باور به توانایی خود در کنار آمدن با سختی‌ها (آزاد برای استفاده با ذکر منبع)."
        ),
        Source(
            "swls", "Diener و همکاران (1985)", "The Satisfaction With Life Scale. Journal of Personality Assessment, 49(1)",
            "مقیاس ۵ سؤالی رضایت از زندگی و تفسیر بازه‌های نمره.",
            "https://doi.org/10.1207/s15327752jpa4901_13"
        ),
        Source(
            "ucla3", "Hughes و همکاران (2004)", "A short scale for measuring loneliness in large surveys. Research on Aging, 26(6)",
            "نسخه سه‌سؤالی مقیاس تنهایی UCLA؛ نمره ۶ و بالاتر معمولاً «احساس تنهایی» در نظر گرفته می‌شود.",
            "https://doi.org/10.1177/0164027504268574"
        ),
        Source(
            "csi4", "Funk و Rogge (2007)", "Testing the ruler with item response theory: The Couples Satisfaction Index. Journal of Family Psychology, 21(4)",
            "شاخص ۴ سؤالی رضایت زوجین؛ نمره زیر ۱۳٫۵ نشانه نارضایتی قابل‌توجه است.",
            "https://doi.org/10.1037/0893-3200.21.4.572"
        ),
        // ---- Pillars and tools added in 4.0 ----
        Source(
            "fisher-ury", "Fisher, Ury و Patton (2011)", "Getting to Yes: Negotiating Agreement Without Giving In (3rd ed.)",
            "مذاکره بر پایه منافع، نه موضع؛ و شناختن بهترین جایگزین خودت (BATNA)."
        ),
        Source(
            "dweck", "Carol S. Dweck (2006)", "Mindset: The New Psychology of Success",
            "ذهنیت رشد: توانایی با تمرین و بازخورد بیشتر می‌شود."
        ),
        Source(
            "goleman", "Daniel Goleman (2000)", "Leadership That Gets Results. Harvard Business Review, 78(2)",
            "سبک‌های رهبری و اثر هوش هیجانی رهبر بر جو تیم."
        ),
        Source(
            "edmondson", "Amy Edmondson (1999)", "Psychological safety and learning behavior in work teams. Administrative Science Quarterly, 44(2)",
            "امنیت روانی: تیمی که در آن پرسیدن و اشتباه کردن ترسناک نیست، بهتر یاد می‌گیرد.",
            "https://doi.org/10.2307/2666999"
        ),
        Source(
            "uspstf", "U.S. Preventive Services Task Force", "USPSTF A and B Recommendations",
            "غربالگری‌های پیشنهادی بزرگسالان بر اساس سن و عوامل خطر؛ پایه ابزار «چکاپ مردانه».",
            "https://www.uspreventiveservicestaskforce.org/uspstf/recommendation-topics/uspstf-a-and-b-recommendations"
        ),
        Source(
            "who-bp", "World Health Organization", "Hypertension — fact sheet",
            "فشار خون بالا اغلب بی‌علامت است و فقط با اندازه‌گیری منظم پیدا می‌شود.",
            "https://www.who.int/news-room/fact-sheets/detail/hypertension"
        ),
        Source(
            "who-bmi", "World Health Organization", "A healthy lifestyle — WHO recommendations (body mass index)",
            "دسته‌بندی شاخص توده بدنی بزرگسالان: کم‌وزن، طبیعی، اضافه‌وزن و چاقی.",
            "https://www.who.int/europe/news-room/fact-sheets/item/a-healthy-lifestyle---who-recommendations"
        ),
        Source(
            "nice-whtr", "NICE (2022)", "Obesity: identification, assessment and management — waist-to-height ratio",
            "نسبت دور کمر به قد زیر ۰٫۵ سالم؛ ۰٫۵ تا ۰٫۶ خطر بیشتر و ۰٫۶ به بالا خطر زیاد برای سلامت قلب و متابولیسم."
        ),
        Source(
            "acsm", "American College of Sports Medicine (2021)", "ACSM's Guidelines for Exercise Testing and Prescription (11th ed.)",
            "اصول تمرین قدرتی: هر گروه عضلانی دو تا سه بار در هفته، ۸ تا ۱۲ تکرار، افزایش تدریجی بار."
        ),
        Source(
            "newport-dm", "Cal Newport (2019)", "Digital Minimalism: Choosing a Focused Life in a Noisy World",
            "استفاده آگاهانه از فناوری: فقط ابزاری که به ارزش‌هایت کمک می‌کند."
        ),
        Source(
            "hunt", "Hunt و همکاران (2018)", "No more FOMO: Limiting social media decreases loneliness and depression. Journal of Social and Clinical Psychology, 37(10)",
            "محدود کردن شبکه‌های اجتماعی به حدود ۳۰ دقیقه در روز، تنهایی و افسردگی را در سه هفته کم کرد.",
            "https://doi.org/10.1521/jscp.2018.37.10.751"
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
        "father" to listOf("cdc-parenting", "aap"),
        "career" to listOf("fisher-ury", "dweck", "newport"),
        "leader" to listOf("goleman", "edmondson", "carnegie"),
        "menhealth" to listOf("uspstf", "who-bp", "sleep"),
        "fitness" to listOf("who-pa", "acsm"),
        "digital" to listOf("newport-dm", "hunt", "sleep")
    )

    fun forPillar(pillarId: String): List<Source> = PILLAR_SOURCES[pillarId].orEmpty().map(::get)

    /** Every pillar that has sources, for the tests. */
    internal val pillarIds: Set<String> get() = PILLAR_SOURCES.keys
}
