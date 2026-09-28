package com.theoriongd.reqstrata.domain.model

enum class ProjectRole(val title: String, val description: String) {
    ADMIN("Project Owner / Admin", "Full project governance, settings, user management, and approvals"),
    BUSINESS_ANALYST("Business Analyst", "Requirement authoring, ambiguity analysis, acceptance criteria, use cases"),
    ARCHITECT("System Architect", "System architecture, component design, UML studio, ERD, API specs"),
    DEVELOPER("Developer", "Implementation tasks, architecture & API review, AI coding guidance"),
    TESTER("Tester / QA", "Test suite & case design, test execution, bug reporting, requirement coverage");

    companion object {
        fun fromString(role: String?): ProjectRole {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: DEVELOPER
        }
    }
}

enum class RequirementType(val displayName: String) {
    FUNCTIONAL("Functional"),
    NON_FUNCTIONAL("Non-Functional"),
    SECURITY("Security"),
    PERFORMANCE("Performance"),
    USABILITY("Usability"),
    RELIABILITY("Reliability"),
    COMPLIANCE("Compliance"),
    BUSINESS_RULE("Business Rule")
}

enum class RequirementPriority(val displayName: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class RequirementStatus(val displayName: String) {
    DRAFT("Draft"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DEPRECATED("Deprecated")
}

enum class TaskStatus(val displayName: String) {
    BACKLOG("Backlog"),
    READY("Ready"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    IN_REVIEW("In Review"),
    COMPLETED("Completed")
}

enum class TestExecutionStatus(val displayName: String) {
    NOT_EXECUTED("Not Executed"),
    PASSED("Passed"),
    FAILED("Failed"),
    BLOCKED("Blocked"),
    SKIPPED("Skipped")
}

enum class UmlDiagramType(val displayName: String, val description: String) {
    USE_CASE("Use Case Diagram", "Actors, use cases, and system boundary"),
    CLASS_DIAGRAM("Class Diagram", "Entities, attributes, methods, and relationships"),
    SEQUENCE("Sequence Diagram", "Message flow between actors and system services"),
    ACTIVITY("Activity Diagram", "Process control flow and decision logic"),
    COMPONENT("Component Diagram", "High-level software modules and interfaces"),
    DEPLOYMENT("Deployment Diagram", "Hardware nodes, servers, networks, and containers")
}

data class RequirementQuality(
    val completeness: Int = 85,
    val ambiguity: Int = 15,
    val consistency: Int = 90,
    val testability: Int = 80,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val missingInformation: List<String> = emptyList()
)

data class ChangeImpactItem(
    val artifactType: String,
    val artifactId: String,
    val artifactTitle: String,
    val reason: String,
    val confidence: String, // "High", "Medium", "Low"
    val recommendedAction: String
)

data class GeneratedRequirementItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val code: String,
    val title: String,
    val type: String, // "Functional" or "Non-Functional"
    val category: String, // e.g. "Core Business", "Security", "Performance", "Usability", "Reliability"
    val description: String,
    val priority: String, // "Critical", "High", "Medium", "Low"
    val userStory: String = "",
    val acceptanceCriteria: List<String> = emptyList(),
    val verificationMetric: String = "",
    val isSelected: Boolean = true
)

data class RequirementsPromptTemplate(
    val id: String,
    val name: String,
    val description: String,
    val template: String
)

data class GeneratedArchitectureSuggestion(
    val architectureStyle: String = "Clean Architecture with Event-Driven Ingress",
    val systemOverview: String = "",
    val components: List<GeneratedArchitectureComponent> = emptyList(),
    val databaseTables: List<GeneratedDatabaseTable> = emptyList(),
    val apiEndpoints: List<GeneratedApiEndpoint> = emptyList(),
    val tradeoffs: List<GeneratedTradeoff> = emptyList(),
    val architecturalDecisions: List<GeneratedAdr> = emptyList()
)

data class GeneratedArchitectureComponent(
    val code: String,
    val name: String,
    val layer: String,
    val responsibilities: String,
    val techStack: String,
    val dependencies: List<String> = emptyList(),
    val linkedRequirements: List<String> = emptyList(),
    val isSelected: Boolean = true
)

data class GeneratedDbColumn(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = false,
    val isUnique: Boolean = false,
    val foreignKeyTarget: String? = null
)

data class GeneratedDatabaseTable(
    val tableName: String,
    val description: String,
    val columns: List<GeneratedDbColumn> = emptyList(),
    val primaryKey: String = "id",
    val relationshipsSummary: String = "",
    val indexes: List<String> = emptyList(),
    val linkedRequirements: List<String> = emptyList(),
    val isSelected: Boolean = true
)

data class GeneratedApiEndpoint(
    val code: String,
    val method: String,
    val path: String,
    val summary: String,
    val description: String,
    val authRequired: Boolean = true,
    val requestSchema: String = "{}",
    val responseSchema: String = "{}",
    val statusCodes: List<String> = listOf("200 OK", "400 Bad Request", "401 Unauthorized"),
    val linkedRequirements: List<String> = emptyList(),
    val isSelected: Boolean = true
)

data class GeneratedTradeoff(
    val decision: String,
    val pros: String,
    val cons: String,
    val recommendation: String
)

data class GeneratedAdr(
    val code: String,
    val title: String,
    val context: String,
    val decision: String,
    val consequences: String
)

data class GeneratedTestSuiteResult(
    val suiteName: String = "Automated Verification Suite",
    val summary: String = "",
    val unitTests: List<GeneratedTestCaseItem> = emptyList(),
    val integrationTests: List<GeneratedTestCaseItem> = emptyList()
)

data class GeneratedTestCaseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val code: String,
    val title: String,
    val testType: String, // "Unit" or "Integration"
    val linkedRequirementCode: String,
    val preconditions: String,
    val testData: String,
    val steps: List<String>,
    val expectedResult: String,
    val priority: String = "High",
    val severity: String = "Major",
    val isSelected: Boolean = true
)


