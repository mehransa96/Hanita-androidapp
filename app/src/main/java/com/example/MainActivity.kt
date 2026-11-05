package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MoodScreen
import com.example.ui.screens.ChatBotScreen
import com.example.ui.screens.MeditationMusicScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.AvatarGeneratorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MahitaViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val crashPrefs = remember { getSharedPreferences(HanitaApplication.CRASH_PREFS, android.content.Context.MODE_PRIVATE) }
            var crashStackTrace by remember { mutableStateOf(crashPrefs.getString(HanitaApplication.KEY_LAST_CRASH, null)) }
            var confirmReset by remember { mutableStateOf(false) }

            if (crashStackTrace != null) {
                // Beautiful Recovery and Crash Diagnosis Screen in Farsi
                MyApplicationTheme(themeCode = "COSMIC_BLOSSOM", themeMode = "DARK", fontCode = "CLASSIC") {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "سامانه ایمن بازیابی خطا 🌸",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "پوزش می‌طلبیم؛ برنامه با یک خطای فنی متوقف شد. برای بازیابی می‌توانید حافظه موقت را بازنشانی کرده یا سیستم را دوباره اجرا کنید.",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                                        Text(
                                            text = "گزارش خطا (جهت ارسال به پشتیبان):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = crashStackTrace ?: "No Trace",
                                            fontSize = 10.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                if (confirmReset) {
                                    AlertDialog(
                                        onDismissRequest = { confirmReset = false },
                                        title = { Text("حذف همه اطلاعات؟") },
                                        text = { Text("با این کار همه سوابق چرخه، علائم و تنظیمات روی این گوشی برای همیشه پاک می‌شود. پیشنهاد می‌کنیم ابتدا «تلاش مجدد» را امتحان کنید.") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                confirmReset = false
                                                resetAllLocalData()
                                            }) { Text("بله، پاک شود", color = MaterialTheme.colorScheme.error) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { confirmReset = false }) { Text("انصراف") }
                                        }
                                    )
                                }
                                Button(
                                    onClick = { confirmReset = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("بازنشانی کامل و حذف اطلاعات 🔄", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedButton(
                                    onClick = {
                                        crashPrefs.edit().remove(HanitaApplication.KEY_LAST_CRASH).apply()
                                        crashStackTrace = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("تلاش مجدد (بدون حذف داده‌ها) 🚀")
                                }
                            }
                        }
                    }
                }
            } else {
                val viewModel: MahitaViewModel = viewModel()
            val appTheme by viewModel.appTheme.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val appFont by viewModel.appFont.collectAsState()

            MyApplicationTheme(
                themeCode = appTheme,
                themeMode = themeMode,
                fontCode = appFont,
                onThemeChanged = { viewModel.setAppTheme(it) },
                onThemeModeChanged = { viewModel.setThemeMode(it) }
            ) {
                // Ensure layout is Right-to-Left for Persian language
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                        com.example.ui.components.AiConsentDialog(viewModel)
                        NotificationPermissionRequest(isLoggedIn)

                        NavHost(
                            navController = navController, 
                            startDestination = if (isLoggedIn) "dashboard" else "login",
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        ) {
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = { name, cycleLength, lastPeriodDateStr ->
                                        viewModel.login(name, cycleLength, lastPeriodDateStr)
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToLog = { navController.navigate("log") },
                                    onNavigateToCalendar = { navController.navigate("calendar") },
                                    onNavigateToHealth = { navController.navigate("health") },
                                    onNavigateToEducation = { navController.navigate("education") },
                                    onNavigateToMood = { navController.navigate("mood") },
                                    onNavigateToChat = { navController.navigate("chatbot") },
                                    onNavigateToMeditation = { navController.navigate("meditation") },
                                    onNavigateToReminders = { navController.navigate("reminders") },
                                    onNavigateToAvatarGenerator = { navController.navigate("avatargenerator") },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToCharts = { navController.navigate("charts") },
                                    onLogout = {
                                        viewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("avatargenerator") {
                                AvatarGeneratorScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("reminders") {
                                RemindersScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("meditation") {
                                MeditationMusicScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("health") {
                                com.example.ui.screens.HealthScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("education") {
                                com.example.ui.screens.EducationScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("mood") {
                                MoodScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("chatbot") {
                                ChatBotScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("log") {
                                LogScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "history",
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(500, easing = LinearOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(500))
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(500, easing = FastOutLinearInEasing)
                                    ) + fadeOut(animationSpec = tween(500))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(500, easing = LinearOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(500))
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(500, easing = FastOutLinearInEasing)
                                    ) + fadeOut(animationSpec = tween(500))
                                }
                            ) {
                                com.example.ui.screens.CycleHistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "charts",
                                enterTransition = {
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = tween(400, easing = LinearOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(400))
                                },
                                exitTransition = {
                                    scaleOut(
                                        targetScale = 0.9f,
                                        animationSpec = tween(400, easing = FastOutLinearInEasing)
                                    ) + fadeOut(animationSpec = tween(400))
                                },
                                popEnterTransition = {
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = tween(400, easing = LinearOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(400))
                                },
                                popExitTransition = {
                                    scaleOut(
                                        targetScale = 0.9f,
                                        animationSpec = tween(400, easing = FastOutLinearInEasing)
                                    ) + fadeOut(animationSpec = tween(400))
                                }
                            ) {
                                com.example.ui.screens.SymptomChartsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("calendar") {
                                CalendarScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    /** Deletes all local data (only after explicit confirmation on the recovery screen). */
    @android.annotation.SuppressLint("ApplySharedPref") // must be on disk before the process restarts
    private fun resetAllLocalData() {
        getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        getSharedPreferences(HanitaApplication.CRASH_PREFS, android.content.Context.MODE_PRIVATE).edit().clear().commit()
        try {
            deleteDatabase("hanita_database")
        } catch (_: Exception) {
        }
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        finish()
    }
}

/**
 * Period and phase reminders are the core of the app, but on Android 13+ they are silently
 * dropped until the user grants POST_NOTIFICATIONS. Ask once, right after onboarding, instead of
 * only when the user happens to open the reminders screen.
 */
@Composable
private fun NotificationPermissionRequest(isLoggedIn: Boolean) {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("hanita_permissions", android.content.Context.MODE_PRIVATE) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* Reminders are rescheduled anyway; the user can change this later in settings. */ }
    LaunchedEffect(isLoggedIn) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (isLoggedIn && !granted && !prefs.getBoolean("notifications_asked", false)) {
            prefs.edit().putBoolean("notifications_asked", true).apply()
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
