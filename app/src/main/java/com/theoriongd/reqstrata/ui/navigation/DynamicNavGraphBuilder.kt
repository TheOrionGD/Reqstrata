package com.theoriongd.reqstrata.ui.navigation
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.theoriongd.reqstrata.data.local.entity.ProjectEntity
import com.theoriongd.reqstrata.domain.auth.CentralizedAuthorizationManager
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.screens.ai.DeveloperAIAssistantScreen
import com.theoriongd.reqstrata.ui.screens.ai.ProjectAiAssistantScreen
import com.theoriongd.reqstrata.ui.screens.ai.RequirementSystemDesignChatScreen
import com.theoriongd.reqstrata.ui.screens.architecture.*
import com.theoriongd.reqstrata.ui.screens.auth.*
import com.theoriongd.reqstrata.ui.screens.design.ApiDesignerScreen
import com.theoriongd.reqstrata.ui.screens.design.DatabaseDesignerScreen
import com.theoriongd.reqstrata.ui.screens.documents.DocumentViewerScreen
import com.theoriongd.reqstrata.ui.screens.profile.ProfileScreen
import com.theoriongd.reqstrata.ui.screens.profile.SettingsScreen
import com.theoriongd.reqstrata.ui.screens.project.*
import com.theoriongd.reqstrata.ui.screens.requirements.*
import com.theoriongd.reqstrata.ui.screens.tasks.DeveloperDashboardScreen
import com.theoriongd.reqstrata.ui.screens.tasks.TaskManagementScreen
import com.theoriongd.reqstrata.ui.screens.team.*
import com.theoriongd.reqstrata.ui.screens.testing.*
import com.theoriongd.reqstrata.ui.screens.traceability.ChangeImpactAnalysisScreen
import com.theoriongd.reqstrata.ui.screens.traceability.TraceabilityMatrixScreen

/**
 * Data class representing a role-authorized navigation destination
 */
data class AuthorizedNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val isBottomBarVisible: Boolean = false,
    val testTag: String = "nav_item_${route.replace("/", "_")}"
)

/**
 * Dynamic navigation builder that reconstructs the NavGraph at runtime based on the
 * user's role and assigned project, ensuring only authorized screens are included
 * in the bottom navigation and menus.
 */
object DynamicNavGraphBuilder {

    /**
     * Compute authorized bottom navigation destinations for the active role and project
     */
    fun getAuthorizedBottomBarDestinations(
        role: ProjectRole,
        hasProject: Boolean
    ): List<AuthorizedNavDestination> {
        return when (role) {
            ProjectRole.ADMIN -> listOf(
                AuthorizedNavDestination(AppRoutes.PROJECT_SELECTION, "Projects", Icons.Default.Dashboard, Screen.ProjectSelection, true),
                AuthorizedNavDestination(AppRoutes.PROJECT_DASHBOARD, "Overview", Icons.Default.Assessment, Screen.ProjectDashboard, true),
                AuthorizedNavDestination(AppRoutes.APPROVAL_CENTER, "Approvals", Icons.Default.Verified, Screen.ApprovalCenter, true),
                AuthorizedNavDestination(AppRoutes.TEAM_MANAGEMENT, "Team", Icons.Default.Groups, Screen.TeamManagement, true),
                AuthorizedNavDestination(AppRoutes.PROJECT_SETTINGS, "Settings", Icons.Default.Tune, Screen.ProjectSettings, true)
            )

            ProjectRole.BUSINESS_ANALYST -> listOf(
                AuthorizedNavDestination(AppRoutes.BA_DASHBOARD, "BA Hub", Icons.Default.Analytics, Screen.BusinessAnalystDashboard, true),
                AuthorizedNavDestination(AppRoutes.REQUIREMENTS_LIST, "Reqs", Icons.AutoMirrored.Filled.ListAlt, Screen.RequirementsList, true),
                AuthorizedNavDestination(AppRoutes.NEW_REQUIREMENT_FORM, "New Spec", Icons.Default.PostAdd, Screen.NewRequirementForm, true),
                AuthorizedNavDestination(AppRoutes.USE_CASES, "Use Cases", Icons.Default.AccountTree, Screen.UseCases, true),
                AuthorizedNavDestination(AppRoutes.GENERATE_REQUIREMENTS, "AI Gen", Icons.Default.AutoAwesome, Screen.GenerateRequirements(), true)
            )

            ProjectRole.ARCHITECT -> listOf(
                AuthorizedNavDestination(AppRoutes.ARCHITECT_DASHBOARD, "Architect", Icons.Default.Architecture, Screen.ArchitectDashboard, true),
                AuthorizedNavDestination(AppRoutes.ARCHITECTURE_WORKSPACE, "Components", Icons.Default.Layers, Screen.ArchitectureWorkspace, true),
                AuthorizedNavDestination(AppRoutes.DATABASE_DESIGNER, "Database", Icons.Default.Storage, Screen.DatabaseDesigner, true),
                AuthorizedNavDestination(AppRoutes.API_DESIGNER, "APIs", Icons.Default.Http, Screen.ApiDesigner, true),
                AuthorizedNavDestination(AppRoutes.PROJECT_AI_ASSISTANT, "AI Design", Icons.Default.AutoAwesome, Screen.ProjectAiAssistant, true)
            )

            ProjectRole.DEVELOPER -> listOf(
                AuthorizedNavDestination(AppRoutes.DEVELOPER_DASHBOARD, "Dev Hub", Icons.Default.Code, Screen.DeveloperDashboard, true),
                AuthorizedNavDestination(AppRoutes.TASK_MANAGEMENT, "Tasks", Icons.Default.Checklist, Screen.TaskManagement, true),
                AuthorizedNavDestination(AppRoutes.API_DESIGNER, "Contracts", Icons.Default.Api, Screen.ApiDesigner, true),
                AuthorizedNavDestination(AppRoutes.REQUIREMENTS_LIST, "Req Specs", Icons.Default.Description, Screen.RequirementsList, true),
                AuthorizedNavDestination(AppRoutes.DEVELOPER_AI_ASSISTANT, "Copilot", Icons.Default.SmartToy, Screen.DeveloperAiAssistant, true)
            )

            ProjectRole.TESTER -> listOf(
                AuthorizedNavDestination(AppRoutes.TESTER_DASHBOARD, "QA Hub", Icons.AutoMirrored.Filled.FactCheck, Screen.TesterDashboard, true),
                AuthorizedNavDestination(AppRoutes.TESTING_WORKSPACE, "Suites", Icons.AutoMirrored.Filled.Rule, Screen.TestingWorkspace, true),
                AuthorizedNavDestination(AppRoutes.TEST_EXECUTION_WORKSPACE, "Execute", Icons.Default.PlayCircle, Screen.TestExecutionWorkspace, true),
                AuthorizedNavDestination(AppRoutes.COVERAGE_DASHBOARD, "Coverage", Icons.Default.PieChart, Screen.CoverageDashboard, true),
                AuthorizedNavDestination(AppRoutes.TRACEABILITY_MATRIX, "Matrix", Icons.Default.Polyline, Screen.TraceabilityMatrix, true)
            )
        }
    }

    /**
     * Dynamically builds role-authorized composable destinations into the NavGraph
     */
    fun buildAuthorizedGraph(
        builder: NavGraphBuilder,
        viewModel: MainViewModel,
        activeRole: ProjectRole,
        currentProject: ProjectEntity?
    ) {
        with(builder) {
            // Unrestricted Public / Auth Destinations
            composable(AppRoutes.SPLASH) { SplashScreen(viewModel = viewModel) }
            composable(AppRoutes.ONBOARDING) { OnboardingScreen(viewModel = viewModel) }
            composable(AppRoutes.LOGIN) { LoginScreen(viewModel = viewModel) }
            composable(AppRoutes.REGISTER) { RegisterScreen(viewModel = viewModel) }
            composable(AppRoutes.TENANT_SEPARATION_REGISTER) { TenantSeparationRegisterScreen(viewModel = viewModel) }
            composable(AppRoutes.FORGOT_PASSWORD) { ForgotPasswordScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.EMAIL_VERIFICATION,
                arguments = listOf(navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                EmailVerificationScreen(viewModel = viewModel, emailArg = backStackEntry.arguments?.getString("email"))
            }
            composable(
                route = AppRoutes.RESET_PASSWORD,
                arguments = listOf(navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                ResetPasswordScreen(viewModel = viewModel, emailArg = backStackEntry.arguments?.getString("email"))
            }

            // Universal Project Selection / Main Dashboard
            composable(AppRoutes.PROJECT_SELECTION) {
                MainDashboardProjectsScreen(viewModel = viewModel)
            }
            composable(AppRoutes.PROJECT_DASHBOARD) {
                val activeRole by viewModel.currentRole.collectAsState()
                when (activeRole) {
                    ProjectRole.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
                    ProjectRole.BUSINESS_ANALYST -> BusinessAnalystDashboardScreen(viewModel = viewModel)
                    ProjectRole.ARCHITECT -> ArchitectDashboardScreen(viewModel = viewModel)
                    ProjectRole.DEVELOPER -> DeveloperDashboardScreen(viewModel = viewModel)
                    ProjectRole.TESTER -> TesterDashboardScreen(viewModel = viewModel)
                }
            }
            composable(AppRoutes.PROJECT_OVERVIEW) {
                ProjectOverviewScreen(viewModel = viewModel)
            }

            // Chat-like System Design Interface (Gemini Retrofit integrated)
            composable(AppRoutes.PROJECT_AI_ASSISTANT) {
                RequirementSystemDesignChatScreen(viewModel = viewModel)
            }

            // Role-Guarded Destinations: Always registered in NavGraph to prevent crashes, guarded by AuthorizedRoute
            composable(
                route = AppRoutes.CREATE_PROJECT,
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN), viewModel = viewModel, routeTitle = "Create New Project") {
                    CreateProjectScreen(viewModel = viewModel)
                }
            }
            composable(AppRoutes.ADMIN_DASHBOARD) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN), viewModel = viewModel, routeTitle = "Executive Administration Dashboard") {
                    AdminDashboardScreen(viewModel = viewModel)
                }
            }
            composable(AppRoutes.PROJECT_SETTINGS) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN), viewModel = viewModel, routeTitle = "Project Settings & Governance") {
                    ProjectSettingsScreen(viewModel = viewModel)
                }
            }
            composable(
                route = AppRoutes.INVITE_MEMBER,
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) {
                AuthorizedRoute(
                    requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.ARCHITECT, ProjectRole.BUSINESS_ANALYST),
                    viewModel = viewModel,
                    routeTitle = "Hierarchical User Provisioning"
                ) {
                    InviteMemberScreen(viewModel = viewModel)
                }
            }
            composable(AppRoutes.ROLE_MANAGEMENT) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN), viewModel = viewModel, routeTitle = "Role Permissions & Authority Matrix") {
                    RoleManagementScreen(viewModel = viewModel)
                }
            }

            composable(AppRoutes.APPROVAL_CENTER) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST, ProjectRole.ARCHITECT), viewModel = viewModel, routeTitle = "Requirements Approval Center") {
                    ApprovalCenterScreen(viewModel = viewModel)
                }
            }

            composable(AppRoutes.BA_DASHBOARD) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST), viewModel = viewModel, routeTitle = "Business Analyst Workspace") {
                    BusinessAnalystDashboardScreen(viewModel = viewModel)
                }
            }
            composable(
                route = AppRoutes.NEW_REQUIREMENT_FORM,
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST), viewModel = viewModel, routeTitle = "Author Requirement Specification") {
                    NewRequirementFormScreen(viewModel = viewModel)
                }
            }
            composable(
                route = AppRoutes.CREATE_EDIT_REQUIREMENT,
                arguments = listOf(navArgument("reqId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) { backStackEntry ->
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST), viewModel = viewModel, routeTitle = "Edit Requirement Specification") {
                    CreateEditRequirementScreen(viewModel = viewModel, reqId = backStackEntry.arguments?.getString("reqId"))
                }
            }
            composable(
                route = AppRoutes.GENERATE_REQUIREMENTS,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) { backStackEntry ->
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST), viewModel = viewModel, routeTitle = "AI Requirement Synthesizer") {
                    GenerateRequirementsScreen(viewModel = viewModel, targetProjectId = backStackEntry.arguments?.getString("projectId"))
                }
            }
            composable(AppRoutes.TEAM_MANAGEMENT) {
                AuthorizedRoute(requiredRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST), viewModel = viewModel, routeTitle = "Project Team Governance") {
                    TeamManagementScreen(viewModel = viewModel)
                }
            }

            // Requirements & Use Cases (Read available to all members)
            composable(AppRoutes.REQUIREMENTS_LIST) { RequirementsListScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.REQUIREMENT_DETAIL,
                arguments = listOf(navArgument("reqId") { type = NavType.StringType }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                RequirementDetailScreen(viewModel = viewModel, reqId = backStackEntry.arguments?.getString("reqId") ?: "")
            }
            composable(
                route = AppRoutes.REQUIREMENT_COMPARISON,
                arguments = listOf(
                    navArgument("reqId") { type = NavType.StringType },
                    navArgument("v1") { type = NavType.IntType; defaultValue = 1 },
                    navArgument("v2") { type = NavType.IntType; defaultValue = 2 }
                )
            ) { backStackEntry ->
                RequirementComparisonScreen(
                    viewModel = viewModel,
                    reqId = backStackEntry.arguments?.getString("reqId") ?: "",
                    v1Num = backStackEntry.arguments?.getInt("v1") ?: 1,
                    v2Num = backStackEntry.arguments?.getInt("v2") ?: 2
                )
            }
            composable(
                route = AppRoutes.REQUIREMENT_AI_ANALYSIS,
                arguments = listOf(navArgument("reqId") { type = NavType.StringType }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                RequirementAiAnalysisScreen(viewModel = viewModel, reqId = backStackEntry.arguments?.getString("reqId") ?: "")
            }
            composable(AppRoutes.USE_CASES) { UseCasesScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.USE_CASE_DETAIL,
                arguments = listOf(navArgument("useCaseId") { type = NavType.StringType }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                UseCaseDetailScreen(viewModel = viewModel, useCaseId = backStackEntry.arguments?.getString("useCaseId") ?: "")
            }

            // Architecture Workspace
            composable(AppRoutes.ARCHITECT_DASHBOARD) { ArchitectDashboardScreen(viewModel = viewModel) }
            composable(AppRoutes.ARCHITECTURE_WORKSPACE) { ArchitectureWorkspaceScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.ARCHITECTURE_COMPONENT_DETAIL,
                arguments = listOf(navArgument("componentId") { type = NavType.StringType }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                ArchitectureComponentDetailScreen(viewModel = viewModel, componentId = backStackEntry.arguments?.getString("componentId") ?: "")
            }
            composable(AppRoutes.ARCHITECTURE_DECISIONS) { ArchitectureDecisionsScreen(viewModel = viewModel) }
            composable(AppRoutes.UML_STUDIO) { UmlStudioScreen(viewModel = viewModel) }
            composable(AppRoutes.DATABASE_DESIGNER) { DatabaseDesignerScreen(viewModel = viewModel) }
            composable(AppRoutes.API_DESIGNER) { ApiDesignerScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.SUGGEST_ARCHITECTURE,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) { backStackEntry ->
                SuggestArchitectureScreen(viewModel = viewModel, targetProjectId = backStackEntry.arguments?.getString("projectId"))
            }

            // Developer Hub
            composable(AppRoutes.DEVELOPER_DASHBOARD) { DeveloperDashboardScreen(viewModel = viewModel) }
            composable(AppRoutes.DEVELOPER_AI_ASSISTANT) { DeveloperAIAssistantScreen(viewModel = viewModel) }
            composable(AppRoutes.TASK_MANAGEMENT) { TaskManagementScreen(viewModel = viewModel) }

            // QA & Testing Hub
            composable(AppRoutes.TESTER_DASHBOARD) { TesterDashboardScreen(viewModel = viewModel) }
            composable(AppRoutes.TESTING_WORKSPACE) { TestingWorkspaceScreen(viewModel = viewModel) }
            composable(AppRoutes.TEST_EXECUTION_WORKSPACE) { TestExecutionWorkspaceScreen(viewModel = viewModel) }
            composable(AppRoutes.COVERAGE_DASHBOARD) { CoverageDashboardScreen(viewModel = viewModel) }
            composable(AppRoutes.TRACEABILITY_MATRIX) { TraceabilityMatrixScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.CHANGE_IMPACT,
                arguments = listOf(navArgument("reqId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                ChangeImpactAnalysisScreen(viewModel = viewModel, reqId = backStackEntry.arguments?.getString("reqId"))
            }
            composable(
                route = AppRoutes.GENERATE_TEST_SUITE,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.formModalExit() }
            ) { backStackEntry ->
                GenerateTestSuiteScreen(viewModel = viewModel, targetProjectId = backStackEntry.arguments?.getString("projectId"))
            }

            // Universal Documents & Shared Utilities
            composable(AppRoutes.DOCUMENT_VIEWER) { DocumentViewerScreen(viewModel = viewModel) }
            composable(AppRoutes.NOTIFICATIONS) { NotificationsScreen(viewModel = viewModel) }
            composable(AppRoutes.ACTIVITY_LOG) { ActivityLogScreen(viewModel = viewModel) }
            composable(AppRoutes.GLOBAL_SEARCH) { GlobalSearchScreen(viewModel = viewModel) }
            composable(AppRoutes.PROFILE) { ProfileScreen(viewModel = viewModel) }
            composable(AppRoutes.SETTINGS) { SettingsScreen(viewModel = viewModel) }
            composable(
                route = AppRoutes.MEMBER_DETAIL,
                arguments = listOf(navArgument("memberId") { type = NavType.StringType }),
                enterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                exitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() },
                popEnterTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailEnter() },
                popExitTransition = { com.theoriongd.reqstrata.ui.motion.MotionTransition.hierarchicalDetailExit() }
            ) { backStackEntry ->
                MemberDetailScreen(viewModel = viewModel, memberId = backStackEntry.arguments?.getString("memberId") ?: "")
            }
        }
    }
}

/**
 * Dynamic Bottom Navigation Bar that automatically reflects authorized screens
 * matching the user's role and assigned project.
 */
@Composable
fun DynamicRoleBottomBar(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeRole by viewModel.currentRole.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val destinations = remember(activeRole, currentProject) {
        DynamicNavGraphBuilder.getAuthorizedBottomBarDestinations(
            role = activeRole,
            hasProject = currentProject != null
        )
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 8.dp,
        modifier = modifier.testTag("dynamic_role_bottom_bar")
    ) {
        destinations.forEach { dest ->
            val isSelected = currentScreen == dest.screen

            NavigationBarItem(
                selected = isSelected,
                onClick = { viewModel.navigateTo(dest.screen) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.title
                    )
                },
                label = {
                    Text(
                        text = dest.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag(dest.testTag)
            )
        }
    }
}
