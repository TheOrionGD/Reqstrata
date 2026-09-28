package com.theoriongd.reqstrata.ui.screens.requirements

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.GeneratedRequirementItem
import com.theoriongd.reqstrata.domain.model.RequirementsPromptTemplate
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.PriorityBadge
import kotlinx.coroutines.launch

private val DEFAULT_TEMPLATES = listOf(
    RequirementsPromptTemplate(
        id = "ieee830",
        name = "IEEE 830 / ISO 29148 Standard",
        description = "Formal software engineering specification with strict functional and non-functional separation.",
        template = """
            You are a Principal Software Requirements Engineer.
            Generate comprehensive, structured software requirements based on these high-level project goals:
            Project Goals: {GOALS}
            Industry / Domain: {DOMAIN}
            Target End-Users: {USERS}
            Constraints & Quality Expectations: {CONSTRAINTS}

            Respond ONLY with a valid JSON object matching this schema:
            {
              "functionalRequirements": [
                {
                  "code": "FR-001",
                  "title": "Clear Functional Title",
                  "category": "Core / Identity / Workflow / etc",
                  "description": "The system shall...",
                  "priority": "Critical / High / Medium / Low",
                  "userStory": "As a [role], I want [capability] so that [benefit]",
                  "acceptanceCriteria": [
                    "Given [precondition], when [action], then [outcome]",
                    "Given [edge case], when [action], then [error handling]"
                  ]
                }
              ],
              "nonFunctionalRequirements": [
                {
                  "code": "NFR-001",
                  "category": "Security / Performance / Reliability / Usability / Scalability",
                  "title": "Clear Quality Title",
                  "description": "Specific quantitative requirement...",
                  "priority": "Critical / High / Medium / Low",
                  "verificationMetric": "Precise measurable metric (e.g., P95 latency <= 300ms, AES-256)"
                }
              ]
            }
        """.trimIndent()
    ),
    RequirementsPromptTemplate(
        id = "agile",
        name = "Agile & User Story Centric",
        description = "User-story driven format focused on actor personas, value propositions, and Given-When-Then criteria.",
        template = """
            You are an Agile Product Owner and Business Analyst.
            Analyze the following project goals and construct detailed user-centric software specifications:
            Goals: {GOALS}
            Domain: {DOMAIN}
            Actors: {USERS}
            Constraints: {CONSTRAINTS}

            Respond ONLY with a valid JSON object:
            {
              "functionalRequirements": [
                {
                  "code": "FR-001",
                  "title": "Feature Name",
                  "category": "User Experience",
                  "description": "Detailed capability description",
                  "priority": "High",
                  "userStory": "As a [user], I want to [goal] so that [business value]",
                  "acceptanceCriteria": [
                    "Given user on screen, when button clicked, then show result"
                  ]
                }
              ],
              "nonFunctionalRequirements": [
                {
                  "code": "NFR-001",
                  "category": "Performance",
                  "title": "App Responsiveness",
                  "description": "Smooth 60fps interaction and low memory usage",
                  "priority": "High",
                  "verificationMetric": "Frame render time under 16ms"
                }
              ]
            }
        """.trimIndent()
    ),
    RequirementsPromptTemplate(
        id = "mission_critical",
        name = "Enterprise & Mission Critical",
        description = "High-compliance, zero-trust security, encryption, and auditability focus.",
        template = """
            You are a Lead Enterprise Architect and Cybersecurity Specialist.
            Generate strict mission-critical software specifications for:
            Project Goals: {GOALS}
            Domain: {DOMAIN}
            Users: {USERS}
            Constraints: {CONSTRAINTS}

            Respond ONLY with a valid JSON object with 'functionalRequirements' and 'nonFunctionalRequirements'. Include strict encryption, audit trails, and deterministic performance benchmarks.
        """.trimIndent()
    )
)

private val GOAL_PRESETS = listOf(
    "Smart Telemedicine & EHR Portal" to Triple(
        "Secure digital healthcare platform allowing patient appointment booking, encrypted video consultations, electronic health record (EHR) synchronization, and e-prescriptions with pharmacy fulfillment.",
        "Healthcare & HealthTech",
        "Patients, Attending Physicians, Pharmacists, System Admins"
    ),
    "Real-Time E-Commerce Engine" to Triple(
        "High-performance e-commerce platform with one-click checkout, real-time inventory reservation, distributed cart state, automated tax calculation, and fraud score inspection.",
        "Retail & E-Commerce",
        "Shoppers, Store Managers, Inventory Staff, Payment Gateways"
    ),
    "Autonomous IoT Logistics Tracker" to Triple(
        "Supply chain tracking system collecting telemetry from fleet vehicles and cold-storage sensors, predicting delivery ETAs, and alerting upon geofence or temperature breaches.",
        "Logistics & Supply Chain",
        "Fleet Dispatchers, Warehouse Managers, Delivery Drivers, Operations Analysts"
    ),
    "Fintech Real-Time Audit Ledger" to Triple(
        "Double-entry bookkeeping financial platform with automated reconciliation, anomaly detection, multi-currency wallet transfers, and regulatory compliance reporting.",
        "FinTech & Banking",
        "Account Holders, Compliance Officers, Financial Controllers, Internal Auditors"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateRequirementsScreen(
    viewModel: MainViewModel,
    targetProjectId: String? = null
) {
    val projects by viewModel.projectRepo.getAllProjects().collectAsState(initial = emptyList())
    val activeProject by viewModel.currentProject.collectAsState()

    var selectedProjectId by remember {
        mutableStateOf(targetProjectId ?: activeProject?.id ?: projects.firstOrNull()?.id ?: "")
    }

    var goalsInput by remember {
        mutableStateOf(
            activeProject?.description?.takeIf { it.isNotBlank() }
                ?: "Comprehensive software platform with automated workflows, real-time state management, secure identity access, and scalable reporting."
        )
    }
    var domainInput by remember { mutableStateOf(activeProject?.domain?.takeIf { it.isNotBlank() } ?: "Enterprise Platform") }
    var usersInput by remember { mutableStateOf("Registered Users, Department Operators, System Administrators") }
    var constraintsInput by remember { mutableStateOf("Sub-second latency, AES-256 encryption at rest, offline Room support, 99.9% uptime SLA") }

    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    var customTemplateText by remember { mutableStateOf(DEFAULT_TEMPLATES[0].template) }
    var showCustomTemplateEditor by remember { mutableStateOf(false) }

    // Model selection per guidelines
    var selectedModel by remember { mutableStateOf(GeminiApiClient.MODEL_PRO) }

    var isGenerating by remember { mutableStateOf(false) }
    var generationStatusMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Results state
    var functionalResults by remember { mutableStateOf<List<GeneratedRequirementItem>>(emptyList()) }
    var nonFunctionalResults by remember { mutableStateOf<List<GeneratedRequirementItem>>(emptyList()) }
    var activeResultTab by remember { mutableIntStateOf(0) } // 0: Functional, 1: Non-Functional

    val scrollState = rememberScrollState()

    LaunchedEffect(projects) {
        if (selectedProjectId.isBlank() && projects.isNotEmpty()) {
            selectedProjectId = activeProject?.id ?: projects.first().id
        }
    }

    if (showExportDialog) {
        com.theoriongd.reqstrata.ui.components.ExportSpecificationDialog(
            project = activeProject,
            requirements = emptyList(),
            onDismiss = { showExportDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Requirement Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Transform High-Level Goals into Structured Specs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("gen_req_export_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Specs", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("gen_req_dark_mode_btn")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF8B5CF6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Target Project Selector
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
                            text = "Target Project in Database",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${projects.size} Available",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (projects.isEmpty()) {
                        Text(
                            text = "No projects found in Room database. Please create a project first.",
                            color = Color(0xFFFCA5A5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(projects) { p ->
                                FilterChip(
                                    selected = selectedProjectId == p.id,
                                    onClick = { selectedProjectId = p.id },
                                    label = { Text(p.name, maxLines = 1) },
                                    leadingIcon = if (selectedProjectId == p.id) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets selector
            Text(
                text = "Preset Industry Goal Blueprints",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA1A1AA)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GOAL_PRESETS) { (name, data) ->
                    SuggestionChip(
                        onClick = {
                            goalsInput = data.first
                            domainInput = data.second
                            usersInput = data.third
                        },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // High-Level Goals Input
            OutlinedTextField(
                value = goalsInput,
                onValueChange = { goalsInput = it },
                label = { Text("High-Level Project Goals & Business Intent") },
                placeholder = { Text("Describe what the system should accomplish, core value propositions, and target outcomes...") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goals_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Domain & Industry
            OutlinedTextField(
                value = domainInput,
                onValueChange = { domainInput = it },
                label = { Text("Domain / Sector") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Target Users
            OutlinedTextField(
                value = usersInput,
                onValueChange = { usersInput = it },
                label = { Text("Target Users & Stakeholders") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Constraints
            OutlinedTextField(
                value = constraintsInput,
                onValueChange = { constraintsInput = it },
                label = { Text("System Constraints & Quality Attributes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Template Selection
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
                            text = "Requirements Prompt Template",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        TextButton(onClick = { showCustomTemplateEditor = !showCustomTemplateEditor }) {
                            Text(
                                text = if (showCustomTemplateEditor) "Hide Template" else "Customize",
                                color = Color(0xFF8B5CF6),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DEFAULT_TEMPLATES.indices.toList()) { idx ->
                            val tmpl = DEFAULT_TEMPLATES[idx]
                            FilterChip(
                                selected = selectedTemplateIndex == idx,
                                onClick = {
                                    selectedTemplateIndex = idx
                                    customTemplateText = tmpl.template
                                },
                                label = { Text(tmpl.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = DEFAULT_TEMPLATES[selectedTemplateIndex].description,
                        color = Color(0xFFA1A1AA),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (showCustomTemplateEditor) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customTemplateText,
                            onValueChange = { customTemplateText = it },
                            label = { Text("Prompt Template (uses {GOALS}, {DOMAIN}, {USERS}, {CONSTRAINTS})") },
                            minLines = 5,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF3F3F46),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Selection
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Gemini Model Selection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose the optimal model based on requirement complexity:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedModel == GeminiApiClient.MODEL_PRO,
                            onClick = { selectedModel = GeminiApiClient.MODEL_PRO },
                            label = { Text("3.1 Pro (Complex)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedModel == GeminiApiClient.MODEL_FLASH,
                            onClick = { selectedModel = GeminiApiClient.MODEL_FLASH },
                            label = { Text("3.5 Flash (General)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedModel == GeminiApiClient.MODEL_FLASH_LITE,
                            onClick = { selectedModel = GeminiApiClient.MODEL_FLASH_LITE },
                            label = { Text("3.1 Flash Lite (Fast)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generate Button
            Button(
                onClick = {
                    if (goalsInput.isBlank()) {
                        goalsInput = "Modern collaborative system with automated requirement validation, testing, and system architecture mapping."
                    }
                    isGenerating = true
                    generationStatusMessage = "Analyzing project goals with $selectedModel..."

                    viewModel.viewModelScope.launch {
                        try {
                            val templateToUse = if (showCustomTemplateEditor) customTemplateText else DEFAULT_TEMPLATES[selectedTemplateIndex].template
                            val (funcs, nonFuncs) = GeminiApiClient.generateStructuredRequirements(
                                goals = goalsInput,
                                domain = domainInput,
                                targetUsers = usersInput,
                                constraints = constraintsInput,
                                promptTemplate = templateToUse,
                                model = selectedModel
                            )
                            functionalResults = funcs
                            nonFunctionalResults = nonFuncs
                            generationStatusMessage = null
                        } catch (e: Exception) {
                            generationStatusMessage = "Generation error: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_requirements_btn")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generating Structured Requirements with Gemini...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Requirements with Gemini", fontWeight = FontWeight.Bold)
                }
            }

            generationStatusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B5CF6)
                )
            }

            // Results Section
            if (functionalResults.isNotEmpty() || nonFunctionalResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                val totalSelected = functionalResults.count { it.isSelected } + nonFunctionalResults.count { it.isSelected }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Requirements ($totalSelected Selected)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row {
                        TextButton(onClick = {
                            functionalResults = functionalResults.map { it.copy(isSelected = true) }
                            nonFunctionalResults = nonFunctionalResults.map { it.copy(isSelected = true) }
                        }) {
                            Text("Select All", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = {
                            functionalResults = functionalResults.map { it.copy(isSelected = false) }
                            nonFunctionalResults = nonFunctionalResults.map { it.copy(isSelected = false) }
                        }) {
                            Text("Deselect", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Tabs for Functional and Non-Functional
                TabRow(
                    selectedTabIndex = activeResultTab,
                    containerColor = Color(0xFF18181B),
                    contentColor = Color(0xFF8B5CF6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeResultTab == 0,
                        onClick = { activeResultTab = 0 },
                        text = { Text("Functional (${functionalResults.size})") }
                    )
                    Tab(
                        selected = activeResultTab == 1,
                        onClick = { activeResultTab = 1 },
                        text = { Text("Non-Functional (${nonFunctionalResults.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val itemsToShow = if (activeResultTab == 0) functionalResults else nonFunctionalResults

                itemsToShow.forEachIndexed { index, reqItem ->
                    GeneratedRequirementCard(
                        item = reqItem,
                        onToggleSelect = {
                            if (activeResultTab == 0) {
                                functionalResults = functionalResults.toMutableList().also {
                                    it[index] = reqItem.copy(isSelected = !reqItem.isSelected)
                                }
                            } else {
                                nonFunctionalResults = nonFunctionalResults.toMutableList().also {
                                    it[index] = reqItem.copy(isSelected = !reqItem.isSelected)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save to Database Button
                Button(
                    onClick = {
                        val allSelected = (functionalResults + nonFunctionalResults).filter { it.isSelected }
                        if (allSelected.isEmpty()) {
                            return@Button
                        }
                        if (selectedProjectId.isBlank()) {
                            // If no project exists yet, create one automatically
                            viewModel.createProject(
                                name = if (domainInput.isNotBlank()) "$domainInput System" else "Requirements Project",
                                description = goalsInput.take(200),
                                domain = domainInput,
                                projectType = "Software Platform",
                                techStack = "Android Kotlin, Compose, Room, Gemini API",
                                methodology = "Agile / Scrum",
                                visibility = "Private"
                            )
                        } else {
                            viewModel.saveGeneratedRequirements(
                                projectId = selectedProjectId,
                                selectedRequirements = allSelected,
                                onComplete = {
                                    viewModel.navigateTo(Screen.RequirementsList)
                                }
                            )
                        }
                    },
                    enabled = totalSelected > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_to_database_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save $totalSelected Requirements to Room Database", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pId = if (selectedProjectId.isNotBlank() && selectedProjectId != "NEW_PROJECT") selectedProjectId else targetProjectId
                            viewModel.navigateTo(Screen.SuggestArchitecture(pId))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Suggest Architecture", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            val pId = if (selectedProjectId.isNotBlank() && selectedProjectId != "NEW_PROJECT") selectedProjectId else targetProjectId
                            viewModel.navigateTo(Screen.GenerateTestSuite(pId))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Test Suite", color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun GeneratedRequirementCard(
    item: GeneratedRequirementItem,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        border = if (item.isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("generated_card_${item.code}")
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
                            checkedColor = Color(0xFF6D28D9),
                            uncheckedColor = Color(0xFF71717A)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                PriorityBadge(priority = item.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category tag
            Surface(
                color = Color(0xFF3F3F46),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFAFAFA),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0)
            )

            if (item.userStory.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "User Story:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                        Text(
                            text = item.userStory,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFAFAFA)
                        )
                    }
                }
            }

            if (item.acceptanceCriteria.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Acceptance Criteria (${item.acceptanceCriteria.size}):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFA1A1AA)
                )
                item.acceptanceCriteria.forEach { crit ->
                    Text(
                        text = "• $crit",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (item.verificationMetric.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Metric: ${item.verificationMetric}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}
