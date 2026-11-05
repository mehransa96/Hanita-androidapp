package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HormoneLevelsChartCard
import com.example.ui.viewmodel.MahitaViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomChartsScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val cycleInfo by viewModel.cycleInfo.collectAsState()
    val averageCycleLength by viewModel.averageCycleLength.collectAsState()

    // Filter logs for the last 90 days
    val threeMonthsAgo = LocalDate.now().minusDays(90)
    val recentLogs = remember(allLogs) {
        allLogs.filter { log ->
            try {
                val date = LocalDate.parse(log.dateStr)
                date.isAfter(threeMonthsAgo) || date.isEqual(threeMonthsAgo)
            } catch (e: Exception) {
                false
            }
        }
    }

    val customPhysical by viewModel.customPhysicalSymptoms.collectAsState()
    val customMood by viewModel.customMoodSymptoms.collectAsState()

    // Tally physical symptoms
    val physicalSymptoms = (listOf("درد شکم", "سردرد", "نفخ", "حساسیت سینه", "جوش") + customPhysical).distinct()
    val physicalTally = remember(recentLogs, physicalSymptoms) {
        val counts = mutableMapOf<String, Float>()
        physicalSymptoms.forEach { counts[it] = 0f }
        recentLogs.forEach { log ->
            if (log.symptoms.isNotBlank()) {
                val symptomsList = log.symptoms.split(",")
                symptomsList.forEach { s ->
                    val trimmed = s.trim()
                    if (counts.containsKey(trimmed)) {
                        counts[trimmed] = counts[trimmed]!! + 1f
                    }
                }
            }
        }
        counts
    }

    // Tally moods
    val moodSymptoms = (listOf("اضطراب", "افسردگی", "استرس", "انرژی پایین", "عدم تمرکز") + customMood).distinct()
    val moodTally = remember(recentLogs, moodSymptoms) {
        val counts = mutableMapOf<String, Float>()
        moodSymptoms.forEach { counts[it] = 0f }
        recentLogs.forEach { log ->
            if (log.moods.isNotBlank()) {
                val moodList = log.moods.split(",")
                moodList.forEach { m ->
                    val trimmed = m.trim()
                    if (counts.containsKey(trimmed)) {
                        counts[trimmed] = counts[trimmed]!! + 1f
                    }
                }
            }
        }
        counts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نمودارها و تحلیل هورمونی", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Visual Hormone fluctuations chart using Vico
            HormoneLevelsChartCard(
                cycleInfo = cycleInfo,
                averageCycleLength = averageCycleLength
            )

            ChartCard(
                title = "فراوانی علائم جسمی (۳ ماه اخیر)",
                data = physicalTally,
                labels = physicalSymptoms
            )
            
            ChartCard(
                title = "فراوانی وضعیت روحی (۳ ماه اخیر)",
                data = moodTally,
                labels = moodSymptoms
            )
            
        }
    }
}

@Composable
fun ChartCard(title: String, data: Map<String, Float>, labels: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val values = labels.map { data[it] ?: 0f }
            val allZero = values.all { it == 0f }
            
            if (allZero) {
                Text(
                    text = "داده‌ای در ۳ ماه اخیر ثبت نشده است.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                val chartEntryModel = entryModelOf(*values.toTypedArray())
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    labels.getOrNull(value.toInt()) ?: ""
                }
                
                Chart(
                    chart = columnChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ -> value.toInt().toString() }
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = bottomAxisValueFormatter
                    ),
                    modifier = Modifier.height(220.dp)
                )
            }
        }
    }
}
