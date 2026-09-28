package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.navigation.AppRoutes.toRoute
import com.example.ui.screens.ai.ProjectAiAssistantScreen
import com.example.ui.screens.ai.DeveloperAIAssistantScreen
import com.example.ui.screens.architecture.ArchitectDashboardScreen
import com.example.ui.screens.architecture.ArchitectureComponentDetailScreen
import com.example.ui.screens.architecture.ArchitectureDecisionsScreen
import com.example.ui.screens.architecture.ArchitectureWorkspaceScreen
import com.example.ui.screens.architecture.SuggestArchitectureScreen
import com.example.ui.screens.architecture.UmlStudioScreen
import com.example.ui.screens.auth.EmailVerificationScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.ResetPasswordScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.design.ApiDesignerScreen
import com.example.ui.screens.design.DatabaseDesignerScreen
import com.example.ui.screens.documents.DocumentViewerScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.screens.project.AdminDashboardScreen
import com.example.ui.screens.project.ApprovalCenterScreen
import com.example.ui.screens.project.CreateProjectScreen
import com.example.ui.screens.project.ProjectDashboardScreen
import com.example.ui.screens.project.ProjectOverviewScreen
import com.example.ui.screens.project.ProjectSelectionScreen
import com.example.ui.screens.project.ProjectSettingsScreen
import com.example.ui.screens.requirements.BusinessAnalystDashboardScreen
import com.example.ui.screens.requirements.CreateEditRequirementScreen
import com.example.ui.screens.requirements.GenerateRequirementsScreen
import com.example.ui.screens.requirements.NewRequirementFormScreen
import com.example.ui.screens.requirements.RequirementAiAnalysisScreen
import com.example.ui.screens.requirements.RequirementComparisonScreen
import com.example.ui.screens.requirements.RequirementDetailScreen
import com.example.ui.screens.requirements.RequirementsListScreen
import com.example.ui.screens.requirements.UseCaseDetailScreen
import com.example.ui.screens.requirements.UseCasesScreen
import com.example.ui.screens.tasks.DeveloperDashboardScreen
import com.example.ui.screens.tasks.TaskManagementScreen
import com.example.ui.screens.team.ActivityLogScreen
import com.example.ui.screens.team.GlobalSearchScreen
import com.example.ui.screens.team.InviteMemberScreen
import com.example.ui.screens.team.MemberDetailScreen
import com.example.ui.screens.team.NotificationsScreen
import com.example.ui.screens.team.RoleManagementScreen
import com.example.ui.screens.team.TeamManagementScreen
import com.example.ui.screens.testing.CoverageDashboardScreen
import com.example.ui.screens.testing.GenerateTestSuiteScreen
import com.example.ui.screens.testing.TesterDashboardScreen
import com.example.ui.screens.testing.TestExecutionWorkspaceScreen
import com.example.ui.screens.testing.TestingWorkspaceScreen
import com.example.ui.screens.traceability.ChangeImpactAnalysisScreen
import com.example.ui.screens.traceability.TraceabilityMatrixScreen

/**
 * Core Navigation Architecture using NavHost and NavController.
 * Supports the complete project ecosystem, role-based routing from login to
 * management dashboards, and route-level authorization guards for MongoDB user roles.
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val inAppNotif by viewModel.inAppNotification.collectAsState()

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 1. Authentication & Onboarding Flow ---
            composable(AppRoutes.SPLASH) {
                SplashScreen(viewModel = viewModel)
            }
            composable(AppRoutes.ONBOARDING) {
                OnboardingScreen(viewModel = viewModel)
            }
            composable(AppRoutes.LOGIN) {
                LoginScreen(viewModel = viewModel)
            }
            composable(AppRoutes.REGISTER) {
                RegisterScreen(viewModel = viewModel)
            }
            composable(AppRoutes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.EMAIL_VERIFICATION,
                arguments = listOf(
                    navArgument("email") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                EmailVerificationScreen(
                    viewModel = viewModel,
                    emailArg = backStackEntry.arguments?.getString("email")
                )
            }
            composable(
                route = AppRoutes.RESET_PASSWORD,
                arguments = listOf(
                    navArgument("email") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                ResetPasswordScreen(
                    viewModel = viewModel,
                    emailArg = backStackEntry.arguments?.getString("email")
                )
            }

            // --- 2. Project Selection & Protected Project Management ---
            composable(AppRoutes.PROJECT_SELECTION) {
                ProjectSelectionScreen(viewModel = viewModel)
            }

            // Route-level authorization decorator: restricts CreateProject to Admin
            authorizedComposable(
                route = AppRoutes.CREATE_PROJECT,
                requiredRoles = setOf(ProjectRole.ADMIN),
                viewModel = viewModel,
                routeTitle = "Create New Project"
            ) {
                CreateProjectScreen(viewModel = viewModel)
            }

            // Project Dashboards (Role-based destinations)
            composable(AppRoutes.PROJECT_DASHBOARD) {
                ProjectDashboardScreen(viewModel = viewModel)
            }
            authorizedComposable(
                route = AppRoutes.ADMIN_DASHBOARD,
                requiredRoles = setOf(ProjectRole.ADMIN),
                viewModel = viewModel,
                routeTitle = "Admin Dashboard"
            ) {
                AdminDashboardScreen(viewModel = viewModel)
            }
            authorizedComposable(
                route = AppRoutes.APPROVAL_CENTER,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST, ProjectRole.ARCHITECT),
                viewModel = viewModel,
                routeTitle = "Approval Center"
            ) {
                ApprovalCenterScreen(viewModel = viewModel)
            }
            composable(AppRoutes.ARCHITECT_DASHBOARD) {
                ArchitectDashboardScreen(viewModel = viewModel)
            }
            composable(AppRoutes.DEVELOPER_DASHBOARD) {
                DeveloperDashboardScreen(viewModel = viewModel)
            }
            composable(AppRoutes.DEVELOPER_AI_ASSISTANT) {
                DeveloperAIAssistantScreen(viewModel = viewModel)
            }
            composable(AppRoutes.TESTER_DASHBOARD) {
                TesterDashboardScreen(viewModel = viewModel)
            }
            authorizedComposable(
                route = AppRoutes.TEST_EXECUTION_WORKSPACE,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.TESTER),
                viewModel = viewModel,
                routeTitle = "Test Execution Workspace"
            ) {
                TestExecutionWorkspaceScreen(viewModel = viewModel)
            }
            composable(AppRoutes.PROJECT_OVERVIEW) {
                ProjectOverviewScreen(viewModel = viewModel)
            }

            // Route-level authorization decorator: restricts Project Settings to Admin
            authorizedComposable(
                route = AppRoutes.PROJECT_SETTINGS,
                requiredRoles = setOf(ProjectRole.ADMIN),
                viewModel = viewModel,
                routeTitle = "Project Settings & Governance"
            ) {
                ProjectSettingsScreen(viewModel = viewModel)
            }

            // Business Analyst Dashboard
            composable(AppRoutes.BA_DASHBOARD) {
                BusinessAnalystDashboardScreen(viewModel = viewModel)
            }

            // --- 3. Requirements & Use Cases ---
            composable(AppRoutes.REQUIREMENTS_LIST) {
                RequirementsListScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.REQUIREMENT_DETAIL,
                arguments = listOf(
                    navArgument("reqId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val reqId = backStackEntry.arguments?.getString("reqId") ?: ""
                RequirementDetailScreen(viewModel = viewModel, reqId = reqId)
            }
            authorizedComposable(
                route = AppRoutes.CREATE_EDIT_REQUIREMENT,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST),
                viewModel = viewModel,
                routeTitle = "Create / Edit Requirement",
                arguments = listOf(
                    navArgument("reqId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val reqId = backStackEntry.arguments?.getString("reqId")
                CreateEditRequirementScreen(viewModel = viewModel, reqId = reqId)
            }
            composable(
                route = AppRoutes.REQUIREMENT_COMPARISON,
                arguments = listOf(
                    navArgument("reqId") { type = NavType.StringType },
                    navArgument("v1") { type = NavType.IntType; defaultValue = 1 },
                    navArgument("v2") { type = NavType.IntType; defaultValue = 2 }
                )
            ) { backStackEntry ->
                val reqId = backStackEntry.arguments?.getString("reqId") ?: ""
                val v1 = backStackEntry.arguments?.getInt("v1") ?: 1
                val v2 = backStackEntry.arguments?.getInt("v2") ?: 2
                RequirementComparisonScreen(viewModel = viewModel, reqId = reqId, v1Num = v1, v2Num = v2)
            }
            composable(
                route = AppRoutes.REQUIREMENT_AI_ANALYSIS,
                arguments = listOf(
                    navArgument("reqId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val reqId = backStackEntry.arguments?.getString("reqId") ?: ""
                RequirementAiAnalysisScreen(viewModel = viewModel, reqId = reqId)
            }
            authorizedComposable(
                route = AppRoutes.NEW_REQUIREMENT_FORM,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST),
                viewModel = viewModel,
                routeTitle = "New Requirement Specification"
            ) {
                NewRequirementFormScreen(viewModel = viewModel)
            }
            composable(AppRoutes.USE_CASES) {
                UseCasesScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.USE_CASE_DETAIL,
                arguments = listOf(
                    navArgument("useCaseId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val ucId = backStackEntry.arguments?.getString("useCaseId") ?: ""
                UseCaseDetailScreen(viewModel = viewModel, useCaseId = ucId)
            }

            // --- 4. Architecture & System Design Studio ---
            composable(AppRoutes.ARCHITECTURE_WORKSPACE) {
                ArchitectureWorkspaceScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.ARCHITECTURE_COMPONENT_DETAIL,
                arguments = listOf(
                    navArgument("componentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val cId = backStackEntry.arguments?.getString("componentId") ?: ""
                ArchitectureComponentDetailScreen(viewModel = viewModel, componentId = cId)
            }
            authorizedComposable(
                route = AppRoutes.ARCHITECTURE_DECISIONS,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.ARCHITECT),
                viewModel = viewModel,
                routeTitle = "Architecture Decisions (ADR)"
            ) {
                ArchitectureDecisionsScreen(viewModel = viewModel)
            }
            composable(AppRoutes.UML_STUDIO) {
                UmlStudioScreen(viewModel = viewModel)
            }
            composable(AppRoutes.DATABASE_DESIGNER) {
                DatabaseDesignerScreen(viewModel = viewModel)
            }
            composable(AppRoutes.API_DESIGNER) {
                ApiDesignerScreen(viewModel = viewModel)
            }

            // --- 5. Tasks, Verification & Traceability ---
            composable(AppRoutes.TASK_MANAGEMENT) {
                TaskManagementScreen(viewModel = viewModel)
            }
            composable(AppRoutes.TESTING_WORKSPACE) {
                TestingWorkspaceScreen(viewModel = viewModel)
            }
            composable(AppRoutes.COVERAGE_DASHBOARD) {
                CoverageDashboardScreen(viewModel = viewModel)
            }
            composable(AppRoutes.TRACEABILITY_MATRIX) {
                TraceabilityMatrixScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.CHANGE_IMPACT,
                arguments = listOf(
                    navArgument("reqId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val reqId = backStackEntry.arguments?.getString("reqId")
                ChangeImpactAnalysisScreen(viewModel = viewModel, reqId = reqId)
            }

            // --- 6. AI Assistants & Documentation ---
            composable(AppRoutes.PROJECT_AI_ASSISTANT) {
                ProjectAiAssistantScreen(viewModel = viewModel)
            }
            composable(AppRoutes.DOCUMENT_VIEWER) {
                DocumentViewerScreen(viewModel = viewModel)
            }
            authorizedComposable(
                route = AppRoutes.GENERATE_REQUIREMENTS,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST),
                viewModel = viewModel,
                routeTitle = "Generate Requirements with AI",
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val targetPid = backStackEntry.arguments?.getString("projectId")
                GenerateRequirementsScreen(viewModel = viewModel, targetProjectId = targetPid)
            }
            authorizedComposable(
                route = AppRoutes.SUGGEST_ARCHITECTURE,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.ARCHITECT),
                viewModel = viewModel,
                routeTitle = "Suggest Architecture with AI",
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val targetPid = backStackEntry.arguments?.getString("projectId")
                SuggestArchitectureScreen(viewModel = viewModel, targetProjectId = targetPid)
            }
            authorizedComposable(
                route = AppRoutes.GENERATE_TEST_SUITE,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.TESTER),
                viewModel = viewModel,
                routeTitle = "Generate Test Suite with AI",
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val targetPid = backStackEntry.arguments?.getString("projectId")
                GenerateTestSuiteScreen(viewModel = viewModel, targetProjectId = targetPid)
            }

            // --- 7. Team Management & Admin Pages (Authorized) ---
            authorizedComposable(
                route = AppRoutes.TEAM_MANAGEMENT,
                requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST),
                viewModel = viewModel,
                routeTitle = "Team Management & Governance"
            ) {
                TeamManagementScreen(viewModel = viewModel)
            }
            authorizedComposable(
                route = AppRoutes.INVITE_MEMBER,
                requiredRoles = setOf(ProjectRole.ADMIN),
                viewModel = viewModel,
                routeTitle = "Invite Team Member"
            ) {
                InviteMemberScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.MEMBER_DETAIL,
                arguments = listOf(
                    navArgument("memberId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mId = backStackEntry.arguments?.getString("memberId") ?: ""
                MemberDetailScreen(viewModel = viewModel, memberId = mId)
            }
            authorizedComposable(
                route = AppRoutes.ROLE_MANAGEMENT,
                requiredRoles = setOf(ProjectRole.ADMIN),
                viewModel = viewModel,
                routeTitle = "Role Management & Access Control"
            ) {
                RoleManagementScreen(viewModel = viewModel)
            }

            // --- 8. System, Activity & Profile ---
            composable(AppRoutes.NOTIFICATIONS) {
                NotificationsScreen(viewModel = viewModel)
            }
            composable(AppRoutes.ACTIVITY_LOG) {
                ActivityLogScreen(viewModel = viewModel)
            }
            composable(AppRoutes.GLOBAL_SEARCH) {
                GlobalSearchScreen(viewModel = viewModel)
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(viewModel = viewModel)
            }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        // Floating In-App Notification Banner overlay
        InAppNotificationBanner(
            notification = inAppNotif,
            onDismiss = { viewModel.dismissInAppNotification() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
