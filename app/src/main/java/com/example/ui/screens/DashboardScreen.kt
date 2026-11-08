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
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun DashboardScreen(
    viewModel: MahitaViewModel,
    onNavigateToLog: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMeditation: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAvatarGenerator: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onLogout: () -> Unit
) {
    val cycleInfo by viewModel.cycleInfo.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val predictionInfo by viewModel.predictionInfo.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val userWeight by viewModel.userWeight.collectAsState()
    val waterGlasses by viewModel.waterGlasses.collectAsState()
    val lastPeriodStart by viewModel.lastPeriodStart.collectAsState()
    val periodRuns by viewModel.periodRuns.collectAsState()
    val completedCycles by viewModel.completedCycles.collectAsState()
    val allPhysicalSymptoms by viewModel.allPhysicalSymptoms.collectAsState()
    val today by viewModel.today.collectAsState()
    val todayPhysicalSymptoms = remember(allPhysicalSymptoms, today) {
        allPhysicalSymptoms.filter { it.dateStr == today.toString() }
    }
    val aiTip by viewModel.aiTip.collectAsState()
    val context = LocalContext.current
    val isDark = ThemeProvider.isDark

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showMedicalReportDialog by rememberSaveable { mutableStateOf(false) }
    var showPartnerSyncDialog by rememberSaveable { mutableStateOf(false) }
    var showPhaseGuideDialog by rememberSaveable { mutableStateOf(false) }
    var showPregnancyDialog by rememberSaveable { mutableStateOf(false) }
    var showInteractivePeriodDialog by rememberSaveable { mutableStateOf(false) }
    var showQuickCycleEditDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }

    // The period being edited is the last one really logged (not a date typed in the settings).
    val lastRun = periodRuns.lastOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HeaderRow(
                userName = userName,
                userAvatar = userAvatar,
                today = today,
                onToggleThemeMode = { viewModel.toggleThemeMode(isDark) },
                onOpenSettings = { showSettingsDialog = true },
                onNavigateToReminders = onNavigateToReminders,
                onNavigateToAvatarGenerator = onNavigateToAvatarGenerator,
                onLogout = { showLogoutConfirm = true }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!cycleInfo.hasData) {
                NoPeriodDataCard(onLogPeriod = { showQuickCycleEditDialog = true })
                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedVisibility(visible = cycleInfo.hasData && !cycleInfo.isPeriod && (cycleInfo.isLate || cycleInfo.daysUntilNextPeriod in 0..3)) {
                Column {
                    AlertBanner(cycleInfo, onLogPeriod = { showQuickCycleEditDialog = true })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CycleVisualizer(
                    info = cycleInfo,
                    cycleLength = predictionInfo.calculatedAverage,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PredictionsCard(cycleInfo, predictionInfo.calculatedAverage)

            Spacer(modifier = Modifier.height(16.dp))

            FeaturesGrid(onNavigateToLog, onNavigateToCalendar, onNavigateToHealth, onNavigateToEducation, onNavigateToMood, onNavigateToChat, onNavigateToMeditation, onNavigateToReminders, onNavigateToCharts)

            Spacer(modifier = Modifier.height(16.dp))

            // Quick period editor: always edits the last period that was really logged.
            QuickPeriodEditCard(
                lastPeriodStart = lastPeriodStart,
                calculatedAverage = predictionInfo.calculatedAverage,
                hasRealAverage = predictionInfo.hasEnoughData,
                onClick = { showQuickCycleEditDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Period Range Logger Card (Touch Start & End Dates)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = "باز کردن تقویم لمسی") { showInteractivePeriodDialog = true }
                    .testTag("open_interactive_period_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🩸", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "ثبت لمسی روزهای شروع و پایان قاعدگی",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "شروع و پایان را روی تقویم لمس کنید",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Daily Insight based on Cycle Phase
            DailyInsightCard(cycleInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // Smart AI Tips Widget (sends the phase only after consent)
            AITipsCard(
                info = cycleInfo,
                averageCycleLength = predictionInfo.calculatedAverage,
                tipState = aiTip,
                onFetchTip = { phase, day, avg -> viewModel.fetchAiDailyTip(phase, day, avg) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Hydration and Daily Mood Widget
            QuickHydrationAndMoodCard(
                waterGlasses = waterGlasses,
                onIncrementWater = { viewModel.incrementWater() },
                onDecrementWater = { viewModel.decrementWater() },
                onLogMood = { mood -> viewModel.logQuickMood(mood) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DailyPhysicalSymptomsCard(
                todaySymptoms = todayPhysicalSymptoms,
                onNavigateToLog = onNavigateToLog
            )

            Spacer(modifier = Modifier.height(16.dp))

            PredictionNotificationCard(predictionInfo)

            Spacer(modifier = Modifier.height(16.dp))

            CycleHistoryChart(cycles = completedCycles, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // Hormonal fluctuations across cycle phases (Vico Chart)
            HormoneLevelsChartCard(
                cycleInfo = cycleInfo,
                averageCycleLength = predictionInfo.calculatedAverage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SmartInsightsCard(
                cycleInfo = cycleInfo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            com.example.ui.components.QuickSymptomWidget(
                onSave = { mood, energy, sleep, pain ->
                    viewModel.saveQuickLog(java.time.LocalDate.now().toString(), mood, energy, sleep, pain)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Health & Partner Empathy Suite
            AdvancedHealthSuiteCard(
                onOpenMedicalReport = { showMedicalReportDialog = true },
                onOpenPartnerSync = { showPartnerSyncDialog = true },
                onOpenPhaseGuide = { showPhaseGuideDialog = true },
                onOpenPregnancyTracker = { showPregnancyDialog = true }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("خروج از حساب؟") },
            text = {
                Text(
                    "یادآورهای چرخه و داروها تا ورود دوباره متوقف می‌شوند. سوابق ثبت‌شده روی همین گوشی باقی می‌ماند.",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) { Text("خروج", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("انصراف") } }
        )
    }

    SettingsDialog(
        show = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel,
        onNavigateToHistory = {
            showSettingsDialog = false
            onNavigateToHistory()
        }
    )

    MedicalReportDialog(
        show = showMedicalReportDialog,
        onDismiss = { showMedicalReportDialog = false },
        userName = userName,
        userWeight = userWeight,
        cycleInfo = cycleInfo,
        predictionInfo = predictionInfo,
        completedCycles = completedCycles,
        logs = logs
    )

    PartnerSyncDialog(
        show = showPartnerSyncDialog,
        onDismiss = { showPartnerSyncDialog = false },
        userName = userName,
        cycleInfo = cycleInfo
    )

    CyclePhaseGuideDialog(
        show = showPhaseGuideDialog,
        onDismiss = { showPhaseGuideDialog = false }
    )

    PregnancyTrackerDialog(
        show = showPregnancyDialog,
        onDismiss = { showPregnancyDialog = false },
        lastPeriodStart = lastPeriodStart
    )

    if (showInteractivePeriodDialog) {
        AlertDialog(
            onDismissRequest = { showInteractivePeriodDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showInteractivePeriodDialog = false }) {
                    Text("بستن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    InteractivePeriodCalendarWidget(
                        recordedLogs = logs,
                        onSavePeriod = { start, end, flow, notes ->
                            val saved = viewModel.savePeriodRange(start, end, flow, notes)
                            showInteractivePeriodDialog = false
                            Toast.makeText(context, periodSavedMessage(saved), Toast.LENGTH_LONG).show()
                        },
                        onClearPeriod = { start, end ->
                            viewModel.clearPeriodRange(start, end)
                            Toast.makeText(context, "روزهای خونریزی این بازه پاک شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showQuickCycleEditDialog) {
        val editStart = lastRun?.start ?: today.minusDays(1)
        ProfessionalCycleEditDialog(
            showCycleLength = !predictionInfo.hasEnoughData,
            initialStartDate = editStart,
            initialEndDate = lastRun?.end ?: minOf(editStart.plusDays(4), today),
            initialCycleLength = predictionInfo.calculatedAverage,
            isExistingCycle = lastRun != null,
            title = if (lastRun != null) "ویرایش آخرین دوره قاعدگی 🩸" else "ثبت دوره قاعدگی 🩸",
            onDismissRequest = { showQuickCycleEditDialog = false },
            onSaveCycle = { newStart, newEnd, length, flow, notes ->
                val saved = viewModel.editPeriod(
                    oldStart = lastRun?.start,
                    newStart = newStart,
                    newEnd = newEnd,
                    flow = flow,
                    notes = notes,
                    cycleLength = length.takeIf { !predictionInfo.hasEnoughData }
                )
                showQuickCycleEditDialog = false
                Toast.makeText(context, periodSavedMessage(saved), Toast.LENGTH_LONG).show()
            }
        )
    }
}

/** Confirmation text for a saved period (the end date may have been capped at today). */
fun periodSavedMessage(saved: Pair<java.time.LocalDate, java.time.LocalDate>?): String =
    if (saved == null) "روزهای آینده را نمی‌توان به‌عنوان خونریزی ثبت کرد."
    else "دوره قاعدگی از ${PersianDates.short(saved.first)} تا ${PersianDates.short(saved.second)} ثبت شد ✨"

@Composable
private fun NoPeriodDataCard(onLogPeriod: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "هنوز پریودی ثبت نشده است",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "برای پیش‌بینی دقیق، تاریخ شروع آخرین پریود خود را ثبت کنید. تا آن زمان اعداد زیر فقط تخمین پیش‌فرض هستند.",
                fontSize = 13.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onLogPeriod) { Text("ثبت آخرین پریود") }
        }
    }
}

@Composable
private fun QuickPeriodEditCard(
    lastPeriodStart: java.time.LocalDate?,
    calculatedAverage: Int,
    hasRealAverage: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "ویرایش آخرین دوره", onClick = onClick)
            .testTag("quick_edit_cycle_button"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp)
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ویرایش آخرین دوره قاعدگی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val lastText = lastPeriodStart?.let { PersianDates.long(it) } ?: "ثبت نشده"
                val lengthText = PersianNumbers.toPersianDigits(calculatedAverage) + " روز" + if (hasRealAverage) "" else " (پیش‌فرض)"
                Text(
                    text = "آخرین پریود: $lastText • طول چرخه: $lengthText",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(14.dp))
                    Text("ویرایش", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    }
}
