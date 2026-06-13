package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MahitaViewModel,
    onBack: () -> Unit
) {
    var todayJalali = JalaliCalendar.gregorianToJalali(LocalDate.now())
    var currentMonth by remember { mutableStateOf(JalaliDate(todayJalali.year, todayJalali.month, 1)) }
    
    val logs by viewModel.allLogs.collectAsState()
    
    // Convert logs into a set of period dates for quick lookup
    val periodDates = logs.filter { it.periodFlow != "NONE" }.map { JalaliCalendar.gregorianToJalali(LocalDate.parse(it.dateStr)) }.toSet()
    
    // Predict next period and fertile window based on the latest period date
    val predictedPeriodDates = mutableSetOf<JalaliDate>()
    val fertileDates = mutableSetOf<JalaliDate>()
    
    val latestPeriod = periodDates.maxOrNull()
    if (latestPeriod != null) {
        val latestLocalDate = latestPeriod.toLocalDate()
        for (i in 0..5) {
            val cycleStart = latestLocalDate.plusDays((i * 28).toLong())
            for (day in 0..4) predictedPeriodDates.add(JalaliCalendar.gregorianToJalali(cycleStart.plusDays(day.toLong())))
            for (day in 11..15) fertileDates.add(JalaliCalendar.gregorianToJalali(cycleStart.plusDays(day.toLong())))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقویم قاعدگی") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    var m = currentMonth.month - 1
                    var y = currentMonth.year
                    if (m < 1) { m = 12; y-- }
                    currentMonth = JalaliDate(y, m, 1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه قبل")
                }
                
                Text(
                    text = "${currentMonth.monthName} ${currentMonth.year}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                
                IconButton(onClick = { 
                    var m = currentMonth.month + 1
                    var y = currentMonth.year
                    if (m > 12) { m = 1; y++ }
                    currentMonth = JalaliDate(y, m, 1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Days of week header
            val daysOfWeek = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextLight,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val daysInMonth = currentMonth.lengthOfMonth
            val firstDayOfWeek = currentMonth.firstDayOfWeek()
            val offset = firstDayOfWeek
            
            var selectedDate by remember { mutableStateOf<JalaliDate?>(todayJalali) }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.height(340.dp),
                userScrollEnabled = false
            ) {
                items(offset) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
                items(daysInMonth) { dayIndex ->
                    val date = JalaliDate(currentMonth.year, currentMonth.month, dayIndex + 1)
                    val isPeriod = periodDates.contains(date) || predictedPeriodDates.contains(date)
                    val isFertile = fertileDates.contains(date) && !isPeriod
                    val isToday = date == todayJalali
                    val isSelected = date == selectedDate
                    
                    val bgColor = when {
                        isSelected -> SleekPrimary
                        isPeriod -> SleekPrimaryLight
                        isFertile -> Indigo50
                        isToday -> SleekBorder
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        isPeriod -> SleekPrimaryDark
                        isFertile -> Indigo500
                        isToday -> SleekTextDark
                        else -> SleekTextMedium
                    }

                    // Smooth color transitions
                    val animatedBgColor by animateColorAsState(
                        targetValue = bgColor,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                        label = "animatedBgColor"
                    )
                    val animatedTextColor by animateColorAsState(
                        targetValue = textColor,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "animatedTextColor"
                    )

                    // Bouncy scale spring animation on selection
                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "itemScale"
                    )

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .scale(itemScale)
                            .clip(CircleShape)
                            .background(animatedBgColor)
                            .clickable { selectedDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dayIndex + 1}",
                            color = animatedTextColor,
                            fontWeight = if (isPeriod || isFertile || isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                     slideInVertically(initialOffsetY = { it / 3 }, animationSpec = spring(stiffness = Spring.StiffnessLow)))
                    .togetherWith(
                     fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    )
                },
                label = "notesTransition"
            ) { date ->
                if (date != null) {
                    val localDateStr = date.toLocalDate().toString()
                    val logForDate = logs.find { it.dateStr == localDateStr }
                    
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SleekSurface),
                            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "یادداشت‌های ${date.day} ${date.monthName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SleekTextDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (logForDate != null && (logForDate.notes.isNotBlank() || logForDate.symptoms.isNotEmpty() || logForDate.moods.isNotEmpty())) {
                                    if (logForDate.notes.isNotBlank()) {
                                        Text("یادداشت: ${logForDate.notes}", style = MaterialTheme.typography.bodyMedium, color = SleekTextMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (logForDate.symptoms.isNotEmpty()) {
                                        Text("علائم: ${logForDate.symptoms.replace(",", "، ")}", style = MaterialTheme.typography.bodyMedium, color = SleekTextMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (logForDate.moods.isNotEmpty()) {
                                        Text("خلق‌و‌خو: ${logForDate.moods.replace(",", "، ")}", style = MaterialTheme.typography.bodyMedium, color = SleekTextMedium)
                                    }
                                } else {
                                    Text("یادداشتی برای این روز ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = SleekTextLight)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                var isEditingNote by remember(date) { mutableStateOf(false) }
                                var noteText by remember(date) { mutableStateOf(logForDate?.notes ?: "") }
                                
                                if (isEditingNote) {
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        placeholder = { Text("یادداشت روزانه...", color = SleekTextLight) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SleekPrimary,
                                            unfocusedBorderColor = SleekBorder,
                                            focusedContainerColor = SleekSurface,
                                            unfocusedContainerColor = SleekSurface,
                                            focusedTextColor = SleekTextDark,
                                            unfocusedTextColor = SleekTextMedium
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { isEditingNote = false }) { Text("لغو", color = SleekTextMedium) }
                                        Button(
                                            onClick = {
                                                viewModel.saveLog(
                                                    dateStr = localDateStr,
                                                    flow = logForDate?.periodFlow ?: "NONE",
                                                    symptoms = logForDate?.symptoms?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                                                    moods = logForDate?.moods?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                                                    notes = noteText
                                                )
                                                isEditingNote = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                                        ) { Text("ذخیره", color = Color.White) }
                                    }
                                } else {
                                    TextButton(
                                        onClick = { isEditingNote = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (logForDate?.notes.isNullOrBlank()) "+ افزودن یادداشت" else "ویرایش یادداشت", color = SleekPrimary)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Legend
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("راهنما", style = MaterialTheme.typography.titleMedium, color = SleekTextDark, modifier = Modifier.padding(bottom = 16.dp))
                    LegendItem(color = SleekPrimaryLight, textColor = SleekPrimaryDark, label = "روزهای قاعدگی")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = Indigo50, textColor = Indigo500, label = "پنجره باروری (تخمک‌گذاری)")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = SleekBorder, textColor = SleekTextDark, label = "امروز")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LegendItem(color: Color, textColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Text("۱", color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = SleekTextMedium, fontSize = 14.sp)
    }
}
