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

    fun phaseInfo(phase: Phase, fertileShared: Boolean = false): PhaseInfo = when (phase) {
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
