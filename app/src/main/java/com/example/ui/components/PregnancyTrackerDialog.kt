package com.example.ui.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.bounceClick
import com.example.utils.JalaliCalendar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WeekMilestone(
    val week: Int,
    val fruit: String,
    val fruitEmoji: String,
    val lengthCm: Double,
    val weightGrams: Double,
    val highlight: String,
    val momTip: String
)

@Composable
fun PregnancyTrackerDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    lastPeriodDateStr: String
) {
    if (!show) return

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Calculate pregnancy week based on last period
    val lastPeriodDate = remember(lastPeriodDateStr) {
        runCatching { LocalDate.parse(lastPeriodDateStr) }.getOrNull() ?: LocalDate.now().minusWeeks(14)
    }

    val daysSinceLmp = remember(lastPeriodDate) {
        val days = ChronoUnit.DAYS.between(lastPeriodDate, LocalDate.now())
        if (days < 0) 0L else days
    }

    var currentWeek by remember(daysSinceLmp) {
        val w = (daysSinceLmp / 7).toInt() + 1
        mutableIntStateOf(w.coerceIn(1, 40))
    }

    val estimatedDueDate = remember(lastPeriodDate) {
        lastPeriodDate.plusDays(280) // 40 weeks standard
    }
    val dueJalali = remember(estimatedDueDate) {
        JalaliCalendar.gregorianToJalali(estimatedDueDate)
    }
    val dueJalaliStr = "${dueJalali.year}/${dueJalali.month}/${dueJalali.day}"

    val daysRemaining = remember(estimatedDueDate) {
        val r = ChronoUnit.DAYS.between(LocalDate.now(), estimatedDueDate)
        if (r < 0) 0L else r
    }

    // Trimester
    val trimesterName = when {
        currentWeek <= 12 -> "سه‌ماهه اول (هفته ۱ تا ۱۲)"
        currentWeek <= 27 -> "سه‌ماهه دوم (هفته ۱۳ تا ۲۷)"
        else -> "سه‌ماهه سوم (هفته ۲۸ تا ۴۰)"
    }

    // Milestones definition
    val milestones = remember {
        listOf(
            WeekMilestone(4, "دانه کنجد", "🌱", 0.1, 0.1, "لانه گزینی رویان در رحم و شروع تشکیل جفت و لوله عصبی", "مصرف روزانه اسید فولیک ۴۰۰ میکروگرم را حتما ادامه دهید."),
            WeekMilestone(8, "تمشک", "🫐", 1.6, 1.0, "شکل‌گیری پلک‌ها، نوک بینی و بند انگشتان؛ قلب با ۱۶۰ تپش در دقیقه می‌زند.", "برای کنترل تهوع صبحگاهی، بیسکویت ترد در کنار تخت داشته باشید."),
            WeekMilestone(12, "لیمو ترش", "🍋", 5.4, 14.0, "تشکیل کامل رفلکس‌ها؛ جنین مشت‌هایش را باز و بسته می‌کند.", "آزمایش‌های غربالگری سه‌ماهه اول را با پزشکتان هماهنگ کنید."),
            WeekMilestone(16, "آووکادو", "🥑", 11.6, 100.0, "حس شنوایی جنین فعال می‌شود و صدای ضربان قلب و جریان خون مادر را می‌شنود.", "خوابیدن به پهلوی چپ خون‌رسانی به جنین را دوچندان می‌کند."),
            WeekMilestone(20, "موز", "🍌", 25.6, 300.0, "رسیدن به نیمه راه بارداری! جنین فعالانه لگد می‌زند و می‌چرخد.", "سونوگرافی آنومالی در این هفته‌ها بررسی کاملی از سلامت جنین ارائه می‌دهد."),
            WeekMilestone(24, "بلال ذرت", "🌽", 30.0, 600.0, "ریه‌ها شاخه‌های نهایی را تشکیل می‌دهند و حس تعادل کودک شکل گرفته است.", "نوشیدن آب کافی و پیاده‌روی آرام روزانه به کاهش ورم پاها کمک می‌کند."),
            WeekMilestone(28, "بادمجان", "🍆", 37.6, 1000.0, "ورود به سه‌ماهه سوم! جنین پلک می‌زند و الگوهای خواب و بیداری دارد.", "در صورت احساس انقباض‌های خفیف استراحت کرده و مایعات بنوشید."),
            WeekMilestone(32, "کدو تنبل", "🎃", 42.4, 1700.0, "استخوان‌های جنین سفت شده‌اند و ناخن‌ها به نوک انگشتان رسیده‌اند.", "شمارش روزانه لگدهای جنین را شروع کنید (۱۰ لگد در ۲ ساعت)."),
            WeekMilestone(36, "خربزه / پاپایا", "🍈", 47.4, 2600.0, "کودک در حال گرفتن وزن نهایی است و سرش ممکن است به سمت لگن بچرخد.", "ساک زایمان و مدارک بیمارستان را آماده نگه دارید."),
            WeekMilestone(40, "هندوانه کامل", "🍉", 51.2, 3500.0, "رشد کامل معجزه کوچک شما و آمادگی کامل برای تولد و دیدار با مادر! ❤️", "تنفس عمیق و آرامش روحی داشته باشید؛ بهترین روز در پیش است.")
        )
    }

    val currentMilestone = remember(currentWeek) {
        milestones.minByOrNull { kotlin.math.abs(it.week - currentWeek) } ?: milestones.first()
    }

    // Kick Counter State
    var kickCount by remember { mutableIntStateOf(0) }

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
            border = BorderStroke(1.dp, Color(0xFFFB7185).copy(alpha = 0.3f))
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
                                .background(Color(0xFFFFF1F2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤰", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "دستیار بارداری و رشد هفته‌به‌هفته 👶",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "هفته $currentWeek بارداری • $trimesterName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Week Slider Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "هفته جاری: هفته $currentWeek از ۴۰",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9F1239)
                                )
                                Text(
                                    text = "$daysRemaining روز تا زایمان",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48)
                                )
                            }

                            Slider(
                                value = currentWeek.toFloat(),
                                onValueChange = { currentWeek = it.toInt().coerceIn(1, 40) },
                                valueRange = 1f..40f,
                                steps = 38,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFE11D48),
                                    activeTrackColor = Color(0xFFE11D48),
                                    inactiveTrackColor = Color(0xFFFECDD3)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("هفته ۱", fontSize = 10.sp, color = Color(0xFF9F1239))
                                Text("تاریخ تقریبی تولد: $dueJalaliStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9F1239))
                                Text("هفته ۴۰", fontSize = 10.sp, color = Color(0xFF9F1239))
                            }
                        }
                    }

                    // Baby Fruit Size Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFB7185).copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFE4E6), Color(0xFFFFF1F2))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(currentMilestone.fruitEmoji, fontSize = 38.sp)
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "اندازه کودک دلبند شما:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "به اندازه یک ${currentMilestone.fruit}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE11D48)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "📏 طول: ~${currentMilestone.lengthCm} cm",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "⚖️ وزن: ~${currentMilestone.weightGrams} gr",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Development Highlight
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Text("رشد جنین در این هفته:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                            Text(
                                text = currentMilestone.highlight,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Mom Health Tip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Text("توصیه سلامت مامان زیبا:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF166534))
                            }
                            Text(
                                text = currentMilestone.momTip,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF14532D)
                            )
                        }
                    }

                    // Interactive Baby Kick Counter
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("👣", fontSize = 16.sp)
                                    Text("شمارنده لگد و حرکات جنین", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6B21A8))
                                }
                                if (kickCount > 0) {
                                    TextButton(onClick = { kickCount = 0 }) {
                                        Text("تنظیم مجدد", fontSize = 11.sp, color = Color(0xFF9333EA))
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "$kickCount",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF7E22CE)
                                )
                                Button(
                                    onClick = {
                                        kickCount++
                                        Toast.makeText(context, "لگد شماره $kickCount ثبت شد 💕", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ثبت حرکت / لگد", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = "حداقل ۱۰ حرکت در طول ۲ ساعت نشانه سلامت و شادابی جنین است.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF581C87),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("متوجه شدم", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
