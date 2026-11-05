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
import androidx.compose.material.icons.filled.Add
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
import com.example.ui.viewmodel.CycleInfo
import com.example.ui.viewmodel.MahitaViewModel
import com.example.ui.viewmodel.PredictionInfo

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.animation.core.*

@Composable
fun CycleHistoryChart(averageCycleLength: Int = 28, modifier: Modifier = Modifier) {
    val mockCycleLengths = remember(averageCycleLength) {
        listOf(
            averageCycleLength - 1,
            averageCycleLength + 1,
            averageCycleLength - 2,
            averageCycleLength + 2,
            averageCycleLength,
            averageCycleLength - 1
        ).map { it.coerceIn(21, 40) }
    }
    val months = listOf("دی", "بهمن", "اسفند", "فروردین", "اردیبهشت", "خرداد")
    val average = mockCycleLengths.average().toInt()
    val maxVal = (mockCycleLengths.maxOrNull() ?: 30).coerceAtLeast(32)
    val minVal = (mockCycleLengths.minOrNull() ?: 24).coerceAtMost(24) - 2
    
    val pColor = SleekPrimary
    val pLightColor = SleekPrimaryLight
    val bColor = SleekBorder

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, bColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "نوسانات ۶ ماه گذشته (طرح Recharts)",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "نمودار خطی هماهنگی و منظم بودن دوره‌ها",
                        fontSize = 10.sp,
                        color = SleekTextMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pLightColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "میانگین: $average روز",
                        fontSize = 10.sp,
                        color = SleekPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Recharts style Area/Line Chart using Compose Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = mockCycleLengths.size
                    
                    // Draw horizontal dotted gridlines
                    val gridLinesCount = 3
                    val gridSpacing = height / (gridLinesCount + 1)
                    for (i in 1..gridLinesCount) {
                        val y = i * gridSpacing
                        drawLine(
                            color = bColor.copy(alpha = 0.4f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Calculate point offsets
                    val stepX = if (pointsCount > 1) width / (pointsCount - 1) else width
                    val rangeDiff = (maxVal - minVal).toFloat().coerceAtLeast(1f)
                    val points = mockCycleLengths.mapIndexed { idx, length ->
                        val ratio = ((length - minVal).toFloat() / rangeDiff).coerceIn(0f, 1f)
                        val paddingY = 20f
                        val usableHeight = (height - paddingY * 2).coerceAtLeast(10f)
                        val y = height - paddingY - (ratio * usableHeight)
                        val x = idx * stepX
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    // Create smooth cubic path
                    val linePath = androidx.compose.ui.graphics.Path()
                    if (points.isNotEmpty()) {
                        linePath.moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val controlPoint1 = androidx.compose.ui.geometry.Offset((prev.x + curr.x) / 2, prev.y)
                            val controlPoint2 = androidx.compose.ui.geometry.Offset((prev.x + curr.x) / 2, curr.y)
                            linePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                curr.x, curr.y
                            )
                        }
                    }

                    // Fill gradient under the curve (Area Chart)
                    val areaPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(linePath)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                pColor.copy(alpha = 0.25f),
                                pColor.copy(alpha = 0.00f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw the primary line cap
                    drawPath(
                        path = linePath,
                        color = pColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Companion.Round
                        )
                    )

                    // Draw circular anchors/dots
                    points.forEach { point ->
                        drawCircle(
                            color = pColor.copy(alpha = 0.15f),
                            radius = 7.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = pColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.5.dp.toPx(),
                            center = point
                        )
                    }
                }

                // Values display
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    mockCycleLengths.forEachIndexed { _, length ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val ratio = (length - minVal).toFloat() / (maxVal - minVal).toFloat()
                            val topPaddingDp = (100 - (ratio * 70).coerceIn(0f, 100f)).dp
                            
                            Box(
                                modifier = Modifier
                                    .padding(top = topPaddingDp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .border(0.5.dp, SleekBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$length روز",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Month labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        fontSize = 9.sp,
                        color = SleekTextMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionNotificationCard(predictionInfo: PredictionInfo) {
    val predictedDate = predictionInfo.predictedNextStart
    val jalaliPrediction = predictedDate?.let { com.example.utils.JalaliCalendar.gregorianToJalali(it) }
    val jalaliPredictionStr = jalaliPrediction?.formatLong() ?: "نامشخص"
    
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
                            color = if (predictionInfo.hasEnoughData) SleekPrimary else Color.Gray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (predictionInfo.hasEnoughData) "طول واقعی" else "طول فرضی",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = if (predictionInfo.hasEnoughData) {
                    "بر اساس ${predictionInfo.totalCyclesCalculated} دوره ثبت شده قبلی شما، طول متوسط چرخه قاعدگی شما ${predictionInfo.calculatedAverage} روز برآورد شد."
                } else {
                    "به دلیل محدودیت داده‌های چرخه‌ شما، زمان‌بندی فعلی بر اساس مقدار تنظیم‌شده در تنظیمات (${predictionInfo.calculatedAverage} روز) است."
                },
                fontSize = 11.5.sp,
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
                        fontSize = 10.sp,
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
                            daysRemaining < 0 -> "${-daysRemaining} روز تاخیر"
                            daysRemaining == 0 -> "شروع از امروز"
                            else -> "$daysRemaining روز دیگر"
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
