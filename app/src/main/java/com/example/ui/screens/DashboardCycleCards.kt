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
import com.example.utils.PersianDates
import com.example.utils.PersianNumbers
import com.example.domain.CyclePredictor
import com.example.ui.viewmodel.AiTipState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.animation.core.*


import androidx.compose.ui.semantics.semantics
@Composable
fun HeaderRow(
    userName: String,
    userAvatar: String,
    today: java.time.LocalDate,
    onToggleThemeMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAvatarGenerator: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .bounceClick { onNavigateToAvatarGenerator() }
        ) {
            AvatarRenderer(avatarId = userAvatar, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "امروز، ${PersianDates.long(today)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "سلام $userName عزیز",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val isDarkEffective = ThemeProvider.isDark
        IconButton(onClick = onToggleThemeMode) {
            Icon(
                imageVector = if (isDarkEffective) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDarkEffective) "تغییر به حالت روشن" else "تغییر به حالت تاریک",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onNavigateToReminders) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = "یادآورها و داروها", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onLogout) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Short Persian label of the current phase (same source as the calendar and notifications). */
fun phaseTitle(info: CycleInfo): String = when {
    info.isLate -> "تأخیر پریود"
    info.isPeriod || info.phase == CyclePredictor.Phase.MENSTRUAL -> "قاعدگی"
    info.isFertile || info.phase == CyclePredictor.Phase.OVULATION -> "تخمک‌گذاری"
    info.isPms -> "پیش از قاعدگی (PMS)"
    info.phase == CyclePredictor.Phase.FOLLICULAR -> "فولیکولی"
    else -> "لوتئال"
}

@Composable
fun CycleVisualizer(info: CycleInfo, cycleLength: Int, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    var rotationOffset by remember { mutableFloatStateOf(0f) }

    val interactionScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "interactionScale"
    )

    val primary = SleekPrimary
    val secondary = SleekSecondary
    val border = SleekBorder
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val totalDays = maxOf(cycleLength, info.currentDay)
    val animatedSweepAngle by animateFloatAsState(
        targetValue = (info.currentDay / totalDays.toFloat()).coerceIn(0f, 1f) * 360f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "sweepAngle"
    )

    // Ambient motion is read only in the draw phase, so it never recomposes the screen.
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ambientRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambientRotation"
    )
    val pulseGlow = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseGlow"
    )

    val gradientColors = when {
        info.isLate || (!info.isPeriod && info.daysUntilNextPeriod == 0) -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
        info.isPeriod -> listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
        info.isPms -> listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
        else -> listOf(Color(0xFFA78BFA), Color(0xFF818CF8))
    }

    val statusText = when {
        !info.hasData -> "بدون داده"
        info.isLate -> "${PersianNumbers.toPersianDigits(info.daysLate)} روز تأخیر"
        !info.isPeriod && info.daysUntilNextPeriod == 0 -> "شروع پریود از امروز"
        info.isPeriod -> "پریود"
        info.isFertile -> "پنجره باروری"
        info.isPms -> "پیش‌قاعدگی (PMS)"
        else -> "روز ${phaseTitle(info)}"
    }
    // Calendar-method estimate only; never a contraception guarantee.
    val chanceText = when {
        !info.hasData -> "ابتدا پریود را ثبت کنید"
        info.isLate -> "در صورت ادامه، تست بارداری"
        info.isFertile -> "احتمال بارداری: زیاد"
        info.isPeriod -> "احتمال بارداری: کم"
        info.phase == CyclePredictor.Phase.FOLLICULAR -> "احتمال بارداری: کم تا متوسط"
        else -> "احتمال بارداری: کم"
    }

    Box(
        modifier = modifier
            .padding(8.dp)
            .semantics(mergeDescendants = true) {}
            // Horizontal only, so a vertical swipe still scrolls the dashboard.
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false; rotationOffset = 0f },
                    onDragCancel = { isPressed = false; rotationOffset = 0f },
                    onHorizontalDrag = { _, dragAmount -> rotationOffset += dragAmount / 3f }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .aspectRatio(1f)
                .scale(interactionScale * 1.05f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = pulseGlow.value * 0.4f),
                                secondary.copy(alpha = pulseGlow.value * 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .scale(interactionScale)
                .graphicsLayer { rotationZ = (ambientRotation.value + rotationOffset) * 0.4f }
        ) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth * 2) / 2
            drawCircle(
                color = primary.copy(alpha = 0.12f),
                radius = radius + (strokeWidth * 1.5f),
                style = Stroke(width = strokeWidth * 0.3f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 25f)))
            )
            drawCircle(color = border.copy(alpha = 0.5f), radius = radius, style = Stroke(width = strokeWidth))
            drawCircle(color = track, radius = radius - strokeWidth * 0.1f, style = Stroke(width = strokeWidth * 0.8f))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .scale(interactionScale)
                .graphicsLayer { rotationZ = rotationOffset }
        ) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth * 2) / 2
            drawArc(
                color = Color.Black.copy(alpha = 0.15f),
                startAngle = -90f + 2f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(colors = gradientColors, center = center),
                startAngle = -90f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
            )
            val angleRad = Math.toRadians((animatedSweepAngle - 90).toDouble())
            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.6f,
                center = androidx.compose.ui.geometry.Offset(
                    center.x + radius * Math.cos(angleRad).toFloat(),
                    center.y + radius * Math.sin(angleRad).toFloat()
                ),
                blendMode = androidx.compose.ui.graphics.BlendMode.Overlay
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(interactionScale)
        ) {
            Text(
                text = "روز ${PersianNumbers.toPersianDigits(info.currentDay)} چرخه",
                style = MaterialTheme.typography.labelMedium,
                color = primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = SleekTextMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = if (info.isFertile) primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = chanceText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (info.isFertile) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyInsightCard(info: CycleInfo) {
    val insightData = remember(info) {
        when {
            info.isLate -> "پریود شما ${PersianNumbers.toPersianDigits(info.daysLate)} روز تأخیر دارد. استرس، تغییر وزن، بیماری یا سفر می‌توانند باعث تأخیر شوند. اگر احتمال بارداری وجود دارد تست بدهید و اگر تأخیر بیش از ۲ هفته شد با پزشک مشورت کنید." to Icons.Filled.Favorite
            info.isPeriod -> "زمان استراحت و بازتوانی است. سعی کنید مایعات گرم بنوشید و از فعالیت‌های سنگین پرهیز کنید." to Icons.Filled.SelfImprovement
            info.isFertile -> "انرژی و تمرکز در اوج است! زمان خوبی برای شروع پروژه‌های جدید و فعالیت‌های اجتماعی است." to Icons.Filled.AutoAwesome
            info.isPms -> "ممکن است کمی خسته یا حساس باشید. به خودتان سخت نگیرید و روی آرامش ذهنی تمرکز کنید." to Icons.Filled.Spa
            info.phase == CyclePredictor.Phase.FOLLICULAR -> "انرژی شما در حال افزایش است. زمان خوبی برای ورزش‌های هوازی و برنامه‌ریزی‌های جدید است." to Icons.AutoMirrored.Filled.DirectionsRun
            else -> "سطح هورمون‌ها در فاز لوتئال است. یک رژیم غذایی متعادل و خواب کافی به حفظ انرژی شما کمک می‌کند." to Icons.Filled.Favorite
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(insightData.second, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "نکته سلامتی امروز (روز ${PersianNumbers.toPersianDigits(info.currentDay)} چرخه)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insightData.first,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
fun AITipsCard(
    info: CycleInfo,
    averageCycleLength: Int,
    tipState: AiTipState,
    onFetchTip: (String, Int, Int) -> Unit
) {
    val phaseName = phaseTitle(info)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاور هوشمند هانیتا", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (tipState) {
                is AiTipState.Success -> {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(tipState.text, fontSize = 14.sp, lineHeight = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        com.example.ai.HanitaAi.MEDICAL_DISCLAIMER,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                else -> {
                    if (tipState is AiTipState.Error) {
                        Text(tipState.message, fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(
                            text = "توصیه‌های کوتاه و شخصی برای فاز «$phaseName» چرخه خود دریافت کنید. برای این کار فاز و روز چرخه (بدون نام و یادداشت) با رضایت شما برای هوش مصنوعی ارسال می‌شود.",
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val loading = tipState is AiTipState.Loading
                    Button(
                        onClick = { onFetchTip(phaseName, info.currentDay, averageCycleLength) },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(if (tipState is AiTipState.Error) "تلاش دوباره" else "دریافت توصیه‌های امروز", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionsCard(info: CycleInfo, cycleLength: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PredictionTile(
            title = "پریود بعدی",
            value = when {
                !info.hasData -> "نامشخص"
                info.isLate -> "${PersianNumbers.toPersianDigits(info.daysLate)} روز تأخیر"
                info.isPeriod -> "در جریان"
                info.daysUntilNextPeriod == 0 -> "امروز"
                else -> "${PersianNumbers.toPersianDigits(info.daysUntilNextPeriod)} روز دیگر"
            },
            color = if (info.isLate) MaterialTheme.colorScheme.error else SleekPrimaryDark,
            modifier = Modifier.weight(1f)
        )
        val ovulationDay = CyclePredictor.ovulationDay(cycleLength)
        val daysToOvulation = if (info.currentDay <= ovulationDay) ovulationDay - info.currentDay
        else (cycleLength - info.currentDay) + ovulationDay
        PredictionTile(
            title = "تخمک‌گذاری",
            value = when {
                !info.hasData -> "نامشخص"
                info.isLate || info.daysUntilNextPeriod == 0 -> "پس از پریود"
                daysToOvulation == 0 -> "امروز (تخمینی)"
                else -> "حدود ${PersianNumbers.toPersianDigits(daysToOvulation)} روز"
            },
            color = Indigo500,
            modifier = Modifier.weight(1f)
        )
        // PMS covers the last 5 days before the expected period (see CyclePredictor).
        PredictionTile(
            title = "دوره PMS",
            value = when {
                !info.hasData -> "نامشخص"
                info.isPms -> "فعال"
                info.isPeriod || info.isLate || info.daysUntilNextPeriod == 0 -> "گذشته"
                info.daysUntilNextPeriod > 5 -> "${PersianNumbers.toPersianDigits(info.daysUntilNextPeriod - 5)} روز دیگر"
                else -> "گذشته"
            },
            color = Amber500,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PredictionTile(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp)
                .semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = SleekTextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, color = color, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FeaturesGrid(onNavigateToLog: () -> Unit, onNavigateToCalendar: () -> Unit, onNavigateToHealth: () -> Unit, onNavigateToEducation: () -> Unit, onNavigateToMood: () -> Unit, onNavigateToChat: () -> Unit, onNavigateToMeditation: () -> Unit, onNavigateToReminders: () -> Unit, onNavigateToCharts: () -> Unit) {
    data class Feature(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val tint: Color, val onClick: () -> Unit)
    val features = listOf(
        Feature(Icons.Filled.Add, "ثبت امروز", MaterialTheme.colorScheme.primary, onNavigateToLog),
        Feature(Icons.Filled.CalendarMonth, "تقویم", Indigo500, onNavigateToCalendar),
        Feature(Icons.Filled.MonitorWeight, "سلامتی", Amber500, onNavigateToHealth),
        Feature(Icons.Filled.BarChart, "نمودارها", Color(0xFFDB2777), onNavigateToCharts),
        Feature(Icons.Filled.AutoAwesome, "دستیار هوشمند", Color(0xFF3B82F6), onNavigateToChat),
        Feature(Icons.Filled.NotificationsActive, "یادآورها", Color(0xFFEF4444), onNavigateToReminders),
        Feature(Icons.Filled.Favorite, "موسیقی فازها", Color(0xFFE11D48), onNavigateToMood),
        Feature(Icons.Filled.Spa, "مراقبه", Color(0xFF10B981), onNavigateToMeditation),
        Feature(Icons.AutoMirrored.Filled.MenuBook, "آموزش", Color(0xFFA855F7), onNavigateToEducation)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        features.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { f ->
                    FeatureItem(
                        icon = f.icon,
                        label = f.label,
                        iconBgColor = f.tint.copy(alpha = 0.14f),
                        iconColor = f.tint,
                        onClick = f.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.bounceClick(onClick),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = SleekTextDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AlertBanner(info: CycleInfo, onLogPeriod: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekPrimaryLight),
        border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = SleekPrimaryDark, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        info.isLate -> "تأخیر در قاعدگی"
                        info.daysUntilNextPeriod == 0 -> "قاعدگی از امروز انتظار می‌رود"
                        else -> "نزدیک شدن قاعدگی"
                    },
                    color = SleekPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = when {
                        info.isLate -> "${PersianNumbers.toPersianDigits(info.daysLate)} روز از زمان پیش‌بینی‌شده گذشته است. با شروع پریود آن را ثبت کنید."
                        info.daysUntilNextPeriod == 0 -> "با شروع خونریزی آن را ثبت کنید تا پیش‌بینی‌ها دقیق‌تر شوند."
                        else -> "${PersianNumbers.toPersianDigits(info.daysUntilNextPeriod)} روز تا شروع پیش‌بینی‌شده باقی است."
                    },
                    color = SleekPrimaryDark,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
            if (info.isLate || info.daysUntilNextPeriod == 0) {
                TextButton(onClick = onLogPeriod) { Text("ثبت", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
