package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.CycleAnalysisResult
import com.example.ui.viewmodel.MahitaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleAnalysisScreen(
    viewModel: MahitaViewModel,
    onNavigateBack: () -> Unit
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadCachedAnalysis()
    }

    Scaffold(
        containerColor = Color(0xFFFAF7F2), // warm calming background matching the aesthetic
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "تحلیل هوشمند چرخه", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 19.sp,
                        color = SleekPrimaryDark
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("analysis_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "بازگشت",
                            tint = SleekPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF7F2),
                    titleContentColor = SleekPrimaryDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Elegant glowing AI banner header
            AIGlowHeader()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = analysisState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "analysis_state_anim"
                ) { state ->
                    when (state) {
                        is AnalysisUiState.Idle -> {
                            IdleStateView(onStartAnalysis = { viewModel.analyzeCycleWithGemini() })
                        }
                        is AnalysisUiState.Loading -> {
                            LoadingStateView()
                        }
                        is AnalysisUiState.Success -> {
                            SuccessStateView(
                                result = state.result,
                                onRecalculate = { viewModel.analyzeCycleWithGemini() }
                            )
                        }
                        is AnalysisUiState.Error -> {
                            ErrorStateView(
                                message = state.message,
                                onRetry = { viewModel.analyzeCycleWithGemini() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIGlowHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SleekPrimary.copy(alpha = 0.85f),
                        SleekPrimaryDark
                    )
                )
            )
    ) {
        // Aesthetic patterns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "دستیار سلامت هانیتا",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تحلیل جامع و شناسایی دوره‌ها",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پردازش الگوهای ثبت شده با کمک موتور محاسباتی Gemini AI",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            
            // Glowing circular avatar placeholder/decor
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌸",
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun IdleStateView(onStartAnalysis: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryLight.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "رمزگشایی از چرخه‌های بدن شما",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPrimaryDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "هانیتا با پردازش آماری و تحلیل سوابق و کارهای روزانه که قبلاً ثبت کرده‌اید، الگوهای چرخه شما را کشف کرده و پیش‌بینی‌های بسیار دقیقی از وضعیت سلامت شما ارائه خواهد داد.",
                fontSize = 13.sp,
                color = SleekTextMedium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStartAnalysis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_analysis_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحلیل داده‌ها و دریافت گزارش",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LoadingStateView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Pulsing circle animation
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(SleekPrimaryLight.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔮",
                    fontSize = 42.sp
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "در حال تحلیل چرخه‌ها...",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPrimaryDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "هوش مصنوعی در حال پردازش نشانه‌ها، سوابق و استخراج منظم بودن هورمون‌ها است. این کار ممکن است چند لحظه طول بکشد.",
                fontSize = 12.sp,
                color = SleekTextMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = SleekPrimary,
                trackColor = SleekPrimaryLight
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SuccessStateView(
    result: CycleAnalysisResult,
    onRecalculate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Estimated Next Period
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekPrimaryLight.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تاریخ تخمینی دوره بعدی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekTextDark
                        )
                    }
                    
                    // Regularity Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.regularityStatus,
                            color = Color(0xFF059669),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = result.predictedNextStartJalali,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = SleekPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = result.predictionReasoning,
                    fontSize = 13.sp,
                    color = SleekTextMedium,
                    lineHeight = 22.sp
                )
            }
        }

        // Card 2: Common Patterns
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timeline,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الگوهای کشف شده از چرخه‌های شما",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekTextDark
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (result.commonPatterns.isEmpty()) {
                    Text(
                        text = "نشانه‌های رفتاری کافی در این بخش یافت نشد. به ثبت روزانه وضعیت خود ادامه دهید تا الگوها شناسایی شوند.",
                        fontSize = 12.sp,
                        color = SleekTextLight
                    )
                } else {
                    result.commonPatterns.forEachIndexed { index, pattern ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pattern,
                                fontSize = 13.sp,
                                color = SleekTextDark,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Lifestyle and Tips
        Text(
            text = "توصیه‌های ویژه هانیتا",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SleekPrimaryDark,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        // Advice Row 1: Nutrition
        AdviceCard(
            title = "رژیم غذایی فاز فعلی چرخه",
            text = result.nutritionRecommend,
            icon = Icons.Filled.LocalDrink,
            iconColor = Color(0xFFD97706),
            bgColor = Color(0xFFFEF3C7)
        )

        // Advice Row 2: Exercise
        AdviceCard(
            title = "تمرینات مناسب فیزیکی",
            text = result.exerciseRecommend,
            icon = Icons.Filled.DirectionsRun,
            iconColor = Color(0xFF2563EB),
            bgColor = Color(0xFFDBEAFE)
        )

        // Advice Row 3: Mindfulness
        AdviceCard(
            title = "سلامت روانی و آرامش ذهن",
            text = result.mentalRecommend,
            icon = Icons.Filled.Spa,
            iconColor = Color(0xFF0D9488),
            bgColor = Color(0xFFCCFBF1)
        )

        // Card 4: Hanita's Conclusion
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "سخن پایانی هانیتا 💛",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.overallSummary,
                    fontSize = 13.sp,
                    color = SleekPrimaryDark,
                    lineHeight = 22.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Floating/Bottom action button
        Button(
            onClick = onRecalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("recalculate_analysis_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, SleekPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = SleekPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "بروزرسانی و تحلیل مجدد داده‌ها",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SleekPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AdviceCard(
    title: String,
    text: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SleekTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = SleekTextMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SleekBorder.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "بروز خطا در بازیابی گزارش",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = SleekTextMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("retry_analysis_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تلاش مجدد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
