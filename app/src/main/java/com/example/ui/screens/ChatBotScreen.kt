package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.*
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

import com.example.ui.viewmodel.*

// --- Chat state classes ---
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(viewModel: com.example.ui.viewmodel.MahitaViewModel, onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var highThinkingEnabled by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }

    val allLogs by viewModel.allLogs.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val startPeriod by viewModel.lastPeriodDate.collectAsState()
    val cycleL by viewModel.averageCycleLength.collectAsState()

    // Chat History Starts with a warm welcome in Persian
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome",
                text = "سلام عزیزم! من هانیتا هستم، مشاور هوشمند سلامت شما. 🌸\nهر سوالی درباره دوران قاعدگی، کاهش دردهای پریود، تغذیه مناسب، علائم هورمونی یا حال خوب داری می‌تونی ازم بپرسی تا راهنماییت کنم. 💕",
                isUser = false
            )
        )
    }

    // Quick suggestions based on common questions asked by women
    val suggestedQuestions = listOf(
        "📊 چرخه و علائم من را تحلیل کن",
        "چطور درد پریود را کاهش بدم؟ 🌸",
        "تغذیه مناسب دوران قاعدگی چیست؟ 🍎",
        "علت نوسانات خلقی قبل پریود چیست؟ 🔮",
        "چگونه چرخه منظم داشته باشم؟ ✨"
    )

    // Scroll to latest message helper
    val scrollToBottom: () -> Unit = {
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val sendMessage: (String) -> Unit = { rawUserText ->
        if (rawUserText.isNotBlank()) {
            val isAnalysisRequest = rawUserText.contains("تحلیل کن")
            val displayUserText = rawUserText
            
            var internalSystemPromptContext = ""
            if (isAnalysisRequest) {
                val logsSummaryText = if (allLogs.isEmpty()) {
                    "سابقه ثبت روزانه علائم فعال: ندارد. تاریخ شروع آخرین پریود: $startPeriod و فواصل فرضی معمول چرخه: $cycleL روز است."
                } else {
                    allLogs.take(25).joinToString("\n") { log ->
                        "تاریخ: ${log.dateStr} | سطح خونریزی: ${log.periodFlow} | علائم بدنی: ${log.symptoms} | نوسانات روحی: ${log.moods} | یادداشت کاربر: ${log.notes}"
                    }
                }
                internalSystemPromptContext = "\nلطفا سوابق زیر مربوط به چرخه من را بررسی کن و یک تحلیل سلامت روان و توصیه‌های شخصی با لحن خواهرانه و گرم ارائه بده:\n$logsSummaryText"
            }

            val userMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), text = displayUserText, isUser = true)
            messages.add(userMsg)
            textInput = ""
            focusManager.clearFocus()
            isLoading = true
            scrollToBottom()

            coroutineScope.launch {
                try {
                    val systemMessage = GemAnalysisContent(
                        role = "system",
                        parts = listOf(GemAnalysisPart(text = "You are a compassionate, expert AI health advisor named 'هانیتا' (Hanita), specializing in women's health. Provide polite, precise, medically supportive, yet friendly answers in Persian language (Farsi). Add friendly emojis. Format the output with bold headings for readability. Always include a tiny disclaimer at the bottom: 'توصیه هانیتا؛ همواره در موارد پزشکی با پزشک مشورت کنید.'"))
                    )

                    // Gather history for context
                    val apiContents = messages.map { msg ->
                        GemAnalysisContent(
                            role = if (msg.isUser) "user" else "model",
                            parts = listOf(GemAnalysisPart(text = msg.text))
                        )
                    }.toMutableList()
                    
                    if (isAnalysisRequest) {
                        // Append hidden context to the latest user message
                        val lastIndex = apiContents.indexOfLast { it.role == "user" }
                        if (lastIndex != -1) {
                            apiContents[lastIndex] = GemAnalysisContent("user", listOf(GemAnalysisPart(text = displayUserText + "\n" + internalSystemPromptContext)))
                        }
                    }

                    val request = GemAnalysisRequest(
                        contents = apiContents,
                        systemInstruction = systemMessage,
                        tools = listOf(GemTool(googleSearch = GemGoogleSearchTool())),
                        generationConfig = if (highThinkingEnabled) GemGenerationConfig(
                            thinkingConfig = GemThinkingConfig(thinkingLevel = "HIGH")
                        ) else null
                    )

                    val apiKey = BuildConfig.GEMINI_API_KEY
                    
                    if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                        messages.add(
                            ChatMessage(
                                id = java.util.UUID.randomUUID().toString(),
                                text = "لطفاً کلید API (Gemini API Key) خود را در بخش Settings وارد کنید تا بتوانم به شما پاسخ دهم.",
                                isUser = false
                            )
                        )
                        isLoading = false
                        scrollToBottom()
                        return@launch
                    }

                    val response = withContext(Dispatchers.IO) {
                        if (highThinkingEnabled) {
                            GeminiAnalysisClient.service.generateContentPro(apiKey, request)
                        } else {
                            GeminiAnalysisClient.service.generateContent(apiKey, request)
                        }
                    }

                    val botReplyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "متاسفم، پاسخی دریافت نشد. لطفاً دوباره تلاش کنید."

                    messages.add(
                        ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            text = botReplyText,
                            isUser = false
                        )
                    )
                } catch (e: retrofit2.HttpException) {
                    val errorMsg = if (e.code() == 403) {
                        "خطای ۴۰۳ (Forbidden): کلید API شما نامعتبر است، یا دسترسی به API محدود شده است. لطفاً کلید معتبر تنظیم کنید."
                    } else if (e.code() == 400) {
                        "خطای ۴۰۰ (Bad Request): درخواست نامعتبر است."
                    } else {
                        "خطا در دریافت اطلاعات: ${e.message()}"
                    }
                    messages.add(
                        ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            text = errorMsg,
                            isUser = false
                        )
                    )
                } catch (e: Exception) {
                    messages.add(
                        ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            text = "ارتباط برقرار نشد. لطفاً اینترنت خود را متصل کرده و از صحت کلید Gemini اطمینان حاصل کنید. \nخطا: ${e.localizedMessage ?: "موجود نیست"}",
                            isUser = false
                        )
                    )
                } finally {
                    isLoading = false
                    scrollToBottom()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text("گفتگو با هانیتا 🌸", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("مشاور هوشمند سلامت زنان", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                        }
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "تفکر عمیق",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (highThinkingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = highThinkingEnabled,
                            onCheckedChange = { highThinkingEnabled = it },
                            modifier = Modifier.scale(0.8f)
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top safety / AI medical notice
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "پیشنهادات هوش مصنوعی جایگزین توصیه‌های تخصصی پزشک نیست.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }

            // Message list area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }

                    if (isLoading) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }

            // Quick suggestion row
            AnimatedVisibility(
                visible = messages.size == 1 && !isLoading,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "سوالات پیشنهادی برای شروع:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestedQuestions.take(2).forEach { question ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .clickable { sendMessage(question) }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = question,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestedQuestions.drop(2).forEach { question ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .clickable { sendMessage(question) }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = question,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Input Bar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text(if (isRecordingAudio) "در حال شنیدن... (درحال تبدیل صدا به متن)" else "پاسخ یا سوال خود را اینجا بنویسید...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    sendMessage(textInput)
                                }
                            }
                        )
                    )

                    // Audio Recording Mic
                    val infiniteTransition = rememberInfiniteTransition(label = "recordingScale")
                    val recordScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isRecordingAudio) 1.2f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = ""
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(recordScale)
                            .clip(CircleShape)
                            .background(if (isRecordingAudio) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (isRecordingAudio) {
                                    isRecordingAudio = false
                                    // Mock transcription since emulator lacks mic bytes
                                    textInput += " [صدای ضبط شده: لطفا شرایط بدنی و روحیه من رو با دقت تحلیل کن] "
                                } else {
                                    isRecordingAudio = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "ضبط صدا",
                            tint = if (isRecordingAudio) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                            .clickable(enabled = textInput.isNotBlank()) {
                                sendMessage(textInput)
                            }
                            .testTag("send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال",
                            tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleTextColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (message.isUser) Alignment.CenterStart else Alignment.CenterEnd
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 16.dp
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                border = if (!message.isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                tonalElevation = if (message.isUser) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    color = bubbleTextColor,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 16.dp
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Infinite bouncing dots simulation
                    val infiniteTransition = rememberInfiniteTransition(label = "typing")
                    listOf(0, 1, 2).forEach { index ->
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(650, delayMillis = index * 180),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = ""
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = scale), CircleShape)
                        )
                    }
                }
            }
        }
    }
}
