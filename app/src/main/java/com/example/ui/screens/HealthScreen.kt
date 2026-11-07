package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: com.example.ui.viewmodel.MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val profileWeight by viewModel.userWeight.collectAsState()
    val waterIntake by viewModel.waterGlasses.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val today by viewModel.today.collectAsState()
    val todayLog = remember(logs, today) { logs.find { it.dateStr == today.toString() } }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Sleep and steps are stored per day; the form starts from what was saved today.
    var sleepHours by rememberSaveable(todayLog?.sleepHours) { mutableFloatStateOf(todayLog?.sleepHours ?: 0f) }
    var steps by rememberSaveable(todayLog?.steps) {
        mutableStateOf(todayLog?.steps?.let { com.example.utils.PersianNumbers.toPersianDigits(it) } ?: "")
    }
    var weight by rememberSaveable(profileWeight) { mutableStateOf(com.example.utils.PersianNumbers.toPersianDigits(profileWeight)) }

    fun saveAll() {
        val stepsValue = if (steps.isBlank()) null else com.example.utils.PersianNumbers.parseInt(steps, 0..100_000)
        val weightValue = if (weight.isBlank()) null else com.example.utils.PersianNumbers.parseDecimal(weight, 25f..250f)
        when {
            steps.isNotBlank() && stepsValue == null ->
                android.widget.Toast.makeText(context, "تعداد قدم باید عددی بین ۰ تا ۱۰۰٬۰۰۰ باشد.", android.widget.Toast.LENGTH_SHORT).show()
            weight.isNotBlank() && weightValue == null ->
                android.widget.Toast.makeText(context, "وزن را بین ۲۵ تا ۲۵۰ کیلوگرم وارد کنید.", android.widget.Toast.LENGTH_SHORT).show()
            else -> {
                viewModel.saveHealthToday(waterIntake, sleepHours.takeIf { it > 0f }, stepsValue)
                weightValue?.let { viewModel.setWeight(it) }
                android.widget.Toast.makeText(context, savedMessage(viewModel.isCloudSyncAvailable()), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("سلامتی و تندرستی", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // Welcome Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "گزارش سلامتی شما",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ثبت آب، خواب، وزن و قدم‌های امروز",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            WaterTrackerCard(waterIntake) { newWater ->
                viewModel.setWaterGlasses(newWater)
            }
            
            SleepTrackerCard(sleepHours) { sleepHours = it }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WeightCard(
                    weight = weight, 
                    onWeightChange = { weight = it }, 
                    onSaveClick = { saveAll() },
                    modifier = Modifier.weight(1f)
                )
                StepsCard(steps, { steps = it }, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { saveAll() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره اطلاعات امروز", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WaterTrackerCard(waterIntake: Int, onWaterChange: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE0F2FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalDrink, contentDescription = "آب", tint = Color(0xFF0284C7))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("مصرف آب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("هدف: ۸ لیوان در روز", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .size(44.dp)
                        .bounceClick { if (waterIntake > 0) onWaterChange(waterIntake - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "یک لیوان کمتر")
                }
                
                Text(
                    text = "${com.example.utils.PersianNumbers.toPersianDigits(waterIntake)} / ۸",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .size(44.dp)
                        .bounceClick { if (waterIntake < 16) onWaterChange(waterIntake + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "یک لیوان بیشتر", tint = Color(0xFF0284C7))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 1..8) {
                    Icon(
                        imageVector = Icons.Filled.LocalDrink,
                        contentDescription = null,
                        tint = if (i <= waterIntake) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SleepTrackerCard(sleepHours: Float, onSleepChange: (Float) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEDE9FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Bedtime, contentDescription = "خواب", tint = Color(0xFF7C3AED))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("خواب شب گذشته", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = when {
                            sleepHours <= 0f -> "ثبت نشده"
                            sleepHours >= 7f -> "کافی"
                            else -> "کمتر از ۷ ساعت توصیه‌شده"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            sleepHours <= 0f -> MaterialTheme.colorScheme.onSurfaceVariant
                            sleepHours >= 7f -> Color(0xFF059669)
                            else -> Color(0xFFD97706)
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${com.example.utils.PersianNumbers.formatDecimal(sleepHours)} ساعت",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF7C3AED)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Slider(
                value = sleepHours,
                onValueChange = { onSleepChange(it) },
                valueRange = 0f..12f,
                steps = 23,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF7C3AED),
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WeightCard(
    weight: String, 
    onWeightChange: (String) -> Unit, 
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MonitorWeight, contentDescription = "وزن", tint = Color(0xFFD97706))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("وزن (کیلوگرم)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Text("ذخیره", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun StepsCard(steps: String, onStepsChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFDCFCE7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = "قدم‌ها", tint = Color(0xFF16A34A))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("قدم‌های امروز", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = steps,
                onValueChange = { onStepsChange(it.take(6)) },
                placeholder = { Text("مثلاً ۴۵۰۰", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF16A34A)),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
