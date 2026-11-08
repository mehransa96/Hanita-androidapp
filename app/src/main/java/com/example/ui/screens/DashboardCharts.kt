package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.ui.components.ProfessionalDatePickerDialog
import com.example.ui.components.ProfessionalCycleEditDialog
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import com.example.ui.theme.ThemeProvider
import com.example.ui.theme.ThemeMode
import com.example.ui.components.MedicalReportDialog
import com.example.ui.components.PartnerSyncDialog
import com.example.ui.components.CyclePhaseGuideDialog
import com.example.ui.components.PregnancyTrackerDialog
import com.example.ui.components.InteractivePeriodCalendarWidget
import com.example.ui.components.HormoneLevelsChartCard
import com.example.ui.components.SmartInsightsCard
import com.example.utils.JalaliCalendar
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.viewmodel.CycleInfo
import com.example.ui.viewmodel.MahitaViewModel
import com.example.ui.viewmodel.PredictionInfo

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.animation.core.*

@Composable
fun CycleHistoryChart(cycles: List<com.example.domain.CyclePredictor.CompletedCycle>, modifier: Modifier = Modifier) {
    // Real data only: the last six finished cycles the user logged.
    val shown = remember(cycles) { cycles.takeLast(6) }
    val pColor = SleekPrimary
    val bColor = SleekBorder
    val labelBg = MaterialTheme.colorScheme.surface
    val labelColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, bColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "طول چرخه‌های اخیر شما",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فاصله شروع هر پریود تا پریود بعدی",
                        fontSize = 12.sp,
                        color = SleekTextLight
                    )
                }
                if (shown.isNotEmpty()) {
                    val average = Math.round(shown.map { it.lengthDays }.average()).toInt()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimaryLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "میانگین: ${PersianNumbers.toPersianDigits(average)} روز",
                            fontSize = 12.sp,
                            color = SleekPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shown.size < 2) {
                Text(
                    text = if (shown.isEmpty())
                        "پس از ثبت دست‌کم دو پریود، طول واقعی چرخه‌های شما اینجا نمایش داده می‌شود."
                    else
                        "چرخه ثبت‌شده: ${PersianNumbers.toPersianDigits(shown.first().lengthDays)} روز. با ثبت پریودهای بعدی، روند تغییرات نمایش داده می‌شود.",
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = SleekTextLight
                )
                return@Column
            }

            val values = shown.map { it.lengthDays }
            val maxVal = (values.max() + 2).coerceAtLeast(30)
            val minVal = (values.min() - 2).coerceAtMost(24)
            val description = shown.joinToString("، ") { "${PersianDates.short(it.start)}: ${PersianNumbers.toPersianDigits(it.lengthDays)} روز" }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .semantics { contentDescription = "طول چرخه‌ها: $description" }
            ) {
                val chartHeight = maxHeight
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
                    val width = size.width
                    val height = size.height
                    for (i in 0..2) {
                        val y = height * i / 2f
                        drawLine(
                            color = bColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                    val stepX = width / (values.size - 1)
                    val range = (maxVal - minVal).toFloat()
                    // Chronological from right to left, matching the Persian reading direction.
                    val points = values.mapIndexed { idx, length ->
                        val ratio = ((length - minVal) / range).coerceIn(0f, 1f)
                        androidx.compose.ui.geometry.Offset(width - idx * stepX, height - ratio * height)
                    }
                    val linePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }
                    val areaPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    drawPath(areaPath, brush = Brush.verticalGradient(listOf(pColor.copy(alpha = 0.25f), Color.Transparent)))
                    drawPath(linePath, color = pColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    points.forEach { point ->
                        drawCircle(color = pColor, radius = 4.dp.toPx(), center = point)
                        drawCircle(color = labelBg, radius = 1.5.dp.toPx(), center = point)
                    }
                }
                // Value labels above each point.
                val innerHeight = chartHeight - 56.dp
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)) {
                    values.forEach { length ->
                        val ratio = ((length - minVal).toFloat() / (maxVal - minVal)).coerceIn(0f, 1f)
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                            Text(
                                text = PersianNumbers.toPersianDigits(length),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelColor,
                                modifier = Modifier
                                    .padding(top = (innerHeight * (1f - ratio)) + 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(labelBg)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                shown.forEach { cycle ->
                    Text(
                        text = JalaliCalendar.gregorianToJalali(cycle.start).monthName,
                        fontSize = 11.sp,
                        color = SleekTextLight,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionNotificationCard(predictionInfo: PredictionInfo) {
    val predictedDate = predictionInfo.predictedNextStart
    val jalaliPredictionStr = when {
        predictionInfo.lastPeriodStart == null -> "پس از ثبت اولین پریود"
        predictionInfo.daysRemaining < -com.example.domain.CyclePredictor.MAX_LATE_DAYS -> "مدتی است پریودی ثبت نشده است"
        else -> predictedDate?.let { PersianDates.long(it) } ?: "نامشخص"
    }
    
    val daysRemaining = predictionInfo.daysRemaining
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "پیش‌بینی هوشمند",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پیش‌بینی هوشمند سیکل بعدی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = if (predictionInfo.hasEnoughData) SleekPrimary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (predictionInfo.hasEnoughData) "بر اساس داده شما" else "تخمین پیش‌فرض",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (predictionInfo.hasEnoughData) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = if (predictionInfo.hasEnoughData) {
                    "بر اساس ${PersianNumbers.toPersianDigits(predictionInfo.totalCyclesCalculated)} چرخه ثبت‌شده، طول متوسط چرخه شما ${PersianNumbers.toPersianDigits(predictionInfo.calculatedAverage)} روز است" +
                        if (predictionInfo.isIrregular) " و چرخه‌ها نامنظم هستند (اختلاف ${PersianNumbers.toPersianDigits(predictionInfo.cycleVariationDays)} روز)؛ پیش‌بینی دقت کمتری دارد." else "."
                } else {
                    "هنوز چرخه کاملی ثبت نشده است؛ پیش‌بینی بر اساس طول چرخه‌ای است که وارد کرده‌اید (${PersianNumbers.toPersianDigits(predictionInfo.calculatedAverage)} روز)."
                },
                fontSize = 13.sp,
                color = SleekTextMedium,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(0.5.dp, SleekBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تاریخ تخمینی شروع بعدی:",
                        fontSize = 12.sp,
                        color = SleekTextLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = jalaliPredictionStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextDark
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                daysRemaining < 0 -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                daysRemaining in 0..3 -> SleekPrimary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            predictionInfo.lastPeriodStart == null -> "—"
                            // After a long gap the data is most likely just missing (same rule as the dashboard).
                            daysRemaining < -com.example.domain.CyclePredictor.MAX_LATE_DAYS -> "ثبت پریود اخیر لازم است"
                            daysRemaining < 0 -> "${PersianNumbers.toPersianDigits(-daysRemaining)} روز تأخیر"
                            daysRemaining == 0 -> "شروع از امروز"
                            else -> "${PersianNumbers.toPersianDigits(daysRemaining)} روز دیگر"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysRemaining < 0 -> Color(0xFFEF4444)
                            daysRemaining in 0..3 -> SleekPrimaryDark
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}
