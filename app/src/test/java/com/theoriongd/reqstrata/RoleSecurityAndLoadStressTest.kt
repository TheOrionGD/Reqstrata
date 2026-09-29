package com.theoriongd.reqstrata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.theoriongd.reqstrata.data.local.AppDatabase
import com.theoriongd.reqstrata.data.local.entity.RequirementEntity
import com.theoriongd.reqstrata.data.local.entity.UserEntity
import com.theoriongd.reqstrata.data.repository.RequirementRepository
import com.theoriongd.reqstrata.data.repository.TaskRepository
import com.theoriongd.reqstrata.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

/**
 * Concurrency, High-Volume Load, and Stress Test Suite.
 *
 * Verifies that:
 * 1. Concurrent permission evaluation across all 5 user roles is thread-safe and fast.
 * 2. High-volume bulk requirement & task mutations execute without race conditions.
 * 3. Rapid role-switching across simulated concurrent sessions maintains strict boundary isolation.
 * 4. Extreme payloads, large text blocks, and special characters are handled safely.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoleSecurityAndLoadStressTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var reqRepo: RequirementRepository
    private lateinit var taskRepo: TaskRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = AppDatabase.getDatabase(context)
        reqRepo = RequirementRepository(db)
        taskRepo = TaskRepository(db)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. HIGH-CONCURRENCY MULTI-ROLE LOAD TEST (500 Concurrent Evaluations)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testHighConcurrencyRolePermissionEvaluationUnderLoad() = runBlocking {
        val totalRequests = 500
        val roles = ProjectRole.entries.toTypedArray()
        val modules = ProjectModule.entries.toTypedArray()

        val successCount = AtomicInteger(0)
        val adminFullAccessCount = AtomicInteger(0)
        val rejectionCount = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        val jobs = List(totalRequests) { i ->
            async(Dispatchers.Default) {
                val role = roles[i % roles.size]
                val module = modules[i % modules.size]

                val permission = ProjectAccessPolicy.getPermission(role, module)
                val canRead = ProjectAccessPolicy.canRead(role, module)
                val canWrite = ProjectAccessPolicy.canWrite(role, module)

                if (role == ProjectRole.ADMIN) {
                    assertEquals(PermissionLevel.FULL, permission)
                    assertTrue(canRead)
                    assertTrue(canWrite)
                    adminFullAccessCount.incrementAndGet()
                } else {
                    if (permission == PermissionLevel.NONE) {
                        assertFalse(canRead)
                        assertFalse(canWrite)
                        rejectionCount.incrementAndGet()
                    }
                }
                successCount.incrementAndGet()
            }
        }

        jobs.awaitAll()
        val durationMs = System.currentTimeMillis() - startTime

        assertEquals(totalRequests, successCount.get())
        assertTrue("Admin access must have been asserted in load test", adminFullAccessCount.get() > 0)
        assertTrue("Rejections must have been recorded for non-admin restricted modules", rejectionCount.get() > 0)
        assertTrue("500 permission evaluations must execute rapidly (< 3000ms)", durationMs < 3000L)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. BULK DATA MUTATION LOAD TEST (Entities Processed Concurrently)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testBulkRequirementAndTaskCreationUnderLoad() = runBlocking {
        val batchSize = 25
        val projectId = "proj-load-test-1"

        val workerUser = UserEntity(
            id = "worker-load-1",
            fullName = "Worker User",
            email = "worker@req2sys.io",
            passwordHash = "hash".hashCode().toString(),
            titleOrRole = ProjectRole.BUSINESS_ANALYST.title,
            status = "ACTIVE"
        )
        db.userDao().insertUser(workerUser)

        val startTime = System.currentTimeMillis()

        // Create requirements sequentially to preserve code sequence formatting
        val createdReqs = mutableListOf<RequirementEntity>()
        for (i in 0 until batchSize) {
            val req = reqRepo.createRequirement(
                projectId = projectId,
                title = "Automated High-Load Requirement $i",
                description = "Load stress testing requirement entity $i with automated acceptance criteria",
                type = if (i % 2 == 0) RequirementType.FUNCTIONAL else RequirementType.SECURITY,
                priority = if (i % 3 == 0) RequirementPriority.CRITICAL else RequirementPriority.HIGH,
                acceptanceCriteria = listOf("Latency < 100ms", "Zero data loss"),
                author = workerUser
            )
            createdReqs.add(req)
        }
        assertEquals(batchSize, createdReqs.size)

        // Concurrent task creation linked to requirements
        val taskJobs = createdReqs.mapIndexed { idx, req ->
            async(Dispatchers.IO) {
                taskRepo.createTask(
                    projectId = projectId,
                    title = "Implement Engine for ${req.code}",
                    description = "High load worker task execution",
                    priority = "High",
                    assigneeName = "Developer Worker $idx",
                    linkedReqId = req.id,
                    linkedCompId = "",
                    linkedApiId = "",
                    estHours = 8,
                    actor = "Developer Worker $idx",
                    actorUserId = workerUser.id
                )
            }
        }
        val createdTasks = taskJobs.awaitAll()
        assertEquals(batchSize, createdTasks.size)

        val durationMs = System.currentTimeMillis() - startTime
        assertTrue("Bulk database operations under load must complete successfully (< 5000ms)", durationMs < 5000L)

        // Verify all entities persisted in DB
        val retrievedReqs = db.requirementDao().getRequirements(projectId).first()
        assertEquals(batchSize, retrievedReqs.size)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. RAPID ROLE-SWITCHING & ACCESS BOUNDARY STRESS TEST
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testRapidRoleSwitchingMaintainsDeterministicBoundaries() {
        val roles = ProjectRole.entries
        val sensitiveModules = listOf(
            ProjectModule.TEAM_MANAGEMENT,
            ProjectModule.ROLE_MANAGEMENT,
            ProjectModule.AUDIT_LOGS
        )

        // Rapidly alternate roles 200 times and assert invariant safety
        for (iteration in 0 until 200) {
            val role = roles[iteration % roles.size]

            for (mod in sensitiveModules) {
                val canWrite = ProjectAccessPolicy.canWrite(role, mod)
                val canRead = ProjectAccessPolicy.canRead(role, mod)

                if (role == ProjectRole.ADMIN) {
                    assertTrue("ADMIN must always have write access to ${mod.name}", canWrite)
                    assertTrue("ADMIN must always have read access to ${mod.name}", canRead)
                } else {
                    assertFalse("Non-admin role ${role.title} must be rejected from ${mod.name}", canWrite)
                    assertFalse("Non-admin role ${role.title} must be rejected from ${mod.name}", canRead)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. EXTREME PAYLOAD & INJECTION RESISTANCE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testExtremePayloadAndSpecialCharactersSafety() = runBlocking {
        val hugeDescription = "A".repeat(8000)
        val xssTitle = "<script>alert('XSS')</script> & <b>bold</b> \uD83D\uDE80 \uD83D\uDD25"
        val unicodeName = "システム要件仕様書 — 🚀 Enterprise Traceability"

        val authorUser = UserEntity(
            id = "author-xss-1",
            fullName = unicodeName,
            email = "unicode@req2sys.io",
            passwordHash = "hash".hashCode().toString(),
            titleOrRole = ProjectRole.BUSINESS_ANALYST.title,
            status = "ACTIVE"
        )
        db.userDao().insertUser(authorUser)

        val req = reqRepo.createRequirement(
            projectId = "proj-stress-1",
            title = xssTitle,
            description = hugeDescription,
            type = RequirementType.NON_FUNCTIONAL,
            priority = RequirementPriority.HIGH,
            acceptanceCriteria = listOf("Safe storage", "No SQL/XSS execution"),
            author = authorUser
        )

        assertNotNull(req.id)
        assertEquals(xssTitle, req.title)
        assertEquals(8000, req.description.length)
        assertEquals(unicodeName, req.authorName)

        val retrieved = reqRepo.getRequirementDirect(req.id)
        assertNotNull(retrieved)
        assertEquals(xssTitle, retrieved?.title)
        assertEquals(8000, retrieved?.description?.length)
    }
}
