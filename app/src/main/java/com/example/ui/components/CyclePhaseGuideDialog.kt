package com.example.ui.components

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
    val themeColor: Color
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
            dayRange = "روزهای ۱ تا ۵",
            hormones = "استروژن و پروژسترون در پایین‌ترین سطح قرار دارند.",
            energyLevel = "کمینه انرژی؛ بدن نیازمند استراحت و بازسازی درونی است.",
            nutrition = listOf("سوپ و غذاهای گرم و زودهضم", "اسفناج و عدس (تامین آهن)", "شکلات تلخ ۷۰٪ به بالا (تامین منیزیم)", "دمنوش دارچین، زنجبیل و نبات"),
            workout = "کشش ملایم، یوگای یین، تنفس عمیق و خواب کافی",
            seedCycling = "۱ قاشق دانه کتان آسیاب‌شده + ۱ قاشق تخم کدو (کمک به تنظیم استروژن)",
            themeColor = Color(0xFFEF4444)
        ),
        PhaseDetail(
            id = "follicular",
            title = "فاز فولیکولار",
            emoji = "🌱",
            dayRange = "روزهای ۶ تا ۱۳",
            hormones = "هورمون FSH ترشح شده و استروژن روزبه‌روز افزایش می‌یابد.",
            energyLevel = "انرژی رو به اوج، تمرکز ذهنی بالا، خلاقیت و برنامه‌ریزی عالی.",
            nutrition = listOf("جوانه‌ها، کلم بروکلی و سبزیجات برگ سبز", "ماهی و اسیدهای چرب امگا ۳", "حبوبات و غلات کامل", "مرکبات و منابع ویتامین C"),
            workout = "تمرینات قدرتی و با وزنه، دویدن، دوچرخه‌سواری، ورزش‌های گروهی",
            seedCycling = "تخم کدو و دانه کتان (ادامه مصرف تا روز تخمک‌گذاری)",
            themeColor = Color(0xFF10B981)
        ),
        PhaseDetail(
            id = "ovulation",
            title = "فاز تخمک‌گذاری",
            emoji = "✨",
            dayRange = "روزهای ۱۴ تا ۱۶",
            hormones = "اوج ترشح LH و استروژن؛ آزادسازی تخمک از تخمدان.",
            energyLevel = "بالاترین سطح شادابی، جذابیت ظاهری، میل جنسی و ارتباطات اجتماعی.",
            nutrition = listOf("توت‌ها و میوه‌های سرشار از آنتی‌اکسیدان", "آووکادو و بادام درختی", "تخم‌مرغ و منابع پروتئین سالم", "آب فراوان جهت هیدراتاسیون کامل"),
            workout = "تمرینات HIIT سنگین، رقص پرانرژی، پیلاتس پیشرفته",
            seedCycling = "تغییر رژیم دانه‌ها: شروع مصرف دانه آفتابگردان و کنجد",
            themeColor = Color(0xFFA855F7)
        ),
        PhaseDetail(
            id = "luteal",
            title = "فاز لوتئال (PMS)",
            emoji = "🍂",
            dayRange = "روزهای ۱۷ تا ۲۸",
            hormones = "ترشح پروژسترون بالا؛ در صورت عدم بارداری، هورمون‌ها افت می‌کنند.",
            energyLevel = "کاهش انرژی، افزایش اشتها و کالری مصرفی، نیاز به فضایی آرام.",
            nutrition = listOf("سیب‌زمینی شیرین و کربوهیدرات‌های پیچیده", "موز و مغزیجات (ویتامین B6 جهت کاهش اضطراب)", "دمنوش بابونه و گل‌گاوزبان", "پرهیز از مصرف زیاد نمک و شکر مصنوعی"),
            workout = "پیاده‌روی در طبیعت، پیلاتس سبک، یوگا و استراحت منظم",
            seedCycling = "۱ قاشق دانه آفتابگردان + ۱ قاشق کنجد (کمک به ترشح بهینه پروژسترون)",
            themeColor = Color(0xFFF59E0B)
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
                                .clickable { selectedPhaseId = p.id }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(p.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.title.replace("فاز ", ""),
                                    fontSize = 10.sp,
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

                    // Seed Cycling
                    DetailSectionCard(
                        icon = Icons.Filled.Eco,
                        iconColor = Color(0xFF10B981),
                        title = "دانه درمانی هورمونی (Seed Cycling)",
                        content = currentPhase.seedCycling
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
