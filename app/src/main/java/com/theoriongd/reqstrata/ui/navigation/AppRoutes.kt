package com.theoriongd.reqstrata.ui.navigation

import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.Screen

/**
 * Centralized Route definitions for the full application ecosystem.
 * Supports NavHost, NavController, and role-based routing from login to
 * management dashboards.
 */
object AppRoutes {
    // Auth & Onboarding Ecosystem
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TENANT_SEPARATION_REGISTER = "tenant_separation_register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val EMAIL_VERIFICATION = "email_verification?email={email}"
    const val RESET_PASSWORD = "reset_password?email={email}"

    // Project Ecosystem & Dashboards
    const val PROJECT_SELECTION = "project_selection"
    const val CREATE_PROJECT = "create_project"
    const val PROJECT_DASHBOARD = "project_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val APPROVAL_CENTER = "approval_center"
    const val ARCHITECT_DASHBOARD = "architect_dashboard"
    const val DEVELOPER_DASHBOARD = "developer_dashboard"
    const val DEVELOPER_AI_ASSISTANT = "developer_ai_assistant"
    const val TESTER_DASHBOARD = "tester_dashboard"
    const val TEST_EXECUTION_WORKSPACE = "test_execution_workspace"
    const val PROJECT_OVERVIEW = "project_overview"
    const val PROJECT_SETTINGS = "project_settings"
    const val BA_DASHBOARD = "ba_dashboard"

    // Requirements & Use Cases
    const val REQUIREMENTS_LIST = "requirements_list"
    const val REQUIREMENT_DETAIL = "requirement_detail/{reqId}"
    const val CREATE_EDIT_REQUIREMENT = "create_edit_requirement?reqId={reqId}"
    const val REQUIREMENT_COMPARISON = "requirement_comparison/{reqId}?v1={v1}&v2={v2}"
    const val REQUIREMENT_AI_ANALYSIS = "requirement_ai_analysis/{reqId}"
    const val NEW_REQUIREMENT_FORM = "new_requirement_form"
    const val USE_CASES = "use_cases"
    const val USE_CASE_DETAIL = "use_case_detail/{useCaseId}"

    // Architecture & Design Studio
    const val ARCHITECTURE_WORKSPACE = "architecture_workspace"
    const val ARCHITECTURE_COMPONENT_DETAIL = "architecture_component_detail/{componentId}"
    const val ARCHITECTURE_DECISIONS = "architecture_decisions"
    const val UML_STUDIO = "uml_studio"
    const val DATABASE_DESIGNER = "database_designer"
    const val API_DESIGNER = "api_designer"

    // Execution, Testing & Quality
    const val TASK_MANAGEMENT = "task_management"
    const val TESTING_WORKSPACE = "testing_workspace"
    const val COVERAGE_DASHBOARD = "coverage_dashboard"
    const val TRACEABILITY_MATRIX = "traceability_matrix"
    const val CHANGE_IMPACT = "change_impact?reqId={reqId}"

    // AI Generators & Workspace Assistants
    const val PROJECT_AI_ASSISTANT = "project_ai_assistant"
    const val DOCUMENT_VIEWER = "document_viewer"
    const val GENERATE_REQUIREMENTS = "generate_requirements?projectId={projectId}"
    const val SUGGEST_ARCHITECTURE = "suggest_architecture?projectId={projectId}"
    const val GENERATE_TEST_SUITE = "generate_test_suite?projectId={projectId}"

    // Team, Governance & Profile
    const val TEAM_MANAGEMENT = "team_management"
    const val INVITE_MEMBER = "invite_member"
    const val MEMBER_DETAIL = "member_detail/{memberId}"
    const val ROLE_MANAGEMENT = "role_management"
    const val NOTIFICATIONS = "notifications"
    const val ACTIVITY_LOG = "activity_log"
    const val GLOBAL_SEARCH = "global_search"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    /**
     * Role-based routing: Resolves the appropriate destination dashboard
     * for a user based on their assigned role in the project.
     */
    fun getRoleDashboardRoute(role: ProjectRole): String {
        return when (role) {
            ProjectRole.ADMIN -> ADMIN_DASHBOARD
            ProjectRole.BUSINESS_ANALYST -> BA_DASHBOARD
            ProjectRole.ARCHITECT -> ARCHITECT_DASHBOARD
            ProjectRole.DEVELOPER -> DEVELOPER_DASHBOARD
            ProjectRole.TESTER -> TESTER_DASHBOARD
        }
    }

    // Helper functions to construct route paths with arguments
    fun buildRequirementDetail(reqId: String) = "requirement_detail/$reqId"
    fun buildCreateEditRequirement(reqId: String? = null) = if (reqId != null) "create_edit_requirement?reqId=$reqId" else "create_edit_requirement"
    fun buildRequirementComparison(reqId: String, v1: Int, v2: Int) = "requirement_comparison/$reqId?v1=$v1&v2=$v2"
    fun buildRequirementAiAnalysis(reqId: String) = "requirement_ai_analysis/$reqId"
    fun buildUseCaseDetail(useCaseId: String) = "use_case_detail/$useCaseId"
    fun buildArchitectureComponentDetail(componentId: String) = "architecture_component_detail/$componentId"
    fun buildMemberDetail(memberId: String) = "member_detail/$memberId"
    fun buildChangeImpact(reqId: String? = null) = if (reqId != null) "change_impact?reqId=$reqId" else "change_impact"
    fun buildEmailVerification(email: String? = null) = if (email != null) "email_verification?email=$email" else "email_verification"
    fun buildResetPassword(email: String? = null) = if (email != null) "reset_password?email=$email" else "reset_password"
    fun buildGenerateRequirements(projectId: String? = null) = if (projectId != null) "generate_requirements?projectId=$projectId" else "generate_requirements"
    fun buildSuggestArchitecture(projectId: String? = null) = if (projectId != null) "suggest_architecture?projectId=$projectId" else "suggest_architecture"
    fun buildGenerateTestSuite(projectId: String? = null) = if (projectId != null) "generate_test_suite?projectId=$projectId" else "generate_test_suite"

    /**
     * Maps a legacy or sealed Screen instance to its NavHost route representation.
     */
    fun Screen.toRoute(): String {
        return when (this) {
            Screen.Splash -> SPLASH
            Screen.Onboarding -> ONBOARDING
            Screen.Login -> LOGIN
            Screen.Register -> REGISTER
            Screen.TenantSeparationRegister -> TENANT_SEPARATION_REGISTER
            Screen.ForgotPassword -> FORGOT_PASSWORD
            is Screen.EmailVerification -> buildEmailVerification(email)
            is Screen.ResetPassword -> buildResetPassword(email)
            Screen.ProjectSelection -> PROJECT_SELECTION
            Screen.CreateProject -> CREATE_PROJECT
            is Screen.GenerateRequirements -> buildGenerateRequirements(targetProjectId)
            is Screen.SuggestArchitecture -> buildSuggestArchitecture(targetProjectId)
            is Screen.GenerateTestSuite -> buildGenerateTestSuite(targetProjectId)
            Screen.ProjectDashboard -> PROJECT_DASHBOARD
            Screen.AdminDashboard -> ADMIN_DASHBOARD
            Screen.ApprovalCenter -> APPROVAL_CENTER
            Screen.ChangeImpactDashboard -> CHANGE_IMPACT
            Screen.ArchitectDashboard -> ARCHITECT_DASHBOARD
            Screen.DeveloperDashboard -> DEVELOPER_DASHBOARD
            Screen.DeveloperAiAssistant -> DEVELOPER_AI_ASSISTANT
            Screen.TesterDashboard -> TESTER_DASHBOARD
            Screen.TestExecutionWorkspace -> TEST_EXECUTION_WORKSPACE
            Screen.ProjectOverview -> PROJECT_OVERVIEW
            Screen.ProjectSettings -> PROJECT_SETTINGS
            Screen.BusinessAnalystDashboard -> BA_DASHBOARD
            Screen.RequirementsList -> REQUIREMENTS_LIST
            is Screen.RequirementDetail -> buildRequirementDetail(reqId)
            is Screen.CreateEditRequirement -> buildCreateEditRequirement(reqId)
            is Screen.RequirementComparison -> buildRequirementComparison(reqId, v1, v2)
            is Screen.RequirementAiAnalysis -> buildRequirementAiAnalysis(reqId)
            Screen.NewRequirementForm -> NEW_REQUIREMENT_FORM
            Screen.UseCases -> USE_CASES
            is Screen.UseCaseDetail -> buildUseCaseDetail(useCaseId)
            Screen.ArchitectureWorkspace -> ARCHITECTURE_WORKSPACE
            is Screen.ArchitectureComponentDetail -> buildArchitectureComponentDetail(componentId)
            Screen.ArchitectureDecisions -> ARCHITECTURE_DECISIONS
            Screen.UmlStudio -> UML_STUDIO
            Screen.DatabaseDesigner -> DATABASE_DESIGNER
            Screen.ApiDesigner -> API_DESIGNER
            Screen.TaskManagement -> TASK_MANAGEMENT
            Screen.TestingWorkspace -> TESTING_WORKSPACE
            Screen.CoverageDashboard -> COVERAGE_DASHBOARD
            Screen.TraceabilityMatrix -> TRACEABILITY_MATRIX
            is Screen.ChangeImpact -> buildChangeImpact(reqId)
            Screen.ProjectAiAssistant -> PROJECT_AI_ASSISTANT
            Screen.DocumentViewer -> DOCUMENT_VIEWER
            Screen.TeamManagement -> TEAM_MANAGEMENT
            Screen.InviteMember -> INVITE_MEMBER
            is Screen.MemberDetail -> buildMemberDetail(memberId)
            Screen.RoleManagement -> ROLE_MANAGEMENT
            Screen.Notifications -> NOTIFICATIONS
            Screen.ActivityLog -> ACTIVITY_LOG
            Screen.GlobalSearch -> GLOBAL_SEARCH
            Screen.Profile -> PROFILE
            Screen.Settings -> SETTINGS
        }
    }
}
