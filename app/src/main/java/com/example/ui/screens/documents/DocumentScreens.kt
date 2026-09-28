package com.example.ui.screens.documents

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Converters
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class DocType(val code: String, val title: String) {
    SRS("SRS", "Software Requirements Specification"),
    SDD("SDD", "Software Design Document"),
    API_DOCS("API_DOCS", "REST API Specification"),
    TEST_PLAN("TEST_PLAN", "Master QA Test Plan"),
    TEST_REPORT("TEST_REPORT", "Verification & Test Report"),
    RTM("RTM", "Traceability Matrix Report"),
    EXECUTIVE_SUMMARY("PROJECT_SUMMARY", "Executive Project Summary")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())
    val executions by viewModel.testRepo.getExecutions(project?.id ?: "").collectAsState(initial = emptyList())

    var selectedDocType by remember { mutableStateOf(DocType.SRS) }
    var generatedMarkdown by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    fun buildDocumentContent(type: DocType): String {
        val p = project ?: return "No active project."
        val date = dateFormat.format(Date())
        return when (type) {
            DocType.SRS -> """
                # SOFTWARE REQUIREMENTS SPECIFICATION (SRS)
                **Project:** ${p.name}
                **Domain:** ${p.domain}
                **Generated At:** $date
                **Status:** Approved Release Candidate
                
                ---
                ## 1. Introduction & Scope
                ${p.description}
                
                **Technology Preferences:** ${p.techStack}
                **Development Methodology:** ${p.methodology}
                
                ## 2. Verified Functional & Quality Requirements
                ${reqs.mapIndexed { idx, r ->
                    """
                    ### 2.${idx + 1} ${r.code}: ${r.title}
                    - **Classification:** ${r.type}
                    - **Priority:** ${r.priority} | **Status:** ${r.status} | **Version:** v${r.version}
                    - **Description:** ${r.description}
                    - **Acceptance Criteria:**
                    ${Converters().toStringList(r.acceptanceCriteriaJson).joinToString("\n") { "  * $it" }}
                    """.trimIndent()
                }.joinToString("\n\n")}
            """.trimIndent()

            DocType.SDD -> """
                # SOFTWARE DESIGN DOCUMENT (SDD)
                **Project:** ${p.name}
                **Architecture Pattern:** Clean Architecture with Domain-Driven Design
                **Generated At:** $date
                
                ---
                ## 1. Architectural Layers & Components
                ${archs.mapIndexed { idx, c ->
                    """
                    ### 1.${idx + 1} ${c.code}: ${c.name}
                    - **Layer:** ${c.layerOrModule}
                    - **Tech Stack:** ${c.techStack}
                    - **Responsibilities:** ${c.responsibilities}
                    """.trimIndent()
                }.joinToString("\n\n")}
            """.trimIndent()

            DocType.API_DOCS -> """
                # RESTful API CONTRACT SPECIFICATION
                **Base URL:** https://api.${p.name.lowercase().replace(" ", "")}.com/v1
                **Generated At:** $date
                
                ---
                ${apis.mapIndexed { idx, api ->
                    """
                    ### ${idx + 1}. [${api.method}] ${api.path}
                    - **Description:** ${api.description}
                    - **Authentication:** ${if (api.authRequired) "Bearer JWT Required" else "Public"}
                    - **Request Schema:** `${api.requestSchema}`
                    - **Response Schema:** `${api.responseSchema}`
                    """.trimIndent()
                }.joinToString("\n\n")}
            """.trimIndent()

            DocType.TEST_PLAN -> """
                # MASTER QUALITY ASSURANCE TEST PLAN
                **Project:** ${p.name}
                **Generated At:** $date
                
                ---
                ## Test Inventory
                - Total Registered Test Cases: ${tests.size}
                - Execution Records Logged: ${executions.size}
                
                ${tests.mapIndexed { idx, tc ->
                    """
                    ### Test ${idx + 1}: ${tc.code} - ${tc.title}
                    - **Severity:** ${tc.severity} | **Priority:** ${tc.priority}
                    - **Preconditions:** ${tc.preconditions}
                    - **Expected Result:** ${tc.expectedResult}
                    """.trimIndent()
                }.joinToString("\n\n")}
            """.trimIndent()

            DocType.TEST_REPORT -> """
                # VERIFICATION & TEST EXECUTION AUDIT REPORT
                **Project:** ${p.name}
                **Generated At:** $date
                
                ---
                ## Summary Statistics
                - Passed: ${executions.count { it.status == "Passed" }}
                - Failed: ${executions.count { it.status == "Failed" }}
                - Blocked / Skipped: ${executions.count { it.status in listOf("Blocked", "Skipped") }}
                
                ## Execution Trail
                ${executions.mapIndexed { idx, exec ->
                    """
                    - **Execution #${idx + 1}:** Status: **${exec.status}** | Tester: ${exec.executedBy}
                      Actual Result: ${exec.actualResult}
                      ${if (exec.failureNotes.isNotBlank()) "Notes: ${exec.failureNotes}" else ""}
                    """.trimIndent()
                }.joinToString("\n")}
            """.trimIndent()

            DocType.RTM -> """
                # REQUIREMENTS TRACEABILITY MATRIX (RTM)
                **Project:** ${p.name}
                **Generated At:** $date
                
                ---
                ${reqs.map { r ->
                    val linkedTest = tests.firstOrNull { it.linkedRequirementId == r.id }
                    "- **${r.code}** (${r.title}) → Test: ${linkedTest?.code ?: "⚠️ NO LINKED TEST"}"
                }.joinToString("\n")}
            """.trimIndent()

            DocType.EXECUTIVE_SUMMARY -> """
                # EXECUTIVE PROJECT SUMMARY
                **Project:** ${p.name} (${p.domain})
                **Owner:** Lead Architect
                **Generated At:** $date
                
                ---
                - **Scope:** ${reqs.size} Requirements (${reqs.count { it.status == "Approved" }} Approved)
                - **System Modules:** ${archs.size} Architectural Components
                - **Integrations:** ${apis.size} Verified REST Endpoints
                - **Quality:** ${tests.size} Test Cases
                
                *Generated automatically by Requirement2System AI.*
            """.trimIndent()
        }
    }

    LaunchedEffect(selectedDocType, reqs, archs, apis, tests, executions) {
        generatedMarkdown = buildDocumentContent(selectedDocType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Engineering Documentation", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val p = project ?: return@IconButton
                        viewModel.viewModelScope.launch {
                            viewModel.docRepo.saveDocument(
                                projectId = p.id,
                                docType = selectedDocType.code,
                                title = selectedDocType.title,
                                markdown = generatedMarkdown,
                                author = viewModel.currentUser.value?.fullName ?: "Architect"
                            )
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Document", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Doc Type Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DocType.entries) { type ->
                    FilterChip(
                        selected = selectedDocType == type,
                        onClick = { selectedDocType = type },
                        label = { Text(type.title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = generatedMarkdown,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
