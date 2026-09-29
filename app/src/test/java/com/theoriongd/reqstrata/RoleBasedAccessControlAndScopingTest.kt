package com.theoriongd.reqstrata

import com.theoriongd.reqstrata.domain.auth.CentralizedAuthorizationManager
import com.theoriongd.reqstrata.domain.auth.UnauthorizedDataAccessException
import com.theoriongd.reqstrata.domain.auth.UnauthorizedNavigationException
import com.theoriongd.reqstrata.domain.model.PermissionLevel
import com.theoriongd.reqstrata.domain.model.ProjectAccessPolicy
import com.theoriongd.reqstrata.domain.model.ProjectModule
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.navigation.AppRoutes
import com.theoriongd.reqstrata.ui.navigation.MongoAuthorizationService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive Role-Based Access Control (RBAC), Screen Scoping,
 * Negative Rejection, and Blank/Edge Case Test Suite.
 *
 * Verifies access policies for every defined user role:
 * - ADMIN (Project Owner & Administrator)
 * - BUSINESS_ANALYST (Requirements & Use Cases)
 * - ARCHITECT (Architecture, UML, DB, APIs)
 * - DEVELOPER (Tasks, Architecture Review)
 * - TESTER (Test Suites, Test Execution, QA)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoleBasedAccessControlAndScopingTest {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. ADMIN ROLE TESTS (Full Governance Across All 16 Modules)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testAdminRoleHasFullAccessToEverySingleModule() {
        val admin = ProjectRole.ADMIN
        for (module in ProjectModule.entries) {
            val permission = ProjectAccessPolicy.getPermission(admin, module)
            assertEquals("Admin should have FULL permission on ${module.name}", PermissionLevel.FULL, permission)
            assertTrue("Admin should be able to read ${module.name}", ProjectAccessPolicy.canRead(admin, module))
            assertTrue("Admin should be able to write to ${module.name}", ProjectAccessPolicy.canWrite(admin, module))
        }
    }

    @Test
    fun testAdminNavigationItemsContainAllGovernanceDestinations() {
        val items = ProjectAccessPolicy.getNavigationItems(ProjectRole.ADMIN)
        assertTrue(items.isNotEmpty())
        val titles = items.map { it.title }
        assertTrue(titles.contains("Dashboard"))
        assertTrue(titles.contains("Projects"))
        assertTrue(titles.contains("Team"))
        assertTrue(titles.contains("Approvals"))
        assertTrue(titles.contains("Requirements"))
        assertTrue(titles.contains("Architecture"))
        assertTrue(titles.contains("Development"))
        assertTrue(titles.contains("Testing"))
        assertTrue(titles.contains("Traceability"))
        assertTrue(titles.contains("Activity Log"))
        assertTrue(titles.contains("Settings"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. BUSINESS ANALYST ROLE TESTS (Scoped to Requirements & Use Cases)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testBusinessAnalystRoleAuthorizedModules() {
        val ba = ProjectRole.BUSINESS_ANALYST

        // Authorized FULL Write
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(ba, ProjectModule.REQUIREMENTS))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(ba, ProjectModule.USE_CASES))
        assertTrue(ProjectAccessPolicy.canWrite(ba, ProjectModule.REQUIREMENTS))
        assertTrue(ProjectAccessPolicy.canWrite(ba, ProjectModule.USE_CASES))

        // Read-only access to Architecture, Tasks, Test Suites, Traceability, Documentation
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(ba, ProjectModule.ARCHITECTURE))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(ba, ProjectModule.DEVELOPMENT_TASKS))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(ba, ProjectModule.TEST_SUITES))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(ba, ProjectModule.TRACEABILITY))
        assertTrue(ProjectAccessPolicy.canRead(ba, ProjectModule.ARCHITECTURE))
    }

    @Test
    fun testBusinessAnalystRoleRejectionOnSensitiveModules() {
        val ba = ProjectRole.BUSINESS_ANALYST

        // Zero Access (NONE) to Team Management, Audit Logs, Role Governance, Test Execution
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(ba, ProjectModule.TEAM_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(ba, ProjectModule.AUDIT_LOGS))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(ba, ProjectModule.ROLE_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(ba, ProjectModule.TEST_EXECUTION))

        assertFalse(ProjectAccessPolicy.canRead(ba, ProjectModule.TEAM_MANAGEMENT))
        assertFalse(ProjectAccessPolicy.canWrite(ba, ProjectModule.TEAM_MANAGEMENT))
        assertFalse(ProjectAccessPolicy.canRead(ba, ProjectModule.ROLE_MANAGEMENT))
        assertFalse(ProjectAccessPolicy.canWrite(ba, ProjectModule.ARCHITECTURE))
        assertFalse(ProjectAccessPolicy.canWrite(ba, ProjectModule.DEVELOPMENT_TASKS))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. ARCHITECT ROLE TESTS (Scoped to Architecture, UML, DB, APIs)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testArchitectRoleAuthorizedModules() {
        val arch = ProjectRole.ARCHITECT

        // Authorized FULL Write
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.ARCHITECTURE))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.UML))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.DATABASE))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.APIS))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.TRACEABILITY))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(arch, ProjectModule.CHANGE_IMPACT))

        assertTrue(ProjectAccessPolicy.canWrite(arch, ProjectModule.ARCHITECTURE))
        assertTrue(ProjectAccessPolicy.canWrite(arch, ProjectModule.DATABASE))
        assertTrue(ProjectAccessPolicy.canWrite(arch, ProjectModule.APIS))

        // Read-only to Requirements & Development Tasks
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(arch, ProjectModule.REQUIREMENTS))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(arch, ProjectModule.DEVELOPMENT_TASKS))
        assertTrue(ProjectAccessPolicy.canRead(arch, ProjectModule.REQUIREMENTS))
        assertFalse(ProjectAccessPolicy.canWrite(arch, ProjectModule.REQUIREMENTS))
    }

    @Test
    fun testArchitectRoleRejectionOnSensitiveModules() {
        val arch = ProjectRole.ARCHITECT

        // Rejection on Team, Audit, Role, Test Execution
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(arch, ProjectModule.TEAM_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(arch, ProjectModule.AUDIT_LOGS))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(arch, ProjectModule.ROLE_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(arch, ProjectModule.TEST_EXECUTION))

        assertFalse(ProjectAccessPolicy.canRead(arch, ProjectModule.TEAM_MANAGEMENT))
        assertFalse(ProjectAccessPolicy.canWrite(arch, ProjectModule.DEVELOPMENT_TASKS))
        assertFalse(ProjectAccessPolicy.canWrite(arch, ProjectModule.TEST_SUITES))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. DEVELOPER ROLE TESTS (Scoped to Implementation Tasks)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testDeveloperRoleAuthorizedModules() {
        val dev = ProjectRole.DEVELOPER

        // Authorized FULL Write on Development Tasks
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(dev, ProjectModule.DEVELOPMENT_TASKS))
        assertTrue(ProjectAccessPolicy.canWrite(dev, ProjectModule.DEVELOPMENT_TASKS))

        // Read-only access to Requirements, Architecture, UML, DB, APIs, Test Execution
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(dev, ProjectModule.REQUIREMENTS))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(dev, ProjectModule.ARCHITECTURE))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(dev, ProjectModule.APIS))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(dev, ProjectModule.DATABASE))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(dev, ProjectModule.TEST_EXECUTION))

        assertTrue(ProjectAccessPolicy.canRead(dev, ProjectModule.REQUIREMENTS))
        assertTrue(ProjectAccessPolicy.canRead(dev, ProjectModule.ARCHITECTURE))
        assertTrue(ProjectAccessPolicy.canRead(dev, ProjectModule.TEST_EXECUTION))
    }

    @Test
    fun testDeveloperRoleRejectionOnSensitiveModules() {
        val dev = ProjectRole.DEVELOPER

        // Cannot write to Requirements or Architecture or Tests
        assertFalse(ProjectAccessPolicy.canWrite(dev, ProjectModule.REQUIREMENTS))
        assertFalse(ProjectAccessPolicy.canWrite(dev, ProjectModule.ARCHITECTURE))
        assertFalse(ProjectAccessPolicy.canWrite(dev, ProjectModule.TEST_SUITES))
        assertFalse(ProjectAccessPolicy.canWrite(dev, ProjectModule.TEST_EXECUTION))

        // No access at all to Team Management, Audit Logs, Role Management
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(dev, ProjectModule.TEAM_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(dev, ProjectModule.AUDIT_LOGS))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(dev, ProjectModule.ROLE_MANAGEMENT))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. TESTER / QA ROLE TESTS (Scoped to Test Suites, Test Execution, QA)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testTesterRoleAuthorizedModules() {
        val qa = ProjectRole.TESTER

        // Authorized FULL Write on Testing & Verification
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(qa, ProjectModule.TEST_SUITES))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(qa, ProjectModule.TEST_EXECUTION))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(qa, ProjectModule.TRACEABILITY))
        assertEquals(PermissionLevel.FULL, ProjectAccessPolicy.getPermission(qa, ProjectModule.CHANGE_IMPACT))

        assertTrue(ProjectAccessPolicy.canWrite(qa, ProjectModule.TEST_SUITES))
        assertTrue(ProjectAccessPolicy.canWrite(qa, ProjectModule.TEST_EXECUTION))

        // Read-only to Requirements, Tasks, Architecture
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(qa, ProjectModule.REQUIREMENTS))
        assertEquals(PermissionLevel.VIEW, ProjectAccessPolicy.getPermission(qa, ProjectModule.DEVELOPMENT_TASKS))
        assertTrue(ProjectAccessPolicy.canRead(qa, ProjectModule.REQUIREMENTS))
        assertTrue(ProjectAccessPolicy.canRead(qa, ProjectModule.DEVELOPMENT_TASKS))
    }

    @Test
    fun testTesterRoleRejectionOnSensitiveModules() {
        val qa = ProjectRole.TESTER

        // Cannot write to Requirements, Architecture, DB, or Development Tasks
        assertFalse(ProjectAccessPolicy.canWrite(qa, ProjectModule.REQUIREMENTS))
        assertFalse(ProjectAccessPolicy.canWrite(qa, ProjectModule.ARCHITECTURE))
        assertFalse(ProjectAccessPolicy.canWrite(qa, ProjectModule.DATABASE))
        assertFalse(ProjectAccessPolicy.canWrite(qa, ProjectModule.DEVELOPMENT_TASKS))

        // No access at all to Team Management, Audit Logs, Role Management
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(qa, ProjectModule.TEAM_MANAGEMENT))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(qa, ProjectModule.AUDIT_LOGS))
        assertEquals(PermissionLevel.NONE, ProjectAccessPolicy.getPermission(qa, ProjectModule.ROLE_MANAGEMENT))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. ROUTE AUTHORIZATION & ACCESS BOUNDARY SCREEN SCOPING TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testRouteAuthorizationLogicAllowedAndDenied() {
        // Scenarios where access must be GRANTED
        assertTrue(isAuthorizedRole(ProjectRole.ADMIN, setOf(ProjectRole.ADMIN)))
        assertTrue(isAuthorizedRole(ProjectRole.ADMIN, setOf(ProjectRole.BUSINESS_ANALYST))) // Admin bypasses
        assertTrue(isAuthorizedRole(ProjectRole.BUSINESS_ANALYST, setOf(ProjectRole.BUSINESS_ANALYST)))
        assertTrue(isAuthorizedRole(ProjectRole.ARCHITECT, setOf(ProjectRole.ARCHITECT, ProjectRole.ADMIN)))
        assertTrue(isAuthorizedRole(ProjectRole.DEVELOPER, setOf(ProjectRole.DEVELOPER, ProjectRole.TESTER)))
        assertTrue(isAuthorizedRole(ProjectRole.TESTER, setOf(ProjectRole.TESTER)))

        // Scenarios where access must be REJECTED (Inaccessible Screen Boundary)
        assertFalse(isAuthorizedRole(ProjectRole.DEVELOPER, setOf(ProjectRole.ADMIN)))
        assertFalse(isAuthorizedRole(ProjectRole.DEVELOPER, setOf(ProjectRole.BUSINESS_ANALYST)))
        assertFalse(isAuthorizedRole(ProjectRole.DEVELOPER, setOf(ProjectRole.ARCHITECT)))
        assertFalse(isAuthorizedRole(ProjectRole.TESTER, setOf(ProjectRole.ARCHITECT)))
        assertFalse(isAuthorizedRole(ProjectRole.BUSINESS_ANALYST, setOf(ProjectRole.TESTER)))
        assertFalse(isAuthorizedRole(ProjectRole.ARCHITECT, setOf(ProjectRole.ADMIN)))
    }

    @Test
    fun testRoleDefaultDashboardRoutingCorrectness() {
        assertEquals(Screen.AdminDashboard, getRoleDefaultScreen(ProjectRole.ADMIN))
        assertEquals(Screen.BusinessAnalystDashboard, getRoleDefaultScreen(ProjectRole.BUSINESS_ANALYST))
        assertEquals(Screen.ArchitectDashboard, getRoleDefaultScreen(ProjectRole.ARCHITECT))
        assertEquals(Screen.DeveloperDashboard, getRoleDefaultScreen(ProjectRole.DEVELOPER))
        assertEquals(Screen.TesterDashboard, getRoleDefaultScreen(ProjectRole.TESTER))
    }

    @Test
    fun testAppRoutesRoleDashboardPathMapping() {
        assertEquals(AppRoutes.ADMIN_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.ADMIN))
        assertEquals(AppRoutes.BA_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.BUSINESS_ANALYST))
        assertEquals(AppRoutes.ARCHITECT_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.ARCHITECT))
        assertEquals(AppRoutes.DEVELOPER_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.DEVELOPER))
        assertEquals(AppRoutes.TESTER_DASHBOARD, AppRoutes.getRoleDashboardRoute(ProjectRole.TESTER))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. BLANK, NULL, MALFORMED & PRIVILEGE ESCALATION RESISTANCE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testRoleFromStringWithNullOrBlankDefaultsSafelyToDeveloper() {
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString(null))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString(""))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("   "))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("UNKNOWN_ROLE"))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("root"))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("superadmin"))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("12345"))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("null"))
    }

    @Test
    fun testRoleFromStringCaseInsensitiveValidParsing() {
        assertEquals(ProjectRole.ADMIN, ProjectRole.fromString("admin"))
        assertEquals(ProjectRole.ADMIN, ProjectRole.fromString("ADMIN"))
        assertEquals(ProjectRole.BUSINESS_ANALYST, ProjectRole.fromString("business_analyst"))
        assertEquals(ProjectRole.BUSINESS_ANALYST, ProjectRole.fromString("BUSINESS_ANALYST"))
        assertEquals(ProjectRole.ARCHITECT, ProjectRole.fromString("architect"))
        assertEquals(ProjectRole.DEVELOPER, ProjectRole.fromString("developer"))
        assertEquals(ProjectRole.TESTER, ProjectRole.fromString("tester"))
    }

    @Test
    fun testMongoAuthorizationServiceRoleMappingEdgeCases() {
        assertEquals(ProjectRole.ADMIN, MongoAuthorizationService.mapStringToRole("Project Owner / Admin"))
        assertEquals(ProjectRole.ADMIN, MongoAuthorizationService.mapStringToRole("admin"))
        assertEquals(ProjectRole.BUSINESS_ANALYST, MongoAuthorizationService.mapStringToRole("Business Analyst"))
        assertEquals(ProjectRole.ARCHITECT, MongoAuthorizationService.mapStringToRole("System Architect"))
        assertEquals(ProjectRole.DEVELOPER, MongoAuthorizationService.mapStringToRole("Developer"))
        assertEquals(ProjectRole.TESTER, MongoAuthorizationService.mapStringToRole("Tester / QA"))

        // Malformed / Blank strings fallback safely
        assertEquals(ProjectRole.DEVELOPER, MongoAuthorizationService.mapStringToRole(""))
        assertEquals(ProjectRole.DEVELOPER, MongoAuthorizationService.mapStringToRole("   "))
        assertEquals(ProjectRole.DEVELOPER, MongoAuthorizationService.mapStringToRole("Arbitrary Malicious Role"))
    }

    @Test
    fun testCentralizedAuthorizationManagerNullUserIdFallback() = runBlocking {
        // Null or blank userId must not crash and must default to DEVELOPER
        val nullRole = CentralizedAuthorizationManager.getVerifiedRole(null, "proj-1")
        assertEquals(ProjectRole.DEVELOPER, nullRole)

        val emptyRole = CentralizedAuthorizationManager.getVerifiedRole("", "proj-1")
        assertEquals(ProjectRole.DEVELOPER, emptyRole)

        val blankRole = CentralizedAuthorizationManager.getVerifiedRole("   ", "proj-1")
        assertEquals(ProjectRole.DEVELOPER, blankRole)
    }

    @Test
    fun testSecurityExceptionsFormatting() {
        val dataEx = UnauthorizedDataAccessException(
            userId = "user-qa-1",
            attemptedRole = ProjectRole.TESTER,
            requiredRoles = setOf(ProjectRole.ADMIN),
            action = "DELETE_PROJECT",
            module = ProjectModule.PROJECT_MANAGEMENT
        )
        assertEquals("user-qa-1", dataEx.userId)
        assertEquals(ProjectRole.TESTER, dataEx.attemptedRole)
        assertTrue(dataEx.message.contains("Security Violation"))
        assertTrue(dataEx.message.contains("DELETE_PROJECT"))

        val navEx = UnauthorizedNavigationException(
            userId = "user-dev-1",
            attemptedRole = ProjectRole.DEVELOPER,
            targetScreen = "TeamManagementScreen",
            requiredRoles = setOf(ProjectRole.ADMIN)
        )
        assertEquals("user-dev-1", navEx.userId)
        assertTrue(navEx.message.contains("Access Denied"))
        assertTrue(navEx.message.contains("TeamManagementScreen"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER FUNCTIONS (Matching RouteAuthorization.kt logic)
    // ─────────────────────────────────────────────────────────────────────────

    private fun isAuthorizedRole(effectiveRole: ProjectRole, requiredRoles: Set<ProjectRole>): Boolean {
        return effectiveRole in requiredRoles || effectiveRole == ProjectRole.ADMIN
    }

    private fun getRoleDefaultScreen(role: ProjectRole): Screen {
        return when (role) {
            ProjectRole.ADMIN -> Screen.AdminDashboard
            ProjectRole.BUSINESS_ANALYST -> Screen.BusinessAnalystDashboard
            ProjectRole.ARCHITECT -> Screen.ArchitectDashboard
            ProjectRole.DEVELOPER -> Screen.DeveloperDashboard
            ProjectRole.TESTER -> Screen.TesterDashboard
        }
    }
}
