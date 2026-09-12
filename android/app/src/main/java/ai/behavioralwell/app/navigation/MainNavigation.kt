package ai.behavioralwell.app.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.BehavioralWellApplication
import ai.behavioralwell.app.data.repositories.TelemetryRepository
import ai.behavioralwell.app.features.auth.AuthViewModel
import ai.behavioralwell.app.features.auth.LoginScreen
import ai.behavioralwell.app.features.auth.RegisterScreen
import ai.behavioralwell.app.features.dashboard.DashboardScreen
import ai.behavioralwell.app.features.dashboard.DashboardViewModel
import ai.behavioralwell.app.features.interventions.BreathingResetScreen
import ai.behavioralwell.app.features.interventions.InterventionsScreen
import ai.behavioralwell.app.features.interventions.ReactionChallengeScreen
import ai.behavioralwell.app.features.privacy.PrivacySettingsScreen
import ai.behavioralwell.app.features.profile.ProfileSettingsScreen
import ai.behavioralwell.app.features.selfcheck.SelfCheckInDialog
import ai.behavioralwell.app.features.sensors.SensorStatusDiagnosticsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Destinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val SENSORS = "sensors"
    const val PRIVACY = "privacy"
    const val INTERVENTIONS = "interventions"
    const val BREATHING_RESET = "breathing_reset"
    const val REACTION_CHALLENGE = "reaction_challenge"
    const val PROFILE = "profile"
}

@Composable
fun MainNavigation() {
    val navStartTime = System.currentTimeMillis()
    if (BuildConfig.DEBUG) {
        Log.d("PerfTrace", "[MainNavigation] Component composition started at $navStartTime")
    }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = BehavioralWellApplication.instance

    val telemetryRepository = remember { TelemetryRepository(context) }
    val tokenStorage = remember { BehavioralWellApplication.instance.tokenStorage }

    var showSelfCheckDialog by remember { mutableStateOf(false) }
    val accessToken by tokenStorage.accessTokenFlow.collectAsState(initial = null)

    val startDestination = remember(accessToken) {
        if (!accessToken.isNull_or_empty()) {
            Destinations.DASHBOARD
        } else {
            Destinations.LOGIN
        }
    }

    if (BuildConfig.DEBUG) {
        Log.d("PerfTrace", "[MainNavigation] Computed startDestination: $startDestination in ${System.currentTimeMillis() - navStartTime}ms")
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Destinations.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    authViewModel.resetState()
                    navController.navigate(Destinations.REGISTER)
                }
            )
        }

        composable(Destinations.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    authViewModel.resetState()
                    navController.navigate(Destinations.LOGIN)
                }
            )
        }

        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToSensors = { navController.navigate(Destinations.SENSORS) },
                onNavigateToInterventions = { navController.navigate(Destinations.INTERVENTIONS) },
                onNavigateToProfile = { navController.navigate(Destinations.PROFILE) },
                onOpenSelfCheckIn = { showSelfCheckDialog = true }
            )

            if (showSelfCheckDialog) {
                SelfCheckInDialog(
                    onDismiss = { showSelfCheckDialog = false },
                    onSubmit = { mood, stressLevel, note ->
                        showSelfCheckDialog = false
                        scope.launch(Dispatchers.IO) {
                            val startMs = System.currentTimeMillis()
                            telemetryRepository.submitSelfCheck(mood, stressLevel, note)
                            if (BuildConfig.DEBUG) {
                                Log.d("PerfTrace", "[SelfCheck] Submitted self-check in ${System.currentTimeMillis() - startMs}ms")
                            }
                            dashboardViewModel.loadDashboardData()
                        }
                    }
                )
            }
        }

        composable(Destinations.SENSORS) {
            SensorStatusDiagnosticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.PRIVACY) {
            PrivacySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.INTERVENTIONS) {
            InterventionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartBreathing = {
                    scope.launch(Dispatchers.IO) {
                        val startMs = System.currentTimeMillis()
                        telemetryRepository.startIntervention("Breathing Reset")
                        if (BuildConfig.DEBUG) {
                            Log.d("PerfTrace", "[Intervention] Started Breathing Reset in ${System.currentTimeMillis() - startMs}ms")
                        }
                    }
                    navController.navigate(Destinations.BREATHING_RESET)
                },
                onStartReactionChallenge = {
                    scope.launch(Dispatchers.IO) {
                        val startMs = System.currentTimeMillis()
                        telemetryRepository.startIntervention("Reaction Challenge")
                        if (BuildConfig.DEBUG) {
                            Log.d("PerfTrace", "[Intervention] Started Reaction Challenge in ${System.currentTimeMillis() - startMs}ms")
                        }
                    }
                    navController.navigate(Destinations.REACTION_CHALLENGE)
                }
            )
        }

        composable(Destinations.BREATHING_RESET) {
            BreathingResetScreen(
                onComplete = {
                    scope.launch(Dispatchers.IO) {
                        telemetryRepository.completeIntervention(sessionId = null, feedbackScore = 5, metrics = mapOf("heart_rate_bpm" to 68))
                    }
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Destinations.REACTION_CHALLENGE) {
            ReactionChallengeScreen(
                onComplete = { reactionTimeMs ->
                    scope.launch(Dispatchers.IO) {
                        telemetryRepository.completeIntervention(sessionId = null, feedbackScore = 5, metrics = mapOf("reaction_time_ms" to reactionTimeMs))
                    }
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Destinations.PROFILE) {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiagnostics = { navController.navigate(Destinations.SENSORS) },
                onLogout = {
                    scope.launch(Dispatchers.IO) {
                        tokenStorage.clearTokens()
                        launch(Dispatchers.Main) {
                            navController.navigate(Destinations.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
    }
}


private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
