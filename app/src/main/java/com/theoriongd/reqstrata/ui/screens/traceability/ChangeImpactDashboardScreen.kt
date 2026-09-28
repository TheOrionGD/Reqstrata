package com.theoriongd.reqstrata.ui.screens.traceability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.theoriongd.reqstrata.domain.model.ChangeImpactItem
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.theme.*

import androidx.compose.animation.AnimatedVisibility
import com.theoriongd.reqstrata.ui.motion.AiGenerationMotionContainer
import com.theoriongd.reqstrata.ui.motion.MotionTransition
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeImpactDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val pId = project?.id ?: ""

    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())
    val components by viewModel.archRepo.getComponents(pId).collectAsState(initial = emptyList())
    val endpoints by viewModel.apiRepo.getEndpoints(pId).collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(pId).collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases(pId).collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Derive real impact items across architecture, database, APIs, tasks, and test cases
    val impactItems = remember(requirements, components, endpoints, tasks, testCases) {
        val list = mutableListOf<ChangeImpactItem>()

        requirements.forEach { req ->
            // Check linked components
            components.filter { it.linkedRequirementIdsJson.contains(req.id) || it.linkedRequirementIdsJson.contains(req.code) }.forEach { c ->
                list.add(
                    ChangeImpactItem(
                        artifactType = "Architecture Component",
                        artifactId = c.id,
                        artifactTitle = "${c.code}: ${c.name}",
                        reason = "Direct dependency on ${req.code} (${req.title})",
                        confidence = "High",
                        recommendedAction = "Verify component layer '${c.layerOrModule}' and update interfaces"
                    )
                )
            }

            // Check linked endpoints
            endpoints.filter { it.linkedRequirementId == req.id || it.linkedRequirementId == req.code }.forEach { ep ->
                list.add(
                    ChangeImpactItem(
                        artifactType = "API Endpoint",
                        artifactId = ep.id,
                        artifactTitle = "${ep.method} ${ep.path}",
                        reason = "API contract linked to ${req.code}",
                        confidence = "High",
                        recommendedAction = "Review payload schemas and breaking change impact"
                    )
                )
            }

            // Check linked tasks
            tasks.filter { it.linkedRequirementId == req.id || it.linkedRequirementId == req.code }.forEach { t ->
                list.add(
                    ChangeImpactItem(
                        artifactType = "Development Task",
                        artifactId = t.id,
                        artifactTitle = "${t.code}: ${t.title}",
                        reason = "Sprint task implementing ${req.code}",
                        confidence = "Medium",
                        recommendedAction = "Re-estimate effort and verify implementation status (${t.status})"
                    )
                )
            }

            // Check linked test cases
            testCases.filter { it.linkedRequirementId == req.id || it.linkedRequirementId == req.code }.forEach { tc ->
                list.add(
                    ChangeImpactItem(
                        artifactType = "Test Case",
                        artifactId = tc.id,
                        artifactTitle = "${tc.code}: ${tc.title}",
                        reason = "Verification suite test covering ${req.code}",
                        confidence = "High",
                        recommendedAction = "Update test assertions, test data, and re-execute in QA"
                    )
                )
            }
        }
        list
    }

    val filteredItems = when (selectedFilter) {
        "ARCHITECTURE" -> impactItems.filter { it.artifactType.contains("Architecture") }
        "API" -> impactItems.filter { it.artifactType.contains("API") }
        "TASKS" -> impactItems.filter { it.artifactType.contains("Task") }
        "TESTS" -> impactItems.filter { it.artifactType.contains("Test") }
        else -> impactItems
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Impact Analysis") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isAnalyzing = true
                                delay(300)
                                viewModel.postNotification("Impact Analysis Run", "Calculated blast radius across ${impactItems.size} project artifacts.")
                                isAnalyzing = false
                            }
                        },
                        modifier = Modifier.pressScale()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recalculate Blast Radius")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Overview Banner & Analysis Trigger
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cross-Artifact Blast Radius",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${impactItems.size} linked artifacts tracked across requirements, design, and code.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // AI Analyzing Container
            item {
                AiGenerationMotionContainer(
                    isGenerating = isAnalyzing,
                    hasResult = !isAnalyzing && impactItems.isNotEmpty(),
                    generationSubtitle = "Analyzing ripple effect across Architecture, APIs, Tasks, and Tests...",
                    modifier = Modifier.fillMaxWidth()
                ) {}
            }

            // 2. Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf(
                        "ALL" to "All (${impactItems.size})",
                        "ARCHITECTURE" to "Architecture",
                        "API" to "APIs",
                        "TASKS" to "Tasks",
                        "TESTS" to "Tests"
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 3. Impact Artifact Cards
            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No impact dependencies found for this filter. Link requirements to architecture, APIs, or tasks to track ripple effects.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(),
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
                                    text = item.artifactTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text(item.artifactType) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cause: ${item.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.recommendedAction,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
