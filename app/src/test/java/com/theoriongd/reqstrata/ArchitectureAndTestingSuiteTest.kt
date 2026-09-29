package com.theoriongd.reqstrata

import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.GeneratedArchitectureSuggestion
import com.theoriongd.reqstrata.domain.model.GeneratedTestCaseItem
import com.theoriongd.reqstrata.domain.model.GeneratedTestSuiteResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchitectureAndTestingSuiteTest {

    @Test
    fun testGeneratedArchitectureModelIntegrity() {
        val suggestion = GeneratedArchitectureSuggestion(
            architectureStyle = "Clean Architecture + Microservices",
            systemOverview = "Test overview for banking platform",
            components = emptyList(),
            databaseTables = emptyList(),
            apiEndpoints = emptyList(),
            tradeoffs = emptyList(),
            architecturalDecisions = emptyList()
        )

        assertEquals("Clean Architecture + Microservices", suggestion.architectureStyle)
        assertEquals("Test overview for banking platform", suggestion.systemOverview)
        assertTrue(suggestion.components.isEmpty())
        assertTrue(suggestion.databaseTables.isEmpty())
        assertTrue(suggestion.apiEndpoints.isEmpty())
    }

    @Test
    fun testGeneratedTestSuiteResultIntegrity() {
        val unitTest = GeneratedTestCaseItem(
            code = "TC-UNIT-001",
            title = "Validate Currency Decimal Precision",
            testType = "Unit",
            linkedRequirementCode = "FR-001",
            preconditions = "Math engine loaded",
            testData = "Amount = 100.555",
            steps = listOf("Pass amount to engine", "Assert rounded to 2 decimals"),
            expectedResult = "Amount returns 100.56",
            priority = "High",
            severity = "Major"
        )

        val integrationTest = GeneratedTestCaseItem(
            code = "TC-INT-001",
            title = "End-to-End Payment Gateway Callback Verification",
            testType = "Integration",
            linkedRequirementCode = "FR-002",
            preconditions = "Webhook receiver active",
            testData = "Payment notification payload",
            steps = listOf("POST to webhook endpoint", "Verify transaction status marked PAID"),
            expectedResult = "Transaction updated and receipt email queued",
            priority = "Critical",
            severity = "Critical"
        )

        val suite = GeneratedTestSuiteResult(
            suiteName = "Core Banking Test Suite",
            summary = "Automated test cases generated for requirement specifications",
            unitTests = listOf(unitTest),
            integrationTests = listOf(integrationTest)
        )

        assertEquals("Core Banking Test Suite", suite.suiteName)
        assertEquals(1, suite.unitTests.size)
        assertEquals(1, suite.integrationTests.size)
        assertEquals("TC-UNIT-001", suite.unitTests[0].code)
        assertEquals("Unit", suite.unitTests[0].testType)
        assertEquals("TC-INT-001", suite.integrationTests[0].code)
        assertEquals("Integration", suite.integrationTests[0].testType)
    }

    @Test
    fun testGeminiApiClientFallbackArchitectureGeneration() = runBlocking {
        // Without an active network or API key, GeminiApiClient safely falls back to structured synthetic generation
        val result = GeminiApiClient.suggestSystemArchitecture(
            projectName = "E-Commerce Cloud",
            projectDomain = "Retail",
            requirementsSummary = "FR-001: User Authentication\nFR-002: Shopping Cart Checkout"
        )

        assertNotNull(result)
        assertTrue(result.architectureStyle.isNotEmpty())
        assertTrue(result.systemOverview.isNotEmpty())
        assertTrue(result.components.isNotEmpty())
        assertTrue(result.databaseTables.isNotEmpty())
        assertTrue(result.apiEndpoints.isNotEmpty())
        assertTrue(result.tradeoffs.isNotEmpty())
        assertTrue(result.architecturalDecisions.isNotEmpty())
    }

    @Test
    fun testGeminiApiClientFallbackTestSuiteGeneration() = runBlocking {
        // Without an active network or API key, GeminiApiClient safely falls back to structured unit & integration test generation
        val suite = GeminiApiClient.generateTestSuiteFromRequirements(
            projectName = "HealthTrack App",
            requirementsSummary = "FR-001: Record heart rate\nFR-002: Export clinical records"
        )

        assertNotNull(suite)
        assertTrue(suite.suiteName.isNotEmpty())
        assertTrue(suite.unitTests.isNotEmpty())
        assertTrue(suite.integrationTests.isNotEmpty())

        val unitItem = suite.unitTests.first()
        assertEquals("Unit", unitItem.testType)
        assertTrue(unitItem.steps.isNotEmpty())

        val intItem = suite.integrationTests.first()
        assertEquals("Integration", intItem.testType)
        assertTrue(intItem.steps.isNotEmpty())
    }

    @Test
    fun testMongoDbServiceConfigurationAndClusterDetails() {
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_USERNAME, com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.MONGODB_USERNAME)
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_PASSWORD, com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.MONGODB_PASSWORD)
        assertEquals("requirement2system", com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.DATABASE_NAME)
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_URI, com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.MONGODB_URI)
        assertNotNull(com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.MONGODB_CLUSTER)
    }

    @Test
    fun testMongoDbServiceSyncAndArtifactStorage() = runBlocking {
        val testProject = com.theoriongd.reqstrata.data.local.entity.ProjectEntity(
            id = "proj-mongo-1",
            name = "Cloud Native Banking Platform",
            domain = "FinTech",
            description = "High-throughput banking backend with MongoDB Atlas persistence",
            projectType = "Microservices Backend",
            techStack = "Kotlin, Ktor, MongoDB, Jetpack Compose",
            methodology = "Agile Scrum",
            ownerId = "owner-1"
        )

        val result = com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.syncProjectArtifacts(
            project = testProject,
            requirements = emptyList(),
            components = emptyList(),
            tables = emptyList(),
            endpoints = emptyList(),
            testSuites = emptyList(),
            testCases = emptyList()
        )

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()
        assertNotNull(stats)
        assertEquals(1, stats?.syncedProjects)
        assertEquals(1, com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.getRemoteDocumentCount(com.theoriongd.reqstrata.data.remote.mongo.MongoDbService.COLL_PROJECTS))
    }

    @Test
    fun testMongoStitchClientAndEnvVariables() {
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_USERNAME, com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.username)
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_PASSWORD, com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.password)
        assertEquals(com.theoriongd.reqstrata.BuildConfig.MONGODB_URI, com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.uri)
        assertEquals("requirement2system", com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.databaseName)
        assertNotNull(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.cluster)
    }

    @Test
    fun testMongoDocumentOperationsAndRealtimeSyncService() = runBlocking {
        val stitch = com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
        val reqCollection = stitch.requirements

        // 1. Document insertOne operation
        val reqDoc = com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument()
            .put("_id", "test-req-mongo-001")
            .put("projectId", "proj-realtime-1")
            .put("code", "REQ-001")
            .put("title", "Realtime Atlas Synchronization")
            .put("description", "Changes pushed in real-time to MongoDB Atlas")
            .put("type", "Functional")
            .put("priority", "Critical")

        val insertResult = reqCollection.insertOne(reqDoc)
        assertTrue(insertResult.isSuccess)
        assertEquals("test-req-mongo-001", reqCollection.findById("test-req-mongo-001")?.id)
        assertEquals(1L, reqCollection.countDocuments(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter.eq("projectId", "proj-realtime-1")))

        // 2. Document replaceOne operation
        val updatedDoc = com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument()
            .put("_id", "test-req-mongo-001")
            .put("projectId", "proj-realtime-1")
            .put("code", "REQ-001")
            .put("title", "Realtime Atlas Synchronization (Updated)")
            .put("priority", "High")
        val replaced = reqCollection.replaceOne(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter.eq("_id", "test-req-mongo-001"), updatedDoc)
        assertTrue(replaced)
        assertEquals("Realtime Atlas Synchronization (Updated)", reqCollection.findById("test-req-mongo-001")?.getString("title"))

        // 3. Background Sync Service real-time push
        com.theoriongd.reqstrata.data.remote.mongo.sync.MongoBackgroundSyncService.pushChangeRealtime(
            collection = com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient.COLL_REQUIREMENTS,
            operation = "replace",
            documentId = "test-req-mongo-001",
            document = updatedDoc
        )
        val metrics = com.theoriongd.reqstrata.data.remote.mongo.sync.MongoBackgroundSyncService.metrics.value
        assertNotNull(metrics)
        assertTrue(metrics.activeClusterUri.startsWith("mongodb+srv://"))

        // 4. Document deleteOne operation
        val deleted = reqCollection.deleteOne(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter.eq("_id", "test-req-mongo-001"))
        assertTrue(deleted)
        assertEquals(0L, reqCollection.countDocuments(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter.eq("projectId", "proj-realtime-1")))
    }

    @Test
    fun testMongoRepositoryLayerDocumentOperations() = runBlocking {
        val user = com.theoriongd.reqstrata.data.local.entity.UserEntity(
            id = "user-test-1",
            fullName = "Sarah Connor",
            email = "sarah@cyberdyne.io",
            passwordHash = "hash123",
            titleOrRole = "Administrator"
        )
        val stitch = com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
        stitch.users.insertOne(com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument.fromUser(user))
        val reqRepo = com.theoriongd.reqstrata.data.remote.mongo.repository.MongoRequirementDocumentRepository()
        val archRepo = com.theoriongd.reqstrata.data.remote.mongo.repository.MongoArchitectureDocumentRepository()

        // Test requirement document creation
        val req = reqRepo.createRequirement(
            projectId = "proj-doc-test",
            title = "Zero Trust Authorization Layer",
            description = "Token-based RBAC enforcement at gateway",
            type = com.theoriongd.reqstrata.domain.model.RequirementType.SECURITY,
            priority = com.theoriongd.reqstrata.domain.model.RequirementPriority.CRITICAL,
            acceptanceCriteria = listOf("Verify JWT expiration", "Validate claims"),
            author = user
        )
        assertNotNull(req.id)
        assertEquals("Zero Trust Authorization Layer", req.title)

        val directReq = reqRepo.getRequirementDirect(req.id)
        assertNotNull(directReq)
        assertEquals(req.id, directReq?.id)

        // Test architecture component document creation
        val comp = archRepo.addComponent(
            projectId = "proj-doc-test",
            name = "Auth & Identity Gateway",
            style = "Microservices",
            layer = "Security Perimeter",
            responsibilities = "OIDC token validation and rate limiting",
            techStack = "Kotlin, Ktor, MongoDB Atlas",
            dependencies = listOf("API Gateway"),
            linkedReqIds = listOf(req.id),
            actor = user.fullName
        )
        assertNotNull(comp.id)
        assertEquals("Auth & Identity Gateway", comp.name)

        // Clean up
        reqRepo.deleteRequirement(req.id, actorUserId = user.id, projectId = "proj-doc-test")
        archRepo.deleteComponent(comp.id)
    }

    @Test
    fun testActiveDatabaseAccountVerificationAndRoleRouting() = runBlocking {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = com.theoriongd.reqstrata.data.local.AppDatabase.getDatabase(context)
        val authRepo = com.theoriongd.reqstrata.data.repository.AuthRepository(db)

        // Ensure default database accounts are seeded for verification
        if (authRepo.getActiveDatabaseAccounts().none { it.email == "admin@req2sys.io" }) {
            val seedUsers = listOf(
                com.theoriongd.reqstrata.data.local.entity.UserEntity(
                    id = "seed-admin-1",
                    fullName = "Admin User",
                    email = "admin@req2sys.io",
                    passwordHash = authRepo.hashPassword("Pass123!"),
                    titleOrRole = "Project Owner / Admin",
                    status = "ACTIVE"
                ),
                com.theoriongd.reqstrata.data.local.entity.UserEntity(
                    id = "seed-ba-1",
                    fullName = "BA User",
                    email = "ba@req2sys.io",
                    passwordHash = authRepo.hashPassword("Pass123!"),
                    titleOrRole = "Business Analyst",
                    status = "ACTIVE"
                ),
                com.theoriongd.reqstrata.data.local.entity.UserEntity(
                    id = "seed-arch-1",
                    fullName = "Architect User",
                    email = "architect@req2sys.io",
                    passwordHash = authRepo.hashPassword("Pass123!"),
                    titleOrRole = "System Architect",
                    status = "ACTIVE"
                ),
                com.theoriongd.reqstrata.data.local.entity.UserEntity(
                    id = "seed-dev-1",
                    fullName = "Developer User",
                    email = "dev@req2sys.io",
                    passwordHash = authRepo.hashPassword("Pass123!"),
                    titleOrRole = "Developer",
                    status = "ACTIVE"
                ),
                com.theoriongd.reqstrata.data.local.entity.UserEntity(
                    id = "seed-qa-1",
                    fullName = "QA User",
                    email = "qa@req2sys.io",
                    passwordHash = authRepo.hashPassword("Pass123!"),
                    titleOrRole = "Tester / QA",
                    status = "ACTIVE"
                )
            )
            for (u in seedUsers) {
                db.userDao().insertUser(u)
            }
        }

        val activeAccounts = authRepo.getActiveDatabaseAccounts()
        assertTrue(activeAccounts.isNotEmpty())
        assertTrue(activeAccounts.all { it.status == "ACTIVE" })

        // Check seeded accounts for all roles (Direct Database Auth)
        val adminAccount = activeAccounts.firstOrNull { it.email == "admin@req2sys.io" }
        assertNotNull(adminAccount)
        assertEquals("ACTIVE", adminAccount?.status)

        val baAccount = activeAccounts.firstOrNull { it.email == "ba@req2sys.io" }
        assertNotNull(baAccount)

        val archAccount = activeAccounts.firstOrNull { it.email == "architect@req2sys.io" }
        assertNotNull(archAccount)

        val devAccount = activeAccounts.firstOrNull { it.email == "dev@req2sys.io" }
        assertNotNull(devAccount)

        val qaAccount = activeAccounts.firstOrNull { it.email == "qa@req2sys.io" }
        assertNotNull(qaAccount)

        // Verify inactive account rejection
        val inactiveUser = com.theoriongd.reqstrata.data.local.entity.UserEntity(
            id = "inactive-1",
            fullName = "Inactive User",
            email = "inactive@test.io",
            passwordHash = "hash".hashCode().toString(),
            status = "INACTIVE"
        )
        db.userDao().insertUser(inactiveUser)
        val inactiveLoginResult = authRepo.login("inactive@test.io", "hash")
        assertTrue(inactiveLoginResult.isFailure)
        assertTrue(inactiveLoginResult.exceptionOrNull()?.message?.contains("inactive", ignoreCase = true) == true)
    }

    @Test
    fun testInAppAndPhoneNotificationDispatch() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        com.theoriongd.reqstrata.ui.notifications.AppNotificationManager.initChannel(context)

        com.theoriongd.reqstrata.ui.notifications.AppNotificationManager.notify(
            context = context,
            title = "Requirement Specification Committed",
            message = "REQ-042: Real-time Gateway has been pushed to MongoDB Atlas",
            type = com.theoriongd.reqstrata.ui.notifications.NotificationType.SUCCESS
        )

        val inApp = com.theoriongd.reqstrata.ui.notifications.AppNotificationManager.inAppNotification.value
        assertNotNull(inApp)
        assertEquals("Requirement Specification Committed", inApp?.title)
        assertEquals(com.theoriongd.reqstrata.ui.notifications.NotificationType.SUCCESS, inApp?.type)

        com.theoriongd.reqstrata.ui.notifications.AppNotificationManager.dismissInApp()
        assertNull(com.theoriongd.reqstrata.ui.notifications.AppNotificationManager.inAppNotification.value)
    }
}
