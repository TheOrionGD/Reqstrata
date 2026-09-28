package com.theoriongd.reqstrata.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.GlobalLoadingOverlay
import com.theoriongd.reqstrata.ui.components.InAppNotificationBanner
import com.theoriongd.reqstrata.ui.navigation.AppRoutes.toRoute

/**
 * Core Navigation Architecture using dynamic NavGraph builder and role-authorized destinations.
 * Dynamically reconstructs the NavGraph at runtime based on the user's role and assigned project,
 * ensuring only authorized screens are included in the bottom navigation and menus.
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val globalLoadingState by viewModel.globalLoadingState.collectAsState()
    val inAppNotif by viewModel.inAppNotification.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val tactileFeedback by viewModel.tactileFeedback.collectAsState()

    // Provide LocalReduceMotion & LocalTactileFeedback throughout the Composable tree
    CompositionLocalProvider(
        com.theoriongd.reqstrata.ui.motion.LocalReduceMotion provides reduceMotion,
        com.theoriongd.reqstrata.ui.motion.LocalTactileFeedback provides tactileFeedback
    ) {
        // Synchronize ViewModel screen state with NavHost NavController
        LaunchedEffect(currentScreen) {
            val targetRoute = with(AppRoutes) { currentScreen.toRoute() }
            val currentDestinationRoute = navController.currentDestination?.route

            if (currentDestinationRoute != targetRoute) {
                try {
                    navController.navigate(targetRoute) {
                        if (targetRoute in listOf(
                                AppRoutes.SPLASH,
                                AppRoutes.ONBOARDING,
                                AppRoutes.LOGIN,
                                AppRoutes.PROJECT_SELECTION
                            )
                        ) {
                            popUpTo(0) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
                } catch (_: Exception) {
                    // Ignore transient navigation issues during rapid state switches
                }
            }
        }

        // Android System Back Navigation Handler
        BackHandler(enabled = navController.previousBackStackEntry != null) {
            if (!viewModel.navigateBack()) {
                navController.popBackStack()
            }
        }

        val isAuthScreen = currentScreen in listOf(
            Screen.Splash,
            Screen.Onboarding,
            Screen.Login,
            Screen.Register,
            Screen.ForgotPassword
        ) || currentScreen is Screen.EmailVerification || currentScreen is Screen.ResetPassword

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (!isAuthScreen) {
                        DynamicRoleBottomBar(viewModel = viewModel)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.SPLASH,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideEnter(isForward = true, isReduceMotion = reduceMotion) },
                    exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideExit(isForward = true, isReduceMotion = reduceMotion) },
                    popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideEnter(isForward = false, isReduceMotion = reduceMotion) },
                    popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideExit(isForward = false, isReduceMotion = reduceMotion) }
                ) {
                    // Dynamically reconstruct authorized routes based on role and project
                    DynamicNavGraphBuilder.buildAuthorizedGraph(
                        builder = this,
                        viewModel = viewModel,
                        activeRole = activeRole,
                        currentProject = currentProject
                    )
                }
            }

            // Global Loading State Overlay: visual feedback during AI generation or Database queries
            GlobalLoadingOverlay(
                loadingState = globalLoadingState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Floating In-App Notification Banner overlay
            InAppNotificationBanner(
                notification = inAppNotif,
                onDismiss = { viewModel.dismissInAppNotification() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
