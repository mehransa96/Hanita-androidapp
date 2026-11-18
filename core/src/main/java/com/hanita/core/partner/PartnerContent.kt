package com.hanita.core.partner

import com.example.utils.PersianNumbers
import com.hanita.core.partner.PartnerDay.Event
import com.hanita.core.partner.PartnerDay.Phase

/**
 * Persian texts of the partner app: what each phase means, how to help, and messages the
 * partner can send to the owner. Written in the partner's voice, never clinical.
 */
object PartnerContent {

    data class PhaseInfo(
        val emoji: String,
        val title: String,
        val summary: String,
        val tips: List<String>,
        /** Ready messages from the partner to the owner; "{name}" is replaced by her name. */
        val messages: List<String>
    )

    /** Tips and messages for [phase], with the extra choices of 4.1 after the original ones. */
    fun phaseInfo(phase: Phase, fertileShared: Boolean = false): PhaseInfo {
        val base = basePhaseInfo(phase, fertileShared)
        val key = if (phase in GENERAL_PHASES) null else phase
        return base.copy(
            tips = base.tips + (key?.let { MORE_TIPS[it] } ?: MORE_GENERAL_TIPS),
            messages = base.messages + (key?.let { MORE_MESSAGES[it] } ?: MORE_GENERAL_MESSAGES)
        )
    }

    private val GENERAL_PHASES = setOf(Phase.AWAITING_UPDATE, Phase.STALE, Phase.PAUSED, Phase.NO_DATA)

    private val MORE_TIPS: Map<Phase, List<String>> = mapOf(
        Phase.PERIOD to listOf(
            "غذای گرم و مقوی (سوپ، عدسی، غذای آهن‌دار) درست کن یا بگیر؛ بدنش در این روزها آهن از دست می‌دهد.",
            "یک پتو، فیلم سبک و فضای آرام در خانه، بهتر از هر برنامه بیرون است.",
            "اگر درد شدید است، مسکن یا مشورت با پزشک را پیشنهاد بده و خودت داروخانه برو.",
            "درباره تمیزی و خرید وسایل بهداشتی بی‌منت کمک کن؛ از خرید پد یا مسکن خجالت نکش."
        ),
        Phase.AFTER_PERIOD to listOf(
            "برای کاری که مدتی عقب افتاده (خرید، مهمانی، سفر کوتاه) با هم برنامه بریزید.",
            "یک کلاس یا تجربه تازه دونفره (کوهنوردی، آشپزی، بازی) امتحان کنید.",
            "اگر ورزش می‌کند، همراهی‌اش کن یا تشویقش کن."
        ),
        Phase.MID_CYCLE to listOf(
            "برای عکس گرفتن، مهمانی یا دیدار با دوستان، این روزها انتخاب خوبی است.",
            "یک خاطره خوب مشترک را یادآوری کن و از آینده‌ای که با هم می‌سازید حرف بزن.",
            "صمیمیت فقط جسمی نیست؛ وقت بی‌گوشی و گفت‌وگوی عمیق هم بگذار."
        ),
        Phase.LUTEAL to listOf(
            "قرارها و مهمانی‌های شلوغ را کمتر کن و شب‌های آرام در خانه را بیشتر.",
            "پیاده‌روی ملایم یا یوگا را با هم پیشنهاد بده؛ به کم شدن تنش کمک می‌کند.",
            "اگر کمی دیرتر جواب داد یا کم‌حرف‌تر بود، به خودت نگیر."
        ),
        Phase.PMS to listOf(
            "نمک، کافئین و شیرینی زیاد علائم را بدتر می‌کنند؛ میوه، آجیل و آب بیشتر را پیشنهاد بده، نه تحمیل کن.",
            "اگر بحثی شد، بگو «بیا بعداً درباره‌اش حرف بزنیم» و واقعاً بعداً برگرد.",
            "اگر هر ماه این روزها خیلی سخت است (غم شدید، خشم، اضطراب)، مراجعه به پزشک زنان را با مهربانی پیشنهاد بده."
        ),
        Phase.PERIOD_DUE to listOf(
            "وسایل بهداشتی و مسکن را در خانه چک کن و اگر کم است، بخر.",
            "اگر برنامه سفر یا مهمانی داری، از او بپرس راحت است یا بهتر است عقب بیندازید."
        )
    )

    private val MORE_MESSAGES: Map<Phase, List<String>> = mapOf(
        Phase.PERIOD to listOf(
            "{name} جان، سوپ گرم سفارش دادم، فقط استراحت کن 🍲",
            "اگه دردت زیاده بگو، می‌رم داروخونه 💊❤️",
            "امروز هر چی تو بگی، همون. ملکه امروز تویی 👑"
        ),
        Phase.AFTER_PERIOD to listOf(
            "{name}، یه برنامه تازه برای این هفته بچینیم؟ 🗓️",
            "انرژیت بهم انرژی می‌ده عزیزم 🌞",
            "بیا این هفته یه چیز جدید با هم امتحان کنیم 🌿"
        ),
        Phase.MID_CYCLE to listOf(
            "امروز خیلی خوشگل شدی {name} 😍",
            "هر روز بیشتر از دیروز دوستت دارم 💞",
            "یه شب خاص فقط برای ما دوتا بذاریم؟ 🕯️"
        ),
        Phase.LUTEAL to listOf(
            "امشب شام ساده و یه فیلم آروم؟ 🎞️",
            "{name} جان، اگه خسته‌ای امروز کارها با من 🤍",
            "یه قدم آروم عصرگاهی با هم؟ 🍃"
        ),
        Phase.PMS to listOf(
            "هر حسی داری اشکالی نداره، من همین‌جام 🤍",
            "{name} جان، امروز با هیچی خودتو اذیت نکن ☕",
            "برات یه چیز خوشمزه گرفتم، فقط لبخند بزن 🍓"
        ),
        Phase.PERIOD_DUE to listOf(
            "{name}، وسایل لازم رو گرفتم، خیالت راحت 🌸",
            "این چند روز هر چی خواستی فقط بگو ❤️"
        )
    )

    private val MORE_GENERAL_TIPS = listOf(
        "هفته‌ای یک قرار دونفره، حتی یک چای در کافه، را ثابت نگه دار.",
        "یک قدردانی مشخص بگو: «ممنونم که امروز…» بهتر از «ممنون» کلی است.",
        "از برنامه‌های او در این هفته بپرس و یکی را یادت بماند تا بعداً پیگیری کنی."
    )

    private val MORE_GENERAL_MESSAGES = listOf(
        "به فکرت بودم، روزت قشنگ باشه 🌼",
        "{name}، ممنونم که هستی 🤍",
        "آخر هفته یه برنامه دونفره داشته باشیم؟ 🗓️"
    )

    private fun basePhaseInfo(phase: Phase, fertileShared: Boolean): PhaseInfo = when (phase) {
        Phase.PERIOD -> PhaseInfo(
            "🩸", "روزهای پریود",
            "بدنش خسته‌تر است و ممکن است درد داشته باشد. آرامش، گرما و کمک عملی بیشترین اثر را دارد.",
            listOf(
                "یک دمنوش گرم (زنجبیل، دارچین یا بابونه) یا کیسه آب گرم آماده کن.",
                "کارهای سنگین خانه، خرید یا آشپزی امروز را خودت به عهده بگیر.",
                "برنامه‌های شلوغ و خسته‌کننده را به روزهای بعد موکول کن.",
                "اگر کم‌حوصله است به دل نگیر؛ کنارش بودن و صبوری کافی است."
            ),
            listOf(
                "{name} جان، امروز فقط استراحت کن؛ کارها با من ❤️",
                "عزیزم چیزی لازم داری سر راه بگیرم؟ دمنوش، شکلات یا هر چی دوست داری 🌸",
                "{name}، امشب شام با من. تو فقط استراحت کن 💙",
                "می‌دونم امروز روز سختیه. هر وقت خواستی بگو، کنارتم 🤍"
            )
        )
        Phase.AFTER_PERIOD -> PhaseInfo(
            "🌱", "بعد از پریود",
            "انرژی و حال خوبش کم‌کم برمی‌گردد. زمان خوبی برای برنامه‌های تازه و کارهای مشترک است.",
            listOf(
                "یک پیاده‌روی، ورزش یا گردش دونفره پیشنهاد بده.",
                "برای برنامه‌های آینده و کارهای مشترک با هم صحبت کنید.",
                "از اینکه دوباره سرحال است ابراز خوشحالی کن.",
                "یک غذای سبک و سالم با هم درست کنید."
            ),
            listOf(
                "{name} جان، این آخر هفته بریم یه جای قشنگ؟ 🌿",
                "خوشحالم که حالت بهتره عزیزم 😊 امشب یه برنامه دونفره داشته باشیم؟",
                "{name}، امروز چه کاری دوست داری با هم انجام بدیم؟ 💚",
                "یه پیاده‌روی عصرگاهی بریم؟ دلم برای قدم زدن باهات تنگ شده 🚶‍♀️🚶‍♂️"
            )
        )
        Phase.MID_CYCLE -> PhaseInfo(
            "✨", if (fertileShared) "میانه چرخه (روزهای باروری)" else "میانه چرخه",
            "معمولاً پرانرژی‌ترین و شادترین روزهای ماه است؛ وقت خوبی برای صمیمیت و گفتگوهای عمیق.",
            buildList {
                add("یک شام دونفره یا قرار عاشقانه ترتیب بده.")
                add("از ظاهر و حال خوبش تعریف کن؛ این روزها بیشتر به چشم می‌آید.")
                add("گفتگوهای مهم و برنامه‌ریزی‌های مشترک را برای این روزها بگذار.")
                if (fertileShared) add("اگر برای بارداری برنامه دارید، این روزها بیشترین شانس را دارند.")
                else add("یک سورپرایز کوچک (گل، کادو یا یک پیام عاشقانه) حسابی خوشحالش می‌کند.")
            },
            listOf(
                "{name} جان، امشب شام بیرون مهمون من؟ ✨",
                "امروز خیلی دلم برات تنگ شده {name} 💕",
                "تو بهترین اتفاق زندگی منی عزیزم 🌹",
                "{name}، امشب یه فیلم خوب ببینیم و حرف بزنیم؟ 🎬"
            )
        )
        Phase.LUTEAL -> PhaseInfo(
            "🌙", "روزهای آرام",
            "انرژی کمی پایین‌تر می‌آید و به آرامش و نظم بیشتری نیاز دارد.",
            listOf(
                "برنامه‌ها را ساده و بی‌استرس نگه دار.",
                "خواب کافی و غذای مقوی برایش مهم‌تر از همیشه است.",
                "برای کارهای روزمره پیش‌قدم شو تا خستگی‌اش کمتر شود.",
                "با یک پیام کوتاه نشان بده که حواست به او هست."
            ),
            listOf(
                "{name} جان، امروز چطوری؟ کاری از دستم برمیاد؟ 🌙",
                "امشب زود استراحت کنیم؟ یه چای گرم با هم بخوریم ☕",
                "به فکرتم عزیزم، مراقب خودت باش 🤍",
                "{name}، امروز هر چی دوست داری بگو برات بگیرم 😊"
            )
        )
        Phase.PMS -> PhaseInfo(
            "🍂", "روزهای پیش از پریود",
            "ممکن است حساس‌تر، کم‌حوصله‌تر یا خسته‌تر باشد. این اثر تغییرات هورمونی است، نه چیزی شخصی.",
            listOf(
                "حرف‌های تند یا بی‌حوصلگی را به دل نگیر و بحث‌های سنگین را عقب بینداز.",
                "خوراکی‌هایی که دوست دارد (مثلاً شکلات تلخ یا میوه) دم دست بگذار.",
                "بیشتر گوش بده و کمتر راه‌حل بده؛ همدلی مهم‌تر از نصیحت است.",
                "یک ماساژ ساده یا یک شب آرام در خانه خیلی کمک می‌کند."
            ),
            listOf(
                "{name} جان، اگه امروز حوصله نداری اشکالی نداره؛ من کنارتم ❤️",
                "عزیزم شکلات تلخ گرفتم برات 🍫",
                "امشب هیچ کاری نکن، فقط استراحت. بقیه‌اش با من 🤍",
                "{name}، دوستت دارم، حتی روزهایی که حالت خوب نیست 🌷"
            )
        )
        Phase.PERIOD_DUE -> PhaseInfo(
            "⏳", "روزهای پریود نزدیک است",
            "طبق آخرین اطلاعات، پریود از همین روزها انتظار می‌رود. آماده باش که کمکش کنی.",
            listOf(
                "برنامه امروز و فردا را سبک نگه دار.",
                "دمنوش، کیسه آب گرم و خوراکی‌های دلخواهش را آماده داشته باش.",
                "اگر خسته یا کم‌حوصله بود، صبوری کن و کمک عملی بده."
            ),
            listOf(
                "{name} جان، امروز چیزی لازم داری برات بگیرم؟ 🌸",
                "امشب کارها با من، تو استراحت کن ❤️",
                "به فکرتم عزیزم، هر وقت خواستی بگو 🤍"
            )
        )
        Phase.AWAITING_UPDATE -> PhaseInfo(
            "💌", "منتظر به‌روزرسانی",
            "روز مورد انتظار گذشته و اطلاعات تازه‌ای نرسیده است. می‌توانی از همسرت بخواهی یک به‌روزرسانی از هانیتا بفرستد.",
            GENERAL_TIPS,
            GENERAL_MESSAGES
        )
        Phase.STALE -> PhaseInfo(
            "🕰️", "اطلاعات قدیمی است",
            "مدتی است اطلاعات تازه‌ای نرسیده و پیش‌بینی دقیق نیست. از همسرت بخواه در هانیتا «ارسال به‌روزرسانی» را بزند.",
            GENERAL_TIPS,
            GENERAL_MESSAGES
        )
        Phase.PAUSED -> PhaseInfo(
            "⏸️", "اشتراک موقتاً متوقف است",
            "همسرت اشتراک اطلاعات را فعلاً متوقف کرده است. توصیه‌ها و پیام‌های عمومی همچنان در دسترس است.",
            GENERAL_TIPS,
            GENERAL_MESSAGES
        )
        Phase.NO_DATA -> PhaseInfo(
            "💞", "هنوز اطلاعاتی نیست",
            "وقتی همسرت در هانیتا تاریخ پریود را ثبت کند و به‌روزرسانی بفرستد، اینجا روز به روز راهنمایی می‌بینی.",
            GENERAL_TIPS,
            GENERAL_MESSAGES
        )
    }

    private val GENERAL_TIPS = listOf(
        "هر روز یک پیام کوتاه محبت‌آمیز بفرست؛ همین کار کوچک اثر بزرگی دارد.",
        "بپرس امروز چه حالی دارد و با دقت گوش بده.",
        "در کارهای خانه بدون اینکه بخواهد پیش‌قدم شو."
    )

    private val GENERAL_MESSAGES = listOf(
        "{name} جان، امروز چطوری؟ 🌷",
        "دوستت دارم عزیزم ❤️",
        "امشب چه برنامه‌ای دوست داری با هم داشته باشیم؟ 😊"
    )

    /** Replaces "{name}" with the owner's name, or a warm default. */
    fun personalize(message: String, ownerName: String): String {
        val name = ownerName.trim()
        return if (name.isEmpty()) message.replace("{name} جان، ", "عزیزم، ").replace("{name}، ", "عزیزم، ").replace("{name}", "عزیزم")
        else message.replace("{name}", name)
    }

    fun eventTitle(event: Event): String = when (event) {
        Event.PERIOD_SOON -> "چند روز قبل از پریود"
        Event.PERIOD_START -> "روز شروع پریود"
        Event.PMS_START -> "شروع روزهای پیش از پریود"
        Event.FERTILE_START -> "شروع روزهای باروری"
    }

    fun eventEmoji(event: Event): String = when (event) {
        Event.PERIOD_SOON -> "⏳"
        Event.PERIOD_START -> "🩸"
        Event.PMS_START -> "🍂"
        Event.FERTILE_START -> "🌸"
    }

    /**
     * Notification text. It stays discreet (no "period" or "fertile") because the partner's
     * phone may be seen by others; the app itself shows the details.
     */
    fun notificationText(event: Event, ownerName: String, daysBefore: Int): String {
        val name = ownerName.trim().ifEmpty { "همسرت" }
        return when (event) {
            Event.PERIOD_SOON -> "حدود ${PersianNumbers.toPersianDigits(daysBefore)} روز دیگر روزهای خاص $name شروع می‌شود. یک پیام مهربان بفرست 💙"
            Event.PERIOD_START -> "از امروز روزهای خاص $name است؛ امروز بیشتر هوایش را داشته باش 🤍"
            Event.PMS_START -> "این چند روز $name ممکن است حساس‌تر باشد؛ صبوری و همدلی یادت نرود 🍂"
            Event.FERTILE_START -> "یادآوری از تقویم مشترک شما با $name 🌸"
        }
    }

    fun dailyTipText(phase: Phase, fertileShared: Boolean, dayOfYear: Int): String {
        val tips = phaseInfo(phase, fertileShared).tips
        return tips[Math.floorMod(dayOfYear, tips.size)]
    }
}
