package com.theoriongd.reqstrata.ui.screens.requirements
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.theoriongd.reqstrata.data.local.entity.Converters
import com.theoriongd.reqstrata.data.local.entity.RequirementEntity
import com.theoriongd.reqstrata.data.local.entity.RequirementVersionEntity
import com.theoriongd.reqstrata.data.local.entity.UseCaseEntity
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.*
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.*
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementsListScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val filteredReqs = reqs.filter { req ->
        val matchesSearch = searchQuery.isBlank() ||
                req.title.contains(searchQuery, ignoreCase = true) ||
                req.code.contains(searchQuery, ignoreCase = true) ||
                req.description.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatus == null || req.status.equals(selectedStatus, ignoreCase = true)
        val matchesType = selectedType == null || req.type.equals(selectedType, ignoreCase = true)
        val matchesPriority = selectedPriority == null || req.priority.equals(selectedPriority, ignoreCase = true)
        matchesSearch && matchesStatus && matchesType && matchesPriority
    }

    if (showExportDialog) {
        ExportSpecificationDialog(
            project = project,
            requirements = reqs,
            onDismiss = { showExportDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requirements Repository", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("requirements_export_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Requirements", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("requirements_dark_mode_btn")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF8B5CF6)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.GenerateRequirements(project?.id)) },
                        modifier = Modifier.testTag("requirements_ai_generate_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Generate from Goals", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SuggestArchitecture(project?.id)) },
                        modifier = Modifier.testTag("requirements_suggest_arch_btn")
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = "Suggest Architecture", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.GenerateTestSuite(project?.id)) },
                        modifier = Modifier.testTag("requirements_generate_tests_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "Generate Test Suite", tint = Color(0xFF10B981))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.CreateEditRequirement()) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Requirement", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.CreateEditRequirement()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_requirement_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Requirement")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by code, title, or keyword...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("requirement_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("All Status") }
                    )
                }
                items(RequirementStatus.entries) { status ->
                    FilterChip(
                        selected = selectedStatus == status.displayName,
                        onClick = { selectedStatus = if (selectedStatus == status.displayName) null else status.displayName },
                        label = { Text(status.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Priority Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedPriority == null,
                        onClick = { selectedPriority = null },
                        label = { Text("All Priorities") }
                    )
                }
                items(RequirementPriority.entries) { prio ->
                    FilterChip(
                        selected = selectedPriority == prio.displayName,
                        onClick = { selectedPriority = if (selectedPriority == prio.displayName) null else prio.displayName },
                        label = { Text(prio.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredReqs.isEmpty()) {
                EmptyStateView(
                    title = "No Requirements Found",
                    message = if (reqs.isEmpty()) "Define your first software requirement to initiate the engineering lifecycle." else "No requirements match your active search and filter criteria.",
                    icon = Icons.Default.Description,
                    buttonText = "Add Requirement",
                    onButtonClick = { viewModel.navigateTo(Screen.CreateEditRequirement()) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredReqs, key = { it.id }) { req ->
                        RequirementItemCard(req = req, onClick = { viewModel.navigateTo(Screen.RequirementDetail(req.id)) })
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementItemCard(
    req: RequirementEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(onClick = onClick)
            .testTag("requirement_item_${req.code}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = req.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "v${req.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA)
                    )
                }
                StatusBadge(status = req.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = req.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = req.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                        Text(req.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    PriorityBadge(priority = req.priority)
                }
                Text(
                    text = "By ${req.authorName.take(15)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF71717A)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditRequirementScreen(
    viewModel: MainViewModel,
    reqId: String? = null
) {
    val project by viewModel.currentProject.collectAsState()
    val isEditMode = reqId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(RequirementType.FUNCTIONAL) }
    var selectedPriority by remember { mutableStateOf(RequirementPriority.HIGH) }
    var criteriaList by remember { mutableStateOf(mutableListOf<String>()) }
    var newCriterionText by remember { mutableStateOf("") }
    var changeReason by remember { mutableStateOf("") }

    var isAnalyzingQuality by remember { mutableStateOf(false) }
    var qualityResult by remember { mutableStateOf<RequirementQuality?>(null) }

    var existingReq by remember { mutableStateOf<RequirementEntity?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(reqId) {
        if (reqId != null) {
            val req = viewModel.reqRepo.getRequirementDirect(reqId)
            if (req != null) {
                existingReq = req
                title = req.title
                description = req.description
                selectedType = RequirementType.entries.firstOrNull { it.displayName == req.type } ?: RequirementType.FUNCTIONAL
                selectedPriority = RequirementPriority.entries.firstOrNull { it.displayName == req.priority } ?: RequirementPriority.HIGH
                criteriaList = Converters().toStringList(req.acceptanceCriteriaJson).toMutableList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Requirement (${existingReq?.code})" else "Author Requirement", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Requirement Title") },
                placeholder = { Text("e.g. Student Course Enrollment Validation") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("req_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Specification Description") },
                placeholder = { Text("The system shall validate prerequisite completion, available class capacity, and student financial clearance before allowing course enrollment...") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("req_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI Quality Analysis Engine trigger
            AiActionCard(
                title = "Run Gemini Quality & Ambiguity Engine",
                description = "Evaluates completeness, detects ambiguous words, verifies testability, and suggests formal acceptance criteria.",
                buttonText = "Analyze with Gemini",
                isLoading = isAnalyzingQuality,
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        title = if (title.isBlank()) "Automated Course Prerequisite Verification" else title
                        description = if (description.isBlank()) "The system should quickly check student prerequisites and approve registration." else description
                    }
                    isAnalyzingQuality = true
                    viewModel.viewModelScope.launch {
                        val quality = GeminiApiClient.analyzeRequirementQuality(title, description, selectedType.displayName)
                        qualityResult = quality
                        isAnalyzingQuality = false
                    }
                }
            )

            // Quality feedback display
            qualityResult?.let { q ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini Quality Assessment", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                            Text("Completeness: ${q.completeness}%", fontWeight = FontWeight.Bold, color = if (q.completeness > 70) Color(0xFF10B981) else Color(0xFFF59E0B))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Ambiguity: ${q.ambiguity}%", style = MaterialTheme.typography.bodySmall, color = if (q.ambiguity < 20) Color(0xFF10B981) else Color(0xFFEF4444))
                            Text("Testability: ${q.testability}%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B5CF6))
                            Text("Consistency: ${q.consistency}%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                        }

                        if (q.issues.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Detected Issues:", fontWeight = FontWeight.SemiBold, color = Color(0xFFFCA5A5), style = MaterialTheme.typography.labelMedium)
                            q.issues.forEach { issue ->
                                Text("• $issue", color = Color(0xFFFAFAFA), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (q.suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Recommendations:", fontWeight = FontWeight.SemiBold, color = Color(0xFF93C5FD), style = MaterialTheme.typography.labelMedium)
                            q.suggestions.forEach { sug ->
                                Text("• $sug", color = Color(0xFFFAFAFA), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type Dropdown Selector
            Text("Requirement Classification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RequirementType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Priority Selector
            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RequirementPriority.entries) { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Acceptance Criteria Builder
            Text("Acceptance Criteria (Given / When / Then)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))

            criteriaList.forEachIndexed { index, criterion ->
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = criterion,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { criteriaList.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCriterionText,
                    onValueChange = { newCriterionText = it },
                    placeholder = { Text("Given student holds prerequisite, When course is open, Then enroll student...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newCriterionText.isNotBlank()) {
                            criteriaList.add(newCriterionText.trim())
                            newCriterionText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Criteria", tint = Color(0xFF8B5CF6), modifier = Modifier.size(32.dp))
                }
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = changeReason,
                    onValueChange = { changeReason = it },
                    label = { Text("Version Change Reason (Required for audit history)") },
                    placeholder = { Text("e.g. Added exception handling and capacity verification") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val user = viewModel.currentUser.value ?: return@Button
                    val p = project ?: return@Button
                    if (title.isBlank() || description.isBlank()) {
                        return@Button
                    }
                    viewModel.viewModelScope.launch {
                        if (isEditMode && existingReq != null) {
                            viewModel.reqRepo.updateRequirement(
                                current = existingReq!!,
                                newTitle = title,
                                newDescription = description,
                                newType = selectedType,
                                newPriority = selectedPriority,
                                newCriteria = criteriaList,
                                changeReason = changeReason,
                                editor = user
                            )
                        } else {
                            val created = viewModel.reqRepo.createRequirement(
                                projectId = p.id,
                                title = title,
                                description = description,
                                type = selectedType,
                                priority = selectedPriority,
                                acceptanceCriteria = criteriaList,
                                author = user
                            )
                            qualityResult?.let { q ->
                                viewModel.reqRepo.saveAiQualityAnalysis(created.id, q)
                            }
                        }
                        viewModel.navigateBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_requirement_button")
            ) {
                Text(if (isEditMode) "Save Version & Submit for Review" else "Save Requirement", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementDetailScreen(
    viewModel: MainViewModel,
    reqId: String
) {
    val req by viewModel.reqRepo.getRequirement(reqId).collectAsState(initial = null)
    val versions by viewModel.reqRepo.getVersions(reqId).collectAsState(initial = emptyList())
    val useCases by viewModel.reqRepo.getUseCasesForReq(reqId).collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases("").collectAsState(initial = emptyList())
    val linkedTests = testCases.filter { it.linkedRequirementId == reqId }

    val user by viewModel.currentUser.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var showApprovalDialog by remember { mutableStateOf(false) }
    var approvalNotes by remember { mutableStateOf("") }
    var approvalDecision by remember { mutableStateOf(RequirementStatus.APPROVED) }

    var isGeneratingUseCase by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(req?.code ?: "Requirement Detail", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.RequirementAiAnalysis(reqId)) },
                        modifier = Modifier.testTag("req_detail_ai_quality_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Quality Analysis", tint = Color(0xFF10B981))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.CreateEditRequirement(reqId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.ChangeImpact(reqId)) }) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Change Impact Analysis", tint = Color(0xFFEF4444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        req?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(r.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF3F3F46),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("v${r.version}", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                        }
                    }
                    StatusBadge(status = r.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(r.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color(0xFF27272A), shape = RoundedCornerShape(6.dp)) {
                        Text(r.type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    PriorityBadge(priority = r.priority)
                    Text("Authored by ${r.authorName}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA), modifier = Modifier.align(Alignment.CenterVertically))
                }

                Spacer(modifier = Modifier.height(16.dp))

                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val detailTabs = listOf(
                    "Overview",
                    "Use Cases (${useCases.size})",
                    "Versions (${versions.size})",
                    "Traceability",
                    "Approval"
                )

                com.theoriongd.reqstrata.ui.motion.ReqstrataTabRow(
                    selectedTabIndex = selectedTabIndex,
                    tabs = detailTabs,
                    onTabSelected = { selectedTabIndex = it },
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideEnter(true) togetherWith
                                com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideExit(true)
                        } else {
                            com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideEnter(false) togetherWith
                                com.theoriongd.reqstrata.ui.motion.MotionTransition.horizontalSlideExit(false)
                        }
                    },
                    label = "req_detail_tab_content"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> {
                            Column {
                                // Description Box
                                Surface(
                                    color = Color(0xFF27272A),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Specification", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(r.description, style = MaterialTheme.typography.bodyMedium, color = Color.White, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Acceptance Criteria
                                val criteria = Converters().toStringList(r.acceptanceCriteriaJson)
                                SectionHeader(title = "Acceptance Criteria (${criteria.size})")
                                if (criteria.isEmpty()) {
                                    Text("No acceptance criteria defined yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                } else {
                                    for ((i, c) in criteria.withIndex()) {
                                        Surface(
                                            color = Color(0xFF27272A),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(modifier = Modifier.padding(10.dp)) {
                                                Text("${i + 1}.", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                                Text(c, color = Color.White, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column {
                                // Use Cases section
                                SectionHeader(
                                    title = "Linked Use Cases (${useCases.size})",
                                    actionText = "Generate Use Case with Gemini",
                                    onActionClick = {
                                        isGeneratingUseCase = true
                                        viewModel.viewModelScope.launch {
                                            val ucJson = GeminiApiClient.generateUseCase(r.code, r.title, r.description)
                                            val mainFlow = Converters().toStringList(ucJson.optJSONArray("mainFlow")?.toString())
                                            val altFlow = Converters().toStringList(ucJson.optJSONArray("altFlows")?.toString())
                                            val excFlow = Converters().toStringList(ucJson.optJSONArray("exceptionFlows")?.toString())

                                            viewModel.reqRepo.addUseCase(
                                                projectId = r.projectId,
                                                reqId = r.id,
                                                name = ucJson.optString("name", "Execute ${r.title}"),
                                                actor = ucJson.optString("actor", "Authorized User"),
                                                goal = ucJson.optString("goal", "Accomplish task"),
                                                preconditions = ucJson.optString("preconditions", "Preconditions verified"),
                                                postconditions = ucJson.optString("postconditions", "Postconditions recorded"),
                                                mainFlow = mainFlow,
                                                altFlows = altFlow,
                                                exceptionFlows = excFlow
                                            )
                                            isGeneratingUseCase = false
                                        }
                                    }
                                )

                                if (isGeneratingUseCase) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF8B5CF6))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (useCases.isEmpty()) {
                                    Text("No linked use cases yet. Click Generate to synthesize with Gemini AI.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                } else {
                                    useCases.forEach { uc ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .pressScale()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${uc.code}: ${uc.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                                    Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                                                        Text(uc.actor, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Goal: ${uc.goal}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column {
                                // Version History
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionHeader(title = "Version History (${versions.size})")
                                    TextButton(onClick = { viewModel.navigateTo(Screen.RequirementComparison(r.id)) }) {
                                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Compare Diff", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                versions.forEach { v ->
                                    Surface(
                                        color = Color(0xFF27272A),
                                        shape = RoundedCornerShape(8.dp),
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
                                                Text("Version ${v.versionNumber}", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                                                Text(dateFormat.format(Date(v.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Changed by: ${v.changedBy}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                            Text("Reason: ${v.changeReason}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column {
                                // Traceability Breadcrumb chain
                                SectionHeader(title = "Traceability Chain")
                                TraceabilityChainView(
                                    reqCode = r.code,
                                    useCaseCode = useCases.firstOrNull()?.code,
                                    archCode = "ARCH-001",
                                    apiCode = "API-001",
                                    taskCode = "DEV-001",
                                    testCode = linkedTests.firstOrNull()?.code
                                )
                            }
                        }
                        4 -> {
                            Column {
                                // Approval Actions (Only Project Owner / Admin can approve/reject)
                                SectionHeader(title = "Governance & Approval")
                                if (role == ProjectRole.ADMIN) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = {
                                                approvalDecision = RequirementStatus.APPROVED
                                                showApprovalDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).pressScale()
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Approve")
                                        }

                                        Button(
                                            onClick = {
                                                approvalDecision = RequirementStatus.REJECTED
                                                showApprovalDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).pressScale()
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reject")
                                        }
                                    }
                                } else {
                                    Text("Current status is '${r.status}'. Only Admin or Project Owner holds governance approval authority.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Approval Dialog
            if (showApprovalDialog) {
                AlertDialog(
                    onDismissRequest = { showApprovalDialog = false },
                    title = { Text("${approvalDecision.displayName} ${r.code}") },
                    text = {
                        Column {
                            Text("Add review assessment notes for the audit log:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = approvalNotes,
                                onValueChange = { approvalNotes = it },
                                placeholder = { Text("e.g. Verified against architectural constraints.") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                user?.let { u ->
                                    viewModel.viewModelScope.launch {
                                        viewModel.reqRepo.setApprovalStatus(
                                            req = r,
                                            status = approvalDecision,
                                            notes = approvalNotes,
                                            reviewer = u
                                        )
                                        showApprovalDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showApprovalDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseCasesScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val useCases by viewModel.reqRepo.getUseCases(project?.id ?: "").collectAsState(initial = emptyList())
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedReqId by remember { mutableStateOf(reqs.firstOrNull()?.id ?: "") }
    var ucName by remember { mutableStateOf("") }
    var ucActor by remember { mutableStateOf("User") }
    var ucGoal by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Use Case") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ucName,
                        onValueChange = { ucName = it },
                        label = { Text("Use Case Name") },
                        placeholder = { Text("e.g. Enroll in Advanced Course") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ucActor,
                        onValueChange = { ucActor = it },
                        label = { Text("Primary Actor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ucGoal,
                        onValueChange = { ucGoal = it },
                        label = { Text("User Goal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (ucName.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.reqRepo.addUseCase(
                                    projectId = p.id,
                                    reqId = selectedReqId.ifBlank { reqs.firstOrNull()?.id ?: "" },
                                    name = ucName,
                                    actor = ucActor,
                                    goal = ucGoal,
                                    preconditions = "User is logged in",
                                    postconditions = "Transaction recorded",
                                    mainFlow = listOf("Actor initiates request", "System validates parameters", "System completes action", "System displays confirmation"),
                                    altFlows = emptyList(),
                                    exceptionFlows = emptyList()
                                )
                                showCreateDialog = false
                            }
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Use Cases Repository", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Use Case", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_usecase_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Use Case")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (useCases.isEmpty()) {
                EmptyStateView(
                    title = "No Use Cases",
                    message = "Create use cases manually or generate them from requirements using Gemini.",
                    icon = Icons.Default.AccountTree,
                    buttonText = "Create Use Case",
                    onButtonClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(useCases, key = { it.id }) { uc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.UseCaseDetail(uc.id)) }
                                .testTag("usecase_card_${uc.code}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(uc.code, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                                        Text(uc.actor, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(uc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Goal: ${uc.goal}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                val mainFlow = Converters().toStringList(uc.mainFlowJson)
                                if (mainFlow.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Main Operational Flow:", fontWeight = FontWeight.SemiBold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                                    for ((i, step) in mainFlow.take(3).withIndex()) {
                                        Text("${i + 1}. $step", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))
                                    }
                                    if (mainFlow.size > 3) {
                                        Text("+ ${mainFlow.size - 3} more steps...", color = Color(0xFF71717A), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
