package com.theoriongd.reqstrata.data.local.dao

import androidx.room.*
import com.theoriongd.reqstrata.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    suspend fun getAllUsersDirect(): List<UserEntity>

    @Query("SELECT * FROM users WHERE tenantId = :tenantId ORDER BY fullName ASC")
    fun getUsersByTenant(tenantId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE tenantId = :tenantId ORDER BY fullName ASC")
    suspend fun getUsersByTenantDirect(tenantId: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = 'Active' ORDER BY updatedAt DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'Active'")
    suspend fun getActiveProjectCount(): Int

    @Query("SELECT * FROM projects WHERE tenantId = :tenantId ORDER BY updatedAt DESC")
    fun getProjectsByTenant(tenantId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE tenantId = :tenantId AND status = 'Active' ORDER BY updatedAt DESC")
    fun getActiveProjectsByTenant(tenantId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectDirect(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    // Project Members
    @Query("SELECT * FROM project_members WHERE projectId = :projectId")
    fun getProjectMembers(projectId: String): Flow<List<ProjectMemberEntity>>

    @Query("SELECT * FROM project_members WHERE projectId = :projectId AND userId = :userId LIMIT 1")
    suspend fun getMember(projectId: String, userId: String): ProjectMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ProjectMemberEntity)

    @Query("DELETE FROM project_members WHERE id = :id")
    suspend fun deleteMember(id: String)
}

@Dao
interface RequirementDao {
    @Query("SELECT * FROM requirements WHERE projectId = :projectId ORDER BY code ASC")
    fun getRequirements(projectId: String): Flow<List<RequirementEntity>>

    @Query("SELECT * FROM requirements WHERE id = :id LIMIT 1")
    fun getRequirementById(id: String): Flow<RequirementEntity?>

    @Query("SELECT * FROM requirements WHERE id = :id LIMIT 1")
    suspend fun getRequirementDirect(id: String): RequirementEntity?

    @Query("SELECT * FROM requirements WHERE projectId = :projectId AND code = :code LIMIT 1")
    suspend fun getRequirementByCode(projectId: String, code: String): RequirementEntity?

    @Query("SELECT COUNT(*) FROM requirements WHERE projectId = :projectId")
    suspend fun getCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequirement(req: RequirementEntity)

    @Update
    suspend fun updateRequirement(req: RequirementEntity)

    @Query("DELETE FROM requirements WHERE id = :id")
    suspend fun deleteRequirement(id: String)

    // Versions
    @Query("SELECT * FROM requirement_versions WHERE requirementId = :reqId ORDER BY versionNumber DESC")
    fun getVersions(reqId: String): Flow<List<RequirementVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: RequirementVersionEntity)

    // Use Cases
    @Query("SELECT * FROM use_cases WHERE projectId = :projectId ORDER BY code ASC")
    fun getUseCases(projectId: String): Flow<List<UseCaseEntity>>

    @Query("SELECT * FROM use_cases WHERE requirementId = :reqId")
    fun getUseCasesByRequirement(reqId: String): Flow<List<UseCaseEntity>>

    @Query("SELECT COUNT(*) FROM use_cases WHERE projectId = :projectId")
    suspend fun getUseCaseCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUseCase(useCase: UseCaseEntity)

    @Update
    suspend fun updateUseCase(useCase: UseCaseEntity)

    @Query("DELETE FROM use_cases WHERE id = :id")
    suspend fun deleteUseCase(id: String)
}

@Dao
interface ArchitectureDao {
    @Query("SELECT * FROM architecture_components WHERE projectId = :projectId ORDER BY code ASC")
    fun getComponents(projectId: String): Flow<List<ArchitectureComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(comp: ArchitectureComponentEntity)

    @Update
    suspend fun updateComponent(comp: ArchitectureComponentEntity)

    @Query("DELETE FROM architecture_components WHERE id = :id")
    suspend fun deleteComponent(id: String)

    @Query("SELECT COUNT(*) FROM architecture_components WHERE projectId = :projectId")
    suspend fun getComponentCount(projectId: String): Int

    // ADRs
    @Query("SELECT * FROM architecture_decisions WHERE projectId = :projectId ORDER BY code ASC")
    fun getDecisions(projectId: String): Flow<List<ArchitectureDecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(adr: ArchitectureDecisionEntity)
}

@Dao
interface DatabaseDesignDao {
    @Query("SELECT * FROM database_entities WHERE projectId = :projectId ORDER BY name ASC")
    fun getEntities(projectId: String): Flow<List<DatabaseEntityRecord>>

    @Query("SELECT COUNT(*) FROM database_entities WHERE projectId = :projectId")
    suspend fun getEntityCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: DatabaseEntityRecord)

    @Update
    suspend fun updateEntity(entity: DatabaseEntityRecord)

    @Query("DELETE FROM database_entities WHERE id = :id")
    suspend fun deleteEntity(id: String)
}

@Dao
interface ApiDao {
    @Query("SELECT * FROM api_endpoints WHERE projectId = :projectId ORDER BY path ASC")
    fun getEndpoints(projectId: String): Flow<List<ApiEndpointEntity>>

    @Query("SELECT COUNT(*) FROM api_endpoints WHERE projectId = :projectId")
    suspend fun getEndpointCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndpoint(endpoint: ApiEndpointEntity)

    @Update
    suspend fun updateEndpoint(endpoint: ApiEndpointEntity)

    @Query("DELETE FROM api_endpoints WHERE id = :id")
    suspend fun deleteEndpoint(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM development_tasks WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getTasks(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM development_tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT COUNT(*) FROM development_tasks WHERE projectId = :projectId")
    suspend fun getTaskCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM development_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface TestDao {
    // Suites
    @Query("SELECT * FROM test_suites WHERE projectId = :projectId ORDER BY code ASC")
    fun getSuites(projectId: String): Flow<List<TestSuiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuite(suite: TestSuiteEntity)

    // Cases
    @Query("SELECT * FROM test_cases WHERE id = :id LIMIT 1")
    suspend fun getTestCaseDirect(id: String): TestCaseEntity?

    @Query("SELECT * FROM test_cases WHERE projectId = :projectId ORDER BY code ASC")
    fun getTestCases(projectId: String): Flow<List<TestCaseEntity>>

    @Query("SELECT * FROM test_cases WHERE linkedRequirementId = :reqId")
    fun getTestCasesByRequirement(reqId: String): Flow<List<TestCaseEntity>>

    @Query("SELECT COUNT(*) FROM test_cases WHERE projectId = :projectId")
    suspend fun getTestCaseCount(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestCase(testCase: TestCaseEntity)

    @Update
    suspend fun updateTestCase(testCase: TestCaseEntity)

    @Query("DELETE FROM test_cases WHERE id = :id")
    suspend fun deleteTestCase(id: String)

    // Executions
    @Query("SELECT * FROM test_executions WHERE projectId = :projectId ORDER BY executedAt DESC")
    fun getExecutions(projectId: String): Flow<List<TestExecutionEntity>>

    @Query("SELECT * FROM test_executions WHERE testCaseId = :testCaseId ORDER BY executedAt DESC LIMIT 1")
    fun getLatestExecution(testCaseId: String): Flow<TestExecutionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: TestExecutionEntity)
}

@Dao
interface TraceabilityDao {
    @Query("SELECT * FROM traceability_links WHERE projectId = :projectId")
    fun getLinks(projectId: String): Flow<List<TraceabilityLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: TraceabilityLinkEntity)

    @Query("DELETE FROM traceability_links WHERE id = :id")
    suspend fun deleteLink(id: String)

    @Query("DELETE FROM traceability_links WHERE sourceId = :id OR targetId = :id")
    suspend fun deleteLinksForArtifact(id: String)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getDocuments(projectId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: String): Flow<DocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE recipientUserId = :userId ORDER BY createdAt DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientUserId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientUserId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getActivities(projectId: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity)
}

@Dao
interface AiGenerationDao {
    @Query("SELECT * FROM ai_generations WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getAiGenerations(projectId: String): Flow<List<AiGenerationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiGeneration(generation: AiGenerationEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
