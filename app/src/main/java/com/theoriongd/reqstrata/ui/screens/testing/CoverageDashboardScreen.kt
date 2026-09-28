package com.theoriongd.reqstrata.ui.screens.testing

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverageDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqList by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())

    val approvedReqs = remember(reqList) {
        reqList.filter { it.status.equals("Approved", ignoreCase = true) || it.status.equals("Active", ignoreCase = true) }
    }

    // Set of requirement IDs covered by test cases
    val coveredReqIds = remember(testCases) {
        testCases.map { it.linkedRequirementId }.filter { it.isNotBlank() }.toSet()
    }

    val coveredReqs = remember(approvedReqs, coveredReqIds) {
        approvedReqs.filter { coveredReqIds.contains(it.id) || coveredReqIds.contains(it.code) }
    }

    val uncoveredReqs = remember(approvedReqs, coveredReqIds) {
        approvedReqs.filter { !coveredReqIds.contains(it.id) && !coveredReqIds.contains(it.code) }
    }

    val coveragePercentage = remember(approvedReqs, coveredReqs) {
        if (approvedReqs.isEmpty()) 0 else (coveredReqs.size * 100) / approvedReqs.size
    }

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, COVERED, UNCOVERED

    val activeUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = coveragePercentage / 100f,
        animationSpec = MotionSpec.standardTween(),
        label = "coverage_meter_anim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Requirement Test Coverage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(project?.name ?: "Current Project", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .testTag("coverage_back_btn")
                            .pressScale()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.GenerateTestSuite(project?.id)) },
                        modifier = Modifier.pressScale()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Synthesize QA Tests", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // QA Auditor Persona & Tenant Separation Card
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(pressedScale = 0.99f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF059669), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (activeUser?.fullName ?: "Q").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeUser?.fullName ?: "QA Engineer",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF059669).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = currentRole.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Verification Scope: ${project?.name ?: "Default"} (Tenant Isolated)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                        Surface(
                            color = Color(0xFF27272A),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "QA Gated",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA78BFA),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Metric Card
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(pressedScale = 0.99f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Verification Coverage",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${coveredReqs.size} of ${approvedReqs.size} Approved Specs Covered",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                            Text(
                                text = "$coveragePercentage%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (coveragePercentage >= 80) Color(0xFF10B981) else if (coveragePercentage >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = if (coveragePercentage >= 80) Color(0xFF10B981) else if (coveragePercentage >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444),
                            trackColor = Color(0xFF27272A)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .pressScale(pressedScale = 0.97f),
                                color = Color(0xFF27272A),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${coveredReqs.size}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), style = MaterialTheme.typography.titleMedium)
                                    Text("Covered", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .pressScale(pressedScale = 0.97f),
                                color = Color(0xFF27272A),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uncoveredReqs.size}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), style = MaterialTheme.typography.titleMedium)
                                    Text("Uncovered", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .pressScale(pressedScale = 0.97f),
                                color = Color(0xFF27272A),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${testCases.size}", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.titleMedium)
                                    Text("Test Cases", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                                }
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${approvedReqs.size})") },
                        modifier = Modifier.pressScale(pressedScale = 0.96f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6D28D9),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF27272A),
                            labelColor = Color(0xFFA1A1AA)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "COVERED",
                        onClick = { selectedFilter = "COVERED" },
                        label = { Text("Covered (${coveredReqs.size})") },
                        modifier = Modifier.pressScale(pressedScale = 0.96f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF065F46),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF27272A),
                            labelColor = Color(0xFFA1A1AA)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "UNCOVERED",
                        onClick = { selectedFilter = "UNCOVERED" },
                        label = { Text("Uncovered Gap (${uncoveredReqs.size})") },
                        modifier = Modifier.pressScale(pressedScale = 0.96f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7F1D1D),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF27272A),
                            labelColor = Color(0xFFA1A1AA)
                        )
                    )
                }
            }

            // List of requirements with coverage badge
            val displayList = when (selectedFilter) {
                "COVERED" -> coveredReqs
                "UNCOVERED" -> uncoveredReqs
                else -> approvedReqs
            }

            items(displayList, key = { it.id }) { req ->
                val isCovered = coveredReqIds.contains(req.id) || coveredReqIds.contains(req.code)
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isCovered) Color(0xFF27272A) else Color(0xFF7F1D1D).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(pressedScale = 0.98f) {
                            viewModel.navigateTo(Screen.RequirementDetail(req.id))
                        }
                        .testTag("coverage_item_${req.code}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (isCovered) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(req.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(req.priority, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(req.title, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isCovered) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isCovered) "Verified" else "Gap / No Tests",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCovered) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
