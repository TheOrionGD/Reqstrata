package com.example.ui.screens.architecture

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.RequirementEntity
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.*
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateView
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestArchitectureScreen(
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
    var architectureFocus by remember { mutableStateOf("Clean Architecture with Domain Invariants") }

    var isSynthesizing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var suggestedArchitecture by remember { mutableStateOf<GeneratedArchitectureSuggestion?>(null) }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Components, 1: DB Schema, 2: API Structure, 3: Trade-offs & ADRs
    var showExportDialog by remember { mutableStateOf(false) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Auto-select all requirements on first load
    LaunchedEffect(requirements) {
        if (selectedReqIds.isEmpty() && requirements.isNotEmpty()) {
            selectedReqIds = requirements.map { it.id }.toSet()
        }
    }

    if (showExportDialog) {
        com.example.ui.components.ExportSpecificationDialog(
            project = activeProject,
            requirements = requirements,
            onDismiss = { showExportDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI System Architecture Suggester",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeProject?.name ?: "Software Project",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("suggest_arch_export_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Specs", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("suggest_arch_dark_mode_btn")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF8B5CF6)
                        )
                    }
                    if (suggestedArchitecture != null) {
                        IconButton(onClick = {
                            viewModel.navigateTo(Screen.GenerateTestSuite(projectId))
                        }) {
                            Icon(
                                Icons.Default.FactCheck,
                                contentDescription = "Generate Test Suite",
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Header Information Card
            item {
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF5B21B6).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountTree,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Requirements to Architecture Synthesis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Translates functional and non-functional specifications into components, relational schemas, and REST APIs.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }
            }

            // Input Requirements Scope & Model Selection
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
                                text = "Source Requirements (${selectedReqIds.size}/${requirements.size} Included)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row {
                                TextButton(onClick = { selectedReqIds = requirements.map { it.id }.toSet() }) {
                                    Text("All", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { selectedReqIds = emptySet() }) {
                                    Text("None", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (requirements.isEmpty()) {
                            Text(
                                text = "No requirements registered yet. Gemini will derive standard domain architecture baseline.",
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
                                                "${req.code}: ${req.title.take(24)}...",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF5B21B6),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF18181B),
                                            labelColor = Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF3F3F46), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Model Selector
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
                                label = { Text("Gemini 3.1 Pro (Deep Architecture)") },
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

                        // Architectural Style Style Chips
                        Text(
                            text = "Architectural Paradigm",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val styles = listOf(
                                "Clean Architecture with DDD",
                                "Modular Monolith",
                                "Event-Driven Microservices",
                                "Hexagonal / Ports & Adapters"
                            )
                            items(styles) { style ->
                                FilterChip(
                                    selected = architectureFocus == style,
                                    onClick = { architectureFocus = style },
                                    label = { Text(style, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Synthesize Button
                        Button(
                            onClick = {
                                isSynthesizing = true
                                statusMessage = null
                                viewModel.viewModelScope.launch {
                                    try {
                                        val activeReqs = requirements.filter { selectedReqIds.contains(it.id) }
                                        val reqSummary = if (activeReqs.isNotEmpty()) {
                                            activeReqs.joinToString("\n---\n") { r ->
                                                "[${r.code}] ${r.title} (${r.type}, Priority: ${r.priority})\nDescription: ${r.description}\nAcceptance Criteria: ${r.acceptanceCriteriaJson}"
                                            }
                                        } else {
                                            "Project Goals: Build high reliability, scalable software platform in domain '${activeProject?.domain ?: "Enterprise"}'. Include robust user auth, data persistence, and REST APIs."
                                        }

                                        val suggestion = GeminiApiClient.suggestSystemArchitecture(
                                            projectName = activeProject?.name ?: "Software System",
                                            projectDomain = activeProject?.domain ?: "Enterprise",
                                            requirementsSummary = "$reqSummary\nTarget Style: $architectureFocus",
                                            model = selectedModel
                                        )

                                        suggestedArchitecture = suggestion
                                        statusMessage = "Architecture successfully synthesized with Gemini!"
                                    } catch (e: Exception) {
                                        statusMessage = "Synthesis error: ${e.message}"
                                    } finally {
                                        isSynthesizing = false
                                    }
                                }
                            },
                            enabled = !isSynthesizing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("synthesize_architecture_btn")
                        ) {
                            if (isSynthesizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Synthesizing System Architecture with Gemini...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Suggest System Architecture with Gemini", fontWeight = FontWeight.Bold)
                            }
                        }

                        statusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (msg.contains("error", ignoreCase = true)) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
            }

            // Results View
            suggestedArchitecture?.let { arch ->
                // System Overview & Style Card
                item {
                    Surface(
                        color = Color(0xFF27272A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Synthesized System Architecture",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = arch.architectureStyle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (arch.systemOverview.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = arch.systemOverview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFAFAFA)
                                )
                            }
                        }
                    }
                }

                // Tabs for Components, Database Schema, API Structure, and Trade-offs
                item {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color(0xFF18181B),
                        contentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Components (${arch.components.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("DB Schema (${arch.databaseTables.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("API Structure (${arch.apiEndpoints.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        Tab(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            text = { Text("ADRs & Tradeoffs", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Tab 0: Components
                if (activeTab == 0) {
                    items(arch.components, key = { it.code }) { comp ->
                        ArchitectureComponentCard(
                            component = comp,
                            onToggleSelect = {
                                val updated = arch.components.map {
                                    if (it.code == comp.code) it.copy(isSelected = !it.isSelected) else it
                                }
                                suggestedArchitecture = arch.copy(components = updated)
                            }
                        )
                    }
                }

                // Tab 1: Database Schema (Tables, Columns, Relationships)
                if (activeTab == 1) {
                    items(arch.databaseTables, key = { it.tableName }) { table ->
                        DatabaseTableCard(
                            table = table,
                            onToggleSelect = {
                                val updated = arch.databaseTables.map {
                                    if (it.tableName == table.tableName) it.copy(isSelected = !it.isSelected) else it
                                }
                                suggestedArchitecture = arch.copy(databaseTables = updated)
                            }
                        )
                    }
                }

                // Tab 2: API Structure (Endpoints, Request/Response contracts)
                if (activeTab == 2) {
                    items(arch.apiEndpoints, key = { it.code }) { api ->
                        ApiEndpointCard(
                            endpoint = api,
                            onToggleSelect = {
                                val updated = arch.apiEndpoints.map {
                                    if (it.code == api.code) it.copy(isSelected = !it.isSelected) else it
                                }
                                suggestedArchitecture = arch.copy(apiEndpoints = updated)
                            }
                        )
                    }
                }

                // Tab 3: ADRs & Trade-offs
                if (activeTab == 3) {
                    item {
                        Text(
                            text = "Architectural Trade-offs & Decisions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    items(arch.tradeoffs) { tr ->
                        TradeoffCard(tradeoff = tr)
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Architectural Decision Records (ADR)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    items(arch.architecturalDecisions, key = { it.code }) { adr ->
                        AdrCard(adr = adr)
                    }
                }

                // Save to Room Database Action Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    val selectedComps = arch.components.count { it.isSelected }
                    val selectedTables = arch.databaseTables.count { it.isSelected }
                    val selectedApis = arch.apiEndpoints.count { it.isSelected }

                    Button(
                        onClick = {
                            viewModel.saveArchitectureSuggestionToProject(
                                projectId = projectId,
                                suggestion = arch,
                                onComplete = {
                                    viewModel.navigateTo(Screen.ArchitectureWorkspace)
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("apply_architecture_to_db_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Architecture ($selectedComps Comps, $selectedTables Tables, $selectedApis APIs) to Project",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary action: Generate Test Suite directly from this specification
                    OutlinedButton(
                        onClick = {
                            viewModel.navigateTo(Screen.GenerateTestSuite(projectId))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Continue to Automatic Test Suite Generation",
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
private fun ArchitectureComponentCard(
    component: GeneratedArchitectureComponent,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        border = if (component.isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("component_card_${component.code}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = component.isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6D28D9),
                            uncheckedColor = Color(0xFF71717A)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = component.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF3F3F46),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = component.layer,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = component.responsibilities,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFAFAFA)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tech Stack: ${component.techStack}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF59E0B)
                )
            }

            if (component.dependencies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dependencies: ${component.dependencies.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        }
    }
}

@Composable
private fun DatabaseTableCard(
    table: GeneratedDatabaseTable,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        border = if (table.isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("db_table_card_${table.tableName}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = table.isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6D28D9),
                            uncheckedColor = Color(0xFF71717A)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = table.tableName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${table.columns.size} Columns",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (table.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = table.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Column Schema Table View
            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    table.columns.forEach { col ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (col.isPrimaryKey) {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = "Primary Key",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else if (col.foreignKeyTarget != null) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = "Foreign Key",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = col.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (col.isPrimaryKey) Color(0xFFF59E0B) else Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (col.isPrimaryKey) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = col.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA1A1AA),
                                    fontFamily = FontFamily.Monospace
                                )
                                if (col.isUnique) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("UQ", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                                }
                                if (col.isNullable) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("NULL", style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                                }
                            }
                        }
                    }
                }
            }

            if (table.relationshipsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Relationships: ${table.relationshipsSummary}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
private fun ApiEndpointCard(
    endpoint: GeneratedApiEndpoint,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        border = if (endpoint.isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("api_endpoint_card_${endpoint.code}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = endpoint.isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6D28D9),
                            uncheckedColor = Color(0xFF71717A)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val methodColor = when (endpoint.method.uppercase()) {
                        "GET" -> Color(0xFF8B5CF6)
                        "POST" -> Color(0xFF10B981)
                        "PUT" -> Color(0xFFF59E0B)
                        "DELETE" -> Color(0xFFEF4444)
                        else -> Color(0xFFA855F7)
                    }

                    Surface(
                        color = methodColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = endpoint.method.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = methodColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endpoint.path,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (endpoint.authRequired) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Auth Required",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = endpoint.summary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0)
            )

            if (endpoint.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = endpoint.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )
            }

            if (endpoint.requestSchema != "{}" && endpoint.requestSchema.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request Body Schema:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B5CF6)
                )
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = endpoint.requestSchema,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFAFAFA),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (endpoint.statusCodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    endpoint.statusCodes.forEach { code ->
                        Surface(
                            color = Color(0xFF18181B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (code.startsWith("2")) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeoffCard(tradeoff: GeneratedTradeoff) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = tradeoff.decision,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row {
                Text("Pros: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text(tradeoff.pros, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text("Cons: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                Text(tradeoff.cons, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
            }

            if (tradeoff.recommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text("Rec: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                    Text(tradeoff.recommendation, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B5CF6))
                }
            }
        }
    }
}

@Composable
private fun AdrCard(adr: GeneratedAdr) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
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
                    text = "${adr.code}: ${adr.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Accepted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Context: ${adr.context}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Decision: ${adr.decision}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Consequences: ${adr.consequences}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFAFAFA)
            )
        }
    }
}
