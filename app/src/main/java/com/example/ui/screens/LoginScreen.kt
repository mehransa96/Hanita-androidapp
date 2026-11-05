package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate
import java.time.LocalDate

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, Int, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    
    val todayJalali = remember { JalaliCalendar.gregorianToJalali(LocalDate.now()) }
    var selectedYear by remember { mutableIntStateOf(todayJalali.year) }
    var selectedMonth by remember { mutableIntStateOf(todayJalali.month) }
    var selectedDay by remember { mutableIntStateOf(todayJalali.day) }
    var averageCycleLength by remember { mutableFloatStateOf(28f) }
    
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    val isDark = ThemeProvider.isDark
    val themeController = ThemeProvider.controller

    val bgGradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
            Color(0xFF2C2554)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.background
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = bgGradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.08f)
        val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = if (isDark) 0.12f else 0.06f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = primaryGlow,
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = secondaryGlow,
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }

        // Quick theme toggle button in top corner
        IconButton(
            onClick = { themeController.toggleMode() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDark) "تغییر به حالت روشن" else "تغییر به حالت تاریک",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .alpha(alphaAnimation)
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 16.dp else 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Application Logo / Header
                Text(
                    text = "هانیتا",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "همراه سلامتی و تندرستی تو",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Active Step indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val active = step == i
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (active) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    },
                    label = "step"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> StepWelcome(
                            name = name,
                            onNameChange = { name = it },
                            onNext = { step = 2 }
                        )
                        2 -> StepPeriodDate(
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedDay = selectedDay,
                            onYearChange = { selectedYear = it },
                            onMonthChange = { selectedMonth = it },
                            onDayChange = { selectedDay = it },
                            onBack = { step = 1 },
                            onNext = { step = 3 }
                        )
                        3 -> StepCycleLength(
                            averageCycleLength = averageCycleLength,
                            onCycleLengthChange = { averageCycleLength = it },
                            onBack = { step = 2 },
                            onSubmit = {
                                val selectedLocalDate = JalaliCalendar.jalaliToGregorian(selectedYear, selectedMonth, selectedDay)
                                onLoginSuccess(name, averageCycleLength.toInt(), selectedLocalDate.toString())
                            }
                        )
                    }
                }
            }
        }
}
}

@Composable
fun StepWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    onNameChange(account.displayName ?: "کاربر عزیز")
                    onNext()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Step 1 Welcome Illustration using elegant canvas petals style
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            // Beautiful modern flower blossom
            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.15f),
                radius = size.width * 0.45f,
                center = center
            )
            drawCircle(
                color = Color(0xFFE879A0).copy(alpha = 0.2f),
                radius = size.width * 0.32f,
                center = center
            )
            for (i in 0 until 6) {
                val angle = (i * 60) * (Math.PI / 180)
                val distance = size.width * 0.22f
                val leafCenter = Offset(
                    (center.x + distance * Math.cos(angle)).toFloat(),
                    (center.y + distance * Math.sin(angle)).toFloat()
                )
                drawCircle(
                    color = Color(0xFFE879A0).copy(alpha = 0.6f),
                    radius = size.width * 0.14f,
                    center = leafCenter
                )
            }
            drawCircle(
                color = Color.White,
                radius = size.width * 0.12f,
                center = center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "خوش آمدی!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "برای شخصی‌سازی پیش‌بینی‌ها، لطفاً با حساب کاربری خود وارد شوید یا نام خود را بنویسید:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("ورود با حساب گوگل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "یا",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("ورود مهمان (نام شما)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("next_step_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            ),
            enabled = name.isNotBlank()
        ) {
            Text("مرحله بعدی", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun StepPeriodDate(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    // Calculate maximum days in current solar hijri selection
    val isLeap = remember(selectedYear) {
        val matches = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        matches.contains(selectedYear % 33)
    }
    val maxDays = when {
        selectedMonth <= 6 -> 31
        selectedMonth <= 11 -> 30
        else -> if (isLeap) 30 else 29
    }

    // Adjust selected day if it exceeds the max days of the new month
    LaunchedEffect(selectedMonth, selectedYear) {
        if (selectedDay > maxDays) {
            onDayChange(maxDays)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Step 2 Calendar sheet illustration using elegant Canvas drawings
        Canvas(modifier = Modifier.size(110.dp)) {
            val w = size.width
            val h = size.height
            // Draw a stylish wireframe mini calendar
            drawRoundRect(
                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f)
            )
            // Header bar
            drawRoundRect(
                color = Color(0xFFE879A0),
                size = androidx.compose.ui.geometry.Size(w, h * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Binder spiral rings
            for (i in 1..4) {
                val rX = w * 0.2f * i
                drawCircle(color = Color(0xFF0F0C20), radius = 6f, center = Offset(rX, h * 0.28f))
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(rX, h * 0.1f),
                    end = Offset(rX, h * 0.35f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            // Calendar active grid points
            for (col in 0..2) {
                for (row in 0..2) {
                    val pX = w * 0.25f + col * w * 0.25f
                    val pY = h * 0.45f + row * h * 0.18f
                    val isFirst = col == 1 && row == 1
                    drawCircle(
                        color = if (isFirst) Color(0xFFE879A0) else Color.White.copy(alpha = 0.3f),
                        radius = if (isFirst) 10f else 6f,
                        center = Offset(pX, pY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "آخرین شروع قاعدگی",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "اولین روز آخرین پریود خودت رو ثبت کن:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Customizable Inline Persian Roller Pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Day selector Column
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("روز", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                RollerControls(
                    value = selectedDay,
                    onIncrement = { if (selectedDay < maxDays) onDayChange(selectedDay + 1) else onDayChange(1) },
                    onDecrement = { if (selectedDay > 1) onDayChange(selectedDay - 1) else onDayChange(maxDays) }
                )
            }

            // Month selector Column
            Column(modifier = Modifier.weight(1.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ماه", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                RollerTextControls(
                    valueText = months[selectedMonth - 1],
                    onIncrement = { if (selectedMonth < 12) onMonthChange(selectedMonth + 1) else onMonthChange(1) },
                    onDecrement = { if (selectedMonth > 1) onMonthChange(selectedMonth - 1) else onMonthChange(12) }
                )
            }

            // Year selector Column
            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("سال", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                RollerControls(
                    value = selectedYear,
                    onIncrement = { onYearChange(selectedYear + 1) },
                    onDecrement = { onYearChange(selectedYear - 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "تاریخ انتخابی شما: $selectedDay ${months[selectedMonth - 1]} $selectedYear",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("بازگشت", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1.5f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("مرحله بعدی", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun StepCycleLength(
    averageCycleLength: Float,
    onCycleLengthChange: (Float) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Step 3 Slider Arc Speedometer illustration using elegant Canvas drawings
        val trackBgColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        val trackActiveColor = MaterialTheme.colorScheme.primary
        val trackCenterColor = MaterialTheme.colorScheme.secondary
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawArc(
                    color = trackBgColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                    size = size
                )
                val fraction = (averageCycleLength - 21f) / (45f - 21f)
                drawArc(
                    color = trackActiveColor,
                    startAngle = 180f,
                    sweepAngle = 180f * fraction,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                    size = size
                )
                // Center node
                drawCircle(color = trackCenterColor, radius = size.width * 0.15f, center = center)
            }
            Text(
                averageCycleLength.toInt().toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "طول چرخه قاعدگی",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "فاصله متوسط اول هر پریود تا پریود بعدی شما چند روز است؟ (فاصله طبیعی بین ۲۱ تا ۴۵ روز است)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "${averageCycleLength.toInt()} روز",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )

        Slider(
            value = averageCycleLength,
            onValueChange = onCycleLengthChange,
            valueRange = 21f..45f,
            steps = 23,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.padding(horizontal = 16.dp).testTag("cycle_length_slider")
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("بازگشت", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .weight(1.5f)
                    .height(54.dp)
                    .testTag("onboarding_finish_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("شروع برنامه", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun RollerControls(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "کاهش", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = value.toString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "افزایش", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun RollerTextControls(
    valueText: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "کاهش", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = valueText,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "افزایش", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
