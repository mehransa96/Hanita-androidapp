package com.example.ui.components

import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class PhaseDetail(
    val id: String,
    val title: String,
    val emoji: String,
    val dayRange: String,
    val hormones: String,
    val energyLevel: String,
    val nutrition: List<String>,
    val workout: String,
    val seedCycling: String,
    val themeColor: Color,
    val mindAndMood: String = "",
    val selfCare: List<String> = emptyList(),
    val redFlags: String = ""
)

@Composable
fun CyclePhaseGuideDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    val phases = listOf(
        PhaseDetail(
            id = "menstrual",
            title = "فاز قاعدگی",
            emoji = "🩸",
            dayRange = "از روز اول خونریزی؛ معمولاً ۳ تا ۷ روز",
            hormones = "استروژن و پروژسترون در پایین‌ترین سطح هستند؛ پروستاگلاندین‌ها با انقباض رحم پوشش آن را دفع می‌کنند و عامل اصلی گرفتگی و درد هستند.",
            energyLevel = "انرژی معمولاً کمتر است، به‌ویژه دو روز اول؛ از روز سوم به بعد بسیاری از افراد کم‌کم بهتر می‌شوند.",
            nutrition = listOf("سوپ و غذاهای گرم و زودهضم", "اسفناج و عدس (تامین آهن)", "شکلات تلخ ۷۰٪ به بالا (تامین منیزیم)", "دمنوش دارچین، زنجبیل و نبات"),
            workout = "کشش ملایم، یوگای یین، تنفس عمیق و خواب کافی",
            seedCycling = "۱ قاشق دانه کتان آسیاب‌شده + ۱ قاشق تخم کدو (کمک به تنظیم استروژن)",
            themeColor = Color(0xFFEF4444),
            mindAndMood = "ممکن است حساس‌تر، خسته‌تر یا درون‌گراتر باشید؛ برای خیلی‌ها شروع خونریزی همراه با سبک شدن تنش‌های PMS است.",
            selfCare = listOf("کیسه آب گرم روی زیر شکم ۱۵ تا ۲۰ دقیقه", "مسکن ضدالتهاب از شروع درد و همراه غذا (اگر منعی ندارید)", "تعویض پد هر ۴ تا ۸ ساعت", "تنفس شکمی آهسته در بخش حال خوب"),
            redFlags = "تعویض پد هر ساعت، لخته بزرگ‌تر از سکه، خونریزی بیش از ۷ روز، درد مقاوم به مسکن یا سرگیجه"
        ),
        PhaseDetail(
            id = "follicular",
            title = "فاز فولیکولار",
            emoji = "🌱",
            dayRange = "بعد از پریود تا تخمک‌گذاری؛ طولش در افراد متفاوت است",
            hormones = "FSH رشد فولیکول‌ها را آغاز می‌کند و فولیکول غالب استروژن بیشتری می‌سازد؛ پوشش رحم دوباره ضخیم می‌شود.",
            energyLevel = "با بالا رفتن استروژن، انرژی، تمرکز و انگیزه برای بیشتر افراد بیشتر می‌شود.",
            nutrition = listOf("جوانه‌ها، کلم بروکلی و سبزیجات برگ سبز", "ماهی و اسیدهای چرب امگا ۳", "حبوبات و غلات کامل", "مرکبات و منابع ویتامین C"),
            workout = "افزایش تدریجی شدت: تمرین قدرتی، دویدن، دوچرخه‌سواری و ورزش‌های گروهی",
            seedCycling = "تخم کدو و دانه کتان (ادامه مصرف تا روز تخمک‌گذاری)",
            themeColor = Color(0xFF10B981),
            mindAndMood = "حال‌وهوا معمولاً پایدارتر و خوش‌بین‌تر است؛ زمان مناسبی برای یادگیری و شروع برنامه‌های تازه.",
            selfCare = listOf("کارهای مهم و نیازمند تمرکز را در این روزها بچینید", "شدت ورزش را کم‌کم بالا ببرید", "اگر قصد بارداری دارید، اسید فولیک روزانه را با نظر پزشک شروع کنید"),
            redFlags = "خونریزی بین دو پریود، ترشحات بدبو یا خارش، درد لگن شدید"
        ),
        PhaseDetail(
            id = "ovulation",
            title = "فاز تخمک‌گذاری",
            emoji = "✨",
            dayRange = "حدود ۱۴ روز قبل از پریود بعدی (۱۰ تا ۱۶ روز)",
            hormones = "اوج استروژن جهش LH را ایجاد می‌کند و حدود ۲۴ تا ۳۶ ساعت بعد تخمک آزاد می‌شود. تخمک ۱۲ تا ۲۴ ساعت و اسپرم تا ۵ روز زنده می‌ماند.",
            energyLevel = "برای بسیاری از افراد اوج انرژی، میل جنسی و تمایل به معاشرت است؛ ترشحات شفاف و کشسان و گاهی درد خفیف یکطرفه زیر شکم دیده می‌شود.",
            nutrition = listOf("توت‌ها و میوه‌های سرشار از آنتی‌اکسیدان", "آووکادو و بادام درختی", "تخم‌مرغ و منابع پروتئین سالم", "آب فراوان جهت هیدراتاسیون کامل"),
            workout = "اگر حالتان خوب است: HIIT، رقص پرانرژی، تمرین قدرتی؛ گرم کردن را جدی بگیرید",
            seedCycling = "تغییر رژیم دانه‌ها: شروع مصرف دانه آفتابگردان و کنجد",
            themeColor = Color(0xFFA855F7),
            mindAndMood = "اعتمادبه‌نفس و تمایل به ارتباط بیشتر است، اما همه این تغییرات را تجربه نمی‌کنند و تفاوت فردی طبیعی است.",
            selfCare = listOf("ثبت ترشحات، تست LH و دمای پایه برای تشخیص دقیق‌تر", "گرم کردن کامل قبل از تمرین شدید", "پیش‌بینی برنامه روش پیشگیری از بارداری نیست"),
            redFlags = "درد یکطرفه شدید با تب، استفراغ یا ضعف (به‌ویژه با احتمال بارداری) نیاز به مراجعه فوری دارد"
        ),
        PhaseDetail(
            id = "luteal",
            title = "فاز لوتئال (PMS)",
            emoji = "🍂",
            dayRange = "از تخمک‌گذاری تا پریود بعدی؛ معمولاً ۱۲ تا ۱۴ روز",
            hormones = "جسم زرد پروژسترون می‌سازد و دمای پایه بدن ۰٫۲ تا ۰٫۵ درجه بالا می‌رود؛ اگر بارداری رخ ندهد، در روزهای آخر پروژسترون و استروژن افت می‌کنند.",
            energyLevel = "در نیمه اول انرژی خوب است؛ در ۵ تا ۷ روز آخر خستگی، اشتهای بیشتر (مصرف انرژی بدن حدود ۱۰۰ تا ۳۰۰ کالری بالا می‌رود) و علائم PMS شایع است.",
            nutrition = listOf("سیب‌زمینی شیرین و کربوهیدرات‌های پیچیده", "موز و مغزیجات (ویتامین B6 جهت کاهش اضطراب)", "دمنوش بابونه و گل‌گاوزبان", "پرهیز از مصرف زیاد نمک و شکر مصنوعی"),
            workout = "پیاده‌روی در طبیعت، پیلاتس سبک، یوگا و استراحت منظم",
            seedCycling = "۱ قاشق دانه آفتابگردان + ۱ قاشق کنجد (کمک به ترشح بهینه پروژسترون)",
            themeColor = Color(0xFFF59E0B),
            mindAndMood = "در روزهای آخر کم‌حوصلگی، تحریک‌پذیری، اضطراب یا غم خفیف شایع است و با شروع پریود بهتر می‌شود.",
            selfCare = listOf("ثبت روزانه علائم برای دیدن الگوی دو تا سه ماه", "خواب منظم و کاهش کافئین و نمک", "ورزش هوازی سبک تا متوسط", "تنفس جعبه‌ای در لحظه‌های تنش"),
            redFlags = "علائم خلقی شدید که کار یا روابط را مختل می‌کند (احتمال PMDD) یا هر گونه فکر آسیب به خود"
        )
    )

    var selectedPhaseId by remember { mutableStateOf("menstrual") }
    val currentPhase = phases.first { it.id == selectedPhaseId }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, currentPhase.themeColor.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(currentPhase.themeColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = currentPhase.themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "راهنمای فازهای چهارگانه چرخه 🌸",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تغذیه هورمونی، دانه درمانی و تنظیم انرژی",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    phases.forEach { p ->
                        val isSelected = p.id == selectedPhaseId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) p.themeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .selectable(selected = isSelected, role = Role.RadioButton) { selectedPhaseId = p.id }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(p.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.title.replace("فاز ", ""),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Detail Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner of the Phase
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = currentPhase.themeColor.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, currentPhase.themeColor.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${currentPhase.emoji} ${currentPhase.title}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = currentPhase.themeColor
                                )
                                Text(
                                    text = currentPhase.dayRange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Hormones Card
                    DetailSectionCard(
                        icon = Icons.Filled.Timeline,
                        iconColor = currentPhase.themeColor,
                        title = "تغییرات هورمونی و وضعیت انرژی",
                        content = "${currentPhase.hormones}\n\n${currentPhase.energyLevel}"
                    )

                    if (currentPhase.mindAndMood.isNotBlank()) {
                        DetailSectionCard(
                            icon = Icons.Filled.Psychology,
                            iconColor = Color(0xFF8B5CF6),
                            title = "ذهن و حال‌وهوا",
                            content = currentPhase.mindAndMood
                        )
                    }

                    if (currentPhase.selfCare.isNotEmpty()) {
                        DetailSectionCard(
                            icon = Icons.Filled.Spa,
                            iconColor = Color(0xFFEC4899),
                            title = "مراقبت از خود در این فاز",
                            content = currentPhase.selfCare.joinToString("\n") { "• $it" }
                        )
                    }

                    if (currentPhase.redFlags.isNotBlank()) {
                        DetailSectionCard(
                            icon = Icons.Filled.Warning,
                            iconColor = MaterialTheme.colorScheme.error,
                            title = "چه زمانی به پزشک مراجعه کنم؟",
                            content = currentPhase.redFlags
                        )
                    }

                    // Seed Cycling
                    DetailSectionCard(
                        icon = Icons.Filled.Eco,
                        iconColor = Color(0xFF10B981),
                        title = "دانه درمانی (Seed Cycling) — روش سنتی",
                        content = currentPhase.seedCycling + "\n\nاین روش سنتی است و شواهد علمی محکمی برای اثر آن بر هورمون‌ها وجود ندارد؛ مصرف این دانه‌ها برای اغلب افراد بی‌خطر و مغذی است."
                    )

                    // Nutrition
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Text("تغذیه توصیه‌شده در این فاز:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                            currentPhase.nutrition.forEach { item ->
                                Text("• $item", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Workout
                    DetailSectionCard(
                        icon = Icons.Filled.FitnessCenter,
                        iconColor = Color(0xFF6366F1),
                        title = "بهترین فعالیت بدنی و ورزش",
                        content = currentPhase.workout
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentPhase.themeColor)
                ) {
                    Text("متوجه شدم", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
            Text(
                text = content,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
