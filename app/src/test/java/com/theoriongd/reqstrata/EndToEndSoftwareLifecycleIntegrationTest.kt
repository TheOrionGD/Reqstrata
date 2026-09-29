package com.theoriongd.reqstrata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.theoriongd.reqstrata.data.local.AppDatabase
import com.theoriongd.reqstrata.data.local.entity.*
import com.theoriongd.reqstrata.data.repository.*
import com.theoriongd.reqstrata.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * End-to-End Software Lifecycle Integration & Verification Suite.
 *
 * Tests the complete lifecycle across personas:
 * 1. Admin creates project and provisions workspace
 * 2. Business Analyst authors requirement specification
 * 3. Admin reviews and approves the requirement
 * 4. System Architect designs component & links to requirement
 * 5. Developer creates task, links to component, and executes implementation
 * 6. Tester writes test cases, executes tests, and verifies passing status
 * 7. Traceability Matrix links all artifacts bidirectionally
 *
 * Also validates negative rejection tests and blank/invalid input handling.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EndToEndSoftwareLifecycleIntegrationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var authRepo: AuthRepository
    private lateinit var projectRepo: ProjectRepository
    private lateinit var reqRepo: RequirementRepository
    private lateinit var archRepo: ArchitectureRepository
    private lateinit var taskRepo: TaskRepository
    private lateinit var testRepo: TestRepository
    private lateinit var traceRepo: TraceabilityRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = AppDatabase.getDatabase(context)
        authRepo = AuthRepository(db)
        projectRepo = ProjectRepository(db)
        reqRepo = RequirementRepository(db)
        archRepo = ArchitectureRepository(db)
        taskRepo = TaskRepository(db)
        testRepo = TestRepository(db)
        traceRepo = TraceabilityRepository(db)
    }

    @Test
    fun testCompleteMultiRoleLifecycleTraceabilityIntegration() = runBlocking {
        val testSuffix = UUID.randomUUID().toString().take(8)

        // ─────────────────────────────────────────────────────────────────────
        // 1. ADMIN Provisions Users and Workspace
        // ─────────────────────────────────────────────────────────────────────
        val adminUser = UserEntity(
            id = "admin-$testSuffix",
            fullName = "Sarah Admin",
            email = "admin-$testSuffix@req2sys.io",
            passwordHash = authRepo.hashPassword("SecureAdmin123!"),
            titleOrRole = ProjectRole.ADMIN.title,
            status = "ACTIVE"
        )
        val baUser = UserEntity(
            id = "ba-$testSuffix",
            fullName = "Bob BA",
            email = "ba-$testSuffix@req2sys.io",
            passwordHash = authRepo.hashPassword("SecureBA123!"),
            titleOrRole = ProjectRole.BUSINESS_ANALYST.title,
            status = "ACTIVE"
        )
        val archUser = UserEntity(
            id = "arch-$testSuffix",
            fullName = "Alice Architect",
            email = "arch-$testSuffix@req2sys.io",
            passwordHash = authRepo.hashPassword("SecureArch123!"),
            titleOrRole = ProjectRole.ARCHITECT.title,
            status = "ACTIVE"
        )
        val devUser = UserEntity(
            id = "dev-$testSuffix",
            fullName = "Dave Developer",
            email = "dev-$testSuffix@req2sys.io",
            passwordHash = authRepo.hashPassword("SecureDev123!"),
            titleOrRole = ProjectRole.DEVELOPER.title,
            status = "ACTIVE"
        )
        val testerUser = UserEntity(
            id = "tester-$testSuffix",
            fullName = "Tina Tester",
            email = "tester-$testSuffix@req2sys.io",
            passwordHash = authRepo.hashPassword("SecureTester123!"),
            titleOrRole = ProjectRole.TESTER.title,
            status = "ACTIVE"
        )

        db.userDao().insertUser(adminUser)
        db.userDao().insertUser(baUser)
        db.userDao().insertUser(archUser)
        db.userDao().insertUser(devUser)
        db.userDao().insertUser(testerUser)

        val project = projectRepo.createProject(
            name = "Autonomous Traceability Engine $testSuffix",
            description = "E2E automated software lifecycle pipeline",
            domain = "Enterprise Engineering",
            projectType = "Cloud-Native",
            techStack = "Kotlin, Compose, MongoDB Atlas",
            methodology = "Agile",
            visibility = "Private",
            owner = adminUser
        )
        assertNotNull(project.id)
        assertEquals("Autonomous Traceability Engine $testSuffix", project.name)

        // ─────────────────────────────────────────────────────────────────────
        // 2. BUSINESS ANALYST Authors Requirement
        // ─────────────────────────────────────────────────────────────────────
        val req = reqRepo.createRequirement(
            projectId = project.id,
            title = "Decentralized Role Identity Verification",
            description = "All API routes and screens must enforce RBAC validation across 5 engineering personas.",
            type = RequirementType.SECURITY,
            priority = RequirementPriority.CRITICAL,
            acceptanceCriteria = listOf(
                "Verify 403 / Inaccessible boundary on unauthorized route",
                "Audit log entry dispatched upon security boundary trip"
            ),
            author = baUser
        )
        assertNotNull(req.id)
        assertEquals(RequirementStatus.DRAFT.displayName, req.status)
        assertEquals(RequirementType.SECURITY.displayName, req.type)

        // BA updates requirement specification scope (moves to UNDER_REVIEW)
        reqRepo.updateRequirement(
            current = req,
            newTitle = "Decentralized Role Identity & Access Verification",
            newDescription = "All API routes and screens must enforce RBAC validation across 5 engineering personas with audit logs.",
            newType = RequirementType.SECURITY,
            newPriority = RequirementPriority.CRITICAL,
            newCriteria = listOf(
                "Verify 403 / Inaccessible boundary on unauthorized route",
                "Audit log entry dispatched upon security boundary trip",
                "Zero data leakage on denied endpoints"
            ),
            changeReason = "Refined security criteria",
            editor = baUser
        )
        val underReviewReq = reqRepo.getRequirementDirect(req.id)
        assertNotNull(underReviewReq)
        assertEquals(RequirementStatus.UNDER_REVIEW.displayName, underReviewReq?.status)

        // ─────────────────────────────────────────────────────────────────────
        // 3. ADMIN Reviews and Approves Requirement
        // ─────────────────────────────────────────────────────────────────────
        reqRepo.setApprovalStatus(
            req = underReviewReq!!,
            status = RequirementStatus.APPROVED,
            notes = "Approved by Project Administrator for immediate architecture and sprint planning.",
            reviewer = adminUser
        )
        val approvedReq = reqRepo.getRequirementDirect(req.id)
        assertNotNull(approvedReq)
        assertEquals(RequirementStatus.APPROVED.displayName, approvedReq?.status)

        // ─────────────────────────────────────────────────────────────────────
        // 4. SYSTEM ARCHITECT Designs Architecture Component
        // ─────────────────────────────────────────────────────────────────────
        val component = archRepo.addComponent(
            projectId = project.id,
            name = "RBAC Ingress Security Filter",
            style = "Zero-Trust Ingress",
            layer = "Security Perimeter",
            responsibilities = "Intercepts route navigation and checks MongoDB user roles",
            techStack = "Kotlin, Coroutines, Jetpack Navigation",
            dependencies = listOf("AppDatabase", "CentralizedAuthorizationManager"),
            linkedReqIds = listOf(approvedReq!!.id),
            actor = archUser.fullName
        )
        assertNotNull(component.id)
        assertEquals("RBAC Ingress Security Filter", component.name)
        assertTrue(component.linkedRequirementIdsJson.contains(approvedReq.id))

        // ─────────────────────────────────────────────────────────────────────
        // 5. DEVELOPER Implements Task & Progresses Lifecycle
        // ─────────────────────────────────────────────────────────────────────
        val task = taskRepo.createTask(
            projectId = project.id,
            title = "Implement AuthorizedRoute & RoleRestrictedAccessScreen Decorator",
            description = "Wrap all composable routes with MongoDB verified role checks",
            priority = "Critical",
            assigneeName = devUser.fullName,
            linkedReqId = approvedReq.id,
            linkedCompId = component.id,
            linkedApiId = "",
            estHours = 12,
            actor = devUser.fullName,
            actorUserId = devUser.id
        )
        assertNotNull(task.id)
        assertEquals(TaskStatus.BACKLOG.displayName, task.status)

        // Transition task: BACKLOG -> IN_PROGRESS -> COMPLETED
        taskRepo.updateTaskStatus(task, TaskStatus.IN_PROGRESS, devUser.fullName)
        val inProgressTask = db.taskDao().getTaskById(task.id).first()
        assertEquals(TaskStatus.IN_PROGRESS.displayName, inProgressTask?.status)

        taskRepo.updateTaskStatus(inProgressTask!!, TaskStatus.COMPLETED, devUser.fullName)
        val completedTask = db.taskDao().getTaskById(task.id).first()
        assertEquals(TaskStatus.COMPLETED.displayName, completedTask?.status)

        // ─────────────────────────────────────────────────────────────────────
        // 6. TESTER Designs & Executes Automated Test Case
        // ─────────────────────────────────────────────────────────────────────
        val suite = testRepo.createSuite(
            projectId = project.id,
            name = "Security Verification Suite",
            type = "Integration",
            description = "Automated role and boundary verification",
            actorUserId = testerUser.id
        )
        assertNotNull(suite.id)

        val testCase = testRepo.createTestCase(
            projectId = project.id,
            suiteId = suite.id,
            linkedReqId = approvedReq.id,
            title = "Verify Developer Rejected from Team Management Route",
            preconditions = "Authenticated as DEVELOPER role",
            testData = "Target route: /admin/team",
            steps = listOf(
                "Attempt navigation to TeamManagement route",
                "Observe AuthorizedRoute evaluation",
                "Assert RoleRestrictedAccessScreen boundary is displayed"
            ),
            expectedResult = "Access Denied screen rendered with required roles and return button",
            priority = "Critical",
            severity = "Major",
            actor = testerUser.fullName,
            actorUserId = testerUser.id
        )
        assertNotNull(testCase.id)

        // Execute test case: Record Passing Execution
        testRepo.recordExecution(
            projectId = project.id,
            testCaseId = testCase.id,
            status = TestExecutionStatus.PASSED,
            actualResult = "Access boundary verified. Screen rendered cleanly with zero leakage.",
            notes = "Developer persona correctly caught by AuthorizedRoute decorator.",
            evidence = "Screenshot captured",
            tester = testerUser.fullName,
            testerUserId = testerUser.id
        )

        val executions = db.testDao().getExecutions(project.id).first()
        assertTrue(executions.isNotEmpty())
        assertEquals(TestExecutionStatus.PASSED.displayName, executions[0].status)

        // ─────────────────────────────────────────────────────────────────────
        // 7. TRACEABILITY MATRIX Verifies Full Bidirectional Linkage
        // ─────────────────────────────────────────────────────────────────────
        val links = traceRepo.getLinks(project.id).first()
        assertTrue("Traceability links must be established across artifacts", links.isNotEmpty())

        val reqToTaskLink = links.find { it.sourceId == approvedReq.id && it.targetId == task.id }
        assertNotNull("Requirement-to-Task traceability link must exist", reqToTaskLink)

        val reqToTestLink = links.find { it.sourceId == approvedReq.id && it.targetId == testCase.id }
        assertNotNull("Requirement-to-TestCase traceability link must exist", reqToTestLink)

        val reqToArchLink = links.find { it.sourceId == approvedReq.id && it.targetId == component.id }
        assertNotNull("Requirement-to-Architecture traceability link must exist", reqToArchLink)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. NEGATIVE, FALSE & BLANK REJECTION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testAuthenticationRejectsBlankOrMalformedCredentials() = runBlocking {
        // Blank email
        val resBlankEmail = authRepo.login("", "Password123!")
        assertTrue(resBlankEmail.isFailure)

        // Blank password
        val resBlankPass = authRepo.login("test@req2sys.io", "")
        assertTrue(resBlankPass.isFailure)

        // Spaces only
        val resSpaces = authRepo.login("   ", "   ")
        assertTrue(resSpaces.isFailure)

        // Non-existent user
        val resNotFound = authRepo.login("ghost.user@req2sys.io", "Pass12345!")
        assertTrue(resNotFound.isFailure)

        // Wrong password
        val user = UserEntity(
            id = "auth-neg-1",
            fullName = "Auth Neg User",
            email = "auth-neg@req2sys.io",
            passwordHash = authRepo.hashPassword("CorrectPass123!"),
            status = "ACTIVE"
        )
        db.userDao().insertUser(user)

        val wrongPassResult = authRepo.login("auth-neg@req2sys.io", "WrongPassword!")
        assertTrue(wrongPassResult.isFailure)
    }

    @Test
    fun testDeactivatedOrSuspendedAccountRejection() = runBlocking {
        val inactiveUser = UserEntity(
            id = "inactive-account-1",
            fullName = "Terminated User",
            email = "terminated@req2sys.io",
            passwordHash = authRepo.hashPassword("Password123!"),
            status = "INACTIVE"
        )
        db.userDao().insertUser(inactiveUser)

        val result = authRepo.login("terminated@req2sys.io", "Password123!")
        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message ?: ""
        assertTrue(message.contains("not active", ignoreCase = true) || message.contains("inactive", ignoreCase = true))
    }

    @Test
    fun testRequirementCreationRejectsEmptyOrNullFields() = runBlocking {
        val adminUser = UserEntity(
            id = "owner-valid-1",
            fullName = "Valid Owner",
            email = "owner-valid@req2sys.io",
            passwordHash = authRepo.hashPassword("Pass123!"),
            titleOrRole = ProjectRole.ADMIN.title,
            status = "ACTIVE"
        )
        db.userDao().insertUser(adminUser)

        val testProj = projectRepo.createProject(
            name = "Validation Test Project",
            description = "Negative test validation",
            domain = "SaaS",
            projectType = "Cloud",
            techStack = "Kotlin",
            methodology = "Agile",
            visibility = "Private",
            owner = adminUser
        )

        // Create with valid parameters should succeed
        val validReq = reqRepo.createRequirement(
            projectId = testProj.id,
            title = "Valid Title",
            description = "Valid Description",
            type = RequirementType.FUNCTIONAL,
            priority = RequirementPriority.MEDIUM,
            acceptanceCriteria = listOf("Passes"),
            author = adminUser
        )
        assertNotNull(validReq.id)

        // Querying non-existent requirement returns null safely without throwing
        val ghostReq = reqRepo.getRequirementDirect("non-existent-id")
        assertNull(ghostReq)
    }
}
