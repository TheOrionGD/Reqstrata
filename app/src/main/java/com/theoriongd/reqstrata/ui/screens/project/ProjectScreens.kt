package com.theoriongd.reqstrata.ui.screens.project
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.data.local.entity.ProjectEntity
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.components.RoleBadge
import com.theoriongd.reqstrata.ui.components.StatusBadge
import com.theoriongd.reqstrata.ui.motion.*
import com.theoriongd.reqstrata.ui.components.background.MobiusSpaceBackground
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectionScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isStrictTenant by viewModel.isTenantFilterStrict.collectAsState()
    val allProjects by viewModel.projectRepo.getAllProjects().collectAsState(initial = emptyList())

    val tenantProjects = remember(allProjects, user, isStrictTenant) {
        val u = user
        if (isStrictTenant && u != null && u.tenantId.isNotBlank()) {
            allProjects.filter { it.tenantId == u.tenantId }
        } else {
            allProjects
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDomainFilter by remember { mutableStateOf<String?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredProjects = tenantProjects.filter { p ->
        val matchesSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.domain.contains(searchQuery, ignoreCase = true)
        val matchesDomain = selectedDomainFilter == null || p.domain.equals(selectedDomainFilter, ignoreCase = true)
        matchesSearch && matchesDomain
    }

    val uniqueDomains = remember(tenantProjects) {
        tenantProjects.map { it.domain }.filter { it.isNotBlank() }.distinct()
    }

    // Confirmation dialog for deleting project from local Room database
    projectToDelete?.let { targetProject ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Project?", color = Color.White)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${targetProject.name}' from your local Room database? This action is permanent and will remove all associated requirements, use cases, and architecture components.",
                    color = Color(0xFFFAFAFA)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(targetProject)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_delete_project_btn")
                ) {
                    Text("Delete Project", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF27272A)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reqstrata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Workspaces & Engineering Projects",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.GenerateRequirements()) },
                        modifier = Modifier.testTag("topbar_ai_gen_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Requirements Generator", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.CreateProject) },
                        modifier = Modifier.testTag("topbar_add_project_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Project", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.CreateProject) },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                modifier = Modifier.testTag("create_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Project")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 3D Mobius space background — visible through gaps between project cards
            MobiusSpaceBackground(
                modifier = Modifier.fillMaxSize(),
                particleCount = 180,
                rotationSpeed = 0.9f,
                intensity = 0.88f
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // User greeting banner
            user?.let { u ->
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(u.avatarColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = u.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back, ${u.fullName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${tenantProjects.size} isolated projects in ${u.tenantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = Color(0xFFA1A1AA))
                        }
                    }
                }

                // Automated Tenant Separation Card
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF064E3B), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = u.tenantName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tenant ID: ${u.tenantId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFF064E3B),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF34D399), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Automated Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Zero Admin Approval • Instant Automated Data Separation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF10B981)
                            )
                            TextButton(
                                onClick = { viewModel.toggleTenantFilterStrict() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    if (isStrictTenant) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isStrictTenant) "Isolated (${tenantProjects.size})" else "All (${allProjects.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Banner: Generate Requirements from Goals with Gemini
            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF6D28D9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Requirement Generator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Input high-level goals & generate structured specs with Gemini",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.GenerateRequirements()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("launch_ai_generator_btn")
                    ) {
                        Text("Generate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search projects by name, domain, or keyword...") },
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
                    .testTag("project_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (uniqueDomains.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedDomainFilter == null,
                            onClick = { selectedDomainFilter = null },
                            label = { Text("All Domains") }
                        )
                    }
                    items(uniqueDomains) { dom ->
                        FilterChip(
                            selected = selectedDomainFilter == dom,
                            onClick = { selectedDomainFilter = if (selectedDomainFilter == dom) null else dom },
                            label = { Text(dom) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Projects (${filteredProjects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { viewModel.navigateTo(Screen.CreateProject) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Project", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                }
            }

            if (filteredProjects.isEmpty()) {
                EmptyStateView(
                    title = if (tenantProjects.isEmpty()) "No Projects in Local Workspace" else "No Matching Projects",
                    message = if (tenantProjects.isEmpty())
                        "Create a software requirement project or generate structured requirements using high-level goals and the Gemini API."
                    else "Try adjusting your search query or domain filter.",
                    icon = Icons.Default.FolderOpen,
                    buttonText = "Create New Project",
                    onButtonClick = { viewModel.navigateTo(Screen.CreateProject) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            viewModel = viewModel,
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        }
        } // end Box (MobiusSpaceBackground + content)
    }
}


@Composable
fun ProjectCard(
    project: ProjectEntity,
    viewModel: MainViewModel,
    onDelete: () -> Unit
) {
    // Collect real stats from database
    val reqs by viewModel.reqRepo.getRequirements(project.id).collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project.id).collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project.id).collectAsState(initial = emptyList())

    val totalReqs = reqs.size
    val approvedReqs = reqs.count { it.status.equals("Approved", ignoreCase = true) }
    val archStatus = if (archs.isNotEmpty()) "Designed (${archs.size})" else "Pending"
    val testCoverage = if (approvedReqs > 0) {
        val coveredReqIds = tests.map { it.linkedRequirementId }.toSet()
        val coveredApproved = reqs.count { it.status.equals("Approved", ignoreCase = true) && coveredReqIds.contains(it.id) }
        ((coveredApproved.toFloat() / approvedReqs) * 100).toInt()
    } else {
        0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale { viewModel.selectProject(project) }
            .testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = project.status)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_project_${project.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Project",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Real stats grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Requirements", style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                    Text("$totalReqs total ($approvedReqs apprv)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Architecture", style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                    Text(archStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                }
                Column {
                    Text("Coverage", style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                    Text("$testCoverage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (testCoverage > 70) Color(0xFF10B981) else Color(0xFFF59E0B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Domain: ${project.domain} • ${project.methodology}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF71717A)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.GenerateRequirements(project.id)) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Gen", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.clickable { viewModel.selectProject(project) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Open Workspace", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("Enterprise SaaS") }
    var projectType by remember { mutableStateOf("Mobile & Cloud Platform") }
    var techStack by remember { mutableStateOf("Android Kotlin, Jetpack Compose, Room, Ktor, Gemini API") }
    var methodology by remember { mutableStateOf("Agile / Scrum") }
    var visibility by remember { mutableStateOf("Private") }

    var isAnalyzingIdea by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember { mutableStateOf<JSONObject?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Project", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                placeholder = { Text("e.g. Healthcare Patient Portal") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_name_input"),
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
                label = { Text("Project Description / Brief") },
                placeholder = { Text("Describe the system requirements, business goals, and target users...") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI Suggestion Action Button
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Project Idea with Gemini", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Automatically discover key actors, architectural modules, initial functional requirements, and risk factors.",
                        color = Color(0xFFA1A1AA),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || description.isBlank()) {
                                name = if (name.isBlank()) "College Management System" else name
                                description = if (description.isBlank()) "Comprehensive platform for student records, automated course enrollment, faculty grading, and fee payment." else description
                            }
                            isAnalyzingIdea = true
                            viewModel.viewModelScope.launch {
                                val result = GeminiApiClient.analyzeProjectIdea(name, description, domain)
                                aiAnalysisResult = result
                                isAnalyzingIdea = false
                            }
                        },
                        enabled = !isAnalyzingIdea,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .pressScale()
                            .testTag("analyze_idea_button")
                    ) {
                        if (isAnalyzingIdea) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyzing with Gemini...")
                        } else {
                            Text("Analyze Idea")
                        }
                    }
                }
            }

            // AI Suggestions Result Box
            AnimatedVisibility(
                visible = aiAnalysisResult != null,
                enter = expandVertically(tween(MotionDuration.STANDARD)) + fadeIn(tween(MotionDuration.STANDARD)),
                exit = shrinkVertically(tween(MotionDuration.STANDARD)) + fadeOut(tween(MotionDuration.STANDARD))
            ) {
                aiAnalysisResult?.let { result ->
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFF18181B),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gemini Architectural Suggestions", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                val actors = result.optJSONArray("actors")
                                if (actors != null && actors.length() > 0) {
                                    Text("Identified Actors:", fontWeight = FontWeight.SemiBold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                                    Text((0 until actors.length()).map { actors.getString(it) }.joinToString(", "), color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                val modules = result.optJSONArray("modules")
                                if (modules != null && modules.length() > 0) {
                                    Text("Major Modules:", fontWeight = FontWeight.SemiBold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                                    Text((0 until modules.length()).map { modules.getString(it) }.joinToString(" • "), color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                val risks = result.optJSONArray("risks")
                                if (risks != null && risks.length() > 0) {
                                    Text("Critical Risks & Constraints:", fontWeight = FontWeight.SemiBold, color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelMedium)
                                    (0 until risks.length()).forEach { idx ->
                                        Text("• ${risks.getString(idx)}", color = Color(0xFFFAFAFA), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Domain / Industry") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = projectType,
                onValueChange = { projectType = it },
                label = { Text("Project Type") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = techStack,
                onValueChange = { techStack = it },
                label = { Text("Preferred Technology Stack") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = methodology,
                onValueChange = { methodology = it },
                label = { Text("Development Methodology") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.createProject(
                        name, description, domain, projectType, techStack, methodology, visibility
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .pressScale()
                    .testTag("submit_create_project_button")
            ) {
                Text("Initialize Project", fontWeight = FontWeight.Bold)
            }
        }
    }
}
