package com.theoriongd.reqstrata.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.theoriongd.reqstrata.data.local.entity.*
import com.theoriongd.reqstrata.data.remote.mongo.MongoDbService
import com.theoriongd.reqstrata.ui.theme.PrimaryViolet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportSpecificationDialog(
    project: ProjectEntity?,
    requirements: List<RequirementEntity>,
    components: List<ArchitectureComponentEntity> = emptyList(),
    databaseTables: List<DatabaseEntityRecord> = emptyList(),
    apiEndpoints: List<ApiEndpointEntity> = emptyList(),
    testSuites: List<TestSuiteEntity> = emptyList(),
    testCases: List<TestCaseEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isMarkdownFormat by remember { mutableStateOf(true) }
    var includeRequirements by remember { mutableStateOf(true) }
    var includeArchitecture by remember { mutableStateOf(true) }
    var includeDatabaseSchema by remember { mutableStateOf(true) }
    var includeApiStructure by remember { mutableStateOf(true) }
    var includeTestSuites by remember { mutableStateOf(true) }

    val exportedContent = remember(
        isMarkdownFormat,
        includeRequirements,
        includeArchitecture,
        includeDatabaseSchema,
        includeApiStructure,
        includeTestSuites,
        project,
        requirements,
        components,
        databaseTables,
        apiEndpoints,
        testSuites,
        testCases
    ) {
        buildExportDocument(
            isMarkdown = isMarkdownFormat,
            project = project,
            requirements = if (includeRequirements) requirements else emptyList(),
            components = if (includeArchitecture) components else emptyList(),
            databaseTables = if (includeDatabaseSchema) databaseTables else emptyList(),
            apiEndpoints = if (includeApiStructure) apiEndpoints else emptyList(),
            testSuites = if (includeTestSuites) testSuites else emptyList(),
            testCases = if (includeTestSuites) testCases else emptyList()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export System Specification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = project?.name ?: "Software System",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Toggle & Scope Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isMarkdownFormat,
                        onClick = { isMarkdownFormat = true },
                        label = { Text("Markdown (.md)") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = !isMarkdownFormat,
                        onClick = { isMarkdownFormat = false },
                        label = { Text("Plain Text (.txt)") },
                        leadingIcon = {
                            Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = includeRequirements,
                        onClick = { includeRequirements = !includeRequirements },
                        label = { Text("Reqs (${requirements.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = includeArchitecture,
                        onClick = { includeArchitecture = !includeArchitecture },
                        label = { Text("Arch (${components.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = includeDatabaseSchema,
                        onClick = { includeDatabaseSchema = !includeDatabaseSchema },
                        label = { Text("DB (${databaseTables.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = includeApiStructure,
                        onClick = { includeApiStructure = !includeApiStructure },
                        label = { Text("API (${apiEndpoints.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = includeTestSuites,
                        onClick = { includeTestSuites = !includeTestSuites },
                        label = { Text("Tests (${testCases.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Document Preview Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val previewScrollState = rememberScrollState()
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = exportedContent,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(previewScrollState)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Copy to Clipboard & Share Intent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("System Specification", exportedContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Specification copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("copy_export_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Text")
                    }

                    Button(
                        onClick = {
                            val ext = if (isMarkdownFormat) "md" else "txt"
                            val mime = if (isMarkdownFormat) "text/markdown" else "text/plain"

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = mime
                                putExtra(Intent.EXTRA_SUBJECT, "${project?.name ?: "System"} - Requirements & Architecture Spec")
                                putExtra(Intent.EXTRA_TEXT, exportedContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Specification File"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_export_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share / Save File")
                    }
                }
            }
        }
    }
}

private fun buildExportDocument(
    isMarkdown: Boolean,
    project: ProjectEntity?,
    requirements: List<RequirementEntity>,
    components: List<ArchitectureComponentEntity>,
    databaseTables: List<DatabaseEntityRecord>,
    apiEndpoints: List<ApiEndpointEntity>,
    testSuites: List<TestSuiteEntity>,
    testCases: List<TestCaseEntity>
): String {
    val sb = StringBuilder()
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val projectName = project?.name ?: "Software System"

    if (isMarkdown) {
        sb.appendLine("# System Specification & Architectural Blueprint: $projectName")
        sb.appendLine("*Generated on $dateStr via Reqstrata*")
        sb.appendLine()
        sb.appendLine("## 1. Project Overview")
        sb.appendLine("- **Domain:** ${project?.domain ?: "Enterprise"}")
        sb.appendLine("- **Methodology:** ${project?.methodology ?: "Agile Scrum"}")
        sb.appendLine("- **Description:** ${project?.description ?: "N/A"}")
        sb.appendLine("- **Primary Database Engine:** MongoDB Atlas Cloud (`${MongoDbService.DATABASE_NAME}`)")
        sb.appendLine("- **Cluster URI:** `${MongoDbService.MONGODB_CLUSTER}`")
        sb.appendLine("- **Authenticated User:** `${MongoDbService.MONGODB_USERNAME}`")
        sb.appendLine()

        if (requirements.isNotEmpty()) {
            sb.appendLine("## 2. Requirements Catalog")
            requirements.forEach { r ->
                sb.appendLine("### [${r.code}] ${r.title}")
                sb.appendLine("- **Type:** ${r.type} | **Priority:** ${r.priority} | **Status:** ${r.status}")
                sb.appendLine("- **Author:** ${r.authorName} (v${r.version})")
                sb.appendLine("- **Description:** ${r.description}")
                if (r.acceptanceCriteriaJson.isNotBlank() && r.acceptanceCriteriaJson != "[]") {
                    sb.appendLine("- **Acceptance Criteria:**")
                    sb.appendLine("  ${r.acceptanceCriteriaJson.replace("[", "").replace("]", "").replace("\"", "")}")
                }
                sb.appendLine()
            }
        }

        if (components.isNotEmpty()) {
            sb.appendLine("## 3. High-Level Architecture Components")
            components.forEach { c ->
                sb.appendLine("### ${c.code}: ${c.name}")
                sb.appendLine("- **Layer:** ${c.layerOrModule}")
                sb.appendLine("- **Tech Stack:** ${c.techStack}")
                sb.appendLine("- **Responsibilities:** ${c.responsibilities}")
                if (c.dependenciesJson.isNotBlank() && c.dependenciesJson != "[]") {
                    sb.appendLine("- **Dependencies:** ${c.dependenciesJson}")
                }
                sb.appendLine()
            }
        }

        if (databaseTables.isNotEmpty()) {
            sb.appendLine("## 4. Relational Database Schema & Entities")
            databaseTables.forEach { t ->
                sb.appendLine("### Entity: `${t.name}` (${t.code})")
                sb.appendLine("- **Description:** ${t.description}")
                sb.appendLine("- **Fields Specification:**")
                sb.appendLine("```json")
                sb.appendLine(t.fieldsJson)
                sb.appendLine("```")
                if (t.relationshipsJson.isNotBlank() && t.relationshipsJson != "[]") {
                    sb.appendLine("- **Relationships:** ${t.relationshipsJson}")
                }
                sb.appendLine()
            }
        }

        if (apiEndpoints.isNotEmpty()) {
            sb.appendLine("## 5. RESTful API Structure & Endpoints")
            apiEndpoints.forEach { ep ->
                sb.appendLine("### `${ep.method}` ${ep.path}")
                sb.appendLine("- **Description:** ${ep.description}")
                sb.appendLine("- **Auth Required:** ${ep.authRequired}")
                sb.appendLine("- **Request Schema:** `${ep.requestSchema}`")
                sb.appendLine("- **Response Schema:** `${ep.responseSchema}`")
                sb.appendLine()
            }
        }

        if (testSuites.isNotEmpty() || testCases.isNotEmpty()) {
            sb.appendLine("## 6. Automated Verification Test Suite")
            testSuites.forEach { s ->
                sb.appendLine("### Suite: ${s.name} (${s.code})")
                sb.appendLine("- **Type:** ${s.type}")
                sb.appendLine("- **Description:** ${s.description}")
                sb.appendLine()
            }
            testCases.forEach { tc ->
                sb.appendLine("#### [${tc.code}] ${tc.title}")
                sb.appendLine("- **Priority:** ${tc.priority} | **Severity:** ${tc.severity} | **Status:** ${tc.status}")
                sb.appendLine("- **Expected Result:** ${tc.expectedResult}")
                sb.appendLine()
            }
        }

        sb.appendLine("---")
        sb.appendLine("Document verified and exported from Reqstrata.")
    } else {
        // Plain Text Format
        sb.appendLine("SYSTEM SPECIFICATION & ARCHITECTURAL BLUEPRINT")
        sb.appendLine("Project: $projectName")
        sb.appendLine("Generated: $dateStr")
        sb.appendLine("Database: MongoDB Atlas (${MongoDbService.DATABASE_NAME}) on ${MongoDbService.MONGODB_CLUSTER}")
        sb.appendLine("================================================================================")
        sb.appendLine()

        if (requirements.isNotEmpty()) {
            sb.appendLine("1. REQUIREMENTS CATALOG:")
            requirements.forEach { r ->
                sb.appendLine("[${r.code}] ${r.title}")
                sb.appendLine("  Type: ${r.type} | Priority: ${r.priority} | Status: ${r.status}")
                sb.appendLine("  Description: ${r.description}")
                sb.appendLine()
            }
            sb.appendLine("--------------------------------------------------------------------------------")
        }

        if (components.isNotEmpty()) {
            sb.appendLine("2. ARCHITECTURE COMPONENTS:")
            components.forEach { c ->
                sb.appendLine("${c.code}: ${c.name} [Layer: ${c.layerOrModule}]")
                sb.appendLine("  Tech Stack: ${c.techStack}")
                sb.appendLine("  Responsibilities: ${c.responsibilities}")
                sb.appendLine()
            }
            sb.appendLine("--------------------------------------------------------------------------------")
        }

        if (databaseTables.isNotEmpty()) {
            sb.appendLine("3. DATABASE SCHEMA:")
            databaseTables.forEach { t ->
                sb.appendLine("Table: ${t.name} (${t.code})")
                sb.appendLine("  Description: ${t.description}")
                sb.appendLine("  Fields: ${t.fieldsJson}")
                sb.appendLine()
            }
            sb.appendLine("--------------------------------------------------------------------------------")
        }

        if (apiEndpoints.isNotEmpty()) {
            sb.appendLine("4. REST API ENDPOINTS:")
            apiEndpoints.forEach { ep ->
                sb.appendLine("${ep.method} ${ep.path} (Auth: ${ep.authRequired})")
                sb.appendLine("  Description: ${ep.description}")
                sb.appendLine()
            }
            sb.appendLine("--------------------------------------------------------------------------------")
        }

        if (testCases.isNotEmpty()) {
            sb.appendLine("5. TEST CASES:")
            testCases.forEach { tc ->
                sb.appendLine("[${tc.code}] ${tc.title}")
                sb.appendLine("  Expected: ${tc.expectedResult}")
                sb.appendLine()
            }
        }
    }

    return sb.toString()
}
