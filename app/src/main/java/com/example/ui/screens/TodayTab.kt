package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.ui.theme.rememberHanitaHaptics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CycleInfo
import com.example.ui.viewmodel.PredictionInfo
import com.example.utils.JalaliCalendar

/**
 * First tab: what is used every day, all on one screen without scrolling — cycle ring,
 * predictions, logging the period / symptoms, quick date edit, calendar, water and mood.
 */
@Composable
fun TodayTab(
    userName: String,
    userAvatar: String,
    cycleInfo: CycleInfo,
    predictionInfo: PredictionInfo,
    waterGlasses: Int,
    todaySymptomCount: Int,
    lastPeriodDate: String,
    averageCycleLength: Int,
    onToggleThemeMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAvatarGenerator: () -> Unit,
    onLock: () -> Unit,
    onLogPeriod: () -> Unit,
    onEditCycle: () -> Unit,
    onLogSymptoms: () -> Unit,
    onOpenCalendar: () -> Unit,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    onLogMood: (String) -> Unit,
    today: java.time.LocalDate = java.time.LocalDate.now()
) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    // On a short phone (or with a large system font) everything cannot fit one screen: the tab then
    // scrolls with a fixed-size ring instead of squeezing the ring to nothing or clipping buttons.
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val compact = maxHeight < 600.dp * fontScale
    Column(
        Modifier
            .fillMaxSize()
            .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        HeaderRow(
            userName = userName,
            userAvatar = userAvatar,
            today = today,
            onToggleThemeMode = onToggleThemeMode,
            onOpenSettings = onOpenSettings,
            onNavigateToReminders = onNavigateToReminders,
            onNavigateToAvatarGenerator = onNavigateToAvatarGenerator,
            onLock = onLock
        )
        AnimatedVisibility(visible = !cycleInfo.isPeriod && (cycleInfo.isLate || cycleInfo.daysUntilNextPeriod in 0..3)) {
            Column {
                Spacer(Modifier.height(8.dp))
                AlertBanner(cycleInfo, onLogPeriod = onLogPeriod)
            }
        }

        // The ring takes whatever height is left, so the tab always fits the screen.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (compact) Modifier.height(220.dp) else Modifier.weight(1f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val ring = min(min(maxWidth, maxHeight), 250.dp)
            CycleVisualizer(
                info = cycleInfo,
                cycleLength = predictionInfo.calculatedAverage,
                modifier = Modifier.size(ring)
            )
        }

        PredictionsCard(cycleInfo, predictionInfo.calculatedAverage)
        Spacer(Modifier.height(10.dp))

        val lastJalali = runCatching {
            JalaliCalendar.gregorianToJalali(java.time.LocalDate.parse(lastPeriodDate)).formatShort()
        }.getOrDefault("ثبت نشده")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAction("ثبت پریود", "شروع و پایان", Icons.Filled.WaterDrop, MaterialTheme.colorScheme.primary,
                "open_interactive_period_card", onLogPeriod, Modifier.weight(1f))
            QuickAction("علائم امروز", if (todaySymptomCount == 0) "ثبت علائم" else "${com.example.utils.PersianNumbers.toPersianDigits(todaySymptomCount)} مورد",
                Icons.Filled.Healing, Color(0xFFE11D48), "today_log_symptoms", onLogSymptoms, Modifier.weight(1f))
            QuickAction("ویرایش تاریخ", lastJalali, Icons.Filled.EditCalendar, MaterialTheme.colorScheme.secondary,
                "quick_edit_cycle_button", onEditCycle, Modifier.weight(1f))
            QuickAction("تقویم", "${com.example.utils.PersianNumbers.toPersianDigits(predictionInfo.calculatedAverage)} روزه", Icons.Filled.CalendarMonth, Color(0xFF6366F1),
                "today_open_calendar", onOpenCalendar, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        WaterAndMoodRow(waterGlasses, onIncrementWater, onDecrementWater, onLogMood)
        Spacer(Modifier.height(12.dp))
    }
  }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHanitaHaptics()
    Card(
        onClick = {
            haptics.tick()
            onClick()
        },
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(34.dp).background(tint.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WaterAndMoodRow(
    waterGlasses: Int,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    onLogMood: (String) -> Unit,
    today: java.time.LocalDate = java.time.LocalDate.now()
) {
    val context = LocalContext.current
    val haptics = rememberHanitaHaptics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            // 40dp visual, 48dp touch area (IconButton enforces the minimum interactive size).
            IconButton(
                onClick = {
                    haptics.tick()
                    onDecrementWater()
                },
                enabled = waterGlasses > 0,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "کم کردن یک لیوان آب", tint = Color(0xFF0EA5E9))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                }
            ) {
                AnimatedContent(
                    targetState = waterGlasses,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically { if (up) it / 2 else -it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { if (up) -it / 2 else it / 2 } + fadeOut())
                    },
                    label = "waterCount"
                ) { glasses ->
                    Text(
                        "💧 ${com.example.utils.PersianNumbers.toPersianDigits(glasses)} از ۸",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (com.example.ui.theme.ThemeProvider.isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                    )
                }
                Text("${com.example.utils.PersianNumbers.toPersianDigits(waterGlasses * 250)} میلی‌لیتر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // The counter animates and is announced, so no toast piles up on repeated taps.
            IconButton(
                onClick = {
                    haptics.tick()
                    onIncrementWater()
                },
                modifier = Modifier.size(40.dp).testTag("today_add_water")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن یک لیوان آب", tint = Color(0xFF0EA5E9))
            }
            Box(Modifier.padding(horizontal = 6.dp).width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
            // Same four moods as the full daily card.
            val moods = listOf("پرانرژی ✨" to "✨", "آرام و متمرکز 🧘" to "🧘", "خواب‌آلود و خسته 😴" to "😴", "حساس و احساساتی 🌧️" to "🌧️")
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                moods.forEach { (mood, emoji) ->
                    val label = mood.substringBeforeLast(' ')
                    Box(
                        Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                            .clickable(role = Role.Button, onClickLabel = "ثبت حال «$label» برای امروز") {
                                haptics.confirm()
                                onLogMood(mood)
                                Toast.makeText(context, "حالت «$mood» برای امروز ثبت شد 🌸", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 18.sp, modifier = Modifier.semantics { contentDescription = label })
                    }
                }
            }
        }
    }
}

/** One-line summary at the top of the cycle tab. */
@Composable
fun CycleSummaryStrip(cycleInfo: CycleInfo, predictionInfo: PredictionInfo) {
    // Same date as the cycle ring and the calendar: a late or overdue period is expected now,
    // never shown as an already-past "next period" date.
    val nextJalali = when {
        !cycleInfo.hasData -> "-"
        cycleInfo.isLate -> "${com.example.utils.PersianNumbers.toPersianDigits(cycleInfo.daysLate)} روز تأخیر"
        cycleInfo.isPeriod -> "در جریان"
        else -> runCatching { JalaliCalendar.gregorianToJalali(cycleInfo.predictedPeriodStart).formatShort() }.getOrDefault("-")
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryChip("روز چرخه", com.example.utils.PersianNumbers.toPersianDigits(cycleInfo.currentDay), Modifier.weight(1f))
        SummaryChip("میانگین", "${com.example.utils.PersianNumbers.toPersianDigits(predictionInfo.calculatedAverage)} روز", Modifier.weight(1f))
        SummaryChip("پریود بعدی", nextJalali, Modifier.weight(1f))
        SummaryChip("نظم", if (predictionInfo.isIrregular) "نامنظم" else if (predictionInfo.totalCyclesCalculated < 3) "داده کم" else "منظم", Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
