package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.GeminiApiClient
import com.example.data.repository.*
import com.example.domain.auth.CentralizedAuthorizationManager
import com.example.domain.model.*
import com.example.ui.theme.AppColorPalette
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class Screen {
    data object Splash : Screen()
    data object Onboarding : Screen()
    data object Login : Screen()
    data object Register : Screen()
    data object ForgotPassword : Screen()
    data class EmailVerification(val email: String? = null) : Screen()
    data class ResetPassword(val email: String? = null) : Screen()
    data object ProjectSelection : Screen()
    data object CreateProject : Screen()
    data class GenerateRequirements(val targetProjectId: String? = null) : Screen()
    data class SuggestArchitecture(val targetProjectId: String? = null) : Screen()
    data class GenerateTestSuite(val targetProjectId: String? = null) : Screen()
    data object ProjectDashboard : Screen()
    data object AdminDashboard : Screen()
    data object ApprovalCenter : Screen()
    data object ChangeImpactDashboard : Screen()
    data object ArchitectDashboard : Screen()
    data object DeveloperDashboard : Screen()
    data object DeveloperAiAssistant : Screen()
    data object TesterDashboard : Screen()
    data object TestExecutionWorkspace : Screen()
    data object ProjectOverview : Screen()
    data object ProjectSettings : Screen()
    data object BusinessAnalystDashboard : Screen()
    data object RequirementsList : Screen()
    data class RequirementDetail(val reqId: String) : Screen()
    data class CreateEditRequirement(val reqId: String? = null) : Screen()
    data class RequirementComparison(val reqId: String, val v1: Int = 1, val v2: Int = 2) : Screen()
    data class RequirementAiAnalysis(val reqId: String) : Screen()
    data object NewRequirementForm : Screen()
    data object UseCases : Screen()
    data class UseCaseDetail(val useCaseId: String) : Screen()
    data object ArchitectureWorkspace : Screen()
    data class ArchitectureComponentDetail(val componentId: String) : Screen()
    data object ArchitectureDecisions : Screen()
    data object UmlStudio : Screen()
    data object DatabaseDesigner : Screen()
    data object ApiDesigner : Screen()
    data object TaskManagement : Screen()
    data object TestingWorkspace : Screen()
    data object CoverageDashboard : Screen()
    data object TraceabilityMatrix : Screen()
    data class ChangeImpact(val reqId: String? = null) : Screen()
    data object ProjectAiAssistant : Screen()
    data object DocumentViewer : Screen()
    data object TeamManagement : Screen()
    data object InviteMember : Screen()
    data class MemberDetail(val memberId: String) : Screen()
    data object RoleManagement : Screen()
    data object Notifications : Screen()
    data object ActivityLog : Screen()
    data object GlobalSearch : Screen()
    data object Profile : Screen()
    data object Settings : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val authRepo = AuthRepository(db)
    val projectRepo = ProjectRepository(db)
    val reqRepo = RequirementRepository(db)
    val archRepo = ArchitectureRepository(db)
    val dbDesignRepo = DatabaseDesignRepository(db)
    val apiRepo = ApiRepository(db)
    val taskRepo = TaskRepository(db)
    val testRepo = TestRepository(db)
    val traceRepo = TraceabilityRepository(db)
    val docRepo = DocumentRepository(db)
    val notifRepo = NotificationRepository(db)
    val centralizedNotifRepo = CentralizedNotificationRepository(db)
    val actRepo = ActivityRepository(db)

    // Current State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _currentRole = MutableStateFlow<ProjectRole>(ProjectRole.ADMIN)
    val currentRole: StateFlow<ProjectRole> = _currentRole.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Active Database Accounts & Role-Based Access (No Firebase Auth required)
    private val _activeDatabaseAccounts = MutableStateFlow<List<UserEntity>>(emptyList())
    val activeDatabaseAccounts: StateFlow<List<UserEntity>> = _activeDatabaseAccounts.asStateFlow()

    // Automated Tenant Separation & Strict Isolation
    private val _isTenantFilterStrict = MutableStateFlow(true)
    val isTenantFilterStrict: StateFlow<Boolean> = _isTenantFilterStrict.asStateFlow()

    fun toggleTenantFilterStrict() {
        _isTenantFilterStrict.value = !_isTenantFilterStrict.value
    }

    // In-App Notification Stream
    val inAppNotification = com.example.ui.notifications.CentralizedNotificationService.inAppNotification

    init {
        com.example.ui.notifications.CentralizedNotificationService.init(application, centralizedNotifRepo)
        checkInitialSession()
        loadActiveDatabaseAccounts()
        loadThemePreferences()
    }

    fun loadActiveDatabaseAccounts() {
        viewModelScope.launch {
            val accounts = authRepo.getActiveDatabaseAccounts()
            _activeDatabaseAccounts.value = accounts
        }
    }

    fun dismissInAppNotification() {
        com.example.ui.notifications.AppNotificationManager.dismissInApp()
    }

    fun postNotification(
        title: String,
        message: String,
        type: com.example.ui.notifications.NotificationType = com.example.ui.notifications.NotificationType.SUCCESS
    ) {
        val app = getApplication<Application>()
        com.example.ui.notifications.AppNotificationManager.notify(app, title, message, type)

        // Store notification record in Room database
        viewModelScope.launch {
            val user = _currentUser.value
            val proj = _currentProject.value
            if (user != null) {
                notifRepo.sendNotification(
                    projectId = proj?.id ?: "global",
                    recipientUserId = user.id,
                    title = title,
                    message = message,
                    targetType = "REQUIREMENT",
                    targetId = proj?.id ?: ""
                )
            }
        }
    }

    fun mapTitleToRole(titleOrRole: String): ProjectRole {
        val lower = titleOrRole.lowercase()
        return when {
            lower.contains("admin") || lower.contains("owner") || lower.contains("principal") -> ProjectRole.ADMIN
            lower.contains("analyst") || lower.contains("requirement") || lower.contains("ba") -> ProjectRole.BUSINESS_ANALYST
            lower.contains("architect") -> ProjectRole.ARCHITECT
            lower.contains("developer") || lower.contains("engineer") -> ProjectRole.DEVELOPER
            lower.contains("tester") || lower.contains("qa") || lower.contains("quality") -> ProjectRole.TESTER
            else -> ProjectRole.DEVELOPER
        }
    }

    fun getRoleDefaultScreen(role: ProjectRole): Screen {
        return when (role) {
            ProjectRole.ADMIN -> Screen.AdminDashboard
            ProjectRole.BUSINESS_ANALYST -> Screen.BusinessAnalystDashboard
            ProjectRole.ARCHITECT -> Screen.ArchitectDashboard
            ProjectRole.DEVELOPER -> Screen.DeveloperDashboard
            ProjectRole.TESTER -> Screen.TesterDashboard
        }
    }

    fun updateProjectSettings(
        name: String,
        description: String,
        domain: String,
        techStack: String,
        methodology: String,
        status: String
    ) {
        val proj = _currentProject.value ?: return
        val user = _currentUser.value
        viewModelScope.launch {
            val updated = projectRepo.updateProject(
                project = proj,
                name = name,
                description = description,
                domain = domain,
                techStack = techStack,
                methodology = methodology,
                status = status,
                actor = user?.fullName ?: "Admin"
            )
            _currentProject.value = updated
            postNotification("Project Settings Saved", "Project '${updated.name}' configuration updated.")
        }
    }

    fun archiveCurrentProject() {
        val proj = _currentProject.value ?: return
        val user = _currentUser.value
        viewModelScope.launch {
            val updated = projectRepo.archiveProject(proj, user?.fullName ?: "Admin")
            _currentProject.value = updated
            postNotification("Project Archived", "Project '${updated.name}' has been marked as Archived.")
        }
    }

    fun loginWithActiveAccount(user: UserEntity) {
        viewModelScope.launch {
            val res = authRepo.loginActiveAccountDirect(user)
            res.onSuccess { u ->
                _currentUser.value = u
                val role = mapTitleToRole(u.titleOrRole)
                _currentRole.value = role

                // Auto-bind active project from isolated tenant if available
                val tenantProjects = projectRepo.getProjectsForTenant(u.tenantId).firstOrNull() ?: emptyList()
                val projects = if (tenantProjects.isNotEmpty()) tenantProjects else (projectRepo.getAllProjects().firstOrNull() ?: emptyList())
                if (projects.isNotEmpty() && _currentProject.value == null) {
                    _currentProject.value = projects.first()
                }

                postNotification(
                    title = "Active Account Verified",
                    message = "Authenticated as ${u.fullName} (${role.title}). Role workspace activated.",
                    type = com.example.ui.notifications.NotificationType.SUCCESS
                )

                _currentScreen.value = getRoleDefaultScreen(role)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Authentication failed."
            }
        }
    }

    private fun checkInitialSession() {
        viewModelScope.launch {
            val user = authRepo.getCurrentUser()
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = Screen.ProjectSelection
            } else {
                _currentScreen.value = Screen.Onboarding
            }
        }
    }

    fun navigateTo(screen: Screen) {
        val role = _currentRole.value
        if (!CentralizedAuthorizationManager.isRouteAllowed(screen, role)) {
            val required = CentralizedAuthorizationManager.getRequiredRolesForScreen(screen)
            val msg = "Access Denied: Role '${role.title}' lacks authorization for ${screen.javaClass.simpleName}." +
                    (if (required != null) " (Requires: ${required.joinToString { it.title }})" else "")
            _errorMessage.value = msg
            com.example.ui.notifications.CentralizedNotificationService.showInApp(
                title = "Access Restricted",
                message = "Role '${role.title}' is not authorized to access this section.",
                type = com.example.ui.notifications.NotificationType.ALERT
            )
            return
        }
        screenBackStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun canNavigateTo(screen: Screen): Boolean {
        return CentralizedAuthorizationManager.isRouteAllowed(screen, _currentRole.value)
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    fun selectProject(project: ProjectEntity) {
        _currentProject.value = project
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                val role = CentralizedAuthorizationManager.getVerifiedRole(user.id, project.id)
                _currentRole.value = role
                val defaultScreen = getRoleDefaultScreen(role)
                screenBackStack.add(_currentScreen.value)
                _currentScreen.value = defaultScreen
            } else {
                navigateTo(Screen.ProjectDashboard)
            }
        }
    }

    fun switchRoleForTesting(role: ProjectRole) {
        _currentRole.value = role
        _successMessage.value = "Active role changed to ${role.title}"
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun register(fullName: String, email: String, pass: String, confirmPass: String) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "All fields are required."
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            return
        }
        if (pass != confirmPass) {
            _errorMessage.value = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            val res = authRepo.register(fullName, email, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                _successMessage.value = "Registration successful! Automated Tenant '${user.tenantName}' isolated and activated."
                postNotification(
                    title = "Automated Tenant Isolated",
                    message = "Workspace '${user.tenantName}' provisioned with immediate access (Zero admin approval required).",
                    type = com.example.ui.notifications.NotificationType.SUCCESS
                )
                loadActiveDatabaseAccounts()
                navigateTo(Screen.ProjectSelection)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Registration failed."
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please provide email and password."
            return
        }
        viewModelScope.launch {
            val res = authRepo.login(email, pass)
            res.onSuccess { user ->
                _currentUser.value = user
                val role = mapTitleToRole(user.titleOrRole)
                _currentRole.value = role

                val tenantProjects = projectRepo.getProjectsForTenant(user.tenantId).firstOrNull() ?: emptyList()
                val projects = if (tenantProjects.isNotEmpty()) tenantProjects else (projectRepo.getAllProjects().firstOrNull() ?: emptyList())
                if (projects.isNotEmpty() && _currentProject.value == null) {
                    _currentProject.value = projects.first()
                }

                postNotification(
                    title = "Database Account Verified",
                    message = "Signed in as ${user.fullName} (${role.title}) • Tenant: ${user.tenantName}",
                    type = com.example.ui.notifications.NotificationType.SUCCESS
                )

                _currentScreen.value = getRoleDefaultScreen(role)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Authentication failed."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _currentUser.value = null
            _currentProject.value = null
            screenBackStack.clear()
            _currentScreen.value = Screen.Login
        }
    }

    fun createProject(
        name: String,
        description: String,
        domain: String,
        projectType: String,
        techStack: String,
        methodology: String,
        visibility: String
    ) {
        val user = _currentUser.value ?: return
        if (name.isBlank() || description.isBlank()) {
            _errorMessage.value = "Project name and description are required."
            return
        }
        viewModelScope.launch {
            val project = projectRepo.createProject(
                name, description, domain, projectType, techStack, methodology, visibility, user
            )
            _successMessage.value = "Project '${project.name}' initialized."
            selectProject(project)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            val user = _currentUser.value
            projectRepo.deleteProject(project.id, user?.fullName ?: "User", user?.id)
            if (_currentProject.value?.id == project.id) {
                _currentProject.value = null
            }
            _successMessage.value = "Project '${project.name}' deleted from database."
        }
    }

    fun saveGeneratedRequirements(
        projectId: String,
        selectedRequirements: List<GeneratedRequirementItem>,
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (selectedRequirements.isEmpty()) {
            _errorMessage.value = "No requirements selected to save."
            return
        }
        viewModelScope.launch {
            selectedRequirements.forEach { item ->
                val reqType = when {
                    item.type.equals("Non-Functional", ignoreCase = true) -> {
                        when (item.category.lowercase()) {
                            "security" -> RequirementType.SECURITY
                            "performance" -> RequirementType.PERFORMANCE
                            "usability" -> RequirementType.USABILITY
                            "reliability" -> RequirementType.RELIABILITY
                            "compliance" -> RequirementType.COMPLIANCE
                            else -> RequirementType.NON_FUNCTIONAL
                        }
                    }
                    else -> RequirementType.FUNCTIONAL
                }

                val priority = when (item.priority.lowercase()) {
                    "critical" -> RequirementPriority.CRITICAL
                    "high" -> RequirementPriority.HIGH
                    "medium" -> RequirementPriority.MEDIUM
                    else -> RequirementPriority.LOW
                }

                val descriptionWithMeta = buildString {
                    append(item.description)
                    if (item.userStory.isNotBlank()) {
                        append("\n\nUser Story: ")
                        append(item.userStory)
                    }
                    if (item.verificationMetric.isNotBlank()) {
                        append("\n\nVerification Metric: ")
                        append(item.verificationMetric)
                    }
                }

                val criteria = if (item.acceptanceCriteria.isNotEmpty()) {
                    item.acceptanceCriteria
                } else if (item.verificationMetric.isNotBlank()) {
                    listOf("Given system under test, when verified against metric: ${item.verificationMetric}, then verification succeeds.")
                } else {
                    listOf("Given valid operational context, then ${item.title} shall be satisfied.")
                }

                reqRepo.createRequirement(
                    projectId = projectId,
                    title = item.title,
                    description = descriptionWithMeta,
                    type = reqType,
                    priority = priority,
                    acceptanceCriteria = criteria,
                    author = user
                )
            }
            _successMessage.value = "Saved ${selectedRequirements.size} requirements to project."
            onComplete()
        }
    }

    fun saveArchitectureSuggestionToProject(
        projectId: String,
        suggestion: GeneratedArchitectureSuggestion,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val actorName = user.fullName

            // 1. Save selected components
            val selectedComps = suggestion.components.filter { it.isSelected }
            selectedComps.forEach { comp ->
                archRepo.addComponent(
                    projectId = projectId,
                    name = comp.name,
                    style = suggestion.architectureStyle,
                    layer = comp.layer,
                    responsibilities = comp.responsibilities,
                    techStack = comp.techStack,
                    dependencies = comp.dependencies,
                    linkedReqIds = emptyList(),
                    actor = actorName
                )
            }

            // 2. Save selected database tables
            val selectedTables = suggestion.databaseTables.filter { it.isSelected }
            selectedTables.forEach { table ->
                val fieldsJson = JSONArray().apply {
                    table.columns.forEach { col ->
                        put(
                            JSONObject()
                                .put("name", col.name)
                                .put("type", col.type)
                                .put("pk", col.isPrimaryKey)
                                .put("nullable", col.isNullable)
                                .put("unique", col.isUnique)
                        )
                    }
                }.toString()

                val relJson = if (table.relationshipsSummary.isNotBlank()) {
                    JSONArray().apply {
                        put(JSONObject().put("target", table.relationshipsSummary).put("type", "Rel"))
                    }.toString()
                } else "[]"

                dbDesignRepo.addEntity(
                    projectId = projectId,
                    name = table.tableName,
                    description = table.description,
                    fieldsJson = fieldsJson,
                    relationshipsJson = relJson,
                    indexesJson = JSONArray(table.indexes).toString(),
                    actor = actorName
                )
            }

            // 3. Save selected API endpoints
            val selectedApis = suggestion.apiEndpoints.filter { it.isSelected }
            selectedApis.forEach { api ->
                apiRepo.addEndpoint(
                    projectId = projectId,
                    method = api.method,
                    path = api.path,
                    description = "${api.summary}. ${api.description}".trim(),
                    authRequired = api.authRequired,
                    reqSchema = api.requestSchema,
                    respSchema = api.responseSchema,
                    linkedReqId = "",
                    actor = actorName
                )
            }

            // 4. Save ADRs
            suggestion.architecturalDecisions.forEach { adr ->
                archRepo.addDecision(
                    projectId = projectId,
                    title = adr.title,
                    context = adr.context,
                    decision = adr.decision,
                    consequences = adr.consequences,
                    actor = actorName
                )
            }

            actRepo.logActivity(
                projectId = projectId,
                actor = actorName,
                action = "APPLIED_ARCHITECTURE_SUGGESTION",
                details = "Applied Gemini system architecture with ${selectedComps.size} components, ${selectedTables.size} database tables, and ${selectedApis.size} endpoints."
            )

            _successMessage.value = "Architecture, Database Schema, and API specifications saved to project."
            onComplete()
        }
    }

    fun saveGeneratedTestSuiteToProject(
        projectId: String,
        testSuiteResult: GeneratedTestSuiteResult,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val actorName = user.fullName
            val projectReqs = reqRepo.getRequirements(projectId).firstOrNull() ?: emptyList()

            // 1. Create or get test suite
            val suite = testRepo.createSuite(
                projectId = projectId,
                name = testSuiteResult.suiteName.ifBlank { "Automated Test Suite" },
                type = "Regression",
                description = testSuiteResult.summary.ifBlank { "Automated verification suite generated with Gemini API." }
            )

            val allSelectedCases = (testSuiteResult.unitTests + testSuiteResult.integrationTests).filter { it.isSelected }

            allSelectedCases.forEach { tc ->
                // Try linking to matching requirement by code
                val matchedReq = projectReqs.firstOrNull { it.code.equals(tc.linkedRequirementCode, ignoreCase = true) }
                    ?: projectReqs.firstOrNull()
                val linkedReqId = matchedReq?.id ?: ""

                testRepo.createTestCase(
                    projectId = projectId,
                    suiteId = suite.id,
                    linkedReqId = linkedReqId,
                    title = "[${tc.testType}] ${tc.title}",
                    preconditions = tc.preconditions.ifBlank { "System initialized with nominal state." },
                    testData = tc.testData.ifBlank { "Valid test fixture payload" },
                    steps = tc.steps.ifEmpty { listOf("Execute action", "Verify outcome") },
                    expectedResult = tc.expectedResult,
                    priority = tc.priority,
                    severity = tc.severity,
                    actor = actorName
                )
            }

            actRepo.logActivity(
                projectId = projectId,
                actor = actorName,
                action = "GENERATED_TEST_SUITE",
                details = "Generated test suite '${suite.name}' with ${allSelectedCases.size} unit & integration test cases."
            )

            _successMessage.value = "Saved suite with ${allSelectedCases.size} test cases to project database."
            onComplete()
        }
    }

    // Theme & Dynamic Color State (Material 3)
    val isDarkMode = MutableStateFlow(true)
    val useDynamicColors = MutableStateFlow(false)
    val selectedColorPalette = MutableStateFlow(AppColorPalette.VIOLET)

    private fun loadThemePreferences() {
        viewModelScope.launch {
            try {
                val darkSetting = db.settingsDao().getSetting("theme_is_dark")
                if (darkSetting != null) {
                    isDarkMode.value = darkSetting.toBooleanStrictOrNull() ?: true
                }
                val dynamicSetting = db.settingsDao().getSetting("theme_dynamic_color")
                if (dynamicSetting != null) {
                    useDynamicColors.value = dynamicSetting.toBooleanStrictOrNull() ?: false
                }
                val paletteSetting = db.settingsDao().getSetting("theme_color_palette")
                if (paletteSetting != null) {
                    selectedColorPalette.value = AppColorPalette.fromId(paletteSetting)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to load theme preferences: ${e.message}")
            }
        }
    }

    fun toggleDarkMode() {
        setDarkMode(!isDarkMode.value)
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        viewModelScope.launch {
            try {
                db.settingsDao().setSetting(AppSettingEntity("theme_is_dark", enabled.toString()))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to persist dark mode: ${e.message}")
            }
        }
    }

    fun toggleDynamicColors() {
        val newVal = !useDynamicColors.value
        useDynamicColors.value = newVal
        viewModelScope.launch {
            try {
                db.settingsDao().setSetting(AppSettingEntity("theme_dynamic_color", newVal.toString()))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to persist dynamic colors: ${e.message}")
            }
        }
    }

    fun setColorPalette(palette: AppColorPalette) {
        selectedColorPalette.value = palette
        viewModelScope.launch {
            try {
                db.settingsDao().setSetting(AppSettingEntity("theme_color_palette", palette.id))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to persist color palette: ${e.message}")
            }
        }
    }

    // MongoDB Atlas Cloud Integration (godfreytrprof_db_user / hellotheoriongd.rbxbuxe.mongodb.net)
    val mongoStatus = com.example.data.remote.mongo.MongoDbService.connectionStatus
    val mongoStats = com.example.data.remote.mongo.MongoDbService.syncStats
    val backgroundSyncMetrics = com.example.data.remote.mongo.sync.MongoBackgroundSyncService.metrics

    fun triggerRealtimeSync() {
        com.example.data.remote.mongo.sync.MongoBackgroundSyncService.forceSyncNow()
    }

    fun syncCurrentProjectToMongo() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            _successMessage.value = "Syncing project artifacts with MongoDB Atlas (${com.example.data.remote.mongo.MongoDbService.MONGODB_CLUSTER})..."
            val reqs = reqRepo.getRequirements(proj.id).firstOrNull() ?: emptyList()
            val comps = archRepo.getComponents(proj.id).firstOrNull() ?: emptyList()
            val tables = dbDesignRepo.getEntities(proj.id).firstOrNull() ?: emptyList()
            val apis = apiRepo.getEndpoints(proj.id).firstOrNull() ?: emptyList()
            val suites = testRepo.getSuites(proj.id).firstOrNull() ?: emptyList()
            val cases = testRepo.getTestCases(proj.id).firstOrNull() ?: emptyList()

            val res = com.example.data.remote.mongo.MongoDbService.syncProjectArtifacts(
                project = proj,
                requirements = reqs,
                components = comps,
                tables = tables,
                endpoints = apis,
                testSuites = suites,
                testCases = cases
            )
            if (res.isSuccess) {
                _successMessage.value = "Synced ${reqs.size} reqs, ${comps.size} arch components & ${tables.size} tables to MongoDB Atlas!"
            } else {
                _errorMessage.value = "MongoDB Sync error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun pingMongoCluster() {
        viewModelScope.launch {
            val res = com.example.data.remote.mongo.MongoDbService.pingCluster()
            if (res.isSuccess) {
                _successMessage.value = res.getOrNull()
            } else {
                _errorMessage.value = "MongoDB ping failed: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateRequirementStatus(reqId: String, status: com.example.domain.model.RequirementStatus, notes: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val req = reqRepo.getRequirement(reqId).firstOrNull() ?: return@launch
            reqRepo.setApprovalStatus(req, status, notes, user)
            _successMessage.value = "${req.code} status set to ${status.displayName}."
        }
    }

    fun executeTestCase(testCaseId: String, status: com.example.domain.model.TestExecutionStatus, notes: String) {
        val user = _currentUser.value
        val userName = user?.fullName ?: "Tester"
        val pId = _currentProject.value?.id ?: ""
        viewModelScope.launch {
            testRepo.recordExecution(pId, testCaseId, status, notes, notes, "", userName, user?.id)
            _successMessage.value = "Test execution recorded as ${status.displayName}."
        }
    }
}
