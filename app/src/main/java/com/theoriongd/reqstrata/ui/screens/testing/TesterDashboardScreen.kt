package com.theoriongd.reqstrata.ui.screens.testing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.domain.model.TestExecutionStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.MotionSpec
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.screens.project.ActionChipButton
import com.theoriongd.reqstrata.ui.screens.project.AdminStatCard
import com.theoriongd.reqstrata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesterDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val pId = project?.id ?: ""
    var bannerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bannerVisible = true }

    // Real Data Flows from Repositories
    val suites by viewModel.testRepo.getTestSuites(pId).collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases(pId).collectAsState(initial = emptyList())
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())

    val totalCases = testCases.size
    val passedCases = testCases.count { it.status.equals("Passed", ignoreCase = true) }
    val failedCases = testCases.count { it.status.equals("Failed", ignoreCase = true) }
    val blockedCases = testCases.count { it.status.equals("Blocked", ignoreCase = true) }
    val pendingCases = testCases.count {
        it.status.equals("Active", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true) || it.status.equals("Not Executed", ignoreCase = true)
    }

    val testCoverage = if (totalCases > 0) (passedCases * 100 / totalCases) else 0
    val coveredReqCount = requirements.count { req ->
        testCases.any { it.linkedRequirementId == req.id || it.linkedRequirementId == req.code }
    }
    val reqCoverage = if (requirements.isNotEmpty()) (coveredReqCount * 100 / requirements.size) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Tester Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quality Assurance & Verification Workspace",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ProjectSelection) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Project")
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }) {
                        Icon(Icons.Default.FactCheck, contentDescription = "Testing Workspace")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Welcome & QA Role Banner
            item {
                AnimatedVisibility(
                    visible = bannerVisible,
                    enter = fadeIn(MotionSpec.emphasisTween()) + slideInVertically(
                        animationSpec = MotionSpec.emphasisTween(),
                        initialOffsetY = { -it / 3 }
                    )
                ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Tester"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "QA Coverage: $testCoverage% • $passedCases Passed • $failedCases Failed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                } // AnimatedVisibility
            }

            // 2. Test Execution & Coverage Metrics
            item {
                Text(
                    text = "Quality Verification & Coverage",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Test Cases",
                        value = "$totalCases",
                        subtitle = "${suites.size} Test Suite(s)",
                        icon = Icons.Default.PlaylistAddCheck,
                        color = PrimaryPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                    )
                    AdminStatCard(
                        title = "Passed",
                        value = "$passedCases",
                        subtitle = "$testCoverage% Success rate",
                        icon = Icons.Default.CheckCircle,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Failed Tests",
                        value = "$failedCases",
                        subtitle = if (failedCases > 0) "Critical defect" else "Zero defects",
                        icon = Icons.Default.Cancel,
                        color = if (failedCases > 0) StatusError else StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                    )
                    AdminStatCard(
                        title = "Req Coverage",
                        value = "$reqCoverage%",
                        subtitle = "$coveredReqCount of ${requirements.size} covered",
                        icon = Icons.Default.PieChart,
                        color = if (reqCoverage >= 80) StatusSuccess else StatusAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.CoverageDashboard) }
                    )
                }
            }

            // 3. Quick Actions for QA / Tester
            item {
                Text(
                    text = "QA Workspace Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        ActionChipButton(
                            title = "Generate Tests (AI)",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { viewModel.navigateTo(Screen.GenerateTestSuite(pId)) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Execute Test Run",
                            icon = Icons.Default.PlayCircle,
                            onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Coverage Breakdown",
                            icon = Icons.Default.PieChart,
                            onClick = { viewModel.navigateTo(Screen.CoverageDashboard) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Traceability RTM",
                            icon = Icons.Default.Hub,
                            onClick = { viewModel.navigateTo(Screen.TraceabilityMatrix) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "QA Test Report",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = { viewModel.navigateTo(Screen.DocumentViewer) }
                        )
                    }
                }
            }

            // 4. Test Cases & Failed Issues
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Test Cases & Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }) {
                        Text("View All")
                    }
                }

                if (testCases.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No test cases designed yet. Use AI generation or add manually in the Testing Workspace.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        testCases.take(4).forEach { tc ->
                            val statusColor = when (tc.status.lowercase()) {
                                "passed" -> StatusSuccess
                                "failed" -> StatusError
                                "blocked" -> StatusAmber
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale()
                                    .clickable { viewModel.navigateTo(Screen.TestingWorkspace) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tc.code}: ${tc.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Priority: ${tc.priority} • Expected: ${tc.expectedResult}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(tc.status) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(statusColor)
                                            )
                                        },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
