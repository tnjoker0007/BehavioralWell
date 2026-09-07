package ai.behavioralwell.app.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.behavioralwell.app.BehavioralWellApplication
import ai.behavioralwell.app.data.models.InterventionStartInput
import ai.behavioralwell.app.data.models.SelfReportInput
import ai.behavioralwell.app.core.network.RetrofitClient
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
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var showSelfCheckDialog by remember { mutableStateOf(false) }

    val tokenStorage = BehavioralWellApplication.instance.tokenStorage
    val accessToken by tokenStorage.accessTokenFlow.collectAsState(initial = null)

    val startDestination = if (!accessToken.isNull_or_empty()) {
        Destinations.DASHBOARD
    } else {
        Destinations.LOGIN
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
                        scope.launch {
                            try {
                                RetrofitClient.apiService.submitSelfCheck(
                                    SelfReportInput(mood = mood, stressLevel = stressLevel, note = note)
                                )
                                dashboardViewModel.loadDashboardData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
                    scope.launch {
                        try {
                            RetrofitClient.apiService.startIntervention(
                                InterventionStartInput(activityType = "Breathing Reset")
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    navController.navigate(Destinations.BREATHING_RESET)
                },
                onStartReactionChallenge = {
                    scope.launch {
                        try {
                            RetrofitClient.apiService.startIntervention(
                                InterventionStartInput(activityType = "Reaction Challenge")
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    navController.navigate(Destinations.REACTION_CHALLENGE)
                }
            )
        }

        composable(Destinations.BREATHING_RESET) {
            BreathingResetScreen(
                onComplete = { navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Destinations.REACTION_CHALLENGE) {
            ReactionChallengeScreen(
                onComplete = { _ -> navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Destinations.PROFILE) {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    scope.launch {
                        tokenStorage.clearTokens()
                        navController.navigate(Destinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
