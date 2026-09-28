package com.example.ui.screens.architecture

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entity.ArchitectureComponentEntity
import com.example.data.local.entity.ArchitectureDecisionEntity
import com.example.data.local.entity.Converters
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.UmlDiagramType
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AiActionCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.UmlDiagramCanvas
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureWorkspaceScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val components by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val decisions by viewModel.archRepo.getDecisions(project?.id ?: "").collectAsState(initial = emptyList())
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var isGeneratingArch by remember { mutableStateOf(false) }
    var showAddComponentDialog by remember { mutableStateOf(false) }
    var showAddAdrDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Components, 1: ADRs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Architecture Workspace", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ArchitectureDecisions) }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Architecture Decision Records", tint = Color(0xFFF59E0B))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.SuggestArchitecture(project?.id)) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Suggest Architecture", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.UmlStudio) }) {
                        Icon(Icons.Default.Brush, contentDescription = "UML Studio", tint = Color(0xFFFAFAFA))
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

            // AI Architecture Generator
            AiActionCard(
                title = "Synthesize Architecture with Gemini",
                description = "Generates components, layered boundaries, and technical trade-offs based on approved requirements.",
                buttonText = "Generate Architecture",
                isLoading = isGeneratingArch,
                onClick = {
                    isGeneratingArch = true
                    viewModel.viewModelScope.launch {
                        val p = project ?: return@launch
                        val approved = reqs.filter { it.status.equals("Approved", ignoreCase = true) }
                        val summary = approved.joinToString("; ") { "${it.code}: ${it.title}" }

                        val archJson = GeminiApiClient.generateArchitecture(p.name, summary.ifBlank { "Standard enterprise software architecture" })
                        val compsArray = archJson.optJSONArray("components")
                        if (compsArray != null) {
                            for (i in 0 until compsArray.length()) {
                                val c = compsArray.getJSONObject(i)
                                viewModel.archRepo.addComponent(
                                    projectId = p.id,
                                    name = c.optString("name", "Module $i"),
                                    style = archJson.optString("architectureStyle", "Clean Architecture"),
                                    layer = c.optString("layer", "Domain"),
                                    responsibilities = c.optString("responsibilities", "Process requests"),
                                    techStack = c.optString("techStack", "Kotlin"),
                                    dependencies = Converters().toStringList(c.optJSONArray("dependencies")?.toString()),
                                    linkedReqIds = approved.map { it.id },
                                    actor = viewModel.currentUser.value?.fullName ?: "Architect"
                                )
                            }
                        }
                        isGeneratingArch = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF27272A),
                contentColor = Color(0xFF8B5CF6)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Components (${components.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Decision Records (${decisions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Architecture Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Button(
                        onClick = { showAddComponentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Component")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (components.isEmpty()) {
                    EmptyStateView(
                        title = "No Components Defined",
                        message = "Synthesize architecture using Gemini or manually define your system layers and services.",
                        icon = Icons.Default.Layers
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(components, key = { it.id }) { comp ->
                            ComponentCard(
                                comp = comp,
                                onClick = { viewModel.navigateTo(Screen.ArchitectureComponentDetail(comp.id)) }
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Architecture Decisions (ADRs)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Button(
                        onClick = { showAddAdrDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record ADR")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (decisions.isEmpty()) {
                    EmptyStateView(
                        title = "No ADRs Recorded",
                        message = "Record significant architecture decisions, rationale, trade-offs, and consequences.",
                        icon = Icons.Default.Gavel
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(decisions, key = { it.id }) { adr ->
                            AdrCard(adr = adr)
                        }
                    }
                }
            }
        }
    }

    // Add Component Dialog
    if (showAddComponentDialog) {
        var compName by remember { mutableStateOf("") }
        var compLayer by remember { mutableStateOf("Domain Logic") }
        var compTech by remember { mutableStateOf("Kotlin, Coroutines") }
        var compResp by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddComponentDialog = false },
            title = { Text("Add Architecture Component") },
            text = {
                Column {
                    OutlinedTextField(value = compName, onValueChange = { compName = it }, label = { Text("Component Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = compLayer, onValueChange = { compLayer = it }, label = { Text("Layer / Boundary") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = compTech, onValueChange = { compTech = it }, label = { Text("Technology Stack") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = compResp, onValueChange = { compResp = it }, label = { Text("Responsibilities") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (compName.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.archRepo.addComponent(
                                    projectId = p.id,
                                    name = compName,
                                    style = "Clean Architecture",
                                    layer = compLayer,
                                    responsibilities = compResp,
                                    techStack = compTech,
                                    dependencies = emptyList(),
                                    linkedReqIds = emptyList(),
                                    actor = viewModel.currentUser.value?.fullName ?: "Architect"
                                )
                                showAddComponentDialog = false
                            }
                        }
                    }
                ) { Text("Save Component") }
            },
            dismissButton = {
                TextButton(onClick = { showAddComponentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add ADR Dialog
    if (showAddAdrDialog) {
        var adrTitle by remember { mutableStateOf("") }
        var adrContext by remember { mutableStateOf("") }
        var adrDecision by remember { mutableStateOf("") }
        var adrConseq by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAdrDialog = false },
            title = { Text("Record Architecture Decision (ADR)") },
            text = {
                Column {
                    OutlinedTextField(value = adrTitle, onValueChange = { adrTitle = it }, label = { Text("Decision Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adrContext, onValueChange = { adrContext = it }, label = { Text("Context & Problem") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adrDecision, onValueChange = { adrDecision = it }, label = { Text("Decision") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adrConseq, onValueChange = { adrConseq = it }, label = { Text("Consequences") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (adrTitle.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.archRepo.addDecision(
                                    projectId = p.id,
                                    title = adrTitle,
                                    context = adrContext,
                                    decision = adrDecision,
                                    consequences = adrConseq,
                                    actor = viewModel.currentUser.value?.fullName ?: "Architect"
                                )
                                showAddAdrDialog = false
                            }
                        }
                    }
                ) { Text("Save ADR") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdrDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ComponentCard(
    comp: ArchitectureComponentEntity,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("component_card_${comp.code}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comp.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(comp.name, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                }
                Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                    Text(comp.layerOrModule, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA78BFA), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(comp.responsibilities, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tech: ${comp.techStack}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                Text("Style: ${comp.style}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
            }
        }
    }
}

@Composable
fun AdrCard(adr: ArchitectureDecisionEntity) {
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
                Text("${adr.code}: ${adr.title}", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                Surface(color = Color(0xFF065F46), shape = RoundedCornerShape(4.dp)) {
                    Text(adr.status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF34D399), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Decision: ${adr.decision}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B5CF6))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Consequences: ${adr.consequences}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmlStudioScreen(viewModel: MainViewModel) {
    var selectedDiagram by remember { mutableStateOf(UmlDiagramType.USE_CASE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UML Studio", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* share/export */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Export Diagram", tint = Color(0xFF8B5CF6))
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
                .padding(16.dp)
        ) {
            // Diagram Type Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UmlDiagramType.entries) { type ->
                    FilterChip(
                        selected = selectedDiagram == type,
                        onClick = { selectedDiagram = type },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedDiagram.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Canvas
            UmlDiagramCanvas(
                diagramType = selectedDiagram,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("uml_canvas_${selectedDiagram.name}")
            )
        }
    }
}
