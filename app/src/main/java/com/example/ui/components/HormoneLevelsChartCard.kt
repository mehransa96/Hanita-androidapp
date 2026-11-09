package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
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
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryDark
import com.example.ui.theme.SleekSurface
import com.example.ui.viewmodel.CycleInfo
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.exp

// Hormone representation metadata
enum class HormoneType(
    val faName: String,
    val enName: String,
    val color: Color,
    val roleFa: String,
    val peakPhaseFa: String,
    val descFa: String
) {
    ESTROGEN(
        faName = "استروژن",
        enName = "Estrogen",
        color = Color(0xFFEC4899), // Pink
        roleFa = "رشد فولیکول‌ها، ضخیم‌شدن دیواره رحم، انرژی و شادابی",
        peakPhaseFa = "قبل از تخمک‌گذاری (روز ۱۲ تا ۱۴)",
        descFa = "در فاز فولیکولی افزایش می‌یابد و درست قبل از تخمک‌گذاری به اوج خود می‌رسد. این هورمون باعث طراوت پوست، افزایش خلق‌وخو و انگیزه می‌گردد."
    ),
    PROGESTERONE(
        faName = "پروژسترون",
        enName = "Progesterone",
        color = Color(0xFF8B5CF6), // Violet / Purple
        roleFa = "آماده‌سازی و تثبیت آندومتر، آرامش عضلانی و خواب",
        peakPhaseFa = "فاز لوتئال (روز ۲۱ تا ۲۴)",
        descFa = "پس از تخمک‌گذاری توسط جسم زرد ترشح می‌شود تا رحم را برای لانه‌گزینی آماده کند. اثر آرام‌بخشی دارد ولی افت ناگهانی آن در انتهای چرخه عامل اصلی علائم PMS است."
    ),
    LH(
        faName = "LH",
        enName = "Luteinizing Hormone",
        color = Color(0xFFF59E0B), // Amber / Orange
        roleFa = "محرک آزادسازی تخمک بالغ (تخمک‌گذاری)",
        peakPhaseFa = "پیک تخمک‌گذاری (روز ۱۳ تا ۱۴)",
        descFa = "جهش ناگهانی و کوتاه LH به عنوان کلید اصلی رهاسازی تخمک عمل می‌کند و کیت‌های تخمک‌گذاری خانگی نیز افزایش همین هورمون را اندازه‌گیری می‌کنند."
    ),
    FSH(
        faName = "FSH",
        enName = "Follicle-Stimulating Hormone",
        color = Color(0xFF10B981), // Emerald / Green
        roleFa = "تحریک رشد فولیکول‌های تخمدان در اوایل چرخه",
        peakPhaseFa = "ابتدای فاز فولیکولی و جهش خفیف همراه با LH",
        descFa = "توسط غده هیپوفیز ترشح می‌شود و در ابتدای چرخه به تخمدان‌ها سیگنال می‌دهد تا چندین فولیکول را برای رشد آماده کنند."
    )
}

/**
 * Visual Hormone fluctuations chart using Vico Charting library.
 * Models physiological curve levels (0-100%) across the menstrual cycle phases.
 */
@Composable
fun HormoneLevelsChartCard(
    cycleInfo: CycleInfo?,
    averageCycleLength: Int = 28,
    modifier: Modifier = Modifier
) {
    val totalDays = remember(averageCycleLength) { averageCycleLength.coerceIn(21, 45) }
    val ovulationDay = remember(totalDays) { com.example.domain.CyclePredictor.ovulationDay(totalDays) }

    // Selected hormone filters: All 4 enabled by default, user can toggle each
    var selectedHormones by remember {
        mutableStateOf(setOf(HormoneType.ESTROGEN, HormoneType.PROGESTERONE, HormoneType.LH, HormoneType.FSH))
    }

    // Active expanded info card for learning details
    var selectedInfoHormone by remember { mutableStateOf<HormoneType?>(null) }

    // Generate mathematical bell-curve data for each hormone scaled to 1..totalDays
    val daysList = remember(totalDays) { (1..totalDays).toList() }

    val estrogenEntries = remember(daysList, ovulationDay, totalDays) {
        daysList.map { day ->
            // Estrogen: low in period (10-15%), rises sharply to 90% at ovulationDay - 1, dips, secondary peak in mid luteal (60%)
            val preOvulationPeak = 95f * exp(-0.4 * (day - (ovulationDay - 1.5)) * (day - (ovulationDay - 1.5))).toFloat()
            val lutealPeak = 55f * exp(-0.25 * (day - (ovulationDay + 7.0)) * (day - (ovulationDay + 7.0))).toFloat()
            val base = 15f + ((day.toFloat() / totalDays) * 10f)
            val level = (base + preOvulationPeak + lutealPeak).coerceIn(10f, 100f)
            FloatEntry(day.toFloat(), level)
        }
    }

    val progesteroneEntries = remember(daysList, ovulationDay, totalDays) {
        daysList.map { day ->
            // Progesterone: extremely low before ovulation (5-10%), prominent peak in mid-luteal (days ovulationDay+7) reaching ~85%, falls sharply before menses
            val midLutealDay = ovulationDay + 7.0
            val lutealPeak = 85f * exp(-0.18 * (day - midLutealDay) * (day - midLutealDay)).toFloat()
            val base = if (day <= ovulationDay) 6f else 8f
            val level = (base + lutealPeak).coerceIn(5f, 95f)
            FloatEntry(day.toFloat(), level)
        }
    }

    val lhEntries = remember(daysList, ovulationDay) {
        daysList.map { day ->
            // LH: low steady baseline (~12%), very sharp high spike right at ovulationDay-1 to ovulationDay (~100%)
            val spike = 90f * exp(-1.2 * (day - (ovulationDay - 0.5)) * (day - (ovulationDay - 0.5))).toFloat()
            val base = 12f
            val level = (base + spike).coerceIn(8f, 100f)
            FloatEntry(day.toFloat(), level)
        }
    }

    val fshEntries = remember(daysList, ovulationDay) {
        daysList.map { day ->
            // FSH: elevated in early follicular (~40%), decreases as estrogen rises, small peak coinciding with LH surge (~50%), then low in luteal
            val earlyFollicular = 35f * exp(-0.2 * (day - 3.0) * (day - 3.0)).toFloat()
            val ovulationSurge = 40f * exp(-1.0 * (day - (ovulationDay - 0.5)) * (day - (ovulationDay - 0.5))).toFloat()
            val base = 15f
            val level = (base + earlyFollicular + ovulationSurge).coerceIn(10f, 85f)
            FloatEntry(day.toFloat(), level)
        }
    }

    // Build lineSpecs and dataset based on active selection
    val activeHormonesList = remember(selectedHormones) {
        HormoneType.values().filter { it in selectedHormones }
    }

    val linesList = remember(activeHormonesList) {
        activeHormonesList.map { hormone ->
            lineSpec(
                lineColor = hormone.color
            )
        }
    }

    val chartModel = remember(activeHormonesList, estrogenEntries, progesteroneEntries, lhEntries, fshEntries) {
        val entrySets = activeHormonesList.map { hormone ->
            when (hormone) {
                HormoneType.ESTROGEN -> estrogenEntries
                HormoneType.PROGESTERONE -> progesteroneEntries
                HormoneType.LH -> lhEntries
                HormoneType.FSH -> fshEntries
            }
        }
        if (entrySets.isNotEmpty()) {
            entryModelOf(*entrySets.toTypedArray())
        } else {
            // Fallback when nothing is selected
            entryModelOf(listOf(FloatEntry(1f, 0f)))
        }
    }

    val currentCycleDay = cycleInfo?.currentDay?.coerceIn(1, totalDays) ?: 1
    val currentPhaseTitle = remember(cycleInfo, currentCycleDay, ovulationDay) {
        when {
            cycleInfo?.isPeriod == true -> "فاز قاعدگی (خونریزی)"
            cycleInfo?.isFertile == true -> "فاز باروری و تخمک‌گذاری"
            cycleInfo?.isPms == true -> "فاز پیش از قاعدگی (PMS)"
            currentCycleDay < ovulationDay -> "فاز فولیکولی"
            else -> "فاز لوتئال"
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top title & status header
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEC4899).copy(alpha = 0.15f)), // Rose 100
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "نمودار هورمون‌ها",
                            tint = SleekPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "نمودار هورمونی چرخه قاعدگی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "الگوی تقریبی",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "الگوی عمومی تغییر استروژن، پروژسترون، LH و FSH در طول چرخه (نه اندازه‌گیری بدن شما)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current day indicator chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضعیت کنونی شما: روز ${com.example.utils.PersianNumbers.toPersianDigits(currentCycleDay)} از چرخه",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SleekPrimaryDark
                    )
                    Text(
                        text = "فاز: $currentPhaseTitle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekPrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive hormone filter chips
            Text(
                text = "فیلتر و بررسی هورمون‌ها (برای فعال/غیرفعال کردن لمس کنید):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HormoneType.values().forEach { hormone ->
                    val isSelected = hormone in selectedHormones
                    val chipBg = if (isSelected) hormone.color.copy(alpha = 0.15f) else Color.Transparent
                    val borderCol = if (isSelected) hormone.color else SleekBorder

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedHormones = if (isSelected) {
                                    if (selectedHormones.size > 1) selectedHormones - hormone else selectedHormones
                                } else {
                                    selectedHormones + hormone
                                }
                                selectedInfoHormone = hormone
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(hormone.color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = hormone.faName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) hormone.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phase indicator bar along the horizontal axis
            // Day ranges follow the same rules as the prediction engine.
            val legend = listOf(
                Triple("🩸 قاعدگی", "۱-۵", Color(0xFFEF4444)),
                Triple("🌱 فولیکولی", "۶-${com.example.utils.PersianNumbers.toPersianDigits(ovulationDay - 2)}", Color(0xFF3B82F6)),
                Triple("✨ تخمک‌گذاری", "${com.example.utils.PersianNumbers.toPersianDigits(ovulationDay - 1)}-${com.example.utils.PersianNumbers.toPersianDigits(ovulationDay + 1)}", Color(0xFFF59E0B)),
                Triple("🌙 لوتئال", "${com.example.utils.PersianNumbers.toPersianDigits(ovulationDay + 2)}-${com.example.utils.PersianNumbers.toPersianDigits(totalDays)}", Color(0xFF8B5CF6))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                legend.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { (label, days, color) ->
                            Text("$label (روز $days)", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Vico Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                if (linesList.isNotEmpty()) {
                    // Charts read left-to-right (day 1 on the left), like the axis numbers.
                    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                        Chart(
                            // The whole cycle fits the card (no hidden horizontal scroll).
                            chartScrollSpec = com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec(isScrollEnabled = false),
                            chart = lineChart(
                                lines = linesList
                            ),
                            model = chartModel,
                            startAxis = rememberStartAxis(
                                valueFormatter = { value, _ -> com.example.utils.PersianNumbers.toPersianDigits(value.toInt()) + "٪" },
                                title = "غلظت نسبی"
                            ),
                            bottomAxis = rememberBottomAxis(
                                // Days 7, 14, 21, 28: one label per week with room to draw it (edge labels were cut to "…").
                                itemPlacer = remember { com.patrykandpatrick.vico.core.axis.AxisItemPlacer.Horizontal.default(spacing = 7, offset = 6) },
                                valueFormatter = { value, _ -> com.example.utils.PersianNumbers.toPersianDigits(value.toInt()) },
                                title = "روز چرخه"
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Educational explanation of the active hormone
            val activeDetail = selectedInfoHormone ?: HormoneType.ESTROGEN
            AnimatedContent(targetState = activeDetail, label = "HormoneDetailAnim") { hormone ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = hormone.color.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, hormone.color.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = hormone.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${hormone.faName} (${hormone.enName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = hormone.color
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🎯 زمان اوج: ${hormone.peakPhaseFa}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = hormone.descFa,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
