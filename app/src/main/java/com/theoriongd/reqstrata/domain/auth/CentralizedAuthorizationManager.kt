package com.theoriongd.reqstrata.domain.auth

import android.util.Log
import com.theoriongd.reqstrata.data.remote.mongo.repository.MongoUserDocumentRepository
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import com.theoriongd.reqstrata.data.remote.mongo.sync.MongoBackgroundSyncService
import com.theoriongd.reqstrata.domain.model.PermissionLevel
import com.theoriongd.reqstrata.domain.model.ProjectAccessPolicy
import com.theoriongd.reqstrata.domain.model.ProjectModule
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.notifications.CentralizedNotificationService
import com.theoriongd.reqstrata.ui.notifications.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Exception thrown when a repository method or data manipulation
 * violates RBAC constraints.
 */
class UnauthorizedDataAccessException(
    val userId: String?,
    val attemptedRole: ProjectRole,
    val requiredRoles: Set<ProjectRole>,
    val action: String,
    val module: ProjectModule? = null,
    override val message: String = "Security Violation: Role '${attemptedRole.title}' lacks authorization to execute '$action'. Required roles: ${requiredRoles.joinToString { it.title }}."
) : SecurityException(message)

/**
 * Exception thrown when route navigation is unauthorized.
 */
class UnauthorizedNavigationException(
    val userId: String?,
    val attemptedRole: ProjectRole,
    val targetScreen: String,
    val requiredRoles: Set<ProjectRole>,
    override val message: String = "Access Denied: Role '${attemptedRole.title}' is not authorized to navigate to screen '$targetScreen'."
) : SecurityException(message)

/**
 * Result wrapper for authorization checks.
 */
sealed class AuthorizationResult<out T> {
    data class Granted<T>(val data: T, val role: ProjectRole) : AuthorizationResult<T>()
    data class Denied(
        val reason: String,
        val attemptedRole: ProjectRole,
        val requiredRoles: Set<ProjectRole>,
        val action: String
    ) : AuthorizationResult<Nothing>()
}

/**
 * Centralized Authorization Manager and Decorator.
 * Enforces Role-Based Access Control (RBAC) at both:
 * 1. Route / Navigation level
 * 2. Repository method / Data manipulation level
 * Integrated directly with MongoDB User Repository.
 */
object CentralizedAuthorizationManager {
    private const val TAG = "CentralizedAuthManager"

    val userRepository: MongoUserDocumentRepository by lazy {
        MongoUserDocumentRepository()
    }

    /**
     * Verifies the user's role against MongoDB.
     */
    suspend fun getVerifiedRole(userId: String?, projectId: String? = null): ProjectRole {
        if (userId.isNullOrBlank()) return ProjectRole.DEVELOPER
        return userRepository.getVerifiedRole(userId, projectId)
    }

    /**
     * Checks if a user has sufficient permission for a specific module.
     */
    suspend fun hasModulePermission(
        userId: String?,
        module: ProjectModule,
        requiredLevel: PermissionLevel,
        projectId: String? = null
    ): Boolean {
        val role = getVerifiedRole(userId, projectId)
        if (role == ProjectRole.ADMIN) return true
        val perm = ProjectAccessPolicy.getPermission(role, module)
        return when (requiredLevel) {
            PermissionLevel.NONE -> true
            PermissionLevel.VIEW -> perm != PermissionLevel.NONE
            PermissionLevel.FULL -> perm == PermissionLevel.FULL
        }
    }

    /**
     * Checks if a user has one of the allowed roles.
     */
    suspend fun isRoleAllowed(
        userId: String?,
        allowedRoles: Set<ProjectRole>,
        projectId: String? = null
    ): Boolean {
        val role = getVerifiedRole(userId, projectId)
        return role in allowedRoles || role == ProjectRole.ADMIN
    }

    /**
     * REPOSITORY METHOD DECORATOR:
     * Wraps data manipulation (create, update, delete, approve) in RBAC validation.
     * Prevents unauthorized execution and logs security audit trail to MongoDB.
     */
    suspend fun <T> authorizeMutation(
        userId: String?,
        module: ProjectModule,
        actionName: String,
        projectId: String? = null,
        block: suspend (verifiedRole: ProjectRole) -> T
    ): T = withContext(Dispatchers.IO) {
        val verifiedRole = getVerifiedRole(userId, projectId)
        val hasWriteAccess = ProjectAccessPolicy.canWrite(verifiedRole, module) || verifiedRole == ProjectRole.ADMIN

        if (!hasWriteAccess) {
            val allowedRoles = ProjectRole.entries.filter { ProjectAccessPolicy.canWrite(it, module) }.toSet()
            logSecurityViolation(
                userId = userId,
                role = verifiedRole,
                action = actionName,
                module = module,
                requiredRoles = allowedRoles
            )
            CentralizedNotificationService.showInApp(
                title = "Access Denied: $actionName",
                message = "Role '${verifiedRole.title}' lacks write permissions for ${module.displayName}.",
                type = NotificationType.ALERT
            )
            throw UnauthorizedDataAccessException(
                userId = userId,
                attemptedRole = verifiedRole,
                requiredRoles = allowedRoles,
                action = actionName,
                module = module
            )
        }

        block(verifiedRole)
    }

    /**
     * REPOSITORY METHOD DECORATOR (Role-specific):
     * Wraps data manipulation requiring specific roles.
     */
    suspend fun <T> authorizeAction(
        userId: String?,
        allowedRoles: Set<ProjectRole>,
        actionName: String,
        module: ProjectModule? = null,
        projectId: String? = null,
        block: suspend (verifiedRole: ProjectRole) -> T
    ): T = withContext(Dispatchers.IO) {
        val verifiedRole = getVerifiedRole(userId, projectId)
        val isAllowed = verifiedRole in allowedRoles || verifiedRole == ProjectRole.ADMIN

        if (!isAllowed) {
            logSecurityViolation(
                userId = userId,
                role = verifiedRole,
                action = actionName,
                module = module,
                requiredRoles = allowedRoles
            )
            CentralizedNotificationService.showInApp(
                title = "Permission Denied",
                message = "Role '${verifiedRole.title}' cannot perform '$actionName'.",
                type = NotificationType.ALERT
            )
            throw UnauthorizedDataAccessException(
                userId = userId,
                attemptedRole = verifiedRole,
                requiredRoles = allowedRoles,
                action = actionName,
                module = module
            )
        }

        block(verifiedRole)
    }

    /**
     * ROUTE / NAVIGATION CHECK:
     * Determines whether a given role is allowed to navigate to a target screen.
     */
    fun isRouteAllowed(screen: Screen, role: ProjectRole): Boolean {
        if (role == ProjectRole.ADMIN) return true
        val requiredRoles = getRequiredRolesForScreen(screen)
        return requiredRoles == null || role in requiredRoles
    }

    /**
     * Returns required roles for a given Screen, or null if unrestricted.
     */
    fun getRequiredRolesForScreen(screen: Screen): Set<ProjectRole>? {
        return when (screen) {
            Screen.CreateProject -> setOf(ProjectRole.ADMIN)
            Screen.ProjectSettings -> setOf(ProjectRole.ADMIN)
            Screen.RoleManagement -> setOf(ProjectRole.ADMIN)
            Screen.InviteMember -> setOf(ProjectRole.ADMIN)
            Screen.AdminDashboard -> setOf(ProjectRole.ADMIN)
            Screen.ApprovalCenter -> setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST, ProjectRole.ARCHITECT)
            Screen.TeamManagement -> setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST)
            is Screen.CreateEditRequirement -> setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST)
            Screen.NewRequirementForm -> setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST)
            is Screen.GenerateRequirements -> setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST)
            is Screen.SuggestArchitecture -> setOf(ProjectRole.ADMIN, ProjectRole.ARCHITECT)
            Screen.ArchitectureDecisions -> setOf(ProjectRole.ADMIN, ProjectRole.ARCHITECT)
            is Screen.GenerateTestSuite -> setOf(ProjectRole.ADMIN, ProjectRole.TESTER)
            Screen.TestExecutionWorkspace -> setOf(ProjectRole.ADMIN, ProjectRole.TESTER)
            else -> null
        }
    }

    /**
     * Writes security audit log entry to MongoDB Atlas activity_logs collection.
     */
    private fun logSecurityViolation(
        userId: String?,
        role: ProjectRole,
        action: String,
        module: ProjectModule?,
        requiredRoles: Set<ProjectRole>
    ) {
        try {
            val logId = UUID.randomUUID().toString()
            val details = "Security Alert: Unauthorized manipulation blocked. User '${userId ?: "unknown"}' " +
                    "with role '${role.title}' attempted '$action' on module '${module?.displayName ?: "General"}'. " +
                    "Required roles: ${requiredRoles.joinToString { it.title }}."
            Log.w(TAG, details)

            val auditDoc = MongoDocument()
                .put("_id", logId)
                .put("action", "SECURITY_RBAC_VIOLATION")
                .put("targetType", module?.name ?: "REPOSITORY_MUTATION")
                .put("targetId", action)
                .put("userId", userId ?: "anonymous")
                .put("userName", "User (${role.title})")
                .put("details", details)
                .put("timestamp", System.currentTimeMillis())

            MongoStitchClient.activityLogs.insertOne(auditDoc)
            MongoBackgroundSyncService.pushChangeRealtime(
                collection = MongoStitchClient.COLL_ACTIVITY_LOGS,
                operation = "insert",
                documentId = logId,
                document = auditDoc
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log security violation to MongoDB", e)
        }
    }
}
