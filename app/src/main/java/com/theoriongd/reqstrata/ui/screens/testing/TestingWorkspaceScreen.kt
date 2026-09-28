package com.theoriongd.reqstrata.ui.screens.testing

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.theoriongd.reqstrata.data.local.entity.Converters
import com.theoriongd.reqstrata.data.local.entity.TestCaseEntity
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.TestExecutionStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.AiActionCard
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.components.PriorityBadge
import com.theoriongd.reqstrata.ui.components.StatusBadge
import com.theoriongd.reqstrata.ui.motion.MotionSpec
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestingWorkspaceScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val suites by viewModel.testRepo.getSuites(project?.id ?: "").collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())
    val executions by viewModel.testRepo.getExecutions(project?.id ?: "").collectAsState(initial = emptyList())
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    val approvedReqs = reqs.filter { it.status.equals("Approved", ignoreCase = true) }
    val linkedReqIds = testCases.map { it.linkedRequirementId }.toSet()
    val coveredApproved = approvedReqs.count { linkedReqIds.contains(it.id) }
    val coveragePercent = if (approvedReqs.isNotEmpty()) ((coveredApproved.toFloat() / approvedReqs.size) * 100).toInt() else 0

    var isGeneratingTests by remember { mutableStateOf(false) }
    var showExecuteDialog by remember { mutableStateOf<TestCaseEntity?>(null) }
    var showCreateSuiteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QA & Testing Workspace", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.CoverageDashboard) }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Coverage Dashboard", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.GenerateTestSuite(project?.id)) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Test Generator", tint = Color(0xFF10B981))
                    }
                    IconButton(onClick = { showCreateSuiteDialog = true }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "New Suite", tint = Color(0xFF8B5CF6))
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Real Coverage Header Card
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale()
                    .clickable { viewModel.navigateTo(Screen.CoverageDashboard) }
                    .testTag("coverage_summary_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Requirement Verification Coverage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("$coveredApproved of ${approvedReqs.size} approved requirements covered by test suites", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                        }
                        Text(
                            "$coveragePercent%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (coveragePercent > 70) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { coveragePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (coveragePercent > 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                        trackColor = Color(0xFF18181B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Test Case Generator
            AiActionCard(
                title = "Synthesize Test Cases with Gemini",
                description = "Derives positive, negative, boundary, and security test cases directly from approved requirements.",
                buttonText = "Generate QA Tests",
                isLoading = false,
                onClick = {
                    viewModel.navigateTo(Screen.GenerateTestSuite(project?.id))
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Test Cases (${testCases.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            if (testCases.isEmpty()) {
                EmptyStateView(
                    title = "No Test Cases",
                    message = "Synthesize test cases using Gemini or create custom functional and security test suites.",
                    icon = Icons.Default.FactCheck
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(testCases, key = { it.id }) { tc ->
                        TestCaseCard(
                            testCase = tc,
                            onExecute = { showExecuteDialog = tc }
                        )
                    }
                }
            }
        }
    }

    // Execute Test Dialog
    showExecuteDialog?.let { tc ->
        var execStatus by remember { mutableStateOf(TestExecutionStatus.PASSED) }
        var actualResult by remember { mutableStateOf("Verified operational behavior meets all acceptance criteria.") }
        var failureNotes by remember { mutableStateOf("") }
        var evidence by remember { mutableStateOf("HTTP 200 OK confirmed in local test run") }

        AlertDialog(
            onDismissRequest = { showExecuteDialog = null },
            title = { Text("Execute Test: ${tc.code}") },
            text = {
                Column {
                    Text(tc.title, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Execution Result:", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(TestExecutionStatus.PASSED, TestExecutionStatus.FAILED, TestExecutionStatus.BLOCKED).forEach { st ->
                            FilterChip(
                                selected = execStatus == st,
                                onClick = { execStatus = st },
                                label = { Text(st.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = actualResult,
                        onValueChange = { actualResult = it },
                        label = { Text("Actual Result Observed") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (execStatus == TestExecutionStatus.FAILED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = failureNotes,
                            onValueChange = { failureNotes = it },
                            label = { Text("Defect Description / Cause") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        viewModel.viewModelScope.launch {
                            viewModel.testRepo.recordExecution(
                                projectId = p.id,
                                testCaseId = tc.id,
                                status = execStatus,
                                actualResult = actualResult,
                                notes = failureNotes,
                                evidence = evidence,
                                tester = viewModel.currentUser.value?.fullName ?: "QA Tester"
                            )
                            showExecuteDialog = null
                        }
                    }
                ) { Text("Record Result") }
            },
            dismissButton = {
                TextButton(onClick = { showExecuteDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateSuiteDialog) {
        var suiteName by remember { mutableStateOf("") }
        var suiteType by remember { mutableStateOf("Functional") }
        var suiteDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateSuiteDialog = false },
            title = { Text("New Test Suite") },
            text = {
                Column {
                    OutlinedTextField(value = suiteName, onValueChange = { suiteName = it }, label = { Text("Suite Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = suiteType, onValueChange = { suiteType = it }, label = { Text("Suite Type (e.g. Security)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = suiteDesc, onValueChange = { suiteDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (suiteName.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.testRepo.createSuite(p.id, suiteName, suiteType, suiteDesc)
                                showCreateSuiteDialog = false
                            }
                        }
                    }
                ) { Text("Save Suite") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSuiteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TestCaseCard(
    testCase: TestCaseEntity,
    onExecute: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { expanded = !expanded }
            .animateContentSize(MotionSpec.contentSizeSpec())
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(testCase.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = testCase.priority)
                }
                Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                    Text("Severity: ${testCase.severity}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFAFAFA), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(testCase.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Expected: ${testCase.expectedResult}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))

            val steps = Converters().toStringList(testCase.stepsJson)
            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Steps: ${steps.joinToString(" → ")}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .pressScale()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Execute Test", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
