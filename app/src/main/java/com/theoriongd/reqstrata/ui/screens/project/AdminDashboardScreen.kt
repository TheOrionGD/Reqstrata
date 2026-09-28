package com.theoriongd.reqstrata.ui.screens.project

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.local.entity.*
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.*
import com.theoriongd.reqstrata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()

    val pId = project?.id ?: ""

    // Real Data Flows from Repositories
    val members by viewModel.projectRepo.getMembers(pId).collectAsState(initial = emptyList())
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())
    val components by viewModel.archRepo.getComponents(pId).collectAsState(initial = emptyList())
    val endpoints by viewModel.apiRepo.getEndpoints(pId).collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(pId).collectAsState(initial = emptyList())
    val testCases by viewModel.testRepo.getTestCases(pId).collectAsState(initial = emptyList())
    val activities by viewModel.actRepo.getActivities(pId).collectAsState(initial = emptyList())

    val approvedReqCount = requirements.count { it.status.equals("Approved", ignoreCase = true) }
    val pendingApprovalCount = requirements.count { it.status.equals("Under Review", ignoreCase = true) }
    val failedTestCount = testCases.count { it.status.equals("Failed", ignoreCase = true) }
    val passedTestCount = testCases.count { it.status.equals("Passed", ignoreCase = true) }
    val testCoverage = if (testCases.isNotEmpty()) (passedTestCount * 100 / testCases.size) else 0
    val traceCoverage = if (requirements.isNotEmpty()) {
        val linkedCount = requirements.count { it.approvalNotes.isNotBlank() || components.any { c -> c.linkedRequirementIdsJson.contains(it.id) } }
        (linkedCount * 100 / requirements.size).coerceAtMost(100)
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Admin Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Project Owner / Admin Governance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ProjectSelection) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Project")
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.ProjectSettings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Project Settings")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Role Badge & Welcome Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryPurple.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Administrator"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Full governance active: ${project?.domain ?: "Enterprise"} • ${project?.status ?: "Active"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1b. Automated Tenant Separation & Governance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusSuccess.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentUser?.tenantName ?: "Enterprise Core Workspace",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tenant: ${currentUser?.tenantId ?: "tenant_default"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                color = StatusSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(StatusSuccess, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Automated Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Automated Tenant Separation Active: Isolated project partitions, local Room database encapsulation, and zero system administrator approval delay.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Executive Metric Cards
            item {
                Text(
                    text = "Project Health & Governance Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Requirements",
                        value = "${requirements.size}",
                        subtitle = "$approvedReqCount approved",
                        icon = Icons.Default.ListAlt,
                        color = PrimaryPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.RequirementsList) }
                    )
                    AdminStatCard(
                        title = "Pending Approvals",
                        value = "$pendingApprovalCount",
                        subtitle = if (pendingApprovalCount > 0) "Needs review" else "All cleared",
                        icon = Icons.Default.Verified,
                        color = if (pendingApprovalCount > 0) StatusAmber else StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.ApprovalCenter) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Team Members",
                        value = "${members.size}",
                        subtitle = "Active collaborators",
                        icon = Icons.Default.Group,
                        color = AiPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TeamManagement) }
                    )
                    AdminStatCard(
                        title = "Test Coverage",
                        value = "$testCoverage%",
                        subtitle = "$failedTestCount failed test(s)",
                        icon = Icons.Default.FactCheck,
                        color = if (failedTestCount > 0) StatusError else StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TestingWorkspace) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Architecture",
                        value = "${components.size}",
                        subtitle = "${endpoints.size} API endpoints",
                        icon = Icons.Default.AccountTree,
                        color = PrimaryPurpleLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }
                    )
                    AdminStatCard(
                        title = "Traceability",
                        value = "$traceCoverage%",
                        subtitle = "End-to-end RTM",
                        icon = Icons.Default.Hub,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TraceabilityMatrix) }
                    )
                }
            }

            // 3. Quick Governance Actions
            item {
                Text(
                    text = "Administrative Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        ActionChipButton(
                            title = "Invite Member",
                            icon = Icons.Default.PersonAdd,
                            onClick = { viewModel.navigateTo(Screen.InviteMember) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Approval Center",
                            icon = Icons.Default.CheckCircle,
                            onClick = { viewModel.navigateTo(Screen.ApprovalCenter) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Change Impact",
                            icon = Icons.Default.CompareArrows,
                            onClick = { viewModel.navigateTo(Screen.ChangeImpactDashboard) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Documentation",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = { viewModel.navigateTo(Screen.DocumentViewer) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Activity Log",
                            icon = Icons.Default.History,
                            onClick = { viewModel.navigateTo(Screen.ActivityLog) }
                        )
                    }
                }
            }

            // 4. Pending Approvals Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pending Governance Approvals",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (pendingApprovalCount > 0) {
                        TextButton(onClick = { viewModel.navigateTo(Screen.ApprovalCenter) }) {
                            Text("View All")
                        }
                    }
                }

                if (pendingApprovalCount == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "All requirements and architecture artifacts are approved and up-to-date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val pendingItems = requirements.filter { it.status.equals("Under Review", ignoreCase = true) }.take(3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pendingItems.forEach { req ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo(Screen.RequirementDetail(req.id)) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${req.code}: ${req.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Authored by ${req.authorName} • Priority: ${req.priority}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recent Activity Logs
            item {
                Text(
                    text = "Recent Audit & Activity Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (activities.isEmpty()) {
                    Text(
                        text = "No recorded activity yet in this project.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activities.take(5).forEach { act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${act.actorName}: ${act.action}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = act.details,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.pressScale { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActionChipButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = Modifier.pressScale()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium)
    }
}
