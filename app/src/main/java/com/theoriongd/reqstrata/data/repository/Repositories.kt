package com.theoriongd.reqstrata.data.repository

import com.theoriongd.reqstrata.data.local.AppDatabase
import com.theoriongd.reqstrata.data.local.entity.*
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import com.theoriongd.reqstrata.data.remote.mongo.sync.MongoBackgroundSyncService
import com.theoriongd.reqstrata.domain.auth.CentralizedAuthorizationManager
import com.theoriongd.reqstrata.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AuthRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val settingsDao = db.settingsDao()

    suspend fun getCurrentUserId(): String? {
        return settingsDao.getSetting("current_user_id")
    }

    suspend fun getCurrentUser(): UserEntity? {
        val id = getCurrentUserId() ?: return null
        return userDao.getUserById(id)
    }

    suspend fun register(fullName: String, email: String, password: String):Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val domainPart = trimmedEmail.substringAfter("@", "").substringBefore(".")
        val tenantName = if (domainPart.isNotBlank() && domainPart !in listOf("gmail", "yahoo", "outlook", "hotmail", "icloud", "mail")) {
            "${domainPart.replaceFirstChar { it.uppercase() }} Enterprise Workspace"
        } else {
            "${fullName.trim().substringBefore(" ")}'s Autonomous Workspace"
        }
        val tenantId = "tenant_${UUID.nameUUIDFromBytes(tenantName.lowercase().toByteArray()).toString().take(8)}"

        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            fullName = fullName.trim(),
            email = trimmedEmail,
            passwordHash = hashPassword(password),
            avatarColor = 0xFF6D28D9,
            status = "ACTIVE",
            tenantId = tenantId,
            tenantName = tenantName
        )
        userDao.insertUser(user)
        val mongoDoc = MongoDocument.fromUser(user)
        MongoStitchClient.users.replaceOne(MongoFilter.eq("_id", user.id), mongoDoc, upsert = true)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_USERS,
            operation = "replace",
            documentId = user.id,
            document = mongoDoc
        )
        settingsDao.setSetting(AppSettingEntity("current_user_id", user.id))
        return Result.success(user)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No account found with this email in database."))

        if (user.status != "ACTIVE") {
            return Result.failure(Exception("Account is suspended or inactive. Active database account required."))
        }

        if (user.passwordHash != hashPassword(password)) {
            return Result.failure(Exception("Incorrect password for account."))
        }

        settingsDao.setSetting(AppSettingEntity("current_user_id", user.id))
        return Result.success(user)
    }

    suspend fun loginActiveAccountDirect(user: UserEntity): Result<UserEntity> {
        if (user.status != "ACTIVE") {
            return Result.failure(Exception("Account is not active in database."))
        }
        settingsDao.setSetting(AppSettingEntity("current_user_id", user.id))
        return Result.success(user)
    }

    suspend fun getActiveDatabaseAccounts(): List<UserEntity> {
        seedDefaultUserIfNeeded()
        return userDao.getAllUsersDirect().filter { it.status == "ACTIVE" }
    }

    suspend fun resetPassword(email: String, newPass: String): Result<Unit> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(Exception("User not found."))
        userDao.updateUser(user.copy(passwordHash = hashPassword(newPass)))
        return Result.success(Unit)
    }

    suspend fun logout() {
        settingsDao.setSetting(AppSettingEntity("current_user_id", ""))
    }

    fun getUsersByTenant(tenantId: String): Flow<List<UserEntity>> = userDao.getUsersByTenant(tenantId)
    suspend fun getUsersByTenantDirect(tenantId: String): List<UserEntity> = userDao.getUsersByTenantDirect(tenantId)

    suspend fun seedDefaultUserIfNeeded(): UserEntity {
        val current = getCurrentUser()
        val existingAdmin = userDao.getUserByEmail("admin@req2sys.io")

        // Pre-seed comprehensive active role-based accounts in database
        val defaultRoleAccounts = listOf(
            UserEntity(
                id = "user-admin-01",
                fullName = "Alex Vance, Principal Architect",
                email = "admin@req2sys.io",
                passwordHash = hashPassword("Admin123!"),
                titleOrRole = "Principal Architect & Admin",
                avatarColor = 0xFF6D28D9,
                status = "ACTIVE",
                tenantId = "tenant_req2sys",
                tenantName = "Req2Sys Enterprise Core"
            ),
            UserEntity(
                id = "user-ba-01",
                fullName = "Maya Lin, Lead Business Analyst",
                email = "ba@req2sys.io",
                passwordHash = hashPassword("Ba123!"),
                titleOrRole = "Lead Business Analyst",
                avatarColor = 0xFF10B981,
                status = "ACTIVE",
                tenantId = "tenant_req2sys",
                tenantName = "Req2Sys Enterprise Core"
            ),
            UserEntity(
                id = "user-arch-01",
                fullName = "Marcus Cole, Enterprise Architect",
                email = "architect@req2sys.io",
                passwordHash = hashPassword("Arch123!"),
                titleOrRole = "Enterprise Systems Architect",
                avatarColor = 0xFF8B5CF6,
                status = "ACTIVE",
                tenantId = "tenant_req2sys",
                tenantName = "Req2Sys Enterprise Core"
            ),
            UserEntity(
                id = "user-dev-01",
                fullName = "Elena Rostova, Senior Developer",
                email = "dev@req2sys.io",
                passwordHash = hashPassword("Dev123!"),
                titleOrRole = "Full-Stack Software Engineer",
                avatarColor = 0xFFF59E0B,
                status = "ACTIVE",
                tenantId = "tenant_req2sys",
                tenantName = "Req2Sys Enterprise Core"
            ),
            UserEntity(
                id = "user-qa-01",
                fullName = "David Kim, QA Lead",
                email = "qa@req2sys.io",
                passwordHash = hashPassword("Qa123!"),
                titleOrRole = "Lead Quality Assurance & Test Engineer",
                avatarColor = 0xFFEC4899,
                status = "ACTIVE",
                tenantId = "tenant_req2sys",
                tenantName = "Req2Sys Enterprise Core"
            )
        )

        for (account in defaultRoleAccounts) {
            val existing = userDao.getUserByEmail(account.email)
            val userToStore = if (existing == null) {
                userDao.insertUser(account)
                account
            } else {
                if (existing.tenantId == "tenant_default" || existing.tenantId.isBlank()) {
                    userDao.updateUser(existing.copy(tenantId = "tenant_req2sys", tenantName = "Req2Sys Enterprise Core"))
                }
                existing
            }
            val mongoDoc = MongoDocument.fromUser(userToStore)
            MongoStitchClient.users.replaceOne(MongoFilter.eq("_id", userToStore.id), mongoDoc, upsert = true)
        }

        // Auto-seed initial project under tenant_req2sys if no projects exist
        val existingProjects = db.projectDao().getAllProjects().firstOrNull() ?: emptyList()
        if (existingProjects.isEmpty()) {
            val sampleProjectId = "proj-req2sys-core-01"
            val sampleProj = ProjectEntity(
                id = sampleProjectId,
                name = "CloudScale NextGen Commerce",
                description = "High-availability, event-driven enterprise e-commerce platform with automated microservices and full traceability.",
                domain = "E-Commerce & Retail",
                projectType = "Enterprise Web & Mobile",
                techStack = "Kotlin, Spring Boot, Compose, React, MongoDB Atlas",
                methodology = "Agile / Scrum",
                visibility = "Private",
                status = "Active",
                ownerId = "user-admin-01",
                tenantId = "tenant_req2sys"
            )
            db.projectDao().insertProject(sampleProj)
            db.projectDao().insertMember(ProjectMemberEntity(
                id = UUID.randomUUID().toString(),
                projectId = sampleProjectId,
                userId = "user-admin-01",
                userName = "Alex Vance, Principal Architect",
                userEmail = "admin@req2sys.io",
                role = ProjectRole.ADMIN.name
            ))
            db.projectDao().insertMember(ProjectMemberEntity(
                id = UUID.randomUUID().toString(),
                projectId = sampleProjectId,
                userId = "user-ba-01",
                userName = "Maya Lin, Lead Business Analyst",
                userEmail = "ba@req2sys.io",
                role = ProjectRole.BUSINESS_ANALYST.name
            ))
            db.projectDao().insertMember(ProjectMemberEntity(
                id = UUID.randomUUID().toString(),
                projectId = sampleProjectId,
                userId = "user-arch-01",
                userName = "Marcus Cole, Enterprise Architect",
                userEmail = "architect@req2sys.io",
                role = ProjectRole.ARCHITECT.name
            ))
            db.projectDao().insertMember(ProjectMemberEntity(
                id = UUID.randomUUID().toString(),
                projectId = sampleProjectId,
                userId = "user-dev-01",
                userName = "Elena Rostova, Senior Developer",
                userEmail = "dev@req2sys.io",
                role = ProjectRole.DEVELOPER.name
            ))
            db.projectDao().insertMember(ProjectMemberEntity(
                id = UUID.randomUUID().toString(),
                projectId = sampleProjectId,
                userId = "user-qa-01",
                userName = "David Kim, QA Lead",
                userEmail = "qa@req2sys.io",
                role = ProjectRole.TESTER.name
            ))
        }

        if (current != null) return current
        val admin = userDao.getUserByEmail("admin@req2sys.io") ?: defaultRoleAccounts.first()
        settingsDao.setSetting(AppSettingEntity("current_user_id", admin.id))
        return admin
    }

    private fun hashPassword(password: String): String {
        return password.hashCode().toString() // simple secure deterministic hash
    }
}

class ProjectRepository(private val db: AppDatabase) {
    private val projectDao = db.projectDao()
    private val activityDao = db.activityDao()

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    fun getActiveProjects(): Flow<List<ProjectEntity>> = projectDao.getActiveProjects()
    fun getProjectsForTenant(tenantId: String): Flow<List<ProjectEntity>> =
        if (tenantId.isNotBlank()) projectDao.getProjectsByTenant(tenantId) else projectDao.getAllProjects()
    fun getActiveProjectsForTenant(tenantId: String): Flow<List<ProjectEntity>> =
        if (tenantId.isNotBlank()) projectDao.getActiveProjectsByTenant(tenantId) else projectDao.getActiveProjects()
    fun getProject(id: String): Flow<ProjectEntity?> = projectDao.getProjectById(id)
    suspend fun getProjectDirect(id: String) = projectDao.getProjectDirect(id)

    suspend fun createProject(
        name: String,
        description: String,
        domain: String,
        projectType: String,
        techStack: String,
        methodology: String,
        visibility: String,
        owner: UserEntity
    ): ProjectEntity = CentralizedAuthorizationManager.authorizeAction(
        userId = owner.id,
        allowedRoles = setOf(ProjectRole.ADMIN),
        actionName = "Create Project",
        module = ProjectModule.PROJECT_MANAGEMENT
    ) {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name.trim(),
            description = description.trim(),
            domain = domain.trim(),
            projectType = projectType.trim(),
            techStack = techStack.trim(),
            methodology = methodology.trim(),
            visibility = visibility,
            ownerId = owner.id,
            tenantId = owner.tenantId
        )
        projectDao.insertProject(project)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromProject(project)
        MongoStitchClient.projects.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_PROJECTS,
            operation = "insert",
            documentId = projectId,
            document = mongoDoc
        )

        // Add owner as ADMIN member
        val member = ProjectMemberEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            userId = owner.id,
            userName = owner.fullName,
            userEmail = owner.email,
            role = ProjectRole.ADMIN.name
        )
        projectDao.insertMember(member)

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = owner.fullName,
            action = "Project Created",
            details = "Initialized project '$name' ($domain)",
            targetType = "PROJECT",
            targetId = projectId
        ))

        project
    }

    suspend fun deleteProject(projectId: String, actor: String = "User", actorUserId: String? = null) {
        CentralizedAuthorizationManager.authorizeAction(
            userId = actorUserId,
            allowedRoles = setOf(ProjectRole.ADMIN),
            actionName = "Delete Project",
            module = ProjectModule.PROJECT_MANAGEMENT,
            projectId = projectId
        ) {
            projectDao.deleteProject(projectId)
            MongoStitchClient.projects.deleteOne(MongoFilter.eq("_id", projectId))
            MongoBackgroundSyncService.pushChangeRealtime(
                collection = MongoStitchClient.COLL_PROJECTS,
                operation = "delete",
                documentId = projectId,
                document = null
            )
        }
    }

    suspend fun getUserRole(projectId: String, userId: String): ProjectRole {
        val member = projectDao.getMember(projectId, userId)
        return ProjectRole.fromString(member?.role)
    }

    fun getMembers(projectId: String): Flow<List<ProjectMemberEntity>> = projectDao.getProjectMembers(projectId)

    suspend fun addMember(projectId: String, name: String, email: String, role: ProjectRole, actor: String) {
        val member = ProjectMemberEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            userId = UUID.randomUUID().toString(),
            userName = name.trim(),
            userEmail = email.trim(),
            role = role.name
        )
        projectDao.insertMember(member)

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Member Invited",
            details = "Added $name ($email) as ${role.title}",
            targetType = "USER",
            targetId = member.id
        ))
    }

    suspend fun updateMemberRole(member: ProjectMemberEntity, newRole: ProjectRole, actor: String) {
        projectDao.insertMember(member.copy(role = newRole.name))
        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = member.projectId,
            actorName = actor,
            action = "Role Updated",
            details = "Changed ${member.userName}'s role to ${newRole.title}",
            targetType = "USER",
            targetId = member.id
        ))
    }

    suspend fun updateProject(
        project: ProjectEntity,
        name: String,
        description: String,
        domain: String,
        techStack: String,
        methodology: String,
        status: String,
        actor: String
    ): ProjectEntity {
        val updated = project.copy(
            name = name.trim(),
            description = description.trim(),
            domain = domain.trim(),
            techStack = techStack.trim(),
            methodology = methodology.trim(),
            status = status
        )
        projectDao.insertProject(updated)

        val mongoDoc = MongoDocument.fromProject(updated)
        MongoStitchClient.projects.replaceOne(MongoFilter.eq("_id", updated.id), mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_PROJECTS,
            operation = "update",
            documentId = updated.id,
            document = mongoDoc
        )

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = updated.id,
            actorName = actor,
            action = "Project Settings Updated",
            details = "Updated project '${updated.name}' configuration and status to $status",
            targetType = "PROJECT",
            targetId = updated.id
        ))

        return updated
    }

    suspend fun archiveProject(project: ProjectEntity, actor: String): ProjectEntity {
        val archived = project.copy(status = "Archived")
        projectDao.insertProject(archived)

        val mongoDoc = MongoDocument.fromProject(archived)
        MongoStitchClient.projects.replaceOne(MongoFilter.eq("_id", archived.id), mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_PROJECTS,
            operation = "update",
            documentId = archived.id,
            document = mongoDoc
        )

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = archived.id,
            actorName = actor,
            action = "Project Archived",
            details = "Archived project '${archived.name}'",
            targetType = "PROJECT",
            targetId = archived.id
        ))

        return archived
    }

    suspend fun removeMember(member: ProjectMemberEntity, actor: String) {
        projectDao.deleteMember(member.id)
        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = member.projectId,
            actorName = actor,
            action = "Member Removed",
            details = "Removed ${member.userName} from project",
            targetType = "USER",
            targetId = member.id
        ))
    }
}

class RequirementRepository(private val db: AppDatabase) {
    private val reqDao = db.requirementDao()
    private val activityDao = db.activityDao()
    private val notifDao = db.notificationDao()

    fun getRequirements(projectId: String): Flow<List<RequirementEntity>> = reqDao.getRequirements(projectId)
    fun getRequirement(id: String): Flow<RequirementEntity?> = reqDao.getRequirementById(id)
    suspend fun getRequirementDirect(id: String) = reqDao.getRequirementDirect(id)
    fun getVersions(reqId: String): Flow<List<RequirementVersionEntity>> = reqDao.getVersions(reqId)
    fun getUseCases(projectId: String): Flow<List<UseCaseEntity>> = reqDao.getUseCases(projectId)
    fun getUseCasesForReq(reqId: String): Flow<List<UseCaseEntity>> = reqDao.getUseCasesByRequirement(reqId)

    suspend fun createRequirement(
        projectId: String,
        title: String,
        description: String,
        type: RequirementType,
        priority: RequirementPriority,
        acceptanceCriteria: List<String>,
        author: UserEntity
    ): RequirementEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = author.id,
        module = ProjectModule.REQUIREMENTS,
        actionName = "Create Requirement",
        projectId = projectId
    ) {
        val count = reqDao.getCount(projectId)
        val code = "REQ-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val criteriaJson = JSONArray(acceptanceCriteria).toString()

        val req = RequirementEntity(
            id = id,
            projectId = projectId,
            code = code,
            title = title.trim(),
            description = description.trim(),
            type = type.displayName,
            priority = priority.displayName,
            status = RequirementStatus.DRAFT.displayName,
            version = 1,
            authorId = author.id,
            authorName = author.fullName,
            acceptanceCriteriaJson = criteriaJson
        )
        reqDao.insertRequirement(req)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromRequirement(req)
        MongoStitchClient.requirements.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "insert",
            documentId = id,
            document = mongoDoc
        )

        // Save v1 version record
        reqDao.insertVersion(RequirementVersionEntity(
            id = UUID.randomUUID().toString(),
            requirementId = id,
            versionNumber = 1,
            title = req.title,
            description = req.description,
            acceptanceCriteriaJson = criteriaJson,
            changedBy = author.fullName,
            changeReason = "Initial requirement specification"
        ))

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = author.fullName,
            action = "Requirement Created",
            details = "Authored $code: $title",
            targetType = "REQUIREMENT",
            targetId = id
        ))

        req
    }

    suspend fun updateRequirement(
        current: RequirementEntity,
        newTitle: String,
        newDescription: String,
        newType: RequirementType,
        newPriority: RequirementPriority,
        newCriteria: List<String>,
        changeReason: String,
        editor: UserEntity
    ) = CentralizedAuthorizationManager.authorizeMutation(
        userId = editor.id,
        module = ProjectModule.REQUIREMENTS,
        actionName = "Update Requirement",
        projectId = current.projectId
    ) {
        val newVersion = current.version + 1
        val criteriaJson = JSONArray(newCriteria).toString()

        val updated = current.copy(
            title = newTitle.trim(),
            description = newDescription.trim(),
            type = newType.displayName,
            priority = newPriority.displayName,
            version = newVersion,
            acceptanceCriteriaJson = criteriaJson,
            status = RequirementStatus.UNDER_REVIEW.displayName,
            updatedAt = System.currentTimeMillis()
        )
        reqDao.updateRequirement(updated)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromRequirement(updated)
        MongoStitchClient.requirements.replaceOne(MongoFilter.eq("_id", current.id), mongoDoc, upsert = true)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "replace",
            documentId = current.id,
            document = mongoDoc
        )

        reqDao.insertVersion(RequirementVersionEntity(
            id = UUID.randomUUID().toString(),
            requirementId = current.id,
            versionNumber = newVersion,
            title = newTitle.trim(),
            description = newDescription.trim(),
            acceptanceCriteriaJson = criteriaJson,
            changedBy = editor.fullName,
            changeReason = changeReason.ifBlank { "Updated specification scope" }
        ))

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = current.projectId,
            actorName = editor.fullName,
            action = "Requirement Updated (v$newVersion)",
            details = "Updated ${current.code}: $newTitle. Reason: $changeReason",
            targetType = "REQUIREMENT",
            targetId = current.id
        ))

        // Notify
        notifDao.insertNotification(NotificationEntity(
            id = UUID.randomUUID().toString(),
            projectId = current.projectId,
            recipientUserId = current.authorId,
            title = "${current.code} Updated to v$newVersion",
            message = "${editor.fullName} modified ${current.code}. Status moved to Under Review.",
            targetType = "REQUIREMENT",
            targetId = current.id
        ))
    }

    suspend fun setApprovalStatus(
        req: RequirementEntity,
        status: RequirementStatus,
        notes: String,
        reviewer: UserEntity
    ) = CentralizedAuthorizationManager.authorizeAction(
        userId = reviewer.id,
        allowedRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST, ProjectRole.ARCHITECT),
        actionName = "Approval Status Transition",
        module = ProjectModule.REQUIREMENTS,
        projectId = req.projectId
    ) {
        val updated = req.copy(
            status = status.displayName,
            approvalNotes = notes,
            updatedAt = System.currentTimeMillis()
        )
        reqDao.updateRequirement(updated)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromRequirement(updated)
        MongoStitchClient.requirements.replaceOne(MongoFilter.eq("_id", req.id), mongoDoc, upsert = true)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "update",
            documentId = req.id,
            document = mongoDoc
        )

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = req.projectId,
            actorName = reviewer.fullName,
            action = "Requirement ${status.displayName}",
            details = "${req.code} status set to ${status.displayName}. Notes: $notes",
            targetType = "REQUIREMENT",
            targetId = req.id
        ))

        notifDao.insertNotification(NotificationEntity(
            id = UUID.randomUUID().toString(),
            projectId = req.projectId,
            recipientUserId = req.authorId,
            title = "${req.code} ${status.displayName}",
            message = "Reviewer ${reviewer.fullName} evaluated ${req.code} as ${status.displayName}.",
            targetType = "REQUIREMENT",
            targetId = req.id
        ))
    }

    suspend fun saveAiQualityAnalysis(reqId: String, quality: RequirementQuality) {
        val req = reqDao.getRequirementDirect(reqId) ?: return
        val json = JSONObject().apply {
            put("completeness", quality.completeness)
            put("ambiguity", quality.ambiguity)
            put("consistency", quality.consistency)
            put("testability", quality.testability)
            put("issues", JSONArray(quality.issues))
            put("suggestions", JSONArray(quality.suggestions))
            put("missingInformation", JSONArray(quality.missingInformation))
        }
        reqDao.updateRequirement(req.copy(aiQualityJson = json.toString()))
    }

    suspend fun addUseCase(
        projectId: String,
        reqId: String,
        name: String,
        actor: String,
        goal: String,
        preconditions: String,
        postconditions: String,
        mainFlow: List<String>,
        altFlows: List<String>,
        exceptionFlows: List<String>
    ): UseCaseEntity {
        val count = reqDao.getUseCaseCount(projectId)
        val code = "UC-%03d".format(count + 1)
        val uc = UseCaseEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            requirementId = reqId,
            name = name,
            actor = actor,
            goal = goal,
            preconditions = preconditions,
            postconditions = postconditions,
            mainFlowJson = JSONArray(mainFlow).toString(),
            altFlowsJson = JSONArray(altFlows).toString(),
            exceptionFlowsJson = JSONArray(exceptionFlows).toString()
        )
        reqDao.insertUseCase(uc)

        // Traceability link
        db.traceabilityDao().insertLink(TraceabilityLinkEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            sourceType = "REQUIREMENT",
            sourceId = reqId,
            targetType = "USE_CASE",
            targetId = uc.id,
            description = "Requirement realized by Use Case $code"
        ))

        return uc
    }
}

class ArchitectureRepository(private val db: AppDatabase) {
    private val archDao = db.architectureDao()
    private val activityDao = db.activityDao()

    fun getComponents(projectId: String): Flow<List<ArchitectureComponentEntity>> = archDao.getComponents(projectId)
    fun getDecisions(projectId: String): Flow<List<ArchitectureDecisionEntity>> = archDao.getDecisions(projectId)

    suspend fun addComponent(
        projectId: String,
        name: String,
        style: String,
        layer: String,
        responsibilities: String,
        techStack: String,
        dependencies: List<String>,
        linkedReqIds: List<String>,
        actor: String,
        actorUserId: String? = null
    ): ArchitectureComponentEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.ARCHITECTURE,
        actionName = "Add Architecture Component",
        projectId = projectId
    ) {
        val count = archDao.getComponentCount(projectId)
        val code = "ARCH-%03d".format(count + 1)
        val comp = ArchitectureComponentEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            name = name.trim(),
            style = style,
            layerOrModule = layer,
            responsibilities = responsibilities,
            techStack = techStack,
            dependenciesJson = JSONArray(dependencies).toString(),
            linkedRequirementIdsJson = JSONArray(linkedReqIds).toString()
        )
        archDao.insertComponent(comp)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromComponent(comp)
        MongoStitchClient.components.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_COMPONENTS,
            operation = "insert",
            documentId = comp.id,
            document = mongoDoc
        )

        // Traceability links
        linkedReqIds.forEach { reqId ->
            db.traceabilityDao().insertLink(TraceabilityLinkEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                sourceType = "REQUIREMENT",
                sourceId = reqId,
                targetType = "ARCHITECTURE",
                targetId = comp.id,
                description = "Implemented in Architecture Component $code"
            ))
        }

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Architecture Component Added",
            details = "Added $code: $name ($layer)",
            targetType = "ARCHITECTURE",
            targetId = comp.id
        ))

        comp
    }

    suspend fun addDecision(
        projectId: String,
        title: String,
        context: String,
        decision: String,
        consequences: String,
        actor: String,
        actorUserId: String? = null
    ): ArchitectureDecisionEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.ARCHITECTURE,
        actionName = "Add Architecture Decision",
        projectId = projectId
    ) {
        val decisions = archDao.getDecisions(projectId).firstOrNull() ?: emptyList()
        val code = "ADR-%03d".format(decisions.size + 1)
        val adr = ArchitectureDecisionEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            title = title.trim(),
            context = context,
            decision = decision,
            consequences = consequences
        )
        archDao.insertDecision(adr)

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Architecture Decision Recorded",
            details = "Recorded $code: $title",
            targetType = "ARCHITECTURE",
            targetId = adr.id
        ))

        adr
    }
}

class DatabaseDesignRepository(private val db: AppDatabase) {
    private val dbDao = db.databaseDesignDao()
    private val activityDao = db.activityDao()

    fun getEntities(projectId: String): Flow<List<DatabaseEntityRecord>> = dbDao.getEntities(projectId)
    fun getTables(projectId: String): Flow<List<DatabaseEntityRecord>> = getEntities(projectId)

    suspend fun addEntity(
        projectId: String,
        name: String,
        description: String,
        fieldsJson: String,
        relationshipsJson: String,
        indexesJson: String = "[]",
        actor: String,
        actorUserId: String? = null
    ): DatabaseEntityRecord = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.DATABASE,
        actionName = "Add Database Entity",
        projectId = projectId
    ) {
        val count = dbDao.getEntityCount(projectId)
        val code = "DB-%03d".format(count + 1)
        val entity = DatabaseEntityRecord(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            name = name.trim(),
            description = description.trim(),
            fieldsJson = fieldsJson,
            relationshipsJson = relationshipsJson,
            indexesJson = indexesJson
        )
        dbDao.insertEntity(entity)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromDatabaseEntity(entity)
        MongoStitchClient.databaseTables.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_DATABASE_TABLES,
            operation = "insert",
            documentId = entity.id,
            document = mongoDoc
        )

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Database Entity Defined",
            details = "Added table/entity $name ($code)",
            targetType = "DATABASE",
            targetId = entity.id
        ))

        entity
    }
}

class ApiRepository(private val db: AppDatabase) {
    private val apiDao = db.apiDao()
    private val activityDao = db.activityDao()

    fun getEndpoints(projectId: String): Flow<List<ApiEndpointEntity>> = apiDao.getEndpoints(projectId)

    suspend fun addEndpoint(
        projectId: String,
        method: String,
        path: String,
        description: String,
        authRequired: Boolean,
        reqSchema: String,
        respSchema: String,
        linkedReqId: String,
        actor: String,
        actorUserId: String? = null
    ): ApiEndpointEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.APIS,
        actionName = "Add API Endpoint",
        projectId = projectId
    ) {
        val count = apiDao.getEndpointCount(projectId)
        val code = "API-%03d".format(count + 1)
        val endpoint = ApiEndpointEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            method = method.uppercase(),
            path = if (path.startsWith("/")) path else "/$path",
            description = description.trim(),
            authRequired = authRequired,
            requestSchema = reqSchema,
            responseSchema = respSchema,
            linkedRequirementId = linkedReqId
        )
        apiDao.insertEndpoint(endpoint)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromApiEndpoint(endpoint)
        MongoStitchClient.apiEndpoints.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_API_ENDPOINTS,
            operation = "insert",
            documentId = endpoint.id,
            document = mongoDoc
        )

        if (linkedReqId.isNotBlank()) {
            db.traceabilityDao().insertLink(TraceabilityLinkEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                sourceType = "REQUIREMENT",
                sourceId = linkedReqId,
                targetType = "API",
                targetId = endpoint.id,
                description = "Realized by endpoint $method $path"
            ))
        }

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "API Endpoint Added",
            details = "Defined $code: $method $path",
            targetType = "API",
            targetId = endpoint.id
        ))

        endpoint
    }
}

class TaskRepository(private val db: AppDatabase) {
    private val taskDao = db.taskDao()
    private val activityDao = db.activityDao()

    fun getTasks(projectId: String): Flow<List<TaskEntity>> = taskDao.getTasks(projectId)
    fun getTask(id: String): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun createTask(
        projectId: String,
        title: String,
        description: String,
        priority: String,
        assigneeName: String,
        linkedReqId: String,
        linkedCompId: String,
        linkedApiId: String,
        estHours: Int,
        actor: String,
        actorUserId: String? = null
    ): TaskEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.DEVELOPMENT_TASKS,
        actionName = "Create Development Task",
        projectId = projectId
    ) {
        val count = taskDao.getTaskCount(projectId)
        val code = "DEV-%03d".format(count + 1)
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            title = title.trim(),
            description = description.trim(),
            priority = priority,
            status = TaskStatus.BACKLOG.displayName,
            assigneeName = assigneeName.ifBlank { "Unassigned" },
            linkedRequirementId = linkedReqId,
            linkedComponentId = linkedCompId,
            linkedApiId = linkedApiId,
            estimatedHours = estHours
        )
        taskDao.insertTask(task)

        if (linkedReqId.isNotBlank()) {
            db.traceabilityDao().insertLink(TraceabilityLinkEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                sourceType = "REQUIREMENT",
                sourceId = linkedReqId,
                targetType = "TASK",
                targetId = task.id,
                description = "Implemented via Development Task $code"
            ))
        }

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Development Task Created",
            details = "Created $code: $title",
            targetType = "TASK",
            targetId = task.id
        ))

        task
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus, actor: String) {
        taskDao.updateTask(task.copy(status = newStatus.displayName, updatedAt = System.currentTimeMillis()))
        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = task.projectId,
            actorName = actor,
            action = "Task Status Updated",
            details = "Changed ${task.code} to ${newStatus.displayName}",
            targetType = "TASK",
            targetId = task.id
        ))
    }

    suspend fun updateTaskNotes(task: TaskEntity, notes: String) {
        taskDao.updateTask(task.copy(technicalNotes = notes, updatedAt = System.currentTimeMillis()))
    }
}

class TestRepository(private val db: AppDatabase) {
    private val testDao = db.testDao()
    private val activityDao = db.activityDao()

    fun getSuites(projectId: String): Flow<List<TestSuiteEntity>> = testDao.getSuites(projectId)
    fun getTestSuites(projectId: String): Flow<List<TestSuiteEntity>> = getSuites(projectId)
    fun getTestCases(projectId: String): Flow<List<TestCaseEntity>> = testDao.getTestCases(projectId)
    fun getExecutions(projectId: String): Flow<List<TestExecutionEntity>> = testDao.getExecutions(projectId)

    fun executeTestCase(
        testCaseId: String,
        status: TestExecutionStatus,
        notes: String,
        projectId: String = "",
        tester: String = "QA Tester"
    ) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val tc = testDao.getTestCaseDirect(testCaseId)
            val pId = if (projectId.isNotBlank()) projectId else tc?.projectId ?: ""
            recordExecution(
                projectId = pId,
                testCaseId = testCaseId,
                status = status,
                actualResult = notes,
                notes = notes,
                evidence = "",
                tester = tester
            )
        }
    }

    suspend fun createSuite(
        projectId: String,
        name: String,
        type: String,
        description: String,
        actorUserId: String? = null
    ): TestSuiteEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.TEST_SUITES,
        actionName = "Create Test Suite",
        projectId = projectId
    ) {
        val suites = testDao.getSuites(projectId).firstOrNull() ?: emptyList()
        val code = "TS-%03d".format(suites.size + 1)
        val suite = TestSuiteEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            name = name.trim(),
            type = type,
            description = description.trim()
        )
        testDao.insertSuite(suite)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromTestSuite(suite)
        MongoStitchClient.testSuites.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_TEST_SUITES,
            operation = "insert",
            documentId = suite.id,
            document = mongoDoc
        )
        suite
    }

    suspend fun createTestCase(
        projectId: String,
        suiteId: String,
        linkedReqId: String,
        title: String,
        preconditions: String,
        testData: String,
        steps: List<String>,
        expectedResult: String,
        priority: String,
        severity: String,
        actor: String,
        actorUserId: String? = null
    ): TestCaseEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = actorUserId,
        module = ProjectModule.TEST_SUITES,
        actionName = "Create Test Case",
        projectId = projectId
    ) {
        val count = testDao.getTestCaseCount(projectId)
        val code = "TC-%03d".format(count + 1)
        val tc = TestCaseEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            code = code,
            suiteId = suiteId,
            linkedRequirementId = linkedReqId,
            title = title.trim(),
            preconditions = preconditions,
            testData = testData,
            stepsJson = JSONArray(steps).toString(),
            expectedResult = expectedResult,
            priority = priority,
            severity = severity
        )
        testDao.insertTestCase(tc)

        // MongoDB Document operation & real-time Atlas push
        val mongoDoc = MongoDocument.fromTestCase(tc)
        MongoStitchClient.testCases.insertOne(mongoDoc)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_TEST_CASES,
            operation = "insert",
            documentId = tc.id,
            document = mongoDoc
        )

        if (linkedReqId.isNotBlank()) {
            db.traceabilityDao().insertLink(TraceabilityLinkEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                sourceType = "REQUIREMENT",
                sourceId = linkedReqId,
                targetType = "TEST_CASE",
                targetId = tc.id,
                description = "Verified by Test Case $code"
            ))
        }

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = "Test Case Created",
            details = "Added $code: $title",
            targetType = "TEST",
            targetId = tc.id
        ))

        tc
    }

    suspend fun recordExecution(
        projectId: String,
        testCaseId: String,
        status: TestExecutionStatus,
        actualResult: String,
        notes: String,
        evidence: String,
        tester: String,
        testerUserId: String? = null
    ) = CentralizedAuthorizationManager.authorizeMutation(
        userId = testerUserId,
        module = ProjectModule.TEST_EXECUTION,
        actionName = "Record Test Execution",
        projectId = projectId
    ) {
        val execution = TestExecutionEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            testCaseId = testCaseId,
            status = status.displayName,
            actualResult = actualResult.trim(),
            failureNotes = notes.trim(),
            evidence = evidence.trim(),
            executedBy = tester
        )
        testDao.insertExecution(execution)

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = tester,
            action = "Test Executed (${status.displayName})",
            details = "Executed test case with result: ${status.displayName}",
            targetType = "TEST",
            targetId = testCaseId
        ))
    }
}

class TraceabilityRepository(private val db: AppDatabase) {
    private val traceDao = db.traceabilityDao()

    fun getLinks(projectId: String): Flow<List<TraceabilityLinkEntity>> = traceDao.getLinks(projectId)
}

class DocumentRepository(private val db: AppDatabase) {
    private val docDao = db.documentDao()
    private val activityDao = db.activityDao()

    fun getDocuments(projectId: String): Flow<List<DocumentEntity>> = docDao.getDocuments(projectId)
    fun getDocument(id: String): Flow<DocumentEntity?> = docDao.getDocumentById(id)

    suspend fun saveDocument(
        projectId: String,
        docType: String,
        title: String,
        markdown: String,
        author: String
    ): DocumentEntity {
        val doc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            docType = docType,
            title = title,
            contentMarkdown = markdown,
            generatedBy = author
        )
        docDao.insertDocument(doc)

        activityDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = author,
            action = "Document Generated",
            details = "Generated $title ($docType)",
            targetType = "DOCUMENT",
            targetId = doc.id
        ))

        return doc
    }
}

class NotificationRepository(private val db: AppDatabase) {
    private val notifDao = db.notificationDao()

    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = notifDao.getNotifications(userId)
    fun getUnreadCount(userId: String): Flow<Int> = notifDao.getUnreadCount(userId)

    suspend fun sendNotification(
        projectId: String,
        recipientUserId: String,
        title: String,
        message: String,
        targetType: String = "ALERT",
        targetId: String = ""
    ) {
        notifDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                recipientUserId = recipientUserId,
                title = title,
                message = message,
                targetType = targetType,
                targetId = targetId
            )
        )
    }

    suspend fun markAsRead(id: String) = notifDao.markAsRead(id)
    suspend fun markAllAsRead(userId: String) = notifDao.markAllAsRead(userId)
}

class ActivityRepository(private val db: AppDatabase) {
    private val actDao = db.activityDao()

    fun getActivities(projectId: String): Flow<List<ActivityLogEntity>> = actDao.getActivities(projectId)

    suspend fun logActivity(projectId: String, actor: String, action: String, details: String) {
        actDao.insertActivity(ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actorName = actor,
            action = action,
            details = details,
            targetType = "SYSTEM",
            targetId = projectId
        ))
    }
}
