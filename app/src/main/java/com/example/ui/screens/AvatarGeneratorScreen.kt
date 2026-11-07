package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun AvatarRenderer(avatarId: String, modifier: Modifier = Modifier) {
    if (com.example.utils.AvatarStore.isGenerated(avatarId)) {
        val context = LocalContext.current
        // Decoded off the main thread and downsampled; the file can be a large PNG.
        val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, avatarId) {
            value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.utils.AvatarStore.load(context, avatarId, maxSizePx = 256)
            }
        }
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "تصویر آواتار",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            return
        }
    }
    FantasyAvatarCanvas(avatarId = avatarId, modifier = modifier)
}

@Composable
fun FantasyAvatarCanvas(avatarId: String, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val size = this.size
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w.coerceAtMost(h) / 2f

        // Determine colors based on style matching
        val colors = when {
            avatarId.contains("fairy") || avatarId == "preset1" -> listOf(Color(0xFFFFB0B0), Color(0xFFFFD3B6), Color(0xFFFFE3E3))
            avatarId.contains("galaxy") || avatarId == "preset2" -> listOf(Color(0xFF3F51B5), Color(0xFF673AB7), Color(0xFFE040FB))
            avatarId.contains("nature") || avatarId == "preset3" -> listOf(Color(0xFF00BFA5), Color(0xFF00E676), Color(0xFFB9F6CA))
            avatarId.contains("watercolor") || avatarId == "preset4" -> listOf(Color(0xFF00B0FF), Color(0xFF00E5FF), Color(0xFFF1F8FF))
            avatarId.contains("cyber") || avatarId == "preset5" -> listOf(Color(0xFFFF007F), Color(0xFF00F3FF), Color(0xFFFFFFFF))
            else -> listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFECDD3)) // Rose gold soft pastel
        }

        // Draw radial background
        drawCircle(
            brush = Brush.radialGradient(
                colors = colors,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                radius = radius
            ),
            radius = radius
        )

        // Draw style specific artwork
        when {
            avatarId.contains("fairy") || avatarId == "preset1" -> {
                // Flower petals / Fairy wings
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (Math.PI / 180f).toFloat()
                    val px = cx + (radius * 0.38f) * kotlin.math.cos(angle)
                    val py = cy + (radius * 0.38f) * kotlin.math.sin(angle)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = radius * 0.3f,
                        center = androidx.compose.ui.geometry.Offset(px, py)
                    )
                }
                // Core glow
                drawCircle(
                    color = Color(0xFFFF6D80),
                    radius = radius * 0.22f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                // Celestial specs
                drawCircle(color = Color.White, radius = 5f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.45f, cy - radius * 0.3f))
                drawCircle(color = Color.White, radius = 5f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.4f, cy + radius * 0.45f))
            }
            avatarId.contains("galaxy") || avatarId == "preset2" -> {
                // Galaxy rings
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius * 0.55f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    style = Stroke(width = 6f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = radius * 0.4f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
                // crescent moon
                val moonPath = Path().apply {
                    addArc(
                        oval = androidx.compose.ui.geometry.Rect(cx - radius * 0.28f, cy - radius * 0.28f, cx + radius * 0.28f, cy + radius * 0.28f),
                        startAngleDegrees = -90f, sweepAngleDegrees = 180f
                    )
                    quadraticTo(cx, cy, cx, cy - radius * 0.28f)
                    close()
                }
                drawPath(path = moonPath, color = Color(0xFFFFD54F))
                // magic stars
                drawCircle(color = Color.White, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.45f, cy - radius * 0.5f))
                drawCircle(color = Color.White, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.45f, cy + radius * 0.35f))
            }
            avatarId.contains("nature") || avatarId == "preset3" -> {
                // Nature Leaf representation
                val leafPath1 = Path().apply {
                    moveTo(cx, cy - radius * 0.6f)
                    cubicTo(cx - radius * 0.4f, cy - radius * 0.2f, cx - radius * 0.2f, cy + radius * 0.4f, cx, cy + radius * 0.6f)
                    cubicTo(cx + radius * 0.2f, cy + radius * 0.4f, cx + radius * 0.4f, cy - radius * 0.2f, cx, cy - radius * 0.6f)
                    close()
                }
                drawPath(path = leafPath1, color = Color(0xFF0F9D58).copy(alpha = 0.75f))
                drawCircle(
                    color = Color(0xFFA3E635),
                    radius = radius * 0.22f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                // white stars
                drawCircle(color = Color.White, radius = 5f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.35f, cy - radius * 0.2f))
                drawCircle(color = Color.White, radius = 5f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.35f, cy - radius * 0.3f))
            }
            avatarId.contains("watercolor") || avatarId == "preset4" -> {
                // organic fluid blobs
                drawCircle(color = Color(0x7F22D3EE), radius = radius * 0.52f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.15f, cy - radius * 0.1f))
                drawCircle(color = Color(0x7F6366F1), radius = radius * 0.45f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.2f, cy + radius * 0.15f))
                drawCircle(color = Color(0x7FF43F5E), radius = radius * 0.38f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.05f, cy + radius * 0.25f))
                drawCircle(color = Color.White, radius = radius * 0.14f, center = androidx.compose.ui.geometry.Offset(cx, cy))
            }
            avatarId.contains("cyber") || avatarId == "preset5" -> {
                // Futuristic cyber circles
                drawCircle(color = Color(0xFF38BDF8), radius = radius * 0.75f, center = androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(width = 5f))
                drawRect(color = Color(0xFFEC4899).copy(alpha = 0.65f), topLeft = androidx.compose.ui.geometry.Offset(cx - radius * 0.4f, cy - radius * 0.08f), size = androidx.compose.ui.geometry.Size(radius * 0.8f, radius * 0.16f))
                drawCircle(color = Color.White, radius = radius * 0.18f, center = androidx.compose.ui.geometry.Offset(cx, cy))
            }
            else -> {
                // Default Profile head icon (Aesthetic silhouette)
                drawCircle(color = primaryColor.copy(alpha = 0.25f), radius = radius * 0.45f, center = androidx.compose.ui.geometry.Offset(cx, cy - radius * 0.12f))
                val bodyPath = Path().apply {
                    moveTo(cx - radius * 0.65f, cy + radius * 0.62f)
                    quadraticTo(cx, cy + radius * 0.22f, cx + radius * 0.65f, cy + radius * 0.62f)
                    lineTo(cx + radius * 0.65f, cy + radius * 1.0f)
                    lineTo(cx - radius * 0.65f, cy + radius * 1.0f)
                    close()
                }
                drawPath(path = bodyPath, color = primaryColor.copy(alpha = 0.42f))
            }
        }
    }
}

// Data class representing an avatar option
data class AvatarOption(
    val id: String,
    val name: String,
    val isGenerated: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarGeneratorScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val currentAvatar by viewModel.userAvatar.collectAsState()
    val generated by viewModel.generatedAvatars.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var userPrompt by rememberSaveable { mutableStateOf("") }
    var selectedStyle by rememberSaveable { mutableStateOf("fairy") }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedAvatarId by rememberSaveable(currentAvatar) { mutableStateOf(currentAvatar) }
    var avatarToDelete by remember { mutableStateOf<String?>(null) }

    val fantasyStyles = listOf(
        Triple("fairy", "🌸 پری", "dreamy fairy, glowing wings and spring flower petals"),
        Triple("galaxy", "🔮 کهکشان", "cosmic rings, golden crescent moon, purple star dust"),
        Triple("nature", "🍃 طبیعت", "forest leaves, lotus flower, soft mint green"),
        Triple("watercolor", "🎨 آبرنگ", "soft flowing watercolor in pastel sky and pink tones"),
        Triple("cyber", "⚡ نئون", "modern soft neon, magical circuits in pink and turquoise")
    )

    val presets = listOf(
        AvatarOption("avatar_default", "پیش‌فرض"),
        AvatarOption("preset1", "گلبرگ خیال"),
        AvatarOption("preset2", "نگین شب"),
        AvatarOption("preset3", "آرامش دشت"),
        AvatarOption("preset4", "ابر و باد"),
        AvatarOption("preset5", "مدار نور")
    )
    val options = remember(generated) {
        generated.mapIndexed { i, id -> AvatarOption(id, "هوشمند ${com.example.utils.PersianNumbers.toPersianDigits(i + 1)}", true) } + presets
    }

    val startAIGenerator: () -> Unit = {
        if (userPrompt.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("ابتدا ویژگی یا حسی را که دوست داری در آواتار باشد بنویس. 💕") }
        } else {
            isGenerating = true
            focusManager.clearFocus()
            coroutineScope.launch {
                val style = fantasyStyles.firstOrNull { it.first == selectedStyle }?.third.orEmpty()
                val newId = viewModel.generateAvatar("${userPrompt.trim()}. Style: $style")
                isGenerating = false
                if (newId != null) {
                    selectedAvatarId = newId
                    userPrompt = ""
                    snackbarHostState.showSnackbar("آواتار ساخته شد؛ برای استفاده «ثبت آواتار» را بزنید.")
                } else {
                    snackbarHostState.showSnackbar("ساخت آواتار انجام نشد. اتصال اینترنت را بررسی کنید یا توضیح دیگری بنویسید.")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("آواتار من", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("انتخاب یا ساخت تصویر نمایه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            AvatarRenderer(avatarId = selectedAvatarId, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.setUserAvatar(selectedAvatarId)
                                coroutineScope.launch { snackbarHostState.showSnackbar("آواتار شما ذخیره شد ✨") }
                            },
                            shape = RoundedCornerShape(16.dp),
                            enabled = selectedAvatarId != currentAvatar
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت آواتار", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("ساخت آواتار با هوش مصنوعی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            "فقط همین توضیح برای ساخت تصویر ارسال می‌شود؛ هیچ اطلاعاتی از چرخه شما فرستاده نمی‌شود.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = userPrompt,
                            onValueChange = { userPrompt = it.take(300) },
                            label = { Text("توصیف آواتار") },
                            placeholder = { Text("مثلاً: دختری آرام با چشمان کهکشانی") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            enabled = !isGenerating,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                        Text("سبک هنری:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            fantasyStyles.forEach { (key, label, _) ->
                                val active = selectedStyle == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                        .selectable(selected = active, role = Role.RadioButton) { selectedStyle = key }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (isGenerating) {
                            Text("در حال ساخت تصویر... ممکن است چند ثانیه طول بکشد.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                        } else {
                            Button(onClick = startAIGenerator, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ساخت آواتار", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("گالری آواتارها", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            items(options, key = { it.id }) { item ->
                val isSelected = selectedAvatarId == item.id
                val isCurrentApplied = currentAvatar == item.id
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .selectable(selected = isSelected, role = Role.RadioButton) { selectedAvatarId = item.id }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.size(52.dp).clip(CircleShape)) {
                            AvatarRenderer(avatarId = item.id, modifier = Modifier.fillMaxSize())
                        }
                        Text(
                            text = item.name + if (isCurrentApplied) " ✓" else "",
                            fontSize = 11.sp,
                            maxLines = 1,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (item.isGenerated) {
                        IconButton(
                            onClick = { avatarToDelete = item.id },
                            modifier = Modifier.align(Alignment.TopStart).size(28.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف این آواتار", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    avatarToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { avatarToDelete = null },
            title = { Text("حذف آواتار؟") },
            text = { Text("این تصویر از گالری و حافظه گوشی حذف می‌شود.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGeneratedAvatar(id)
                    if (selectedAvatarId == id) selectedAvatarId = "avatar_default"
                    avatarToDelete = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { avatarToDelete = null }) { Text("انصراف") } }
        )
    }
}
