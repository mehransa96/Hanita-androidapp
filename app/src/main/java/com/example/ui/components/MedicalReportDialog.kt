package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DailyLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.CycleInfo
import com.example.ui.viewmodel.PredictionInfo
import com.example.utils.JalaliCalendar
import java.time.LocalDate
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers

@Composable
fun MedicalReportDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    userName: String,
    userWeight: String,
    cycleInfo: CycleInfo,
    predictionInfo: PredictionInfo,
    completedCycles: List<com.example.domain.CyclePredictor.CompletedCycle>,
    logs: List<DailyLog>
) {
    if (!show) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val todayJalaliStr = remember { PersianDates.long(LocalDate.now()) }
    val averageCycleLength = predictionInfo.calculatedAverage
    val fa = { n: Int -> PersianNumbers.toPersianDigits(n) }

    // Analysis calculations
    val periodLogs = remember(logs) { logs.filter { it.periodFlow != "NONE" } }
    val loggedDaysCount = logs.size
    val totalPeriodDays = periodLogs.size

    val allSymptoms: List<String> = remember(logs) {
        logs.flatMap { log ->
            if (log.symptoms.isBlank()) emptyList()
            else log.symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
    val symptomFrequency: List<Pair<String, Int>> = remember(allSymptoms) {
        allSymptoms.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
    }

    val allMoods: List<String> = remember(logs) {
        logs.flatMap { log ->
            if (log.moods.isBlank()) emptyList()
            else log.moods.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
    val moodFrequency: List<Pair<String, Int>> = remember(allMoods) {
        allMoods.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
    }

    val avgBbt = remember(logs) {
        val bbtLogs = logs.filter { it.bbt != null && it.bbt > 0f }
        if (bbtLogs.isNotEmpty()) bbtLogs.map { it.bbt!! }.average().toFloat() else null
    }
    val avgWater = remember(logs) {
        val waterLogs = logs.filter { it.waterIntake > 0 }
        if (waterLogs.isNotEmpty()) waterLogs.map { it.waterIntake }.average().toFloat() else null
    }

    // Every value below is computed from the user's own logs; nothing is estimated or invented.
    val regularityStatus = when {
        predictionInfo.totalCyclesCalculated < 3 -> "داده کافی نیست (کمتر از ۳ چرخه)"
        predictionInfo.isIrregular -> "نامنظم (اختلاف ${fa(predictionInfo.cycleVariationDays)} روز)"
        else -> "منظم (اختلاف ${fa(predictionInfo.cycleVariationDays)} روز)"
    }
    val avgPeriodDuration = if (periodLogs.isNotEmpty()) "${fa(predictionInfo.averagePeriodLength)} روز" else "ثبت نشده"
    val cycleLengthText = if (predictionInfo.hasEnoughData) "${fa(averageCycleLength)} روز"
    else "${fa(averageCycleLength)} روز (اظهار کاربر)"
    val nextPeriodText = when {
        predictionInfo.lastPeriodStart == null -> "نامشخص"
        else -> PersianDates.long(cycleInfo.predictedPeriodStart)
    }

    // Clinical text builder for sharing with doctor
    val clinicalSummaryText = buildString {
        appendLine("خلاصه سوابق چرخه قاعدگی")
        appendLine("تهیه‌شده با اپلیکیشن هانیتا (ثبت شخصی کاربر، نه تشخیص پزشکی)")
        appendLine("----------------------------------")
        appendLine("نام: $userName")
        appendLine("تاریخ گزارش: $todayJalaliStr")
        if (userWeight.isNotBlank()) appendLine("وزن ثبت‌شده: ${PersianNumbers.toPersianDigits(userWeight)} کیلوگرم")
        if (avgBbt != null) appendLine("میانگین دمای پایه بدن: ${PersianNumbers.formatDecimal(avgBbt)} درجه سانتی‌گراد")
        if (avgWater != null) appendLine("میانگین مصرف آب: ${PersianNumbers.formatDecimal(avgWater)} لیوان در روز")
        predictionInfo.lastPeriodStart?.let { appendLine("شروع آخرین پریود: ${PersianDates.long(it)}") }
        appendLine("میانگین طول چرخه: $cycleLengthText")
        appendLine("میانگین طول خونریزی: $avgPeriodDuration")
        appendLine("نظم چرخه: $regularityStatus")
        if (cycleInfo.isLate) appendLine("وضعیت فعلی: ${fa(cycleInfo.daysLate)} روز تأخیر")
        appendLine("پریود بعدی (تخمین): $nextPeriodText")
        if (completedCycles.isNotEmpty()) {
            appendLine("----------------------------------")
            appendLine("چرخه‌های اخیر (شروع / طول چرخه / روزهای خونریزی):")
            completedCycles.takeLast(6).reversed().forEach {
                appendLine(" • ${PersianDates.long(it.start)} / ${fa(it.lengthDays)} روز / ${fa(it.periodDays)} روز")
            }
        }
        if (symptomFrequency.isNotEmpty()) {
            appendLine("----------------------------------")
            appendLine("🩺 شایع‌ترین علائم ثبت شده:")
            for (item in symptomFrequency.take(4)) {
                appendLine(" • ${item.first} (${fa(item.second)} روز)")
            }
        }
        if (moodFrequency.isNotEmpty()) {
            appendLine("----------------------------------")
            appendLine("🧠 وضعیت‌های خلقی مکرر:")
            for (item in moodFrequency.take(3)) {
                appendLine(" • ${item.first} (${fa(item.second)} روز)")
            }
        }
        appendLine("----------------------------------")
        appendLine("تعداد روزهای ثبت‌شده: ${fa(loggedDaysCount)} روز")
    }

    val shareReport = {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, clinicalSummaryText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "ارسال پرونده سلامت به پزشک یا همراه")
        context.startActivity(shareIntent)
    }

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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "پرونده سلامت و گزارش پزشک",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحلیل بالینی و آماده اشتراک با متخصص زنان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "نام بیمار: $userName",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "تاریخ تنظیم گزارش: $todayJalaliStr",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (userWeight.isNotBlank()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "${PersianNumbers.toPersianDigits(userWeight)} کیلوگرم",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Key Clinical Metrics Grid
                    Text("شاخص‌های بالینی چرخه", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalMetricCard(
                            title = "میانگین طول چرخه",
                            value = cycleLengthText,
                            subtitle = "محدوده نرمال: ۲۱-۳۵",
                            modifier = Modifier.weight(1f),
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                        ClinicalMetricCard(
                            title = "طول خونریزی",
                            value = avgPeriodDuration,
                            subtitle = "محدوده نرمال: ۳-۷",
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFFEF4444)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalMetricCard(
                            title = "شاخص نظم چرخه",
                            value = regularityStatus,
                            subtitle = "بر پایه دوره‌های قبلی",
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFF10B981)
                        )
                        ClinicalMetricCard(
                            title = "چرخه بعدی",
                            value = when {
                                predictionInfo.lastPeriodStart == null -> "نامشخص"
                                cycleInfo.isLate -> "${fa(cycleInfo.daysLate)} روز تأخیر"
                                cycleInfo.daysUntilNextPeriod == 0 -> "امروز"
                                else -> "${fa(cycleInfo.daysUntilNextPeriod)} روز دیگر"
                            },
                            subtitle = nextPeriodText,
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFFA855F7)
                        )
                    }

                    // Symptoms Analysis Section
                    Text("تحلیل علائم شایع ثبت شده", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (symptomFrequency.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "هنوز علائم خاصی ثبت نشده است. با ثبت روزانه در بخش لاگ، الگوی علائم اینجا تحلیل می‌شود.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in symptomFrequency.take(5)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "• ${item.first}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${PersianNumbers.toPersianDigits(item.second)} روز",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Mood Analysis Section
                    if (moodFrequency.isNotEmpty()) {
                        Text("الگوهای خلقی و هیجانی", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in moodFrequency.take(4)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "• ${item.first}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${PersianNumbers.toPersianDigits(item.second)} روز",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1)
                                    )
                                }
                            }
                        }
                    }

                    // Doctor Note
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "این گزارش به عنوان ابزار کمکی جهت پایش و خودآگاهی طراحی شده و جایگزین تشخیص قطعی پزشکی نیست. ارائه این خلاصه به پزشک متخصص، به بررسی دقیق‌تر پرونده کمک شایانی خواهد نمود.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(clinicalSummaryText))
                            Toast.makeText(context, "متن گزارش پزشکی در حافظه کپی شد.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کپی گزارش", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = shareReport,
                        modifier = Modifier.weight(1.3f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اشتراک با پزشک", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}
