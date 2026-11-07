package com.example.ui.screens

import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.utils.PersianNumbers
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
    val prediction by viewModel.cyclePrediction.collectAsState()

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

    // Count what was really logged (any label, including custom ones), most frequent first.
    // A fixed list of names would miss labels such as «جوش صورت» and show empty bars.
    fun tally(values: List<String>): Map<String, Float> = values
        .flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("انرژی:") && !it.startsWith("خواب:") }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(6)
        .associate { it.key to it.value.toFloat() }

    val physicalTally = remember(recentLogs) { tally(recentLogs.map { it.symptoms }) }
    val moodTally = remember(recentLogs) { tally(recentLogs.map { it.moods }) }

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
                averageCycleLength = prediction.averageCycleLength
            )

            ChartCard(
                title = "فراوانی علائم جسمی (۳ ماه اخیر)",
                data = physicalTally,
                labels = physicalTally.keys.toList()
            )
            
            ChartCard(
                title = "فراوانی وضعیت روحی (۳ ماه اخیر)",
                data = moodTally,
                labels = moodTally.keys.toList()
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
                    labels.getOrNull(value.toInt())?.let { if (it.length > 9) it.take(8) + "…" else it } ?: ""
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
                    modifier = Modifier
                        .height(220.dp)
                        .clearAndSetSemantics { }
                )
                Spacer(modifier = Modifier.height(12.dp))
                // The same numbers as text (readable by screen readers and for long labels).
                labels.forEachIndexed { i, label ->
                    Text(
                        text = "• $label: ${PersianNumbers.toPersianDigits(values[i].toInt())} روز",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
