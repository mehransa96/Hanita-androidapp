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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MahitaViewModel = viewModel()
            val appTheme by viewModel.appTheme.collectAsState()
            val appFont by viewModel.appFont.collectAsState()

            MyApplicationTheme(themeCode = appTheme, fontCode = appFont) {
                // Ensure layout is Right-to-Left for Persian language
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val isLoggedIn by viewModel.isLoggedIn.collectAsState()

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
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("health") {
                                com.example.ui.screens.HealthScreen(
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
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("log") {
                                LogScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
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
