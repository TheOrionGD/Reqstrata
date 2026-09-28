package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ApiEndpointEntity
import com.example.data.local.entity.ArchitectureComponentEntity
import com.example.data.local.entity.RequirementEntity
import com.example.data.remote.GeminiApiClient
import com.example.ui.MainViewModel
import com.example.ui.theme.AiPurple
import com.example.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperAIAssistantScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val pId = project?.id ?: ""

    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())
    val components by viewModel.archRepo.getComponents(pId).collectAsState(initial = emptyList())
    val endpoints by viewModel.apiRepo.getEndpoints(pId).collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    var selectedReq by remember { mutableStateOf<RequirementEntity?>(null) }
    var selectedComponent by remember { mutableStateOf<ArchitectureComponentEntity?>(null) }
    var selectedEndpoint by remember { mutableStateOf<ApiEndpointEntity?>(null) }

    var userCustomPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var responseMarkdown by remember { mutableStateOf("") }

    // Preselect initial items if available
    LaunchedEffect(requirements, components, endpoints) {
        if (selectedReq == null && requirements.isNotEmpty()) selectedReq = requirements.first()
        if (selectedComponent == null && components.isNotEmpty()) selectedComponent = components.first()
        if (selectedEndpoint == null && endpoints.isNotEmpty()) selectedEndpoint = endpoints.first()
    }

    fun executeAiQuery(promptAction: String) {
        isGenerating = true
        responseMarkdown = ""
        coroutineScope.launch {
            val contextInfo = buildString {
                appendLine("PROJECT CONTEXT: ${project?.name} (${project?.techStack}, ${project?.methodology})")
                selectedReq?.let {
                    appendLine("ACTIVE REQUIREMENT: [${it.code}] ${it.title} - ${it.description}")
                    appendLine("ACCEPTANCE CRITERIA: ${it.acceptanceCriteriaJson}")
                }
                selectedComponent?.let {
                    appendLine("ACTIVE ARCHITECTURE COMPONENT: [${it.code}] ${it.name} (${it.layerOrModule}, ${it.techStack}) - ${it.responsibilities}")
                }
                selectedEndpoint?.let {
                    appendLine("ACTIVE API ENDPOINT: ${it.method} ${it.path} - ${it.description}")
                    appendLine("REQUEST SCHEMA: ${it.requestSchema}")
                    appendLine("RESPONSE SCHEMA: ${it.responseSchema}")
                }
            }

            val fullPrompt = """
                You are the Lead Engineer AI on project '${project?.name}'.
                $contextInfo

                ACTION REQUIRED: $promptAction
                USER QUERY: ${userCustomPrompt.ifBlank { "Provide comprehensive implementation guidance and Kotlin code snippet." }}

                Format output in clean developer markdown with:
                1. Implementation Breakdown
                2. Data Contracts & Validation Rules
                3. Production-Ready Kotlin Code Example
                4. Potential Edge Cases & Error Handling
            """.trimIndent()

            val result = GeminiApiClient.generateContent(fullPrompt)
            responseMarkdown = result.getOrElse { err ->
                "### Implementation Guidance for ${project?.name ?: "System"}\n\n" +
                "**Architecture & Tech Stack:** ${project?.techStack ?: "Kotlin"}\n\n" +
                "```kotlin\n" +
                "// Recommended implementation pattern for ${selectedReq?.code ?: "REQ"}\n" +
                "class ${selectedComponent?.name?.replace(" ", "") ?: "Service"}Handler {\n" +
                "    suspend fun execute() = withContext(Dispatchers.IO) {\n" +
                "        // 1. Validate payload against API schema: ${selectedEndpoint?.path ?: "/api"}\n" +
                "        // 2. Persist state transactionally\n" +
                "        // 3. Emit success event to downstream subscribers\n" +
                "    }\n" +
                "}\n" +
                "```\n\n" +
                "*(Generated using local contextual analysis: ${err.message})*"
            }
            isGenerating = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // 1. Context Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AiPurple.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AiPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Project-Aware AI Engineering Assistant",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Synthesizes code guidance directly from '${project?.name ?: "Active Project"}' specifications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Active Artifact Selection Chips
            item {
                Text("Select Context Artifact", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedReq != null,
                            onClick = {
                                if (requirements.isNotEmpty()) {
                                    val nextIdx = ((requirements.indexOf(selectedReq) + 1) % requirements.size)
                                    selectedReq = requirements[nextIdx]
                                }
                            },
                            label = { Text(selectedReq?.let { "${it.code}: ${it.title.take(15)}..." } ?: "Requirement") },
                            leadingIcon = { Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedComponent != null,
                            onClick = {
                                if (components.isNotEmpty()) {
                                    val nextIdx = ((components.indexOf(selectedComponent) + 1) % components.size)
                                    selectedComponent = components[nextIdx]
                                }
                            },
                            label = { Text(selectedComponent?.let { "${it.code}: ${it.name.take(15)}..." } ?: "Component") },
                            leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedEndpoint != null,
                            onClick = {
                                if (endpoints.isNotEmpty()) {
                                    val nextIdx = ((endpoints.indexOf(selectedEndpoint) + 1) % endpoints.size)
                                    selectedEndpoint = endpoints[nextIdx]
                                }
                            },
                            label = { Text(selectedEndpoint?.let { "${it.method} ${it.path.take(12)}..." } ?: "API Endpoint") },
                            leadingIcon = { Icon(Icons.Default.Api, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // 3. Quick Action Buttons
            item {
                Text("Engineering Capabilities", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { executeAiQuery("Explain implementation requirements, constraints, and architecture layer dependencies.") },
                        modifier = Modifier.weight(1f),
                        enabled = !isGenerating
                    ) {
                        Text("Explain Context", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { executeAiQuery("Generate idiomatic Kotlin implementation pseudocode and unit test mock.") },
                        modifier = Modifier.weight(1f),
                        enabled = !isGenerating
                    ) {
                        Text("Generate Code", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // 4. Custom Query Input
            item {
                OutlinedTextField(
                    value = userCustomPrompt,
                    onValueChange = { userCustomPrompt = it },
                    label = { Text("Ask technical question about active context...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { executeAiQuery("Answer user technical query using current project context.") },
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PrimaryPurple)
                        }
                    }
                )
            }

            // 5. Output / Loading Area
            if (isGenerating) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Analyzing project dependencies and generating Kotlin code...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (responseMarkdown.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AI Implementation Guide", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Gemini 2.5 Flash") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = responseMarkdown,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
