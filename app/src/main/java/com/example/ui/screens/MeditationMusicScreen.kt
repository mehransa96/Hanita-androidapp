package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MeditationTrack(val id: String, val title: String, val artist: String, val categoryId: String, val durationSec: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationMusicScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showLyriaModal by remember { mutableStateOf(false) }
    val categories = listOf(
        "meditation" to "🧘 مدیتیشن عمیق",
        "nature" to "🌲 آوای طبیعت",
        "healing" to "✨ فرکانس‌ درمانی",
        "lofi" to "🎧 بیت‌های لوفای"
    )

    val tracks = remember {
        listOf(
            // Meditation
            MeditationTrack("m1", "مدیتیشن ذهن‌آگاهی و تنفس", "کلینیک آرامش", "meditation", 300),
            MeditationTrack("m2", "رهاسازی اضطراب و افکار منفی", "استودیو هانیتا", "meditation", 240),
            MeditationTrack("m3", "خواب عمیق و آرامش شبانه", "نوای مهر", "meditation", 360),
            // Nature
            MeditationTrack("n1", "جنگل بارانی و رودخانه جاری", "آواهای طبیعت", "nature", 180),
            MeditationTrack("n2", "امواج ساحلی در غروب آفتاب", "آواهای طبیعت", "nature", 200),
            MeditationTrack("n3", "آواز پرندگان در سپیده‌دم کوهستان", "طبیعت آرام", "nature", 150),
            // Healing
            MeditationTrack("h1", "فرکانس ۵۲۸ هرتز (ترمیم و عشق)", "تراپی صوتی هانیتا", "healing", 420),
            MeditationTrack("h2", "فرکانس ۴۳۲ هرتز (کاهش تنش و ضربان قلب)", "نوای کیهان", "healing", 480),
            MeditationTrack("h3", "فرکانس نویز صورتی برای تمدد اعصاب", "آکوستیک درمانی", "healing", 310),
            // Lo-Fi
            MeditationTrack("l1", "چای گرم و باران پشت پنجره", "کافه لوفای", "lofi", 140),
            MeditationTrack("l2", "کتاب‌خوانی در نیمه‌شب", "لوفای ملایم", "lofi", 160),
            MeditationTrack("l3", "گذر آرام زمان در باغ بهاری", "ریلکس بیتس", "lofi", 175)
        )
    }

    var selectedCategoryId by remember { mutableStateOf("meditation") }
    val filteredTracks = remember(selectedCategoryId) {
        tracks.filter { it.categoryId == selectedCategoryId }
    }

    // Media Player States
    var currentTrack by remember { mutableStateOf<MeditationTrack?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressSec by remember { mutableStateOf(0) }
    var isLooping by remember { mutableStateOf(false) }

    // Interactive Breath Trainer states
    var breathPacingEnabled by remember { mutableStateOf(false) }
    var breathStateText by remember { mutableStateOf("آماده") }
    var breathPulseScale by remember { mutableStateOf(1.0f) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val mediaPlayer = remember { android.media.MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.release()
            } catch(e: Exception) {}
        }
    }

    LaunchedEffect(currentTrack) {
        try {
            mediaPlayer.reset()
            if (currentTrack != null) {
                val songIndex = when (currentTrack?.categoryId) {
                    "meditation" -> 1
                    "nature" -> 2
                    "healing" -> 3
                    "lofi" -> 4
                    else -> 1
                }
                val url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$songIndex.mp3"
                mediaPlayer.setDataSource(context, android.net.Uri.parse(url))
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    if (isPlaying) it.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(isPlaying) {
        try {
            if (isPlaying) {
                mediaPlayer.start()
            } else {
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
            }
        } catch (e: Exception) {}
    }

    // Breathing Trainer Engine
    LaunchedEffect(breathPacingEnabled) {
        if (breathPacingEnabled) {
            while (breathPacingEnabled) {
                // Inhale 4 seconds
                breathStateText = "دم عمیق (Inhale) 🌬️"
                animateTo(1.4f) { breathPulseScale = it }
                delay(4000)
                if (!breathPacingEnabled) break

                // Hold 4 seconds
                breathStateText = "حبس نفس (Hold) 🤫"
                delay(4000)
                if (!breathPacingEnabled) break

                // Exhale 4 seconds
                breathStateText = "بازدم آرام (Exhale) 🍃"
                animateTo(1.0f) { breathPulseScale = it }
                delay(4000)
                if (!breathPacingEnabled) break
            }
        } else {
            breathStateText = "شروع تمرین تنفس"
            breathPulseScale = 1.0f
        }
    }

    // Playback simulator
    LaunchedEffect(isPlaying, currentTrack) {
        if (isPlaying && currentTrack != null) {
            while (isPlaying && currentTrack != null) {
                delay(1000)
                val active = currentTrack ?: break
                if (currentProgressSec < active.durationSec) {
                    currentProgressSec++
                } else {
                    if (isLooping) {
                        currentProgressSec = 0
                    } else {
                        // Go to next track
                        val currentIndex = filteredTracks.indexOf(active)
                        if (currentIndex != -1 && currentIndex < filteredTracks.lastIndex) {
                            currentTrack = filteredTracks[currentIndex + 1]
                        } else {
                            currentTrack = filteredTracks.firstOrNull()
                        }
                        currentProgressSec = 0
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("موزیک و مدیتیشن آرامش 🧖‍♀️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { showLyriaModal = true }) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "تولید موسیقی با Lyria", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                // Topic card
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "پرورش آرامش و ثبات ذهنی",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "انتخابی بی‌نظیر از بهترین نواسازهای دنیا برای دوره‌های کاهش خستگی، تسکین دردهای قاعدگی و شادابی روحیه زنانه شما.",
                                    fontSize = 11.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Breathing interactive exercise widget
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Air,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        "تمرین هوشمند تنفس قاعدگی (۴-۴-۴)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Switch(
                                    checked = breathPacingEnabled,
                                    onCheckedChange = { breathPacingEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Breathing Visual Circle
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .scale(breathPulseScale)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = breathStateText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Category selection LazyRow
                item {
                    Text(
                        "دسته بندی های موسیقی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (id, title) ->
                            val isSelected = selectedCategoryId == id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = id },
                                label = { Text(title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Audio music track list
                items(filteredTracks, key = { it.id }) { track ->
                    val isCurrent = currentTrack == track
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick {
                                if (isCurrent) {
                                    isPlaying = !isPlaying
                                } else {
                                    currentTrack = track
                                    currentProgressSec = 0
                                    isPlaying = true
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCurrent && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = track.artist,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:%02d", track.durationSec / 60, track.durationSec % 60),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Beautiful mini-player popping up at the bottom
            AnimatedVisibility(
                visible = currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                currentTrack?.let { track ->
                    // vinyl rotating animation
                    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(14000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = ""
                    )
                    val rotation = if (isPlaying) rotationAngle else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Album CD rotating visualizer
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .rotate(rotation)
                                        .background(
                                            Brush.sweepGradient(
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
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isLooping = !isLooping }) {
                                        Icon(
                                            imageVector = Icons.Filled.Repeat,
                                            tint = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            contentDescription = "تکرار"
                                        )
                                    }
                                    IconButton(onClick = {
                                        isPlaying = false
                                        currentTrack = null
                                    }) {
                                        Icon(Icons.Filled.Close, contentDescription = "بستن", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // ProgressBar Selector
                            Slider(
                                value = currentProgressSec.toFloat(),
                                onValueChange = { currentProgressSec = it.toInt() },
                                valueRange = 0f..track.durationSec.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
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
                                    text = String.format("%02d:%02d", currentProgressSec / 60, currentProgressSec % 60),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = String.format("%02d:%02d", track.durationSec / 60, track.durationSec % 60),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    val index = filteredTracks.indexOf(track)
                                    if (index != -1 && index > 0) {
                                        currentTrack = filteredTracks[index - 1]
                                    } else {
                                        currentTrack = filteredTracks.lastOrNull()
                                    }
                                    currentProgressSec = 0
                                }) {
                                    Icon(Icons.Filled.SkipPrevious, contentDescription = "قبلی", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .bounceClick { isPlaying = !isPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "پخش",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                IconButton(onClick = {
                                    val index = filteredTracks.indexOf(track)
                                    if (index != -1 && index < filteredTracks.lastIndex) {
                                        currentTrack = filteredTracks[index + 1]
                                    } else {
                                        currentTrack = filteredTracks.firstOrNull()
                                    }
                                    currentProgressSec = 0
                                }) {
                                    Icon(Icons.Filled.SkipNext, contentDescription = "بعدی", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLyriaModal) {
        var musicPrompt by remember { mutableStateOf("") }
        var isGeneratingMusic by remember { mutableStateOf(false) }
        var musicResult by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { if (!isGeneratingMusic) showLyriaModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("آهنگساز هوشمند Lyria", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "توصیف کن چه حس و نغمه‌ای برای این لحظه‌ات می‌خواهی؟ (تولید با lyria-3-clip-preview تا 30 ثانیه)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = musicPrompt,
                        onValueChange = { musicPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: آرامش‌بخش با پیانو ملایم...") },
                        maxLines = 3,
                        enabled = !isGeneratingMusic
                    )
                    if (isGeneratingMusic) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("در حال تنظیم نت‌ها...", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                    }
                    if (musicResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("✅ قطعه موسیقی شما تولید و آماده پخش است!", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isGeneratingMusic = true
                        coroutineScope.launch {
                            val res = viewModel.generateMusicClip(musicPrompt)
                            isGeneratingMusic = false
                            if (res != null) {
                                musicResult = "Success!"
                            } else {
                                musicResult = "Failure!"
                            }
                        }
                    },
                    enabled = musicPrompt.isNotBlank() && !isGeneratingMusic
                ) {
                    Text("تولید نغمه 🎵")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLyriaModal = false }, enabled = !isGeneratingMusic) {
                    Text("بستن")
                }
            }
        )
    }
}

// Simple ease helper for breathing pacing visualization scale
suspend fun animateTo(target: Float, onUpdate: (Float) -> Unit) {
    val durationMs = 4000
    val startValue = if (target == 1.4f) 1.0f else 1.4f
    val steps = 40
    val stepDelay = (durationMs / steps).toLong()
    for (i in 1..steps) {
        val fraction = i.toFloat() / steps
        val value = startValue + (target - startValue) * fraction
        onUpdate(value)
        delay(stepDelay)
    }
}
