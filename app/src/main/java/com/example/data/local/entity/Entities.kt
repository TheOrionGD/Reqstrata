package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val titleOrRole: String = "Software Engineer",
    val avatarColor: Long = 0xFF6D28D9,
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val tenantId: String = "tenant_default",
    val tenantName: String = "Enterprise Core Workspace",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val domain: String,
    val projectType: String,
    val techStack: String,
    val methodology: String,
    val visibility: String = "Private",
    val status: String = "Active", // Active, Archived
    val ownerId: String,
    val tenantId: String = "tenant_default",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "project_members",
    indices = [Index(value = ["projectId", "userId"], unique = true)]
)
data class ProjectMemberEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val role: String, // ADMIN, BUSINESS_ANALYST, ARCHITECT, DEVELOPER, TESTER
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requirements", indices = [Index(value = ["projectId", "code"], unique = true)])
data class RequirementEntity(
    @PrimaryKey val id: String, // UUID
    val projectId: String,
    val code: String, // REQ-001
    val title: String,
    val description: String,
    val type: String, // Functional, Non-functional, etc.
    val priority: String, // Critical, High, Medium, Low
    val status: String, // Draft, Under Review, Approved, Rejected, Deprecated
    val version: Int = 1,
    val authorId: String,
    val authorName: String,
    val acceptanceCriteriaJson: String = "[]",
    val aiQualityJson: String = "{}",
    val dependenciesJson: String = "[]",
    val approvalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requirement_versions")
data class RequirementVersionEntity(
    @PrimaryKey val id: String,
    val requirementId: String,
    val versionNumber: Int,
    val title: String,
    val description: String,
    val acceptanceCriteriaJson: String,
    val changedBy: String,
    val changeReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "use_cases", indices = [Index(value = ["projectId", "code"], unique = true)])
data class UseCaseEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // UC-001
    val requirementId: String,
    val name: String,
    val actor: String,
    val goal: String,
    val preconditions: String,
    val postconditions: String,
    val mainFlowJson: String = "[]",
    val altFlowsJson: String = "[]",
    val exceptionFlowsJson: String = "[]",
    val status: String = "Approved",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "architecture_components", indices = [Index(value = ["projectId", "code"], unique = true)])
data class ArchitectureComponentEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // ARCH-001
    val name: String,
    val style: String, // Clean Architecture, Microservices, etc.
    val layerOrModule: String,
    val responsibilities: String,
    val techStack: String,
    val dependenciesJson: String = "[]",
    val linkedRequirementIdsJson: String = "[]",
    val status: String = "Approved",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "architecture_decisions")
data class ArchitectureDecisionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // ADR-001
    val title: String,
    val status: String = "Accepted", // Proposed, Accepted, Superseded
    val context: String,
    val decision: String,
    val consequences: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "database_entities", indices = [Index(value = ["projectId", "name"], unique = true)])
data class DatabaseEntityRecord(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // DB-001
    val name: String,
    val description: String,
    val fieldsJson: String = "[]", // List of Field(name, type, pk, nullable, unique)
    val relationshipsJson: String = "[]", // List of Rel(target, type, fk)
    val indexesJson: String = "[]",
    val status: String = "Approved",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_endpoints", indices = [Index(value = ["projectId", "method", "path"], unique = true)])
data class ApiEndpointEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // API-001
    val method: String, // GET, POST, PUT, DELETE, PATCH
    val path: String,
    val description: String,
    val authRequired: Boolean = true,
    val requestSchema: String = "{}",
    val responseSchema: String = "{}",
    val validationRulesJson: String = "[]",
    val errorResponsesJson: String = "[]",
    val linkedRequirementId: String = "",
    val status: String = "Approved",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "development_tasks", indices = [Index(value = ["projectId", "code"], unique = true)])
data class TaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // DEV-001
    val title: String,
    val description: String,
    val priority: String = "Medium",
    val status: String = "Backlog", // Backlog, Ready, In Progress, Blocked, In Review, Completed
    val assigneeId: String = "",
    val assigneeName: String = "Unassigned",
    val linkedRequirementId: String = "",
    val linkedComponentId: String = "",
    val linkedApiId: String = "",
    val estimatedHours: Int = 4,
    val technicalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_suites")
data class TestSuiteEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // TS-001
    val name: String,
    val type: String, // Functional, Regression, Security, Performance, Acceptance, Smoke
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_cases", indices = [Index(value = ["projectId", "code"], unique = true)])
data class TestCaseEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String, // TC-001
    val suiteId: String,
    val linkedRequirementId: String,
    val title: String,
    val preconditions: String,
    val testData: String,
    val stepsJson: String = "[]",
    val expectedResult: String,
    val priority: String = "Medium",
    val severity: String = "Major", // Critical, Major, Minor, Trivial
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_executions")
data class TestExecutionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val testCaseId: String,
    val status: String, // Passed, Failed, Blocked, Skipped
    val actualResult: String,
    val failureNotes: String = "",
    val evidence: String = "",
    val executedBy: String,
    val executedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "traceability_links")
data class TraceabilityLinkEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sourceType: String, // REQUIREMENT, USE_CASE, ARCHITECTURE, DATABASE, API, TASK, TEST_CASE
    val sourceId: String,
    val targetType: String,
    val targetId: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val docType: String, // SRS, SDD, API_DOCS, TEST_PLAN, TEST_REPORT, RTM, PROJECT_SUMMARY
    val title: String,
    val contentMarkdown: String,
    val generatedBy: String,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val recipientUserId: String,
    val title: String,
    val message: String,
    val targetType: String, // REQUIREMENT, TASK, ARCHITECTURE, TEST, APPROVAL
    val targetId: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val actorName: String,
    val action: String,
    val details: String,
    val targetType: String,
    val targetId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_generations")
data class AiGenerationEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val generationType: String,
    val promptUsed: String,
    val resultSummary: String,
    val status: String = "SUCCESS",
    val initiatedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
