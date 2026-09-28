package com.example.ui.screens.traceability

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.ChangeImpactItem
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceabilityMatrixScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val useCases by viewModel.reqRepo.getUseCases(project?.id ?: "").collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())

    val horizontalScroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traceability Matrix (RTM)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SyncAlt, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Bidirectional Traceability Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Scroll horizontally to audit Requirement → UseCase → Architecture → API → Task → Test Case", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (reqs.isEmpty()) {
                EmptyStateView(
                    title = "No Traceability Data",
                    message = "Add software requirements and design artifacts to populate the traceability matrix.",
                    icon = Icons.Default.SyncAlt
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF27272A), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                        .horizontalScroll(horizontalScroll)
                ) {
                    LazyColumn(modifier = Modifier.width(900.dp)) {
                        // Header Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF18181B), RoundedCornerShape(6.dp))
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Text("Requirement", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), modifier = Modifier.width(160.dp))
                                Text("Use Case", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), modifier = Modifier.width(140.dp))
                                Text("Architecture", fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA), modifier = Modifier.width(150.dp))
                                Text("API Contract", fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.width(150.dp))
                                Text("Task (DEV)", fontWeight = FontWeight.Bold, color = Color(0xFF2DD4BF), modifier = Modifier.width(140.dp))
                                Text("QA Test Case", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), modifier = Modifier.width(160.dp))
                            }
                            HorizontalDivider(color = Color(0xFF3F3F46))
                        }

                        items(reqs, key = { it.id }) { req ->
                            val uc = useCases.firstOrNull { it.requirementId == req.id }
                            val arch = archs.firstOrNull { it.linkedRequirementIdsJson.contains(req.id) } ?: archs.firstOrNull()
                            val api = apis.firstOrNull { it.linkedRequirementId == req.id } ?: apis.firstOrNull()
                            val task = tasks.firstOrNull { it.linkedRequirementId == req.id } ?: tasks.firstOrNull()
                            val test = tests.firstOrNull { it.linkedRequirementId == req.id }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .clickable { viewModel.navigateTo(Screen.RequirementDetail(req.id)) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.width(160.dp)) {
                                    Text(req.code, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    Text(req.title, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA), maxLines = 1)
                                }
                                Text(
                                    text = uc?.code ?: "— None",
                                    color = if (uc != null) Color(0xFF8B5CF6) else Color(0xFFEF4444),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(140.dp)
                                )
                                Text(
                                    text = arch?.code ?: "— None",
                                    color = if (arch != null) Color(0xFFA78BFA) else Color(0xFF71717A),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(150.dp)
                                )
                                Text(
                                    text = api?.let { "${it.method} ${it.path.take(12)}" } ?: "— None",
                                    color = if (api != null) Color(0xFF34D399) else Color(0xFF71717A),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(150.dp)
                                )
                                Text(
                                    text = task?.code ?: "— None",
                                    color = if (task != null) Color(0xFF2DD4BF) else Color(0xFF71717A),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(140.dp)
                                )
                                Text(
                                    text = test?.code ?: "⚠️ Uncovered",
                                    fontWeight = FontWeight.Bold,
                                    color = if (test != null) Color(0xFF10B981) else Color(0xFFEF4444),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                            HorizontalDivider(color = Color(0xFF3F3F46).copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeImpactAnalysisScreen(
    viewModel: MainViewModel,
    reqId: String? = null
) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var selectedReq by remember { mutableStateOf<com.example.data.local.entity.RequirementEntity?>(null) }
    var modifiedDesc by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var impactResults by remember { mutableStateOf<List<ChangeImpactItem>>(emptyList()) }

    LaunchedEffect(reqId, reqs) {
        if (reqId != null) {
            selectedReq = reqs.firstOrNull { it.id == reqId }
            modifiedDesc = selectedReq?.description ?: ""
        } else if (reqs.isNotEmpty() && selectedReq == null) {
            selectedReq = reqs.first()
            modifiedDesc = selectedReq?.description ?: ""
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Impact Analysis", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(scrollState)
        ) {
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("AI Change Impact Engine", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Trace and evaluate dependencies across Use Cases, Architecture, DB, APIs, Tasks, and Tests", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Select Target Requirement:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))

            // Selector row
            LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                items(reqs) { r ->
                    Surface(
                        color = if (selectedReq?.id == r.id) Color(0xFF6D28D9).copy(alpha = 0.3f) else Color(0xFF27272A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedReq?.id == r.id) Color(0xFF8B5CF6) else Color(0xFF3F3F46)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                selectedReq = r
                                modifiedDesc = r.description
                                impactResults = emptyList()
                            }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(r.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), modifier = Modifier.padding(end = 8.dp))
                            Text(r.title, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }

            selectedReq?.let { r ->
                Spacer(modifier = Modifier.height(14.dp))
                Text("Proposed Requirement Modification:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = modifiedDesc,
                    onValueChange = { modifiedDesc = it },
                    label = { Text("Modified Specification Text") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isAnalyzing = true
                        viewModel.viewModelScope.launch {
                            val results = GeminiApiClient.analyzeChangeImpact(
                                reqCode = r.code,
                                reqTitle = r.title,
                                oldDescription = r.description,
                                newDescription = modifiedDesc,
                                linkedArtifactsSummary = "UseCases: UC-001; APIs: /api/v1/auth/login; DB: users; Tasks: DEV-001; Tests: TC-001"
                            )
                            impactResults = results
                            isAnalyzing = false
                        }
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Evaluating Semantic Impact with Gemini...")
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Impact Analysis")
                    }
                }

                if (impactResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Impacted Artifacts Report (${impactResults.size})")

                    impactResults.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                                            Text(item.artifactType, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${item.artifactId}: ${item.artifactTitle}", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Surface(
                                        color = if (item.confidence == "High") Color(0xFF7F1D1D) else Color(0xFF451A03),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("${item.confidence} Risk", style = MaterialTheme.typography.labelSmall, color = if (item.confidence == "High") Color(0xFFFCA5A5) else Color(0xFFFBBF24), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Impact Reason: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Action: ${item.recommendedAction}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
                            }
                        }
                    }
                }
            }
        }
    }
}
