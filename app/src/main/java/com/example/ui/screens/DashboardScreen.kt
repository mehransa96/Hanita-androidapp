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
    val averageCycleLength by viewModel.averageCycleLength.collectAsState()
    val predictionInfo by viewModel.predictionInfo.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val userWeight by viewModel.userWeight.collectAsState()
    val waterGlasses by viewModel.waterGlasses.collectAsState()
    val lastPeriodDate by viewModel.lastPeriodDate.collectAsState()
    val allPhysicalSymptoms by viewModel.allPhysicalSymptoms.collectAsState()
    val todayDateStr = remember { java.time.LocalDate.now().toString() }
    val todayPhysicalSymptoms = remember(allPhysicalSymptoms, todayDateStr) {
        allPhysicalSymptoms.filter { it.dateStr == todayDateStr }
    }
    val aiDailyTip by viewModel.aiDailyTip.collectAsState()
    val isFetchingAiTip by viewModel.isFetchingAiTip.collectAsState()
    val context = LocalContext.current

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMedicalReportDialog by remember { mutableStateOf(false) }
    var showPartnerSyncDialog by remember { mutableStateOf(false) }
    var showPhaseGuideDialog by remember { mutableStateOf(false) }
    var showPregnancyDialog by remember { mutableStateOf(false) }
    var showInteractivePeriodDialog by remember { mutableStateOf(false) }
    var showQuickCycleEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HeaderRow(
                userName = userName,
                userAvatar = userAvatar,
                onToggleThemeMode = { viewModel.toggleThemeMode() },
                onOpenSettings = { showSettingsDialog = true },
                onNavigateToReminders = onNavigateToReminders,
                onNavigateToAvatarGenerator = onNavigateToAvatarGenerator,
                onLogout = onLogout
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(visible = !cycleInfo.isPeriod && cycleInfo.daysUntilNextPeriod in 1..3) {
                Column {
                    AlertBanner(cycleInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(240.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CycleVisualizer(
                    info = cycleInfo, 
                    cycleLength = predictionInfo.calculatedAverage,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PredictionsCard(cycleInfo)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Period & Cycle Editor Card (User directive: editable from all sections with best UX)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showQuickCycleEditDialog = true }
                    .testTag("quick_edit_cycle_button"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
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
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ویرایش سریع و حرفه‌ای تاریخ پریود 🩸✏️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val lastJalaliFormatted = remember(lastPeriodDate) {
                                runCatching {
                                    JalaliCalendar.gregorianToJalali(java.time.LocalDate.parse(lastPeriodDate)).formatLong()
                                }.getOrDefault(if (lastPeriodDate.isNotBlank()) lastPeriodDate else "ثبت نشده")
                            }
                            Text(
                                text = "آخرین پریود: $lastJalaliFormatted • طول چرخه: $averageCycleLength روز",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text("ویرایش", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Period Range Logger Card (Touch Start & End Dates)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInteractivePeriodDialog = true }
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
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "انتخاب تعاملی تاریخ‌ها با لمس تقویم هوشمند",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "باز کردن تقویم لمسی",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Daily Insight based on Cycle Phase
            DailyInsightCard(cycleInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // Smart AI Tips Widget
            AITipsCard(
                info = cycleInfo,
                averageCycleLength = averageCycleLength.toFloat(),
                aiDailyTip = aiDailyTip,
                isFetchingAiTip = isFetchingAiTip,
                onFetchTip = { phase, day, avg ->
                    viewModel.fetchAiDailyTip(phase, day, avg)
                }
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

            // Daily Physical Symptoms Card (Room Database)
            DailyPhysicalSymptomsCard(
                todaySymptoms = todayPhysicalSymptoms,
                onNavigateToLog = onNavigateToLog
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            PredictionNotificationCard(predictionInfo)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CycleHistoryChart(averageCycleLength = predictionInfo.calculatedAverage, modifier = Modifier.fillMaxWidth())
            
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
                    val todayStr = java.time.LocalDate.now().toString()
                    viewModel.saveQuickLog(todayStr, mood, energy, sleep, pain)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FeaturesGrid(onNavigateToLog, onNavigateToCalendar, onNavigateToHealth, onNavigateToEducation, onNavigateToMood, onNavigateToChat, onNavigateToMeditation, onNavigateToReminders, onNavigateToCharts)
            
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
        averageCycleLength = averageCycleLength,
        cycleInfo = cycleInfo,
        predictionInfo = predictionInfo,
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
        lastPeriodDateStr = lastPeriodDate
    )

    if (showInteractivePeriodDialog) {
        val context = LocalContext.current
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
                            viewModel.savePeriodRange(start, end, flow, notes)
                            showInteractivePeriodDialog = false
                            val startJ = JalaliCalendar.gregorianToJalali(start).formatShort()
                            val endJ = JalaliCalendar.gregorianToJalali(end).formatShort()
                            Toast.makeText(
                                context,
                                "دوره قاعدگی از $startJ تا $endJ با موفقیت ثبت شد ✨",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onClearPeriod = { start, end ->
                            viewModel.clearPeriodRange(start, end)
                            Toast.makeText(context, "بازه انتخابی پاک شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showQuickCycleEditDialog) {
        val parsedLastPeriod = runCatching { java.time.LocalDate.parse(lastPeriodDate) }.getOrNull()
            ?: java.time.LocalDate.now().minusDays(5)
        ProfessionalCycleEditDialog(
            initialStartDate = parsedLastPeriod,
            initialEndDate = parsedLastPeriod.plusDays(4),
            initialCycleLength = averageCycleLength,
            title = "ویرایش حرفه‌ای دوره قاعدگی و تنظیم چرخه 🩸",
            onDismissRequest = { showQuickCycleEditDialog = false },
            onSaveCycle = { newStart, newEnd, length, flow, notes ->
                viewModel.updateFullCycleRecord(
                    oldRecordId = null,
                    oldStartDate = parsedLastPeriod,
                    oldEndDate = null,
                    newStartDate = newStart,
                    newEndDate = newEnd,
                    cycleLength = length,
                    flow = flow,
                    notes = notes
                )
                showQuickCycleEditDialog = false
                val startJalali = JalaliCalendar.gregorianToJalali(newStart).formatShort()
                Toast.makeText(context, "اطلاعات چرخه با شروع $startJalali با موفقیت ثبت گردید ✨", Toast.LENGTH_LONG).show()
            }
        )
    }
}
