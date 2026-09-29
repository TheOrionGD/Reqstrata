package com.theoriongd.reqstrata.ui.screens.testing
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.local.entity.TestCaseEntity
import com.theoriongd.reqstrata.domain.model.TestExecutionStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.theme.PrimaryPurple
import com.theoriongd.reqstrata.ui.theme.StatusAmber
import com.theoriongd.reqstrata.ui.theme.StatusError
import com.theoriongd.reqstrata.ui.theme.StatusSuccess

import com.theoriongd.reqstrata.ui.motion.TestStatusMotionBadge
import com.theoriongd.reqstrata.ui.motion.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestExecutionWorkspaceScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val pId = project?.id ?: ""
    val testCases by viewModel.testRepo.getTestCases(pId).collectAsState(initial = emptyList())

    var activeTestCaseToExecute by remember { mutableStateOf<TestCaseEntity?>(null) }
    var executionNotes by remember { mutableStateOf("") }
    var defectSummary by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Execution Workspace") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Interactive Test Runner & Defect Recorder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Execute test cases against active release artifacts and record verification status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (testCases.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No test cases available to execute in this project.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(testCases, key = { it.id }) { tc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScale()
                                .clickable { activeTestCaseToExecute = tc },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tc.code}: ${tc.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TestStatusMotionBadge(status = tc.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Expected Result: ${tc.expectedResult}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { activeTestCaseToExecute = tc },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.pressScale()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Run Test")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Test Execution Result Dialog
        activeTestCaseToExecute?.let { tc ->
            AlertDialog(
                onDismissRequest = { activeTestCaseToExecute = null },
                title = { Text("Execute ${tc.code}") },
                text = {
                    Column {
                        Text(tc.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Preconditions: ${tc.preconditions}", style = MaterialTheme.typography.bodySmall)
                        Text("Expected: ${tc.expectedResult}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = executionNotes,
                            onValueChange = { executionNotes = it },
                            label = { Text("Execution Notes / Actual Result") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.testRepo.executeTestCase(tc.id, TestExecutionStatus.PASSED, executionNotes)
                                activeTestCaseToExecute = null
                                executionNotes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                            modifier = Modifier.pressScale()
                        ) {
                            Text("Pass")
                        }
                        Button(
                            onClick = {
                                viewModel.testRepo.executeTestCase(tc.id, TestExecutionStatus.FAILED, executionNotes)
                                activeTestCaseToExecute = null
                                executionNotes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                            modifier = Modifier.pressScale()
                        ) {
                            Text("Fail")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTestCaseToExecute = null }, modifier = Modifier.pressScale()) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
