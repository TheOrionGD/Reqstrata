package com.example.ui.screens.testing

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.GeneratedTestCaseItem
import com.example.domain.model.GeneratedTestSuiteResult
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.PriorityBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTestSuiteScreen(
    viewModel: MainViewModel,
    targetProjectId: String? = null
) {
    val currentProj by viewModel.currentProject.collectAsState()
    val allProjects by viewModel.projectRepo.getAllProjects().collectAsState(initial = emptyList())
    val activeProject = allProjects.firstOrNull { it.id == targetProjectId } ?: currentProj

    val projectId = activeProject?.id ?: ""
    val requirements by viewModel.reqRepo.getRequirements(projectId).collectAsState(initial = emptyList())

    var selectedReqIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedModel by remember { mutableStateOf(GeminiApiClient.MODEL_PRO) }
    var testStrategy by remember { mutableStateOf("Full Spectrum (Unit & Integration)") }
    var customSuiteName by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var generatedSuite by remember { mutableStateOf<GeneratedTestSuiteResult?>(null) }

    var activeTab by remember { mutableIntStateOf(0) } // 0: All, 1: Unit, 2: Integration

    // Auto-select all requirements on start
    LaunchedEffect(requirements) {
        if (selectedReqIds.isEmpty() && requirements.isNotEmpty()) {
            selectedReqIds = requirements.map { it.id }.toSet()
        }
        if (customSuiteName.isBlank() && activeProject != null) {
            customSuiteName = "${activeProject.name} Automated Verification Suite"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Test Suite Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = activeProject?.name ?: "Software Project",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Testing Workspace", tint = Color(0xFF8B5CF6))
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.FactCheck,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Automated Test Case Suite Synthesis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Derives isolated Unit Tests (logic, boundaries, validation) and Integration Tests (APIs, persistence, workflows) from specifications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }
            }

            // Requirements Scope & Options
            item {
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Requirement Specifications (${selectedReqIds.size}/${requirements.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row {
                                TextButton(onClick = { selectedReqIds = requirements.map { it.id }.toSet() }) {
                                    Text("Select All", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { selectedReqIds = emptySet() }) {
                                    Text("Clear", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (requirements.isEmpty()) {
                            Text(
                                text = "No requirements in database yet. Gemini will derive standard software verification cases.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(requirements, key = { it.id }) { req ->
                                    val isSelected = selectedReqIds.contains(req.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedReqIds = if (isSelected) {
                                                selectedReqIds - req.id
                                            } else {
                                                selectedReqIds + req.id
                                            }
                                        },
                                        label = {
                                            Text(
                                                "${req.code}: ${req.title.take(22)}...",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF047857),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF3F3F46), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Suite Name
                        OutlinedTextField(
                            value = customSuiteName,
                            onValueChange = { customSuiteName = it },
                            label = { Text("Test Suite Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF3F3F46),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gemini Model
                        Text(
                            text = "Gemini Reasoning Model",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedModel == GeminiApiClient.MODEL_PRO,
                                onClick = { selectedModel = GeminiApiClient.MODEL_PRO },
                                label = { Text("Gemini 3.1 Pro (Deep QA Logic)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7C3AED),
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedModel == GeminiApiClient.MODEL_FLASH,
                                onClick = { selectedModel = GeminiApiClient.MODEL_FLASH },
                                label = { Text("Gemini 3.5 Flash") },
                                leadingIcon = {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Strategy Chips
                        Text(
                            text = "Verification Strategy Focus",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val strategies = listOf(
                                "Full Spectrum (Unit & Integration)",
                                "Boundary & Negative Edge Cases",
                                "Security & Access Invariants",
                                "Performance & Uptime Benchmarks"
                            )
                            items(strategies) { strat ->
                                FilterChip(
                                    selected = testStrategy == strat,
                                    onClick = { testStrategy = strat },
                                    label = { Text(strat, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate Button
                        Button(
                            onClick = {
                                isGenerating = true
                                statusMessage = null
                                viewModel.viewModelScope.launch {
                                    try {
                                        val activeReqs = requirements.filter { selectedReqIds.contains(it.id) }
                                        val reqSummary = if (activeReqs.isNotEmpty()) {
                                            activeReqs.joinToString("\n---\n") { r ->
                                                "[${r.code}] ${r.title} (${r.type}, Priority: ${r.priority})\nDescription: ${r.description}\nAcceptance Criteria:\n${r.acceptanceCriteriaJson}"
                                            }
                                        } else {
                                            "Project: ${activeProject?.name ?: "Software Application"}\nDomain: ${activeProject?.domain ?: "Enterprise Platform"}\nFunctional & Security Scope: User authentication, CRUD operations, database persistence, authorization."
                                        }

                                        val suite = GeminiApiClient.generateTestSuiteFromRequirements(
                                            projectName = activeProject?.name ?: "Software Application",
                                            requirementsSummary = "$reqSummary\nStrategy Focus: $testStrategy",
                                            model = selectedModel
                                        )

                                        generatedSuite = suite.copy(
                                            suiteName = if (customSuiteName.isNotBlank()) customSuiteName else suite.suiteName
                                        )
                                        statusMessage = "Generated ${suite.unitTests.size} Unit Tests and ${suite.integrationTests.size} Integration Tests with Gemini!"
                                    } catch (e: Exception) {
                                        statusMessage = "Test generation error: ${e.message}"
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_test_suite_btn")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Deriving Automated Test Suite with Gemini...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Test Cases Suite with Gemini", fontWeight = FontWeight.Bold)
                            }
                        }

                        statusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (msg.contains("error", ignoreCase = true)) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // Results View
            generatedSuite?.let { suite ->
                val allCases = suite.unitTests + suite.integrationTests
                val selectedCount = allCases.count { it.isSelected }

                // Suite Summary Header
                item {
                    Surface(
                        color = Color(0xFF27272A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = suite.suiteName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$selectedCount / ${allCases.size} Selected",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (suite.summary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = suite.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFAFAFA)
                                )
                            }
                        }
                    }
                }

                // Filter Tabs: All, Unit, Integration
                item {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color(0xFF18181B),
                        contentColor = Color(0xFF10B981),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("All (${allCases.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Unit Tests (${suite.unitTests.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("Integration Tests (${suite.integrationTests.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                val displayedCases = when (activeTab) {
                    1 -> suite.unitTests
                    2 -> suite.integrationTests
                    else -> allCases
                }

                items(displayedCases, key = { it.id }) { tc ->
                    GeneratedTestCaseCard(
                        item = tc,
                        onToggleSelect = {
                            val updatedUnits = suite.unitTests.map {
                                if (it.id == tc.id) it.copy(isSelected = !it.isSelected) else it
                            }
                            val updatedIntegrations = suite.integrationTests.map {
                                if (it.id == tc.id) it.copy(isSelected = !it.isSelected) else it
                            }
                            generatedSuite = suite.copy(
                                unitTests = updatedUnits,
                                integrationTests = updatedIntegrations
                            )
                        }
                    )
                }

                // Save to Room DB Action
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            viewModel.saveGeneratedTestSuiteToProject(
                                projectId = projectId,
                                testSuiteResult = suite,
                                onComplete = {
                                    viewModel.navigateTo(Screen.TestingWorkspace)
                                }
                            )
                        },
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_test_suite_to_room_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save $selectedCount Test Cases into Room Database",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.navigateTo(Screen.TestingWorkspace)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Open QA Workspace & Run Tests",
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedTestCaseCard(
    item: GeneratedTestCaseItem,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        border = if (item.isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("test_case_card_${item.code}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF059669),
                            uncheckedColor = Color(0xFF71717A)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val isUnit = item.testType.equals("Unit", ignoreCase = true)
                    val typeBadgeBg = if (isUnit) Color(0xFF5B21B6) else Color(0xFF7C3AED)

                    Surface(
                        color = typeBadgeBg.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.testType.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnit) Color(0xFF8B5CF6) else Color(0xFFA78BFA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.linkedRequirementCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    PriorityBadge(priority = item.priority)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (item.preconditions.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(
                        "Preconditions: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        item.preconditions,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFAFAFA)
                    )
                }
            }

            if (item.testData.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "Test Data: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        item.testData,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            if (item.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Execution Steps (${item.steps.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        item.steps.forEachIndexed { idx, step ->
                            Text(
                                text = "${idx + 1}. $step",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFAFAFA),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Expected: ${item.expectedResult}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}
