package com.example.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.MedicineReminder
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    val isAiRemindersEnabled by viewModel.isAiRemindersEnabled.collectAsState()
    val isOvulationRemindersEnabled by viewModel.isOvulationRemindersEnabled.collectAsState()
    val daysBefore by viewModel.daysBefore.collectAsState()
    val cycleInfo by viewModel.cycleInfo.collectAsState()
    val medicationReminders by viewModel.medicationReminders.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }

    // Form states for adding medication
    var medName by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("09:00") }
    var medDosage by remember { mutableStateOf("۱ عدد") }
    var medType by remember { mutableStateOf("pill") } // pill, capsule, drop, tea, powder

    val typeOptions = listOf(
        "pill" to "💊 قرص",
        "capsule" to "🧪 کپسول",
        "drop" to "💧 قطره چکان",
        "tea" to "☕ دمنوش آرامبخش",
        "powder" to "❄️ پودر/ساشه"
    )

    // Send a live test notification immediately to test the alert mechanism
    val triggerTestNotification: (String, String) -> Unit = { title, body ->
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "medication_reminders_test_channel"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "تست هشدارهای دارویی و چرخه قاعدگی",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "کانال برای تست و ارسال پیام‌های اضطراری هانیتا"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            
            coroutineScope.launch {
                snackbarHostState.showSnackbar("🔔 اعلان آزمایشی فرستاده شد! نوار اعلان گوشی را چک کنید.")
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("خطا در ارسال اعلان: ${e.localizedMessage}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("یادآورها و هشدار دارویی 🔔", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("مدیریت هوشمند زمان مصرف مکمل‌ها و شروع دوره", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // Section 1: Cycle Reminder Details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text("شدت یا زمان اعلام دوره بعدی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "پیش‌بینی آغاز دوره بعدی: ${cycleInfo.predictedPeriodStart}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = { viewModel.setNotificationEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            if (isNotificationEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "چه زمانی مایلید اعلان هشدار برای شما ارسال شود؟",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(0, 1, 2, 3).forEach { day ->
                                        val selected = daysBefore == day
                                        val label = when (day) {
                                            0 -> "همان روز"
                                            1 -> "۱ روز قبل"
                                            2 -> "۲ روز قبل"
                                            3 -> "۳ روز قبل"
                                            else -> "$day روز پیش"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                                .clickable { viewModel.setDaysBefore(day) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        triggerTestNotification(
                                            "یادآور چرخه قاعدگی هانیتا 🌸",
                                            "بر اساس تنظیمات زنگ هوشمند، دوره شما حدود $daysBefore روز آینده آغاز خواهد شد. مراقبت‌های لازم را در نظر بگیرید."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ارسال تست هشدار چرخه 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section 1.5: AI Personalized Reminders & Predictions
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Psychology,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text("پیش‌بینی و یادآور هوشمند هوش مصنوعی 🧠✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "شخصی‌سازی هشدارها بر اساس تحلیل الگوهای اختصاصی بدن شما",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Switch 1: AI period reminder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("اعلان هوشمند پیش‌بینی شروع دوره بعدی", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("ارسال پیام همراه با توصیه‌های تغذیه و سلامت شخصی‌سازی شده توسط Gemini", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                                }
                                Switch(
                                    checked = isAiRemindersEnabled,
                                    onCheckedChange = { viewModel.setAiRemindersEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch 2: AI ovulation reminder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("اعلان هوشمند پنجره تخمک‌گذاری و باروری", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("اطلاع‌رسانی دوران طلایی انرژی بدن همراه با معرفی بهترین تمرینات ورزشی و پویا", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                                }
                                Switch(
                                    checked = isOvulationRemindersEnabled,
                                    onCheckedChange = { viewModel.setOvulationRemindersEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }

                            if (isAiRemindersEnabled || isOvulationRemindersEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                                        .border(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "📥 پیش‌نمایش پیام سفارشی‌شده هوش مصنوعی شما:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        if (isAiRemindersEnabled) {
                                            val aiMsg = viewModel.getPersonalizedAiReminderMessage()
                                            Text(
                                                text = "📌 ${aiMsg.first}:\n${aiMsg.second}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                lineHeight = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        
                                        if (isOvulationRemindersEnabled) {
                                            val ovMsg = viewModel.getOvulationReminderMessage()
                                            Text(
                                                text = "📌 ${ovMsg.first}:\n${ovMsg.second}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isAiRemindersEnabled) {
                                        Button(
                                            onClick = {
                                                val aiMsg = viewModel.getPersonalizedAiReminderMessage()
                                                triggerTestNotification(aiMsg.first, aiMsg.second)
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تست یادآور دوره 🧠", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (isOvulationRemindersEnabled) {
                                        Button(
                                            onClick = {
                                                val ovMsg = viewModel.getOvulationReminderMessage()
                                                triggerTestNotification(ovMsg.first, ovMsg.second)
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تست یادآور باروری 🌸", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Medication Reminders Header & Add
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("یادآورهای روزانه داروها 💊", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("ساعت تغذیه، مکمل آهن، مسکن‌ها و دمنوش‌های درمانی", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = {
                                medName = ""
                                medTime = "09:00"
                                medDosage = "۱ عدد"
                                medType = "pill"
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("افزودن داروی جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Medication Cards list
                if (medicationReminders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                                Text("یادآوری دارویی فعال یافت نشد.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("جهت جلوگیری از فراموشی مصرف داروها، مکمل‌های آهن و تسکین‌دهنده‌های قاعدگی، یکی ایجاد کنید.", fontSize = 10.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f))
                            }
                        }
                    }
                } else {
                    items(medicationReminders, key = { it.id }) { item ->
                        val iconRes = when (item.type) {
                            "pill" -> Icons.Filled.Face // Or standard Pill style indicator
                            "capsule" -> Icons.Filled.Science
                            "drop" -> Icons.Filled.WaterDrop
                            "tea" -> Icons.Filled.Coffee
                            else -> Icons.Filled.Waves
                        }
                        
                        val iconBg = when (item.type) {
                            "pill" -> Color(0xFFFEE2E2)
                            "capsule" -> Color(0xFFEFF6FF)
                            "drop" -> Color(0xFFECFDF5)
                            "tea" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFF5F3FF)
                        }

                        val iconColor = when (item.type) {
                            "pill" -> Color(0xFFEF4444)
                            "capsule" -> Color(0xFF3B82F6)
                            "drop" -> Color(0xFF10B981)
                            "tea" -> Color(0xFFD97706)
                            else -> Color(0xFF8B5CF6)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, if (item.isEnabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(iconBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconRes,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(item.dosage, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = "هر روز ساعت ${item.time}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Live test reminder button
                                    if (item.isEnabled) {
                                        IconButton(
                                            onClick = {
                                                triggerTestNotification(
                                                    "یادآور بهداشتی هانیتا 💊",
                                                    "زمان مصرف ${item.name} فرا رسیده است ($medDosage) - ساعت ${item.time}"
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.NotificationsActive,
                                                contentDescription = "تست هشدار مصرف",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Switch Toggle Active
                                    Switch(
                                        checked = item.isEnabled,
                                        onCheckedChange = { active ->
                                            val updated = medicationReminders.map {
                                                if (it.id == item.id) it.copy(isEnabled = active) else it
                                            }
                                            viewModel.saveMedicationReminders(updated)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.scale(0.85f)
                                    )

                                    // Delete pill/medicine reminder
                                    IconButton(
                                        onClick = {
                                            val updated = medicationReminders.filter { it.id != item.id }
                                            viewModel.saveMedicationReminders(updated)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("یادآور ${item.name} با موفقیت حذف گردید.")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "حذف دارو",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add medicine reminders elegantly
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "افزودن یادآور داروی جدید 🌸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("اطلاعات مصرف قرص، دارو یا دمنوش مقوی را در فرم زیر وارد نمایید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                    
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("نام دارو/مکمل", fontSize = 12.sp) },
                        placeholder = { Text("مثلاً: مفنامیک اسید", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = medTime,
                            onValueChange = { medTime = it },
                            label = { Text("ساعت مصرف", fontSize = 12.sp) },
                            placeholder = { Text("مثلاً: 14:00", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = medDosage,
                            onValueChange = { medDosage = it },
                            label = { Text("دوز / پیمانه", fontSize = 12.sp) },
                            placeholder = { Text("مثلاً: ۱ کپسول", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }

                    // Medicine visual typeselector
                    Text("نوع بسته‌بندی دارو:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        typeOptions.forEach { (typeKey, label) ->
                            val selected = medType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { medType = typeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.split(" ").last(), // Get only icon/short text
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank() && medTime.isNotBlank()) {
                            val newReminder = MedicineReminder(
                                id = UUID.randomUUID().toString(),
                                name = medName,
                                time = medTime,
                                dosage = medDosage,
                                isEnabled = true,
                                type = medType
                            )
                            val newList = medicationReminders + newReminder
                            viewModel.saveMedicationReminders(newList)
                            showAddDialog = false
                            
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("یادآور داروی $medName با موفقیت ایجاد شد! 🎉")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("ثبت یادآور")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
