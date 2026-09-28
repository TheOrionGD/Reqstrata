package com.example.ui.screens.project

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AiActionCard
import com.example.ui.components.ExportSpecificationDialog
import com.example.ui.components.MetricCard
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionHeader
import com.example.data.remote.mongo.MongoConnectionStatus
import com.example.data.remote.mongo.MongoDbService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectOverviewScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val unreadNotifs by viewModel.notifRepo.getUnreadCount(viewModel.currentUser.value?.id ?: "").collectAsState(initial = 0)
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val mongoStatus by viewModel.mongoStatus.collectAsState()
    val mongoStats by viewModel.mongoStats.collectAsState()
    val syncMetrics by viewModel.backgroundSyncMetrics.collectAsState()

    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val dbs by viewModel.dbDesignRepo.getEntities(project?.id ?: "").collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(project?.id ?: "").collectAsState(initial = emptyList())
    val testSuites by viewModel.testRepo.getSuites(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())
    val executions by viewModel.testRepo.getExecutions(project?.id ?: "").collectAsState(initial = emptyList())

    val totalReqs = reqs.size
    val approvedReqs = reqs.count { it.status.equals("Approved", ignoreCase = true) }
    val underReviewReqs = reqs.count { it.status.equals("Under Review", ignoreCase = true) }
    val passedTests = executions.count { it.status.equals("Passed", ignoreCase = true) }
    val failedTests = executions.count { it.status.equals("Failed", ignoreCase = true) }

    val coveragePercent = if (approvedReqs > 0) {
        val linkedReqIds = tests.map { it.linkedRequirementId }.toSet()
        val covered = reqs.count { it.status.equals("Approved", ignoreCase = true) && linkedReqIds.contains(it.id) }
        ((covered.toFloat() / approvedReqs) * 100).toInt()
    } else 0

    var showRoleMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showExportDialog) {
        ExportSpecificationDialog(
            project = project,
            requirements = reqs,
            components = archs,
            databaseTables = dbs,
            apiEndpoints = apis,
            testSuites = testSuites,
            testCases = tests,
            onDismiss = { showExportDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Project Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${project?.domain} • ${project?.methodology}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ProjectSelection) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Projects", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    // Export Specification button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_spec_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Specification", tint = Color(0xFF8B5CF6))
                    }
                    // Dark Mode / Light Mode toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle_btn")
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF8B5CF6)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ProjectSettings) },
                        modifier = Modifier.testTag("project_settings_action_btn")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Project Settings", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.GlobalSearch) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8B5CF6))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Notifications) }) {
                        BadgedBox(badge = {
                            if (unreadNotifs > 0) {
                                Badge { Text("$unreadNotifs") }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.ActivityLog) }) {
                        Icon(Icons.Default.History, contentDescription = "Activity Audit", tint = MaterialTheme.colorScheme.onSurface)
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
            Spacer(modifier = Modifier.height(8.dp))

            // MongoDB Atlas Cloud Database Status Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MongoDB Atlas Cloud Database",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${MongoDbService.MONGODB_CLUSTER} • ${MongoDbService.DATABASE_NAME}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Surface(
                            color = when (mongoStatus) {
                                MongoConnectionStatus.CONNECTED, MongoConnectionStatus.SYNCED -> Color(0xFF065F46)
                                MongoConnectionStatus.SYNCING -> Color(0xFF854D0E)
                                else -> Color(0xFF7F1D1D)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = when (mongoStatus) {
                                    MongoConnectionStatus.CONNECTED -> "CONNECTED"
                                    MongoConnectionStatus.SYNCING -> "SYNCING..."
                                    MongoConnectionStatus.SYNCED -> "SYNCED"
                                    else -> "ONLINE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "User: ${MongoDbService.MONGODB_USERNAME} | URI from ENV: ${com.example.BuildConfig.MONGODB_URI.take(32)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = if (syncMetrics.isSyncing) Color(0xFFEAB308) else Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (syncMetrics.isSyncing) "Real-time pushing to Atlas..." else "Real-time Sync Active: ${syncMetrics.lastPushedItem}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncCurrentProjectToMongo() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("sync_mongo_btn")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync to Atlas", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.pingMongoCluster() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("ping_mongo_btn")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping Cluster", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Banner & Interactive Role Switcher
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoleMenu = true }
                    .testTag("role_switcher_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleBadge(role = activeRole)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Active Persona: ${activeRole.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = activeRole.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Role", tint = Color(0xFF8B5CF6))
                }

                DropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    ProjectRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(role.title, fontWeight = FontWeight.Bold)
                                    Text(role.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            },
                            onClick = {
                                viewModel.switchRoleForTesting(role)
                                showRoleMenu = false
                            },
                            leadingIcon = {
                                RoleBadge(role = role)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workspace Module Shortcuts
            Text("Engineering Workspaces", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            // Module Navigation Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkspaceShortcutItem(
                    title = "Requirements",
                    count = "$totalReqs",
                    icon = Icons.Default.Description,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.RequirementsList) }
                )
                WorkspaceShortcutItem(
                    title = "Use Cases",
                    count = "${reqs.size}",
                    icon = Icons.Default.AccountTree,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.UseCases) }
                )
                WorkspaceShortcutItem(
                    title = "Architecture",
                    count = "${archs.size}",
                    icon = Icons.Default.Layers,
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkspaceShortcutItem(
                    title = "UML Studio",
                    count = "6 Diagrams",
                    icon = Icons.Default.Brush,
                    color = Color(0xFFF472B6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.UmlStudio) }
                )
                WorkspaceShortcutItem(
                    title = "Database ERD",
                    count = "${dbs.size} Entities",
                    icon = Icons.Default.Storage,
                    color = Color(0xFFFB923C),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.DatabaseDesigner) }
                )
                WorkspaceShortcutItem(
                    title = "API Designer",
                    count = "${apis.size} Endpoints",
                    icon = Icons.Default.Http,
                    color = Color(0xFF34D399),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.ApiDesigner) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkspaceShortcutItem(
                    title = "Dev Tasks",
                    count = "${tasks.size} Tasks",
                    icon = Icons.Default.Code,
                    color = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                )
                WorkspaceShortcutItem(
                    title = "QA Testing",
                    count = "$coveragePercent% Cover",
                    icon = Icons.Default.FactCheck,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                )
                WorkspaceShortcutItem(
                    title = "Traceability",
                    count = "Matrix",
                    icon = Icons.Default.SyncAlt,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TraceabilityMatrix) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Copilot & Impact Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.ChangeImpact()) }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Impact Analysis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Change audit engine", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.ProjectAiAssistant) }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AI Copilot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Contextual assistant", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Architecture & Test Generators Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.SuggestArchitecture(project?.id)) }
                        .testTag("overview_suggest_architecture_card")
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Suggest Architecture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Gemini System Design", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.GenerateTestSuite(project?.id)) }
                        .testTag("overview_generate_test_suite_card")
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Generate Tests", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Unit & Integration Suite", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Metrics Grid
            SectionHeader(title = "Project Health & Traceability")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Requirements",
                    value = "$totalReqs",
                    subtitle = "$approvedReqs Approved • $underReviewReqs Review",
                    icon = Icons.Default.Assignment,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.RequirementsList) }
                )
                MetricCard(
                    title = "Test Coverage",
                    value = "$coveragePercent%",
                    subtitle = "$passedTests Passed • $failedTests Failed",
                    icon = Icons.Default.Shield,
                    color = if (coveragePercent > 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "System Architecture",
                    value = "${archs.size} Components",
                    subtitle = "${apis.size} APIs • ${dbs.size} DB Entities",
                    icon = Icons.Default.Layers,
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }
                )
                MetricCard(
                    title = "Engineering Tasks",
                    value = "${tasks.size} Tasks",
                    subtitle = "${tasks.count { it.status == "Completed" }} Completed",
                    icon = Icons.Default.Engineering,
                    color = Color(0xFF2DD4BF),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions & Governance
            SectionHeader(title = "Governance & Exports")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.DocumentViewer) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SRS & Docs", color = Color.White)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.TeamManagement) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Team & Roles", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WorkspaceShortcutItem(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Text(count, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA), maxLines = 1)
        }
    }
}
