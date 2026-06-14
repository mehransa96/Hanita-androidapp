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

enum class CyclePhaseType {
    PERIOD, FERTILE, PMS, REGULAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MahitaViewModel,
    onBack: () -> Unit
) {
    var todayJalali = JalaliCalendar.gregorianToJalali(LocalDate.now())
    var currentMonth by remember { mutableStateOf(JalaliDate(todayJalali.year, todayJalali.month, 1)) }
    
    val logs by viewModel.allLogs.collectAsState()
    val lastPeriodDateStr by viewModel.lastPeriodDate.collectAsState()
    val averageCycleLength by viewModel.averageCycleLength.collectAsState()
    
    // Convert logs into a set of period dates for quick lookup
    val periodDates = remember(logs) {
        logs.filter { it.periodFlow != "NONE" }.map { JalaliCalendar.gregorianToJalali(LocalDate.parse(it.dateStr)) }.toSet()
    }
    
    // Predict next period, fertile window, and PMS based on the latest period date
    val predictedPeriodDates by remember(periodDates, lastPeriodDateStr, averageCycleLength) {
        derivedStateOf {
            val calculated = mutableSetOf<JalaliDate>()
            val latestPeriod = periodDates.maxOrNull() ?: if (lastPeriodDateStr.isNotBlank()) {
                try {
                    JalaliCalendar.gregorianToJalali(LocalDate.parse(lastPeriodDateStr))
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
            if (latestPeriod != null) {
                val latestLocalDate = latestPeriod.toLocalDate()
                val cycleL = averageCycleLength.coerceIn(21, 45)
                for (i in 0..5) {
                    val cycleStart = latestLocalDate.plusDays((i * cycleL).toLong())
                    for (day in 0..4) {
                        calculated.add(JalaliCalendar.gregorianToJalali(cycleStart.plusDays(day.toLong())))
                    }
                }
            }
            calculated
        }
    }

    val fertileDates by remember(periodDates, lastPeriodDateStr, averageCycleLength) {
        derivedStateOf {
            val calculated = mutableSetOf<JalaliDate>()
            val latestPeriod = periodDates.maxOrNull() ?: if (lastPeriodDateStr.isNotBlank()) {
                try {
                    JalaliCalendar.gregorianToJalali(LocalDate.parse(lastPeriodDateStr))
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
            if (latestPeriod != null) {
                val latestLocalDate = latestPeriod.toLocalDate()
                val cycleL = averageCycleLength.coerceIn(21, 45)
                val ovulationDay = cycleL - 14
                for (i in 0..5) {
                    val cycleStart = latestLocalDate.plusDays((i * cycleL).toLong())
                    for (day in (ovulationDay - 3)..(ovulationDay + 1)) {
                        calculated.add(JalaliCalendar.gregorianToJalali(cycleStart.plusDays(day.toLong())))
                    }
                }
            }
            calculated
        }
    }

    val pmsDates by remember(periodDates, lastPeriodDateStr, averageCycleLength) {
        derivedStateOf {
            val calculated = mutableSetOf<JalaliDate>()
            val latestPeriod = periodDates.maxOrNull() ?: if (lastPeriodDateStr.isNotBlank()) {
                try {
                    JalaliCalendar.gregorianToJalali(LocalDate.parse(lastPeriodDateStr))
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
            if (latestPeriod != null) {
                val latestLocalDate = latestPeriod.toLocalDate()
                val cycleL = averageCycleLength.coerceIn(21, 45)
                for (i in 0..5) {
                    val cycleStart = latestLocalDate.plusDays((i * cycleL).toLong())
                    for (day in (cycleL - 6)..(cycleL - 1)) {
                        calculated.add(JalaliCalendar.gregorianToJalali(cycleStart.plusDays(day.toLong())))
                    }
                }
            }
            calculated
        }
    }

    var selectedDate by remember { mutableStateOf<JalaliDate?>(todayJalali) }

    val selectedDatePhaseType = remember(selectedDate, periodDates, predictedPeriodDates, fertileDates, pmsDates) {
        derivedStateOf {
            val date = selectedDate
            if (date == null) null
            else {
                val isPeriod = periodDates.contains(date) || predictedPeriodDates.contains(date)
                val isFertile = fertileDates.contains(date) && !isPeriod
                val isPms = pmsDates.contains(date) && !isPeriod && !isFertile
                
                when {
                    isPeriod -> CyclePhaseType.PERIOD
                    isFertile -> CyclePhaseType.FERTILE
                    isPms -> CyclePhaseType.PMS
                    else -> CyclePhaseType.REGULAR
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقویم هوشمند قاعدگی هانیتا", fontWeight = FontWeight.Black, fontSize = 16.sp) },
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
            Spacer(modifier = Modifier.height(8.dp))
            
            // Premium Month Navigation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.4f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            var m = currentMonth.month - 1
                            var y = currentMonth.year
                            if (m < 1) { m = 12; y-- }
                            currentMonth = JalaliDate(y, m, 1)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = SleekBorder.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه قبل", tint = SleekPrimary)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentMonth.monthName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPrimary
                        )
                        Text(
                            text = "${currentMonth.year}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SleekTextLight
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            var m = currentMonth.month + 1
                            var y = currentMonth.year
                            if (m > 12) { m = 1; y++ }
                            currentMonth = JalaliDate(y, m, 1)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = SleekBorder.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد", tint = SleekPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Days of week header Inside Sleek Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                val daysOfWeek = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            fontWeight = FontWeight.Black,
                            color = SleekTextLight,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val daysInMonth = currentMonth.lengthOfMonth
            val firstDayOfWeek = currentMonth.firstDayOfWeek()
            val offset = firstDayOfWeek
            
            val totalSlots = offset + daysInMonth
            val weeksCount = (totalSlots + 6) / 7

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (weekIndex in 0 until weeksCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val slotIndex = weekIndex * 7 + dayOfWeek
                            if (slotIndex < offset || slotIndex >= totalSlots) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val dayIndex = slotIndex - offset
                                val date = JalaliDate(currentMonth.year, currentMonth.month, dayIndex + 1)
                                val isPeriod = periodDates.contains(date) || predictedPeriodDates.contains(date)
                                val isFertile = fertileDates.contains(date) && !isPeriod
                                val isPms = pmsDates.contains(date) && !isPeriod && !isFertile
                                val isToday = date == todayJalali
                                val isSelected = date == selectedDate
                                
                                val bgColor = when {
                                    isSelected -> SleekPrimary
                                    isPeriod -> SleekPrimaryLight
                                    isFertile -> Indigo50
                                    isPms -> Amber50
                                    isToday -> SleekBorder
                                    else -> Color.Transparent
                                }
                                val textColor = when {
                                    isSelected -> Color.White
                                    isPeriod -> SleekPrimaryDark
                                    isFertile -> Indigo500
                                    isPms -> Amber500
                                    isToday -> SleekTextDark
                                    else -> SleekTextMedium
                                }

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
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .scale(itemScale)
                                        .clip(CircleShape)
                                        .background(animatedBgColor)
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${dayIndex + 1}",
                                            color = animatedTextColor,
                                            fontWeight = if (isPeriod || isFertile || isPms || isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            if (isPeriod) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else SleekPrimary))
                                            }
                                            if (isFertile) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else Indigo500))
                                            }
                                            if (isPms) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else Amber500))
                                            }
                                            if (isToday && !isSelected && !isPeriod && !isFertile && !isPms) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SleekTextDark))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Dynamic Phase Active Info Banner
            val phaseType = selectedDatePhaseType.value
            if (phaseType != null) {
                // Composable configuration resolved within composition context to avoid errors
                val title = when (phaseType) {
                    CyclePhaseType.PERIOD -> "دوره قاعدگی (شروع چرخه جدید) 🩸"
                    CyclePhaseType.FERTILE -> "پنجره باروری و زمان تخمک‌گذاری 🥚✨"
                    CyclePhaseType.PMS -> "روزهای سندرم پیش از قاعدگی (PMS) 🌪️💛"
                    CyclePhaseType.REGULAR -> "فاز فعال معمول بدنی 🌸"
                }
                val description = when (phaseType) {
                    CyclePhaseType.PERIOD -> "رحم در حال پاکسازی و بازسازی دیواره خود است. این یعنی سطح عمومی هورمون‌ها پایین است و احتمال احساس خستگی طبیعی وجود دارد."
                    CyclePhaseType.FERTILE -> "روزهای اوج شادابی! هورمون استروژن در بالاترین حد بیولوژیکی قرار دارد که منجر به افزایش فوق‌العاده سطح انرژی ذهنی، انگیزه و خلق‌وخوی متعالی می‌شود."
                    CyclePhaseType.PMS -> "پروژسترون کاهش سریعی یافته و ممکن است علائم فیزیکی مانند خستگی خفیف، ویار شیرینی‌جات، جوش و نوسانات احساسی جزئی پیش آید."
                    CyclePhaseType.REGULAR -> "سطح عمومی هورمون‌ها در منظم‌ترین حالت ممکن قرار دارد. تمرکز فکری شما بالا بوده و نوسان احساسی خاصی را تجربه نخواهید کرد."
                }
                val recommendation = when (phaseType) {
                    CyclePhaseType.PERIOD -> "استراحت کافی، مصرف مکمل آهن به همراه آب‌میوه طبیعی، گرم نگه داشتن ناحیه شکم و نوشیدن دمنوش بابونه یا زنجبیل برای تسکین دردهای عضلانی پیشنهاد می‌شود."
                    CyclePhaseType.FERTILE -> "یک زمان استثنایی برای تمرینات ورزشی پرنشاط، فعالیت‌های اجتماعی عمیق عاطفی، حل مسائل پیچیده کاری و خوداندیشی مثبت فراوان است."
                    CyclePhaseType.PMS -> "نوشیدن آب مداوم، کاهش نمک و شکر فرآوری‌شده، تمرینات یوگا یا کشش سبک، دمنوش‌های گیاهی آرامش‌بخش و مراقبه‌های ذهن‌آگاهی هانیتا برای تمدد اعصاب شدیداً توصیه می‌شود."
                    CyclePhaseType.REGULAR -> "روزمرگی خود را با تغذیه متعادل سرشار از فیبر و سبزیجات تازه غنی کنید و ثبات بدنی بی‌نظیر کنونی را جهت برنامه‌ریزی اهداف بلندمدت بکار بگیرید."
                }
                val mainColor = when (phaseType) {
                    CyclePhaseType.PERIOD -> SleekPrimary
                    CyclePhaseType.FERTILE -> Indigo500
                    CyclePhaseType.PMS -> Amber500
                    CyclePhaseType.REGULAR -> SleekSecondary
                }
                val containerColor = when (phaseType) {
                    CyclePhaseType.PERIOD -> SleekPrimaryLight
                    CyclePhaseType.FERTILE -> Indigo50
                    CyclePhaseType.PMS -> Amber50
                    CyclePhaseType.REGULAR -> SleekBorder.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(1.5.dp, mainColor.copy(alpha = 0.4f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(mainColor)
                            )
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor
                            )
                        }
                        
                        HorizontalDivider(color = mainColor.copy(alpha = 0.2f), thickness = 1.dp)
                        
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            color = SleekTextMedium,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SleekSurface.copy(alpha = 0.5f)),
                            border = BorderStroke(0.5.dp, mainColor.copy(alpha = 0.15f)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "💡 پیشنهاد هانیتا:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = mainColor
                                )
                                Text(
                                    text = recommendation,
                                    fontSize = 11.sp,
                                    color = SleekTextLight,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
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
                                        "یادداشت‌های روز ${date.day} ${date.monthName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
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
                                        Text("علائم ثبت شده: ${logForDate.symptoms.replace(",", "، ")}", style = MaterialTheme.typography.bodyMedium, color = SleekTextMedium)
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
                                        ) { Text("ذخیره", color = Color.White, fontWeight = FontWeight.Bold) }
                                    }
                                } else {
                                    TextButton(
                                        onClick = { isEditingNote = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (logForDate?.notes.isNullOrBlank()) "+ افزودن یادداشت روزانه" else "ویرایش یادداشت", color = SleekPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Refined High-End Legend Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("راهنمای رنگی تقویم هانیتا", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SleekTextDark, modifier = Modifier.padding(bottom = 16.dp))
                    LegendItem(color = SleekPrimaryLight, textColor = SleekPrimaryDark, dotColor = SleekPrimary, label = "روزهای دوره قاعدگی (پریود)")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = Indigo50, textColor = Indigo500, dotColor = Indigo500, label = "پنجره باروری طلایی (تخمک‌گذاری)")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = Amber50, textColor = Amber500, dotColor = Amber500, label = "سندرم پیش از قاعدگی (PMS)")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = SleekBorder, textColor = SleekTextDark, dotColor = SleekTextDark, label = "امروز (تاریخ جاری بدنی)")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LegendItem(color: Color, textColor: Color, dotColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = SleekTextMedium, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
