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
            
            CycleHistoryChart(averageCycleLength = averageCycleLength, modifier = Modifier.fillMaxWidth())
            
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
    var isPressed by remember { mutableStateOf(false) }
    var rotationOffset by remember { mutableStateOf(0f) }

    val interactionScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "interactionScale"
    )

    val localSleekPrimary = SleekPrimary
    val localSleekPrimaryVariant = SleekPrimaryVariant
    val localSleekBorder = SleekBorder
    
    val animatedSweepAngle by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (info.currentDay / cycleLength.toFloat()) * 360f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1800, 
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "sweepAngle"
    )
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val ambientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(20000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "ambientRotation"
    )
    
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
  
    Box(
        modifier = modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false; rotationOffset = 0f },
                    onDragCancel = { isPressed = false; rotationOffset = 0f },
                    onDrag = { change, dragAmount -> 
                        rotationOffset += dragAmount.x / 3f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val baseRotation = ambientRotation + rotationOffset
        
        // 3D-like glow shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .aspectRatio(1f)
                .scale(interactionScale * 1.05f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            localSleekPrimary.copy(alpha = pulseGlow * 0.4f),
                            SleekSecondary.copy(alpha = pulseGlow * 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .scale(interactionScale)
                .graphicsLayer { rotationZ = baseRotation * 0.4f }
        ) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth * 2) / 2
            
            // Outer 3D faint orbit
            drawCircle(
                color = localSleekPrimaryVariant.copy(alpha = 0.1f),
                radius = radius + (strokeWidth * 1.5f),
                style = Stroke(width = strokeWidth * 0.3f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 25f)))
            )
            
            // Background track (multi-layered)
            drawCircle(
                color = localSleekBorder.copy(alpha = 0.4f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius - strokeWidth * 0.1f,
                style = Stroke(width = strokeWidth * 0.8f)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .scale(interactionScale)
                .graphicsLayer { rotationZ = rotationOffset }
        ) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth * 2) / 2
            
            val gradientColors = if (info.isPms) {
                listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
            } else if (info.isPeriod) {
                listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
            } else {
                listOf(Color(0xFFA78BFA), Color(0xFF818CF8))
            }
            
            // 3D Shadow under the main arc
            drawArc(
                color = Color.Black.copy(alpha = 0.15f),
                startAngle = -90f + 2f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Main progress arc
            drawArc(
                brush = Brush.sweepGradient(colors = gradientColors, center = androidx.compose.ui.geometry.Offset(size.minDimension/2, size.minDimension/2)),
                startAngle = -90f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
            )

            // Sparkle dot overlay
            val angleRad = Math.toRadians((animatedSweepAngle - 90).toDouble())
            val centerOffset = center
            val dotX = centerOffset.x + radius * Math.cos(angleRad).toFloat()
            val dotY = centerOffset.y + radius * Math.sin(angleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.6f,
                center = androidx.compose.ui.geometry.Offset(dotX, dotY),
                blendMode = androidx.compose.ui.graphics.BlendMode.Overlay
            )
        }
 
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(interactionScale)
        ) {
            Text(
                text = "روز ${info.currentDay} سیکل",
                style = MaterialTheme.typography.labelMedium,
                color = if (info.isPms) Color(0xFFD97706) else localSleekPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (info.isPeriod) "پریود" else if (info.isFertile) "پنجره باروری" else if (info.isPms) "پیش‌قاعدگی (PMS)" else "روز معمولی",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = SleekTextMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = if (info.isFertile) SleekPrimary else if (info.isPms) Color(0xFFF59E0B) else SleekBorder, 
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (info.isFertile) "بارداری: زیاد" else if (info.isPms) "بارداری: کم" else "بارداری: کم / متوسط",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (info.isFertile || info.isPms) Color.White else SleekTextMedium,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("پریود بعدی", style = MaterialTheme.typography.labelSmall, color = SleekTextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${info.daysUntilNextPeriod} روز", fontSize = 13.sp, color = SleekPrimaryDark, fontWeight = FontWeight.Black)
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تخمک‌گذاری", style = MaterialTheme.typography.labelSmall, color = SleekTextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val daysToOvulation = if (info.currentDay <= 14) 14 - info.currentDay else (28 - info.currentDay) + 14
                Text("حدود $daysToOvulation روز", fontSize = 13.sp, color = Indigo500, fontWeight = FontWeight.Black)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("دوره PMS", style = MaterialTheme.typography.labelSmall, color = SleekTextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val pmsText = if (info.isPms) {
                    "فعال 🌪️"
                } else if (info.daysUntilNextPeriod > 6) {
                    "${info.daysUntilNextPeriod - 6} روز دیگر"
                } else {
                    "پایان یافته"
                }
                Text(pmsText, fontSize = 13.sp, color = Amber500, fontWeight = FontWeight.Black)
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
fun CycleHistoryChart(averageCycleLength: Int = 28, modifier: Modifier = Modifier) {
    val mockCycleLengths = remember(averageCycleLength) {
        listOf(
            averageCycleLength - 1,
            averageCycleLength + 1,
            averageCycleLength - 2,
            averageCycleLength + 2,
            averageCycleLength,
            averageCycleLength - 1
        ).map { it.coerceIn(21, 40) }
    }
    val months = listOf("دی", "بهمن", "اسفند", "فروردین", "اردیبهشت", "خرداد")
    val average = mockCycleLengths.average().toInt()
    val maxVal = (mockCycleLengths.maxOrNull() ?: 30).coerceAtLeast(32)
    val minVal = (mockCycleLengths.minOrNull() ?: 24).coerceAtMost(24) - 2
    
    val pColor = SleekPrimary
    val pLightColor = SleekPrimaryLight
    val bColor = SleekBorder

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, bColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "نوسانات ۶ ماه گذشته (طرح Recharts)",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "نمودار خطی هماهنگی و منظم بودن دوره‌ها",
                        fontSize = 10.sp,
                        color = SleekTextMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pLightColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "میانگین: $average روز",
                        fontSize = 10.sp,
                        color = SleekPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Recharts style Area/Line Chart using Compose Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val pointsCount = mockCycleLengths.size
                    
                    // Draw horizontal dotted gridlines
                    val gridLinesCount = 3
                    val gridSpacing = height / (gridLinesCount + 1)
                    for (i in 1..gridLinesCount) {
                        val y = i * gridSpacing
                        drawLine(
                            color = bColor.copy(alpha = 0.4f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Calculate point offsets
                    val stepX = width / (pointsCount - 1)
                    val points = mockCycleLengths.mapIndexed { idx, length ->
                        val ratio = (length - minVal).toFloat() / (maxVal - minVal).toFloat()
                        val paddingY = 20f
                        val usableHeight = height - paddingY * 2
                        val y = height - paddingY - (ratio * usableHeight)
                        val x = idx * stepX
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    // Create smooth cubic path
                    val linePath = androidx.compose.ui.graphics.Path()
                    if (points.isNotEmpty()) {
                        linePath.moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val controlPoint1 = androidx.compose.ui.geometry.Offset((prev.x + curr.x) / 2, prev.y)
                            val controlPoint2 = androidx.compose.ui.geometry.Offset((prev.x + curr.x) / 2, curr.y)
                            linePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                curr.x, curr.y
                            )
                        }
                    }

                    // Fill gradient under the curve (Area Chart)
                    val areaPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(linePath)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                pColor.copy(alpha = 0.25f),
                                pColor.copy(alpha = 0.00f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw the primary line cap
                    drawPath(
                        path = linePath,
                        color = pColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Companion.Round
                        )
                    )

                    // Draw circular anchors/dots
                    points.forEach { point ->
                        drawCircle(
                            color = pColor.copy(alpha = 0.15f),
                            radius = 7.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = pColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.5.dp.toPx(),
                            center = point
                        )
                    }
                }

                // Values display
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    mockCycleLengths.forEachIndexed { _, length ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val ratio = (length - minVal).toFloat() / (maxVal - minVal).toFloat()
                            val topPaddingDp = (100 - (ratio * 70).coerceIn(0f, 100f)).dp
                            
                            Box(
                                modifier = Modifier
                                    .padding(top = topPaddingDp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .border(0.5.dp, SleekBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$length روز",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Month labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        fontSize = 9.sp,
                        color = SleekTextMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
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
    val userWeight by viewModel.userWeight.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val currentFont by viewModel.appFont.collectAsState()
    val lastPeriodDate by viewModel.lastPeriodDate.collectAsState()

    var editWeight by remember { mutableStateOf(userWeight) }
    LaunchedEffect(userWeight) {
        editWeight = userWeight
    }

    var editStartDate by remember { mutableStateOf(lastPeriodDate) }
    var editDuration by remember { mutableStateOf(averageCycleLength.toString()) }

    LaunchedEffect(lastPeriodDate) {
        if (editStartDate.isBlank() && lastPeriodDate.isNotBlank()) {
            editStartDate = lastPeriodDate
        }
    }
    LaunchedEffect(averageCycleLength) {
        editDuration = averageCycleLength.toString()
    }

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

                // Profile Component Card for Menstrual Cycle & Personal Info Sync
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تنظیمات اطلاعات چرخه قاعدگی هانیتا",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SleekPrimary
                            )
                            Text(
                                text = "کاربر: $userName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextMedium
                            )
                        }

                        // 1. Weight Form Field
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editWeight,
                                onValueChange = { editWeight = it },
                                label = { Text("وزن فعلی شما (kg)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.setWeight(editWeight)
                                    android.widget.Toast.makeText(context, "وزن شما با موفقیت در فایربیس ذخیره شد.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(45.dp)
                            ) {
                                Text("ذخیره وزن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        HorizontalDivider(color = SleekBorder.copy(alpha = 0.4f))

                        // 2. Menstrual Cycle Form Fields
                        Text(
                            text = "فرم اطلاعات شروع و طول چرخه (سیستم هوشمند)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = SleekTextMedium
                        )

                        OutlinedTextField(
                            value = editStartDate,
                            onValueChange = { editStartDate = it },
                            label = { Text("تاریخ شروع آخرین قاعدگی (YYYY-MM-DD)", fontSize = 11.sp) },
                            placeholder = { Text("مثال: 2026-06-13", fontSize = 11.sp, color = SleekTextLight) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekPrimary,
                                unfocusedBorderColor = SleekBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "تاریخ آخرین قاعدگی",
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editDuration,
                                onValueChange = { editDuration = it },
                                label = { Text("طول کل چرخه (روز)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "طول دوره",
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )

                            Button(
                                onClick = {
                                    val durationInt = editDuration.toIntOrNull()
                                    if (durationInt == null || durationInt !in 21..45) {
                                        android.widget.Toast.makeText(context, "لطفاً طول چرخه معتبر بین ۲۱ تا ۴۵ روز وارد کنید.", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (editStartDate.isNotBlank()) {
                                        try {
                                            java.time.LocalDate.parse(editStartDate, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "فرمت تاریخ شروع نامعتبر است (باید YYYY-MM-DD باشد)", android.widget.Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                    }
                                    viewModel.updateCycleSettings(editStartDate, durationInt)
                                    android.widget.Toast.makeText(context, "اطلاعات چرخه شما ذخیره و در فایربیس ابری همگام‌سازی شد.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(45.dp).weight(1.3f)
                            ) {
                                Text("ذخیره چرخه در فایربیس", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
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

