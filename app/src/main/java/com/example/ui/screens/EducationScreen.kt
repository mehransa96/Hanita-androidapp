package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("آموزش و سلامت", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        val articles = getEducationalArticles()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "آموزش", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("مطالب آموزش سلامت زنان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("فازهای چرخه، هورمون‌ها، تغذیه، ورزش، علائم هشدار و منابع معتبر (اطلاعات عمومی، جایگزین پزشک نیست)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            articles.groupBy { it.group }.forEach { (group, groupArticles) ->
                item(key = "header_$group") {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(groupArticles, key = { it.title }) { article ->
                    ArticleCard(article)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class Article(
    val title: String,
    val phase: String,
    val summary: String,
    val nutrition: String,
    val exercise: String,
    val color: Color,
    /** What the hormones are doing (empty for topic articles). */
    val hormones: String = "",
    /** Typical body and mood changes. */
    val bodyAndMood: String = "",
    /** Practical, low-risk self-care steps. */
    val selfCare: List<String> = emptyList(),
    /** Signs that need a doctor; always shown in an error-tinted box when present. */
    val seeDoctor: String = "",
    /** Short source names so the reader knows where the advice comes from. */
    val sources: String = "",
    /** Section header: "فازهای چرخه" or a topic group. */
    val group: String = PHASE_GROUP
)

const val PHASE_GROUP = "فازهای چرخه"
const val TOPIC_GROUP = "دانستنی‌های مهم سلامت قاعدگی"

fun getEducationalArticles(): List<Article> {
    return listOf(
        Article(
            title = "مرحله قاعدگی (Menstrual)",
            phase = "معمولاً روز ۱ تا ۵ (طبیعی: ۲ تا ۷ روز)",
            summary = "روز اول خونریزی، روز اول چرخه است. پوشش داخلی رحم (آندومتر) که برای بارداری آماده شده بود، چون بارداری رخ نداده دفع می‌شود. حجم طبیعی خونریزی در کل دوره حدود ۳۰ تا ۸۰ میلی‌لیتر (چند قاشق غذاخوری) است و معمولاً دو روز اول بیشتر است.",
            nutrition = "غذاهای غنی از آهن (گوشت قرمز کم‌چرب، مرغ، عدس، اسفناج، جگر در حد متعادل) را همراه یک منبع ویتامین C (لیمو، پرتقال، فلفل دلمه) بخورید تا جذب آهن بهتر شود؛ چای و قهوه را با فاصله یک ساعت از غذا بنوشید چون جذب آهن را کم می‌کنند. مایعات گرم، دمنوش زنجبیل و غذاهای سبک و گرم به بسیاری از افراد احساس راحتی بیشتری می‌دهند. زنجبیل در چند کارآزمایی بالینی، درد قاعدگی را به اندازه مسکن‌های ساده کم کرده است.",
            exercise = "ورزش سبک تا متوسط مثل پیاده‌روی، کشش، یوگا و شنا می‌تواند درد و نفخ را کمتر کند؛ لازم نیست ورزش را کاملاً کنار بگذارید. اگر خونریزی شدید یا خستگی زیاد دارید، شدت تمرین را کم کنید و به بدن‌تان گوش دهید.",
            color = Color(0xFFF43F5E),
            hormones = "استروژن و پروژسترون در پایین‌ترین سطح خود هستند. افت پروژسترون باعث ترشح پروستاگلاندین‌ها می‌شود؛ همین مواد انقباض رحم و درد پریود را ایجاد می‌کنند. هم‌زمان FSH کم‌کم بالا می‌رود تا فولیکول‌های تازه را برای چرخه بعد آماده کند.",
            bodyAndMood = "گرفتگی زیر شکم یا کمر، خستگی، سردرد، نفخ و گاهی شل شدن مدفوع شایع است. بسیاری از افراد بعد از شروع خونریزی احساس سبکی روحی می‌کنند چون علائم PMS فروکش می‌کند.",
            selfCare = listOf(
                "گرما: کیسه آب گرم یا پد گرمایی روی زیر شکم برای ۱۵ تا ۲۰ دقیقه (پوست را مستقیم نسوزانید).",
                "مسکن‌های ضدالتهاب مثل ایبوپروفن یا مفنامیک اسید اگر منعی ندارید، بهتر است از همان شروع درد و همراه غذا مصرف شوند؛ دوز را طبق بروشور یا نظر پزشک رعایت کنید.",
                "پد یا تامپون را هر ۴ تا ۸ ساعت عوض کنید؛ کاپ قاعدگی را طبق دستور سازنده خالی و تمیز کنید.",
                "خواب کافی و تنفس آهسته شکمی (بخش حال خوب) به کاهش تنش عضلانی کمک می‌کند."
            ),
            seeDoctor = "عوض کردن پد یا تامپون هر ساعت برای چند ساعت پشت سر هم، لخته‌های بزرگ‌تر از یک سکه، خونریزی بیش از ۷ روز، دردی که با مسکن معمولی کنترل نمی‌شود یا شما را از کار و مدرسه بازمی‌دارد، سرگیجه یا تنگی نفس (نشانه احتمالی کم‌خونی).",
            sources = "ACOG • NHS • WHO"
        ),
        Article(
            title = "مرحله فولیکولی (Follicular)",
            phase = "از روز اول پریود تا تخمک‌گذاری (در چرخه ۲۸ روزه حدود روز ۱ تا ۱۳)",
            summary = "فاز فولیکولار در واقع از روز اول پریود شروع می‌شود و تا تخمک‌گذاری ادامه دارد؛ نیمه دوم آن (بعد از قطع خونریزی) همان روزهایی است که بیشتر افراد انرژی بهتری دارند. طول این فاز بیشترین تفاوت را بین افراد دارد و دلیل اصلی متفاوت بودن طول چرخه‌هاست.",
            nutrition = "پروتئین کافی در هر وعده (تخم‌مرغ، لبنیات، حبوبات، مرغ، ماهی)، غلات کامل (جو، نان سبوس‌دار، برنج قهوه‌ای) و سبزیجات متنوع. ماهی‌های چرب یا گردو برای امگا ۳ و لبنیات تخمیری مثل ماست و دوغ برای سلامت روده گزینه‌های خوبی هستند.",
            exercise = "زمان مناسبی برای افزایش تدریجی شدت تمرین است: تمرین قدرتی، دویدن، دوچرخه و ورزش‌های گروهی. توصیه کلی سازمان جهانی بهداشت برای بزرگسالان ۱۵۰ تا ۳۰۰ دقیقه فعالیت متوسط در هفته به‌علاوه دو جلسه تمرین قدرتی است.",
            color = Color(0xFF10B981),
            hormones = "FSH چند فولیکول را رشد می‌دهد و فولیکول غالب استروژن بیشتری می‌سازد. استروژن پوشش رحم را دوباره ضخیم می‌کند و ترشحات دهانه رحم را کم‌کم شفاف‌تر و لغزنده‌تر می‌کند.",
            bodyAndMood = "با بالا رفتن استروژن، بسیاری از افراد انرژی، تمرکز و حال بهتری را گزارش می‌کنند و پوست شفاف‌تر به نظر می‌رسد. ترشحات واژن از کم و چسبنده به سمت کرمی و سپس شفاف حرکت می‌کند.",
            selfCare = listOf(
                "برنامه‌های تازه، کارهای نیازمند تمرکز و یادگیری مهارت جدید را در این روزها بچینید.",
                "اگر قصد بارداری دارید، از همین روزها مصرف روزانه ۴۰۰ میکروگرم اسید فولیک را (با نظر پزشک) فراموش نکنید.",
                "اگر پیشگیری از بارداری مهم است، روزهای نزدیک تخمک‌گذاری را «بی‌خطر» فرض نکنید."
            ),
            seeDoctor = "خونریزی یا لکه‌بینی بین دو پریود، درد لگن یکطرفه شدید، ترشحات بدبو، خارش یا سوزش.",
            sources = "ACOG • WHO Physical Activity Guidelines 2020"
        ),
        Article(
            title = "مرحله تخمک‌گذاری (Ovulation)",
            phase = "حدود ۱۴ روز قبل از پریود بعدی (در چرخه ۲۸ روزه حدود روز ۱۴)",
            summary = "تخمک‌گذاری حدود ۱۰ تا ۱۶ روز (به‌طور میانگین ۱۴ روز) قبل از شروع پریود بعدی رخ می‌دهد، نه لزوماً روز ۱۴. تخمک حدود ۱۲ تا ۲۴ ساعت زنده می‌ماند اما اسپرم تا ۵ روز؛ برای همین «پنجره باروری» حدود ۶ روز است: ۵ روز قبل از تخمک‌گذاری و خود روز تخمک‌گذاری.",
            nutrition = "آب کافی (ادرار باید زرد کم‌رنگ باشد)، میوه و سبزیجات رنگی، مغزها و دانه‌ها. اگر برای بارداری برنامه دارید، اسید فولیک، ید (نمک یددار) و پرهیز از الکل و دخانیات اهمیت دارد.",
            exercise = "برای بسیاری از افراد زمان مناسبی برای تمرین‌های پرشدت (HIIT)، تمرین قدرتی و ورزش‌های تیمی است. گرم کردن را جدی بگیرید؛ برخی پژوهش‌ها به شل‌تر بودن رباط‌ها در این روزها اشاره دارند.",
            color = Color(0xFF8B5CF6),
            hormones = "وقتی استروژن به اوج می‌رسد، جهش ناگهانی LH رخ می‌دهد و حدود ۲۴ تا ۳۶ ساعت بعد تخمک آزاد می‌شود. تست‌های تخمک‌گذاری خانگی همین جهش LH را نشان می‌دهند.",
            bodyAndMood = "ترشحات شفاف، کشسان و لغزنده مثل سفیده تخم‌مرغ، افزایش میل جنسی و گاهی درد خفیف یکطرفه زیر شکم (درد میتل‌اشمرتز) که چند ساعت تا یک روز طول می‌کشد. دمای پایه بدن بعد از تخمک‌گذاری حدود ۰٫۲ تا ۰٫۵ درجه بالا می‌رود.",
            selfCare = listOf(
                "برای تشخیص دقیق‌تر روز تخمک‌گذاری، ترکیبی از ترشحات، تست LH و دمای پایه را در بخش ابزارهای سلامت ثبت کنید.",
                "پیش‌بینی برنامه بر اساس چرخه‌های قبلی است و روش مطمئن پیشگیری از بارداری نیست.",
                "اگر درد میانه چرخه آزارتان می‌دهد، گرما و مسکن ساده معمولاً کافی است."
            ),
            seeDoctor = "درد یکطرفه شدید یا همراه با تب، استفراغ یا غش (به‌ویژه اگر احتمال بارداری هست، برای رد بارداری خارج رحمی فوراً مراجعه کنید)، یا اگر بیش از ۱۲ ماه (بالای ۳۵ سال: ۶ ماه) برای بارداری تلاش کرده‌اید.",
            sources = "ACOG • NICE Fertility Guideline • Wilcox et al. NEJM 1995"
        ),
        Article(
            title = "مرحله لوتئال (Luteal)",
            phase = "از تخمک‌گذاری تا پریود بعدی (معمولاً ۱۲ تا ۱۴ روز)",
            summary = "طول فاز لوتئال در هر فرد نسبتاً ثابت است. جسم زرد پروژسترون می‌سازد تا رحم برای لانه‌گزینی آماده بماند؛ اگر بارداری رخ ندهد، جسم زرد تحلیل می‌رود، هورمون‌ها افت می‌کنند و پریود بعدی شروع می‌شود. علائم پیش از قاعدگی (PMS) در ۵ تا ۷ روز آخر این فاز شایع است.",
            nutrition = "وعده‌های منظم و کوچک‌تر با کربوهیدرات‌های پیچیده و پروتئین، قند خون و خلق را پایدارتر می‌کند. منابع کلسیم (لبنیات) و منیزیم (مغزها، تخم کدو، حبوبات، شکلات تلخ) در پژوهش‌ها با علائم کمتر PMS همراه بوده‌اند. نمک زیاد نفخ و احتباس آب را بیشتر می‌کند؛ کافئین و الکل هم می‌توانند حساسیت سینه و بی‌خوابی را تشدید کنند.",
            exercise = "فعالیت منظم هوازی (پیاده‌روی تند، شنا، دوچرخه) از بهترین راه‌های غیردارویی کاهش علائم PMS است. در روزهای آخر اگر خستگی بیشتری دارید، یوگا، پیلاتس و کشش را جایگزین تمرین سنگین کنید.",
            color = Color(0xFFF59E0B),
            hormones = "پروژسترون بالا می‌رود و استروژن یک اوج دوم ملایم دارد. پروژسترون دمای بدن را کمی بالا می‌برد و می‌تواند خواب‌آلودگی، یبوست و حساسیت سینه ایجاد کند. در روزهای آخر افت هر دو هورمون با تغییرات سروتونین در مغز و علائم خلقی مرتبط است.",
            bodyAndMood = "نفخ، حساسیت سینه، جوش، اشتهای بیشتر به شیرینی، کم‌حوصلگی، تحریک‌پذیری و بی‌خوابی از علائم شایع هستند و با شروع پریود بهتر می‌شوند.",
            selfCare = listOf(
                "علائم را روزانه در ثبت حال و علائم وارد کنید تا الگوی دو تا سه ماه را ببینید؛ این بهترین ابزار برای گفت‌وگو با پزشک است.",
                "خواب منظم (ساعت ثابت خواب و بیداری) و کم کردن صفحه نمایش قبل از خواب.",
                "تنفس جعبه‌ای یا تنفس ۴-۷-۸ در لحظه‌های تنش (بخش حال خوب).",
                "برنامه‌های سنگین و تصمیم‌های پرتنش را در صورت امکان به هفته بعد منتقل کنید."
            ),
            seeDoctor = "اگر علائم خلقی آن‌قدر شدید است که کار یا روابط‌تان را مختل می‌کند (احتمال PMDD)، یا افکار آسیب به خود دارید، حتماً با پزشک یا روان‌شناس صحبت کنید. تأخیر پریود همراه احتمال بارداری هم نیاز به تست دارد.",
            sources = "ACOG PMS/PMDD • NICE CKS • Cochrane"
        ),
        Article(
            title = "PMS یا PMDD؟ تفاوت را بشناسید",
            phase = "دانستنی",
            summary = "تا ۷۵٪ زنان درجاتی از علائم پیش از قاعدگی دارند، اما حدود ۳ تا ۸٪ به اختلال ملال پیش از قاعدگی (PMDD) مبتلا هستند: علائم خلقی شدید مثل غم عمیق، ناامیدی، اضطراب، تحریک‌پذیری شدید یا احساس از دست دادن کنترل که در هفته قبل از پریود شروع می‌شود، چند روز بعد از شروع خونریزی برطرف می‌شود و زندگی روزمره را مختل می‌کند.",
            nutrition = "رژیم متعادل، کلسیم کافی و کاهش کافئین، الکل و نمک برای هر دو حالت مفید است، اما PMDD معمولاً به درمان پزشکی نیاز دارد.",
            exercise = "ورزش هوازی منظم در بیشتر روزهای هفته به کاهش علائم کمک می‌کند.",
            color = Color(0xFFEC4899),
            selfCare = listOf(
                "دو چرخه کامل علائم را روزانه ثبت کنید؛ تشخیص PMDD بر اساس ثبت روزانه است نه حافظه.",
                "درمان‌های مؤثر وجود دارد (مشاوره شناختی‌رفتاری، برخی داروهای ضدافسردگی و قرص‌های هورمونی)؛ تصمیم با پزشک است."
            ),
            seeDoctor = "علائم شدید خلقی هر ماه، یا هر گونه فکر آسیب به خود. در ایران در شرایط بحرانی با اورژانس اجتماعی ۱۲۳ یا صدای مشاور بهزیستی ۱۴۸۰ تماس بگیرید.",
            sources = "ACOG • DSM-5-TR • NICE",
            group = TOPIC_GROUP
        ),
        Article(
            title = "درد پریود: کی طبیعی است و کی نه؟",
            phase = "دانستنی",
            summary = "درد پریود (دیسمنوره اولیه) در نوجوانان و زنان جوان بسیار شایع است و معمولاً از چند ساعت قبل یا همزمان با شروع خونریزی آغاز می‌شود و ۱ تا ۳ روز طول می‌کشد. دردی که با گذشت سال‌ها بدتر می‌شود، خارج از روزهای پریود هم وجود دارد یا با درد هنگام رابطه جنسی همراه است، ممکن است ثانویه باشد (مثلاً آندومتریوز یا فیبروم).",
            nutrition = "زنجبیل (حدود ۲۵۰ میلی‌گرم پودر، ۳ تا ۴ بار در روز در سه روز اول) در کارآزمایی‌ها مؤثر بوده است. مصرف کافی امگا ۳ و کم کردن غذاهای خیلی شور و فرآوری‌شده هم کمک‌کننده است.",
            exercise = "ورزش منظم در طول ماه (نه فقط روزهای پریود) در مرور کاکرین با کاهش درد قاعدگی همراه بوده است.",
            color = Color(0xFFE11D48),
            selfCare = listOf(
                "گرمای موضعی تقریباً به اندازه مسکن‌های ساده اثر دارد و می‌توان با هم استفاده کرد.",
                "مسکن ضدالتهاب (NSAID) را از روز اول و طبق دوز مجاز مصرف کنید؛ اگر زخم معده، آسم حساس به آسپرین یا بیماری کلیه دارید، اول با پزشک مشورت کنید.",
                "شدت درد را روزانه از ۰ تا ۱۰ ثبت کنید تا روند آن روشن شود."
            ),
            seeDoctor = "دردی که با مسکن بهتر نمی‌شود، با هر چرخه بدتر می‌شود، باعث غیبت از کار یا مدرسه می‌شود، یا با تب، ترشحات بدبو یا درد هنگام رابطه همراه است.",
            sources = "ACOG Dysmenorrhea • Cochrane Reviews",
            group = TOPIC_GROUP
        ),
        Article(
            title = "چرخه نامنظم و سندرم تخمدان پلی‌کیستیک (PCOS)",
            phase = "دانستنی",
            summary = "چرخه بزرگسالان معمولاً ۲۱ تا ۳۵ روز است و تفاوت طول چرخه‌ها تا ۷ تا ۹ روز طبیعی محسوب می‌شود. در دو سه سال اول بعد از اولین پریود و در سال‌های نزدیک یائسگی، بی‌نظمی شایع‌تر است. سندرم تخمدان پلی‌کیستیک حدود ۱ نفر از هر ۱۰ زن را درگیر می‌کند و با چرخه‌های طولانی یا نامنظم، موهای زائد، آکنه و گاهی افزایش وزن همراه است.",
            nutrition = "در PCOS، کاهش حتی ۵٪ وزن (در صورت اضافه وزن) و رژیم کم‌قند و پرفیبر می‌تواند نظم چرخه را بهتر کند.",
            exercise = "حداقل ۱۵۰ دقیقه فعالیت متوسط در هفته به حساسیت انسولین و تنظیم چرخه کمک می‌کند.",
            color = Color(0xFF0EA5E9),
            selfCare = listOf(
                "تاریخ شروع و پایان همه پریودها را دقیق ثبت کنید؛ تحلیل هوشمند چرخه نظم چرخه‌های شما را نشان می‌دهد.",
                "استرس شدید، کاهش وزن ناگهانی، ورزش خیلی سنگین و بیماری‌های تیروئید هم می‌توانند چرخه را به هم بزنند."
            ),
            seeDoctor = "چرخه کوتاه‌تر از ۲۱ یا بلندتر از ۳۵ روز به‌طور مکرر، نیامدن پریود بیش از ۳ ماه (در صورت منفی بودن تست بارداری)، کمتر از ۸ پریود در سال، یا رشد موهای زائد و آکنه شدید.",
            sources = "ACOG • International PCOS Guideline 2023",
            group = TOPIC_GROUP
        ),
        Article(
            title = "خونریزی شدید و کم‌خونی فقر آهن",
            phase = "دانستنی",
            summary = "خونریزی شدید قاعدگی یعنی خونریزی‌ای که بر کیفیت زندگی اثر بگذارد؛ به‌طور عملی اگر هر ۱ تا ۲ ساعت نیاز به تعویض پد دارید، شب‌ها برای تعویض بیدار می‌شوید یا لخته‌های بزرگ دفع می‌کنید. این وضعیت شایع‌ترین علت کم‌خونی فقر آهن در زنان است.",
            nutrition = "منابع آهن هِم (گوشت قرمز، مرغ، ماهی) بهتر جذب می‌شوند. آهن گیاهی (عدس، لوبیا، اسفناج، غلات غنی‌شده) را با ویتامین C بخورید. مکمل آهن را فقط با تشخیص و نظر پزشک مصرف کنید چون مصرف بی‌رویه هم ضرر دارد.",
            exercise = "اگر خستگی، تپش قلب یا تنگی نفس هنگام فعالیت دارید، قبل از افزایش شدت ورزش آزمایش خون بدهید.",
            color = Color(0xFFB91C1C),
            selfCare = listOf(
                "تعداد پد یا تامپون روزانه و شدت خونریزی را ثبت کنید؛ این اطلاعات در گزارش پزشک نمایش داده می‌شود.",
                "علائم کم‌خونی: خستگی، رنگ‌پریدگی، ریزش مو، شکنندگی ناخن، سرگیجه و میل به خوردن یخ."
            ),
            seeDoctor = "خونریزی بیش از ۷ روز، تعویض پد هر ساعت، لخته‌های بزرگ، سرگیجه یا غش. خونریزی شدید ناگهانی با ضعف شدید نیاز به مراجعه فوری دارد.",
            sources = "NICE NG88 • WHO • ACOG",
            group = TOPIC_GROUP
        ),
        Article(
            title = "خواب، استرس و چرخه",
            phase = "دانستنی",
            summary = "استرس طولانی از طریق محور هیپوتالاموس–هیپوفیز می‌تواند تخمک‌گذاری را عقب بیندازد و پریود را دیر یا نامنظم کند. در روزهای قبل از پریود هم بسیاری از افراد خواب سبک‌تر و بیدار شدن‌های بیشتری دارند.",
            nutrition = "شام سبک ۲ تا ۳ ساعت قبل از خواب، پرهیز از کافئین بعد از ظهر و نوشیدنی گرم بدون کافئین قبل از خواب.",
            exercise = "فعالیت بدنی در طول روز خواب شب را عمیق‌تر می‌کند، اما تمرین خیلی سنگین را به ۲ تا ۳ ساعت قبل از خواب نزدیک نکنید.",
            color = Color(0xFF6366F1),
            selfCare = listOf(
                "۷ تا ۹ ساعت خواب با ساعت ثابت خواب و بیداری، حتی در تعطیلات.",
                "اتاق خنک، تاریک و ساکت؛ صداهای یکنواخت مثل باران یا نویز قهوه‌ای در بخش حال خوب می‌تواند کمک کند.",
                "۱۰ دقیقه تنفس آهسته یا اسکن بدن قبل از خواب."
            ),
            seeDoctor = "بی‌خوابی بیش از ۳ شب در هفته برای بیش از ۳ ماه، یا خستگی شدید روزانه با وجود خواب کافی.",
            sources = "AASM • NHS",
            group = TOPIC_GROUP
        )
    )
}

@Composable
fun ArticleCard(article: Article) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(12.dp).background(article.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = article.phase, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "بیشتر",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("خلاصه:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                if (article.hormones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("هورمون‌ها چه می‌کنند؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(article.hormones, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }

                if (article.bodyAndMood.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("بدن و حال‌وهوا:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(article.bodyAndMood, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("تغذیه مناسب:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.nutrition, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("ورزش پیشنهادی:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.exercise, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                if (article.selfCare.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("مراقبت از خود:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    article.selfCare.forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (article.seeDoctor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("چه زمانی به پزشک مراجعه کنم؟", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(article.seeDoctor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                if (article.sources.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("منابع: ${article.sources}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
