package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val cycleInfo by viewModel.cycleInfo.collectAsState()
    
    // Determine the phase name string for Persian displays
    val currentPhaseText = when {
        cycleInfo.isPeriod || cycleInfo.daysUntilNextPeriod in 24..28 -> "قاعدگی"
        cycleInfo.isFertile -> "تخمک‌گذاری" 
        cycleInfo.daysUntilNextPeriod in 16..23 -> "فولیکولی"
        else -> "لوتئال"
    }
    
    val currentPhaseKey = when {
        cycleInfo.isPeriod || cycleInfo.daysUntilNextPeriod in 24..28 -> "period"
        cycleInfo.isFertile -> "ovulation"
        cycleInfo.daysUntilNextPeriod in 16..23 -> "follicular"
        else -> "luteal"
    }

    val recommendedMusic = getPhaseMusic(currentPhaseKey)

    // Interactive Music Player states
    var currentPlayingMusic by remember { mutableStateOf<MusicItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionSeconds by remember { mutableStateOf(0) }
    val durationSeconds = 180 // 3 minutes track standard
    var isFavorited by remember { mutableStateOf(false) }

    // Sound Playback ticker simulation
    LaunchedEffect(isPlaying, currentPlayingMusic) {
        if (isPlaying && currentPlayingMusic != null) {
            while (isPlaying && currentPlayingMusic != null) {
                delay(1000)
                if (currentPositionSeconds < durationSeconds) {
                    currentPositionSeconds += 1
                } else {
                    // Automove next track logic
                    val currentIndex = recommendedMusic.indexOf(currentPlayingMusic)
                    if (currentIndex != -1 && currentIndex < recommendedMusic.lastIndex) {
                        currentPlayingMusic = recommendedMusic[currentIndex + 1]
                    } else {
                        currentPlayingMusic = recommendedMusic.firstOrNull()
                    }
                    currentPositionSeconds = 0
                }
            }
        }
    }

    val togglePlayPause: (MusicItem) -> Unit = { music ->
        if (currentPlayingMusic == music) {
            isPlaying = !isPlaying
        } else {
            currentPlayingMusic = music
            currentPositionSeconds = 0
            isPlaying = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("حال خوب شما 🌸", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Welcome / Current Phase Banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "فاز فعلی: $currentPhaseText",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = getPhaseGreeting(currentPhaseKey),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                item {
                    // Inspirational Quote Section
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FormatQuote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "جمله امروز برای شما",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = getPhaseQuote(currentPhaseKey),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 26.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "موزیک‌های پیشنهادی این دوره",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recommendedMusic.forEach { music ->
                            val isThisPlaying = currentPlayingMusic == music
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isThisPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isThisPlaying) 1.5.dp else 1.dp,
                                    color = if (isThisPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { togglePlayPause(music) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = if (isThisPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = music.title,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = music.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    
                                    if (isThisPlaying) {
                                        // Animated pulsing block inside selected row
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                                val pulseHeight1 by infiniteTransition.animateFloat(
                                                    initialValue = 4f, targetValue = 12f,
                                                    animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
                                                    label = ""
                                                )
                                                val pulseHeight2 by infiniteTransition.animateFloat(
                                                    initialValue = 12f, targetValue = 4f,
                                                    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
                                                    label = ""
                                                )
                                                Box(modifier = Modifier.size(3.dp, pulseHeight1.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                                Box(modifier = Modifier.size(3.dp, pulseHeight2.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(130.dp)) // Extra cushion space for bottom player card
                }
            }

            // Beautiful Media Player Slide-up Card
            AnimatedVisibility(
                visible = currentPlayingMusic != null,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                currentPlayingMusic?.let { music ->
                    // Rotate vinyl animation
                    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(12000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "disk_rotation"
                    )
                    val currentRotation = if (isPlaying) rotationAngle else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f)
                        ),
                        elevation = CardDefaults.cardElevation(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top minimal handle
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rotating CD Disk Album Graphic
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .rotate(currentRotation)
                                        .background(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Innermost hole
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = music.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = music.artist,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Heart Button
                                IconButton(onClick = { isFavorited = !isFavorited }) {
                                    Icon(
                                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        tint = if (isFavorited) Color(0xFFFF4C4C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentDescription = "علاقه‌مندی"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Playback Timeline Slider
                            val formattedCurrent = String.format("%02d:%02d", currentPositionSeconds / 60, currentPositionSeconds % 60)
                            val formattedTotal = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)

                            Slider(
                                value = currentPositionSeconds.toFloat(),
                                onValueChange = { currentPositionSeconds = it.toInt() },
                                valueRange = 0f..durationSeconds.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formattedCurrent,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = formattedTotal,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Controller row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close button
                                IconButton(onClick = {
                                    isPlaying = false
                                    currentPlayingMusic = null
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "بستن پلیر", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f))
                                }

                                // Skip Previous
                                IconButton(onClick = {
                                    val currentIndex = recommendedMusic.indexOf(music)
                                    if (currentIndex != -1 && currentIndex > 0) {
                                        currentPlayingMusic = recommendedMusic[currentIndex - 1]
                                    } else {
                                        currentPlayingMusic = recommendedMusic.lastOrNull()
                                    }
                                    currentPositionSeconds = 0
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipPrevious,
                                        contentDescription = "قبلی",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Main Play/Pause circular button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable { isPlaying = !isPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "پخش/توقف",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Skip Next
                                IconButton(onClick = {
                                    val currentIndex = recommendedMusic.indexOf(music)
                                    if (currentIndex != -1 && currentIndex < recommendedMusic.lastIndex) {
                                        currentPlayingMusic = recommendedMusic[currentIndex + 1]
                                    } else {
                                        currentPlayingMusic = recommendedMusic.firstOrNull()
                                    }
                                    currentPositionSeconds = 0
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "بعدی",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Repeat simulated block
                                IconButton(onClick = { currentPositionSeconds = 0 }) {
                                    Icon(Icons.Filled.Replay, contentDescription = "تکرار", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MusicItem(val title: String, val artist: String)

fun getPhaseGreeting(phase: String): String {
    return when (phase) {
        "period" -> "بدن تو در حال استراحت و بازسازی است. با خودت مهربان باش و به صدای درونت گوش بده. یک نوشیدنی گرم و استراحت عمیق برای تو معجزه می‌کند."
        "follicular" -> "انرژی در حال بازگشت است! زمان مناسبی برای برنامه‌ریزی، شروع پروژه‌های جدید و دیدار با دوستان است. از این شادابی لذت ببر."
        "ovulation" -> "تو در اوج انرژی و درخشش هستی! قدرت ارتباطی و خلاقیت تو اکنون در بالاترین سطح است. بهترین زمان برای کارهای جسورانه."
        else -> "بدن تو به آرامی در حال آماده شدن برای استراحت است. کاهش سرعت و انجام کارهای آرام‌بخش مانند مدیتیشن یا یک حمام گرم بسیار مفید است."
    }
}

fun getPhaseQuote(phase: String): String {
    return when (phase) {
        "period" -> "«استراحت کردن اتلاف وقت نیست، بلکه شارژ کردن دوباره روحت است برای پروازهای بلندتر.»"
        "follicular" -> "«هر طلوع، دعوتی است برای شروعی دوباره و خلق زیبایی‌های تازه.»"
        "ovulation" -> "«تو قدرتمندتر از آن چیزی هستی که در آینه می‌بینی؛ جهان منتظر تابش نور توست!»"
        else -> "«آرامش پاداش پذیرش است. به جریان زندگی اعتماد کن و اجازه بده همه چیز در زمان خودش رخ دهد.»"
    }
}

fun getPhaseMusic(phase: String): List<MusicItem> {
    return when (phase) {
        "period" -> listOf(
            MusicItem("پیانو آرام‌بخش (Relaxing Piano)", "موسیقی بی‌کلام آرام"),
            MusicItem("صدای باران و جنگل (Rain & Nature)", "صداهای طبیعت"),
            MusicItem("امواج دلنشین (Ambient Waves)", "تراپی صوتی")
        )
        "follicular" -> listOf(
            MusicItem("آهنگ‌های شاداب صبحگاهی (Morning Joy)", "پاپ ملایم و انرژی‌بخش"),
            MusicItem("شروع تازه (Fresh Start)", "آکوستیک گیتار و آواز روشن"),
            MusicItem("گام‌های مثبت (Positive Vibes)", "الکترونیک ملایم شاد")
        )
        "ovulation" -> listOf(
            MusicItem("انرژی بی‌نهایت (High Energy)", "پاپ، دنس و شاد"),
            MusicItem("قدرت درون (Empowered)", "ریمیکس‌های پرانرژی و بیت‌دار"),
            MusicItem("شادی محض (Pure Joy)", "موسیقی ریتمیک شاد و پرشور")
        )
        else -> listOf(
            MusicItem("رهاسازی تنش‌ها (Lo-Fi Chill)", "بیت‌های لوفای و آرام"),
            MusicItem("مدیتیشن عصرگاهی (Evening Meditate)", "موسیقی فرکانس‌های درمانی"),
            MusicItem("آوای آرامش (Calm Acoustics)", "نواختن ملایم سازهای زهی")
        )
    }
}
