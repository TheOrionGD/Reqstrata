package com.theoriongd.reqstrata.ui.screens.project
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.local.entity.ProjectEntity
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.theme.ThemeToggleIconButton

/**
 * Main Dashboard UI displaying a list of active software requirement projects
 * with summary metrics and a floating action button to create a new project.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardProjectsScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()

    // Collect active projects from Room database via repository Flow
    val allProjects by viewModel.projectRepo.getAllProjects().collectAsState(initial = emptyList())
    val activeProjects = remember(allProjects) {
        allProjects.filter { it.status.equals("Active", ignoreCase = true) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf<String?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredActiveProjects = remember(activeProjects, searchQuery, selectedDomain) {
        activeProjects.filter { proj ->
            val matchesSearch = searchQuery.isBlank() ||
                    proj.name.contains(searchQuery, ignoreCase = true) ||
                    proj.description.contains(searchQuery, ignoreCase = true) ||
                    proj.domain.contains(searchQuery, ignoreCase = true)
            val matchesDomain = selectedDomain == null || proj.domain.equals(selectedDomain, ignoreCase = true)
            matchesSearch && matchesDomain
        }
    }

    val domains = remember(activeProjects) {
        activeProjects.map { it.domain }.filter { it.isNotBlank() }.distinct()
    }

    // Confirmation dialog for deletion
    projectToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Project")
                }
            },
            text = {
                Text("Delete '${target.name}' from local Room database? All associated requirements and architecture specifications will be removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(target)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel")
                }
            }
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Active Software Requirement Projects",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Chat-like System Design Copilot button
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ProjectAiAssistant) },
                        modifier = Modifier.testTag("main_dashboard_ai_chat_btn")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "System Design Chat",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // App-wide theme toggle button
                    ThemeToggleIconButton(viewModel = viewModel)

                    // Profile icon
                    IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.CreateProject) },
                icon = { Icon(Icons.Default.Add, contentDescription = "New Project") },
                text = { Text("New Project", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("dashboard_create_project_fab")
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp)
        ) {
            // --- 1. USER & TENANT WORKSPACE HEADER ---
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.fullName?.take(1) ?: "U").uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back, ${user?.fullName ?: "Engineer"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active Role: ${activeRole.title} • ${activeProjects.size} Active Projects",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = Color(0xFF064E3B),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF34D399), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Room DB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. SUMMARY METRICS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricPill(
                        title = "Active Projects",
                        value = activeProjects.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricPill(
                        title = "All Projects",
                        value = allProjects.size.toString(),
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricPill(
                        title = "Active Role",
                        value = activeRole.name.take(5),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- 3. SEARCH & DOMAIN CHIPS ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search active software projects by name, domain, tech stack...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_projects_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (domains.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedDomain == null,
                                onClick = { selectedDomain = null },
                                label = { Text("All Domains (${activeProjects.size})") }
                            )
                        }
                        items(domains) { dom ->
                            val count = activeProjects.count { it.domain == dom }
                            FilterChip(
                                selected = selectedDomain == dom,
                                onClick = { selectedDomain = if (selectedDomain == dom) null else dom },
                                label = { Text("$dom ($count)") }
                            )
                        }
                    }
                }
            }

            // --- 4. SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Projects (${filteredActiveProjects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(onClick = { viewModel.navigateTo(Screen.CreateProject) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Project")
                    }
                }
            }

            // --- 5. EMPTY STATE OR PROJECT CARDS ---
            if (filteredActiveProjects.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (activeProjects.isEmpty()) "No Active Requirement Projects" else "No Projects Match Query",
                        message = if (activeProjects.isEmpty())
                            "You don't have any active software requirement projects yet. Tap 'Create Project' below to initialize one."
                        else "Try adjusting your search criteria or domain filter.",
                        icon = Icons.Default.FolderOpen,
                        buttonText = "Create New Project",
                        onButtonClick = { viewModel.navigateTo(Screen.CreateProject) }
                    )
                }
            } else {
                items(filteredActiveProjects, key = { it.id }) { project ->
                    ActiveProjectCard(
                        project = project,
                        viewModel = viewModel,
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardMetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveProjectCard(
    project: ProjectEntity,
    viewModel: MainViewModel,
    onDelete: () -> Unit
) {
    val reqs by viewModel.reqRepo.getRequirements(project.id).collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project.id).collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project.id).collectAsState(initial = emptyList())

    val totalReqs = reqs.size
    val approvedReqs = reqs.count { it.status.equals("Approved", ignoreCase = true) }
    val criticalReqs = reqs.count { it.priority.equals("Critical", ignoreCase = true) || it.priority.equals("High", ignoreCase = true) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { viewModel.selectProject(project) }
            .testTag("active_project_card_${project.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title, Active Badge, Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${project.domain} • ${project.projectType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Requirements", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalReqs ($approvedReqs Apprv)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Architecture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${archs.size} Components", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Test Cases", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${tests.size} Verified", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.GenerateRequirements(project.id)) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Generator", style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.clickable { viewModel.selectProject(project) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open Workspace",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
