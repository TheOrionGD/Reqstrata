package com.theoriongd.reqstrata.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.theoriongd.reqstrata.ui.Screen

/**
 * Functional modules across the Reqstrata application.
 */
enum class ProjectModule(val displayName: String) {
    PROJECT_MANAGEMENT("Project Management"),
    TEAM_MANAGEMENT("Team Management"),
    REQUIREMENTS("Requirements"),
    USE_CASES("Use Cases"),
    ARCHITECTURE("Architecture"),
    UML("UML Studio"),
    DATABASE("Database Design"),
    APIS("API Specifications"),
    DEVELOPMENT_TASKS("Development Tasks"),
    TEST_SUITES("Test Suites"),
    TEST_EXECUTION("Test Execution"),
    TRACEABILITY("Traceability Matrix"),
    CHANGE_IMPACT("Change Impact Analysis"),
    DOCUMENTATION("Documentation"),
    AUDIT_LOGS("Activity / Audit Logs"),
    ROLE_MANAGEMENT("Role Governance")
}

enum class PermissionLevel {
    NONE,
    VIEW,
    FULL
}

data class RoleNavigationItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val badgeCount: Int = 0
)

/**
 * Baseline Project Access Policy.
 * Enforces role-based permissions across business logic and navigation.
 * Roles are project-scoped.
 */
object ProjectAccessPolicy {

    fun getPermission(role: ProjectRole, module: ProjectModule): PermissionLevel {
        return when (module) {
            ProjectModule.PROJECT_MANAGEMENT -> when (role) {
                ProjectRole.ADMIN -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.TEAM_MANAGEMENT,
            ProjectModule.AUDIT_LOGS,
            ProjectModule.ROLE_MANAGEMENT -> when (role) {
                ProjectRole.ADMIN -> PermissionLevel.FULL
                else -> PermissionLevel.NONE
            }
            ProjectModule.REQUIREMENTS,
            ProjectModule.USE_CASES -> when (role) {
                ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.ARCHITECTURE,
            ProjectModule.UML,
            ProjectModule.DATABASE,
            ProjectModule.APIS -> when (role) {
                ProjectRole.ADMIN, ProjectRole.ARCHITECT -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.DEVELOPMENT_TASKS -> when (role) {
                ProjectRole.ADMIN, ProjectRole.DEVELOPER -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.TEST_SUITES -> when (role) {
                ProjectRole.ADMIN, ProjectRole.TESTER -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.TEST_EXECUTION -> when (role) {
                ProjectRole.ADMIN, ProjectRole.TESTER -> PermissionLevel.FULL
                ProjectRole.DEVELOPER -> PermissionLevel.VIEW
                else -> PermissionLevel.NONE
            }
            ProjectModule.TRACEABILITY,
            ProjectModule.CHANGE_IMPACT -> when (role) {
                ProjectRole.ADMIN, ProjectRole.ARCHITECT, ProjectRole.TESTER -> PermissionLevel.FULL
                else -> PermissionLevel.VIEW
            }
            ProjectModule.DOCUMENTATION -> PermissionLevel.FULL
        }
    }

    fun canRead(role: ProjectRole, module: ProjectModule): Boolean =
        getPermission(role, module) != PermissionLevel.NONE

    fun canWrite(role: ProjectRole, module: ProjectModule): Boolean =
        getPermission(role, module) == PermissionLevel.FULL

    /**
     * Builds role-specific navigation destinations tailored to the active project role.
     */
    fun getNavigationItems(role: ProjectRole): List<RoleNavigationItem> {
        return when (role) {
            ProjectRole.ADMIN -> listOf(
                RoleNavigationItem("Dashboard", Icons.Default.Dashboard, Screen.AdminDashboard),
                RoleNavigationItem("Projects", Icons.Default.Folder, Screen.ProjectOverview),
                RoleNavigationItem("Team", Icons.Default.Group, Screen.TeamManagement),
                RoleNavigationItem("Approvals", Icons.Default.Verified, Screen.ApprovalCenter),
                RoleNavigationItem("Requirements", Icons.Default.ListAlt, Screen.RequirementsList),
                RoleNavigationItem("Architecture", Icons.Default.AccountTree, Screen.ArchitectureWorkspace),
                RoleNavigationItem("Development", Icons.Default.Task, Screen.TaskManagement),
                RoleNavigationItem("Testing", Icons.Default.FactCheck, Screen.TestingWorkspace),
                RoleNavigationItem("Traceability", Icons.Default.Hub, Screen.TraceabilityMatrix),
                RoleNavigationItem("Impact Analysis", Icons.Default.CompareArrows, Screen.ChangeImpactDashboard),
                RoleNavigationItem("Documents", Icons.AutoMirrored.Filled.MenuBook, Screen.DocumentViewer),
                RoleNavigationItem("Activity Log", Icons.Default.History, Screen.ActivityLog),
                RoleNavigationItem("Settings", Icons.Default.Settings, Screen.ProjectSettings)
            )

            ProjectRole.BUSINESS_ANALYST -> listOf(
                RoleNavigationItem("Dashboard", Icons.Default.Dashboard, Screen.BusinessAnalystDashboard),
                RoleNavigationItem("Requirements", Icons.Default.ListAlt, Screen.RequirementsList),
                RoleNavigationItem("Use Cases", Icons.AutoMirrored.Filled.Assignment, Screen.UseCases),
                RoleNavigationItem("AI Analysis", Icons.Default.AutoAwesome, Screen.GenerateRequirements()),
                RoleNavigationItem("Traceability", Icons.Default.Hub, Screen.TraceabilityMatrix),
                RoleNavigationItem("Documents", Icons.AutoMirrored.Filled.MenuBook, Screen.DocumentViewer),
                RoleNavigationItem("Notifications", Icons.Default.Notifications, Screen.Notifications),
                RoleNavigationItem("Profile", Icons.Default.Person, Screen.Profile)
            )

            ProjectRole.ARCHITECT -> listOf(
                RoleNavigationItem("Dashboard", Icons.Default.Dashboard, Screen.ArchitectDashboard),
                RoleNavigationItem("Requirements", Icons.Default.ListAlt, Screen.RequirementsList),
                RoleNavigationItem("Architecture", Icons.Default.AccountTree, Screen.ArchitectureWorkspace),
                RoleNavigationItem("UML Studio", Icons.Default.Schema, Screen.UmlStudio),
                RoleNavigationItem("Database", Icons.Default.Storage, Screen.DatabaseDesigner),
                RoleNavigationItem("APIs", Icons.Default.Api, Screen.ApiDesigner),
                RoleNavigationItem("Traceability", Icons.Default.Hub, Screen.TraceabilityMatrix),
                RoleNavigationItem("Impact Analysis", Icons.Default.CompareArrows, Screen.ChangeImpactDashboard),
                RoleNavigationItem("Documents", Icons.AutoMirrored.Filled.MenuBook, Screen.DocumentViewer),
                RoleNavigationItem("Profile", Icons.Default.Person, Screen.Profile)
            )

            ProjectRole.DEVELOPER -> listOf(
                RoleNavigationItem("Dashboard", Icons.Default.Dashboard, Screen.DeveloperDashboard),
                RoleNavigationItem("Requirements", Icons.Default.ListAlt, Screen.RequirementsList),
                RoleNavigationItem("Architecture", Icons.Default.AccountTree, Screen.ArchitectureWorkspace),
                RoleNavigationItem("Database", Icons.Default.Storage, Screen.DatabaseDesigner),
                RoleNavigationItem("APIs", Icons.Default.Api, Screen.ApiDesigner),
                RoleNavigationItem("Development", Icons.Default.Task, Screen.TaskManagement),
                RoleNavigationItem("Traceability", Icons.Default.Hub, Screen.TraceabilityMatrix),
                RoleNavigationItem("AI Assistant", Icons.Default.AutoAwesome, Screen.DeveloperAiAssistant),
                RoleNavigationItem("Documents", Icons.AutoMirrored.Filled.MenuBook, Screen.DocumentViewer),
                RoleNavigationItem("Profile", Icons.Default.Person, Screen.Profile)
            )

            ProjectRole.TESTER -> listOf(
                RoleNavigationItem("Dashboard", Icons.Default.Dashboard, Screen.TesterDashboard),
                RoleNavigationItem("Requirements", Icons.Default.ListAlt, Screen.RequirementsList),
                RoleNavigationItem("Test Suites", Icons.Default.FactCheck, Screen.TestingWorkspace),
                RoleNavigationItem("Test Cases", Icons.Default.PlaylistAddCheck, Screen.TestingWorkspace),
                RoleNavigationItem("Execution", Icons.Default.PlayCircle, Screen.TestExecutionWorkspace),
                RoleNavigationItem("Coverage", Icons.Default.PieChart, Screen.CoverageDashboard),
                RoleNavigationItem("Traceability", Icons.Default.Hub, Screen.TraceabilityMatrix),
                RoleNavigationItem("Impact Analysis", Icons.Default.CompareArrows, Screen.ChangeImpactDashboard),
                RoleNavigationItem("Reports", Icons.AutoMirrored.Filled.MenuBook, Screen.DocumentViewer),
                RoleNavigationItem("Profile", Icons.Default.Person, Screen.Profile)
            )
        }
    }
}
