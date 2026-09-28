package com.example

import com.example.data.remote.mongo.model.*
import com.example.domain.model.ProjectRole
import com.example.ui.navigation.AppRoutes
import com.example.ui.navigation.MongoAuthorizationService
import com.example.ui.notifications.CentralizedNotificationService
import com.example.ui.notifications.NotificationType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationAndMongoArchitectureTest {

    @Test
    fun testRoleBasedDashboardRouting() {
        assertEquals(AppRoutes.PROJECT_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.ADMIN))
        assertEquals(AppRoutes.BA_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.BUSINESS_ANALYST))
        assertEquals(AppRoutes.ARCHITECTURE_WORKSPACE, AppRoutes.getRoleDashboardRoute(ProjectRole.ARCHITECT))
        assertEquals(AppRoutes.TASK_MANAGEMENT, AppRoutes.getRoleDashboardRoute(ProjectRole.DEVELOPER))
        assertEquals(AppRoutes.TESTING_WORKSPACE, AppRoutes.getRoleDashboardRoute(ProjectRole.TESTER))
    }

    @Test
    fun testMongoProjectDocumentCompatibility() {
        val project = Project(
            _id = "proj-mongo-1",
            name = "Enterprise Logistics System",
            description = "AI-optimized automated routing",
            domain = "Supply Chain",
            projectType = "Cloud Native",
            techStack = "Kotlin, Ktor, MongoDB",
            methodology = "Scrum",
            ownerId = "user-123"
        )

        val doc = project.toMongoDocument()
        assertEquals("proj-mongo-1", doc.id)
        assertEquals("Enterprise Logistics System", doc.getString("name"))
        assertEquals("Supply Chain", doc.getString("domain"))

        val restored = Project.fromMongoDocument(doc)
        assertEquals(project._id, restored._id)
        assertEquals(project.name, restored.name)
        assertEquals(project.description, restored.description)
        assertEquals(project.domain, restored.domain)
        assertEquals(project.ownerId, restored.ownerId)
    }

    @Test
    fun testMongoRequirementDocumentCompatibility() {
        val req = Requirement(
            _id = "req-mongo-001",
            projectId = "proj-1",
            code = "REQ-001",
            title = "Real-Time Geo-Tracking",
            description = "Must track carrier coordinates every 5 seconds",
            type = "Functional",
            priority = "Critical",
            status = "Approved",
            authorId = "user-ba",
            authorName = "Alice BA",
            acceptanceCriteria = listOf("GPS jitter < 5m", "Latency < 500ms"),
            aiQualityScore = mapOf("completeness" to 95, "testability" to 90)
        )

        val doc = req.toMongoDocument()
        assertEquals("req-mongo-001", doc.id)
        assertEquals("REQ-001", doc.getString("code"))

        val restored = Requirement.fromMongoDocument(doc)
        assertEquals(req._id, restored._id)
        assertEquals(req.code, restored.code)
        assertEquals(req.title, restored.title)
        assertEquals(req.acceptanceCriteria.size, restored.acceptanceCriteria.size)
        assertEquals(95, restored.aiQualityScore["completeness"])
    }

    @Test
    fun testMongoUseCaseDocumentCompatibility() {
        val uc = UseCase(
            _id = "uc-mongo-001",
            projectId = "proj-1",
            code = "UC-001",
            name = "Dispatch Route Optimization",
            actor = "Fleet Manager",
            goal = "Assign optimal path to nearest courier",
            mainFlow = listOf("Select pending order", "Trigger AI solver", "Review vehicle schedule"),
            alternativeFlows = listOf("Manual override by dispatcher")
        )

        val doc = uc.toMongoDocument()
        assertEquals("uc-mongo-001", doc.id)
        assertEquals("UC-001", doc.getString("code"))

        val restored = UseCase.fromMongoDocument(doc)
        assertEquals(uc._id, restored._id)
        assertEquals(uc.actor, restored.actor)
        assertEquals(3, restored.mainFlow.size)
        assertEquals(1, restored.alternativeFlows.size)
    }

    @Test
    fun testMongoArchitectureDecisionCompatibility() {
        val adr = ArchitectureDecision(
            _id = "adr-mongo-001",
            projectId = "proj-1",
            code = "ADR-001",
            title = "Adopt MongoDB Atlas Document Storage",
            status = "Accepted",
            context = "High schema velocity across software engineering artifacts",
            decision = "Store requirements, system designs, and schemas as BSON documents",
            consequences = "Enables rich schema evolution and real-time change stream sync"
        )

        val doc = adr.toMongoDocument()
        assertEquals("adr-mongo-001", doc.id)
        assertEquals("ADR-001", doc.getString("code"))

        val restored = ArchitectureDecision.fromMongoDocument(doc)
        assertEquals(adr._id, restored._id)
        assertEquals(adr.title, restored.title)
        assertEquals(adr.status, restored.status)
        assertEquals(adr.decision, restored.decision)
    }

    @Test
    fun testMongoApiEndpointCompatibility() {
        val ep = APIEndpoint(
            _id = "api-mongo-001",
            projectId = "proj-1",
            code = "API-001",
            method = "POST",
            path = "/api/v1/shipments/optimize",
            description = "Triggers cloud optimization engine",
            authRequired = true,
            requestSchema = "{\"carrierId\": \"string\"}",
            responseSchema = "{\"routeId\": \"string\", \"cost\": \"number\"}"
        )

        val doc = ep.toMongoDocument()
        assertEquals("api-mongo-001", doc.id)
        assertEquals("POST", doc.getString("method"))
        assertEquals("/api/v1/shipments/optimize", doc.getString("path"))

        val restored = APIEndpoint.fromMongoDocument(doc)
        assertEquals(ep._id, restored._id)
        assertEquals(ep.method, restored.method)
        assertEquals(ep.path, restored.path)
        assertTrue(restored.authRequired)
    }

    @Test
    fun testMongoTaskCompatibility() {
        val task = Task(
            _id = "task-mongo-001",
            projectId = "proj-1",
            code = "DEV-001",
            title = "Implement Ingress Gateway",
            description = "Setup reverse proxy with TLS termination",
            priority = "High",
            status = "In Progress",
            assigneeName = "Bob Engineer",
            estimatedHours = 8
        )

        val doc = task.toMongoDocument()
        assertEquals("task-mongo-001", doc.id)
        assertEquals("DEV-001", doc.getString("code"))

        val restored = Task.fromMongoDocument(doc)
        assertEquals(task._id, restored._id)
        assertEquals(task.title, restored.title)
        assertEquals("Bob Engineer", restored.assigneeName)
        assertEquals(8, restored.estimatedHours)
    }

    @Test
    fun testMongoAuthorizationServiceRoleMapping() {
        assertEquals(ProjectRole.ADMIN, MongoAuthorizationService.mapStringToRole("Administrator"))
        assertEquals(ProjectRole.ADMIN, MongoAuthorizationService.mapStringToRole("Project Owner"))
        assertEquals(ProjectRole.ADMIN, MongoAuthorizationService.mapStringToRole("Product Manager"))
        assertEquals(ProjectRole.BUSINESS_ANALYST, MongoAuthorizationService.mapStringToRole("Lead Business Analyst"))
        assertEquals(ProjectRole.BUSINESS_ANALYST, MongoAuthorizationService.mapStringToRole("BA Lead"))
        assertEquals(ProjectRole.ARCHITECT, MongoAuthorizationService.mapStringToRole("Enterprise Architect"))
        assertEquals(ProjectRole.DEVELOPER, MongoAuthorizationService.mapStringToRole("Software Engineer"))
        assertEquals(ProjectRole.TESTER, MongoAuthorizationService.mapStringToRole("QA Engineer"))
        assertEquals(ProjectRole.TESTER, MongoAuthorizationService.mapStringToRole("Quality Assurance Lead"))
    }

    @Test
    fun testCentralizedNotificationInAppAlerts() {
        CentralizedNotificationService.showInApp(
            title = "Atlas Push Succeeded",
            message = "Requirement REQ-001 synchronized with MongoDB Atlas cluster",
            type = NotificationType.SUCCESS
        )

        val currentNotif = CentralizedNotificationService.inAppNotification.value
        assertNotNull(currentNotif)
        assertEquals("Atlas Push Succeeded", currentNotif?.title)
        assertEquals(NotificationType.SUCCESS, currentNotif?.type)

        CentralizedNotificationService.dismissInApp()
        assertNull(CentralizedNotificationService.inAppNotification.value)
    }
}
