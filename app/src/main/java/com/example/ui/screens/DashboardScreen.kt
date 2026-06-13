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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    onLogout: () -> Unit
) {
    val cycleInfo by viewModel.cycleInfo.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val averageCycleLength by viewModel.averageCycleLength.collectAsState()
    val predictionInfo by viewModel.predictionInfo.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                    cycleLength = averageCycleLength,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PredictionsCard(cycleInfo)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PredictionNotificationCard(predictionInfo)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CycleHistoryChart(modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FeaturesGrid(onNavigateToLog, onNavigateToCalendar, onNavigateToHealth, onNavigateToEducation, onNavigateToMood, onNavigateToChat, onNavigateToMeditation, onNavigateToReminders)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    SettingsDialog(
        show = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel
    )
}

@Composable
fun HeaderRow(
    userName: String,
    userAvatar: String,
    onOpenSettings: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAvatarGenerator: () -> Unit,
    onLogout: () -> Unit
) {
    val jalaliToday = com.example.utils.JalaliCalendar.gregorianToJalali(java.time.LocalDate.now())
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Interactive Fantasy Avatar Picture
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .bounceClick { onNavigateToAvatarGenerator() }
            ) {
                FantasyAvatarCanvas(
                    avatarId = userAvatar,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "امروز، ${jalaliToday.formatLong()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "سلام $userName عزیز",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateToReminders) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = "یادآورها و داروها",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "تنظیمات",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "خروج",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CycleVisualizer(info: CycleInfo, cycleLength: Int, modifier: Modifier = Modifier) {
    val gradientColors = listOf(SleekPrimary, SleekPrimaryVariant)
    val trackColor = SleekPrimaryLight
    val primaryColor = SleekPrimary

    val animatedSweepAngle by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (info.currentDay / cycleLength.toFloat()) * 360f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1500, 
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "sweepAngle"
    )
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Outer glow for fertile/period
            if (info.isPeriod || info.isFertile) {
                drawCircle(
                    color = (if (info.isPeriod) Color.Red else primaryColor).copy(alpha = pulseAlpha * 0.2f),
                    radius = radius + (strokeWidth * 1.5f)
                )
            }
            
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                brush = Brush.linearGradient(colors = gradientColors),
                startAngle = -90f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "روز ${info.currentDay} سیکل",
                style = MaterialTheme.typography.labelMedium,
                color = SleekPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (info.isPeriod) "پریود" else if (info.isFertile) "پنجره باروری" else "روز معمولی",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = SleekTextMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = if (info.isFertile) SleekPrimary else SleekBorder, 
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (info.isFertile) "بارداری: زیاد" else "بارداری: کم",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (info.isFertile) Color.White else SleekTextMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PredictionsCard(info: CycleInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("پریود بعدی", style = MaterialTheme.typography.labelSmall, color = SleekTextLight)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${info.daysUntilNextPeriod} روز دیگر", fontSize = 16.sp, color = SleekPrimaryDark, fontWeight = FontWeight.Black)
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تخمک‌گذاری", style = MaterialTheme.typography.labelSmall, color = SleekTextLight)
                Spacer(modifier = Modifier.height(2.dp))
                val daysToOvulation = if (info.currentDay <= 14) 14 - info.currentDay else (28 - info.currentDay) + 14
                Text("حدود $daysToOvulation روز", fontSize = 16.sp, color = Indigo500, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun FeaturesGrid(onNavigateToLog: () -> Unit, onNavigateToCalendar: () -> Unit, onNavigateToHealth: () -> Unit, onNavigateToEducation: () -> Unit, onNavigateToMood: () -> Unit, onNavigateToChat: () -> Unit, onNavigateToMeditation: () -> Unit, onNavigateToReminders: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureItem(
                icon = Icons.Filled.Add,
                label = "علائم",
                iconBgColor = MaterialTheme.colorScheme.primary,
                iconColor = Color.White,
                onClick = onNavigateToLog,
                modifier = Modifier.weight(1f)
            )
            FeatureItem(
                icon = Icons.Filled.CalendarMonth,
                label = "تقویم",
                iconBgColor = Indigo50,
                iconColor = Indigo500,
                onClick = onNavigateToCalendar,
                modifier = Modifier.weight(1f)
            )
            FeatureItem(
                icon = Icons.Filled.MonitorWeight,
                label = "سلامتی",
                iconBgColor = Amber50,
                iconColor = Amber500,
                onClick = onNavigateToHealth,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureItem(
                icon = Icons.Filled.MenuBook,
                label = "آموزش",
                iconBgColor = Color(0xFFF3E8FF),
                iconColor = Color(0xFFA855F7), // Purple 500
                onClick = onNavigateToEducation,
                modifier = Modifier.weight(1f)
            )
            FeatureItem(
                icon = Icons.Filled.Favorite,
                label = "حال خوب",
                iconBgColor = Color(0xFFFFE4E6),
                iconColor = Color(0xFFE11D48), // Rose 600
                onClick = onNavigateToMood,
                modifier = Modifier.weight(1f)
            )
            FeatureItem(
                icon = Icons.Filled.AutoAwesome,
                label = "مشاور هوشمند",
                iconBgColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF3B82F6), // Blue 500
                onClick = onNavigateToChat,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).bounceClick(onNavigateToMeditation),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFECFDF5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = "موسیقی و مدیتیشن",
                            tint = Color(0xFF10B981), // Emerald 500
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "موسیقی و مراقبه 🧘‍♀️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SleekTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "کاهش استرس دوره قاعدگی",
                            fontSize = 8.5.sp,
                            color = SleekTextMedium
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.weight(1f).bounceClick(onNavigateToReminders),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFEF2F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "یادآورها و هشدار دارویی",
                            tint = Color(0xFFEF4444), // Red 500
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "یادآورها و داروها 🔔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SleekTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "کنترل مکمل‌ها و دوره بعدی",
                            fontSize = 8.5.sp,
                            color = SleekTextMedium
                        )
                    }
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
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = SleekTextDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AlertBanner(info: CycleInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekPrimaryLight),
        border = BorderStroke(1.dp, SleekPrimaryVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = "هشدار",
                tint = SleekPrimaryDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "نزدیک شدن قاعدگی",
                    color = SleekPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${info.daysUntilNextPeriod} روز زمان باقیست.",
                    color = SleekPrimaryDark.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CycleHistoryChart(modifier: Modifier = Modifier) {
    val mockCycleLengths = listOf(28, 29, 27, 30, 26, 28)
    val average = mockCycleLengths.average().toInt()
    val max = mockCycleLengths.maxOrNull() ?: 30
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تحلیل چرخه", style = MaterialTheme.typography.titleSmall, color = SleekTextDark, fontWeight = FontWeight.Bold)
                Text("میانگین: $average روز", style = MaterialTheme.typography.labelSmall, color = SleekPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                mockCycleLengths.forEachIndexed { _, length ->
                    val heightRatio = length.toFloat() / max.toFloat()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$length",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SleekTextMedium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(heightRatio)
                                .background(if (length == average) SleekPrimary else SleekPrimaryLight, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: MahitaViewModel
) {
    if (!show) return

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsState()
    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    val daysBefore by viewModel.daysBefore.collectAsState()
    val averageCycleLength by viewModel.averageCycleLength.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val currentFont by viewModel.appFont.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                viewModel.clearSyncStatusMessage()
                onDismiss()
            }) {
                Text("تایید", color = SleekPrimary)
            }
        },
        title = {
            Text(
                "تنظیمات و همگام‌سازی ابری",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SleekTextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Dynamic Theme Selector
                Text(
                    "پوسته و هماهنگی تم‌ها (شیک و مینیمال)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekPrimary
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        Triple("COSMIC_BLOSSOM", "شکوفه کیهانی ✨", listOf(Color(0xFFE879A0), Color(0xFFB59DFF))),
                        Triple("SWEET_CANDY", "آبنبات شیرین ☀️", listOf(Color(0xFFEC4899), Color(0xFF14B8A6))),
                        Triple("MINT_ORCHID", "ارکیده نعنایی 🌿", listOf(Color(0xFF2DD4BF), Color(0xFFF472B6))),
                        Triple("SOFT_LAVENDER", "اسطوخودوس 🔮", listOf(Color(0xFFA78BFA), Color(0xFF2DD4BF)))
                    )
                    
                    themes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { (code, title, colors) ->
                                val isSelected = currentTheme == code
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SleekPrimary.copy(alpha = 0.15f) else SleekBorder.copy(alpha = 0.3f))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SleekPrimary else SleekBorder.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setAppTheme(code) }
                                        .padding(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            colors.forEach { swatchColor ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                                        .background(swatchColor)
                                                )
                                            }
                                        }
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SleekPrimary else SleekTextMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SleekBorder)

                // Section 1.5: Dynamic Font Selector
                Text(
                    "انتخاب قلم و فونت برنامه (جذاب و فانتزی) 🌸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekPrimary
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fontOptions = listOf(
                        "CLASSIC" to "شکوفه کلاسیک (خوانا) 🌸",
                        "CURSIVE" to "دست‌نویس جذاب و فانتزی ✨",
                        "SERIF" to "پرنسس لوکس و باکلاس 👑",
                        "SANS_SERIF" to "مینیمال مدرن و ظریف 🌿"
                    )
                    
                    fontOptions.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { (code, title) ->
                                val isSelected = currentFont == code
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SleekPrimary.copy(alpha = 0.15f) else SleekBorder.copy(alpha = 0.3f))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SleekPrimary else SleekBorder.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setAppFont(code) }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SleekPrimary else SleekTextMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SleekBorder)

                // Section 2: Push Notifications
                Text(
                    "یادآوری چرخه قاعدگی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekPrimary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("فعال‌سازی اعلان یادآور شروع", fontSize = 13.sp, color = SleekTextMedium)
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { viewModel.setNotificationEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SleekPrimary,
                            uncheckedThumbColor = SleekTextLight,
                            uncheckedTrackColor = SleekBorder
                        )
                    )
                }

                HorizontalDivider(color = SleekBorder)

                // Profile Configuration Section
                Text(
                    "پروفایل و طول چرخه قاعدگی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekPrimary
                )
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("نام کاربری: $userName", fontSize = 12.sp, color = SleekTextMedium)
                        Text("طول چرخه پیش‌فرض: $averageCycleLength روز", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = averageCycleLength.toFloat(),
                        onValueChange = { viewModel.setAverageCycleLength(it.toInt()) },
                        valueRange = 21f..45f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = SleekPrimary,
                            activeTrackColor = SleekPrimary,
                            inactiveTrackColor = SleekBorder
                        )
                    )
                }

                if (isNotificationEnabled) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("زمان یادآوری:", fontSize = 12.sp, color = SleekTextLight)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0, 1, 2, 3).forEach { day ->
                                val selected = daysBefore == day
                                val label = when (day) {
                                    0 -> "همان روز"
                                    1 -> "۱ روز قبل"
                                    2 -> "۲ روز قبل"
                                    3 -> "۳ روز قبل"
                                    else -> "$day روز قبل"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) SleekPrimary else SleekPrimaryLight)
                                        .clickable { viewModel.setDaysBefore(day) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else SleekPrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SleekBorder)

                // Section 2: Firebase Firestore Cloud Sync
                Text(
                    "پشتیبان‌گیری و همگام‌سازی ابری",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekPrimary
                )
                
                Text(
                    "با فعال‌سازی همگام‌سازی، کلیه اطلاعات چرخه‌های قاعدگی شما به‌صورت ایمن در سرور ابری Firebase ذخیره شده و در تمامی دستگاه‌هایتان همگام می‌شود.",
                    fontSize = 11.sp,
                    color = SleekTextMedium,
                    lineHeight = 16.sp
                )

                if (syncStatusMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekPrimaryLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = syncStatusMessage!!,
                            color = SleekPrimaryDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.syncWithCloud() },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (isSyncing) "در حال همگام‌سازی..." else "همگام‌سازی دستی با سرور",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PredictionNotificationCard(predictionInfo: PredictionInfo) {
    val predictedDate = predictionInfo.predictedNextStart
    val jalaliPrediction = predictedDate?.let { com.example.utils.JalaliCalendar.gregorianToJalali(it) }
    val jalaliPredictionStr = jalaliPrediction?.formatLong() ?: "نامشخص"
    
    val daysRemaining = predictionInfo.daysRemaining
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "پیش‌بینی هوشمند",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پیش‌بینی هوشمند سیکل بعدی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = if (predictionInfo.hasEnoughData) SleekPrimary else Color.Gray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (predictionInfo.hasEnoughData) "طول واقعی" else "طول فرضی",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = if (predictionInfo.hasEnoughData) {
                    "بر اساس ${predictionInfo.totalCyclesCalculated} دوره ثبت شده قبلی شما، طول متوسط چرخه قاعدگی شما ${predictionInfo.calculatedAverage} روز برآورد شد."
                } else {
                    "به دلیل محدودیت داده‌های چرخه‌ شما، زمان‌بندی فعلی بر اساس مقدار تنظیم‌شده در تنظیمات (${predictionInfo.calculatedAverage} روز) است."
                },
                fontSize = 11.5.sp,
                color = SleekTextMedium,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(0.5.dp, SleekBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تاریخ تخمینی شروع بعدی:",
                        fontSize = 10.sp,
                        color = SleekTextLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = jalaliPredictionStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextDark
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                daysRemaining < 0 -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                daysRemaining in 0..3 -> SleekPrimary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            daysRemaining < 0 -> "${-daysRemaining} روز تاخیر"
                            daysRemaining == 0 -> "شروع از امروز"
                            else -> "$daysRemaining روز دیگر"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysRemaining < 0 -> Color(0xFFEF4444)
                            daysRemaining in 0..3 -> SleekPrimaryDark
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

