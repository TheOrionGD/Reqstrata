package com.example.ui.screens.architecture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.project.ActionChipButton
import com.example.ui.screens.project.AdminStatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val pId = project?.id ?: ""

    // Real Data Flows from Repositories
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())
    val components by viewModel.archRepo.getComponents(pId).collectAsState(initial = emptyList())
    val decisions by viewModel.archRepo.getDecisions(pId).collectAsState(initial = emptyList())
    val tables by viewModel.dbDesignRepo.getTables(pId).collectAsState(initial = emptyList())
    val endpoints by viewModel.apiRepo.getEndpoints(pId).collectAsState(initial = emptyList())

    val approvedReqs = requirements.filter { it.status.equals("Approved", ignoreCase = true) }
    val archCompletion = if (requirements.isNotEmpty()) {
        val linkedCount = requirements.count { req ->
            components.any { it.linkedRequirementIdsJson.contains(req.id) }
        }
        (linkedCount * 100 / requirements.size).coerceAtMost(100)
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Architect Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AiPurple)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System Architect Workspace",
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
                    IconButton(onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }) {
                        Icon(Icons.Default.AccountTree, contentDescription = "Architecture Studio")
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
            // 1. Welcome & Architecture Role Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AiPurple.copy(alpha = 0.1f))
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
                                .background(AiPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Architecture, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Architect"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Architecture Completion: $archCompletion% • Stack: ${project?.techStack ?: "Modern Stack"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Technical System Metrics
            item {
                Text(
                    text = "System Design & Artifact Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Architecture Components",
                        value = "${components.size}",
                        subtitle = "Layered modules",
                        icon = Icons.Default.AccountTree,
                        color = PrimaryPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }
                    )
                    AdminStatCard(
                        title = "Approved Requirements",
                        value = "${approvedReqs.size}",
                        subtitle = "Ready for modeling",
                        icon = Icons.Default.CheckCircle,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.RequirementsList) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Database Entities",
                        value = "${tables.size}",
                        subtitle = "Relational tables / docs",
                        icon = Icons.Default.Storage,
                        color = PrimaryPurpleLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.DatabaseDesigner) }
                    )
                    AdminStatCard(
                        title = "API Endpoints",
                        value = "${endpoints.size}",
                        subtitle = "REST specifications",
                        icon = Icons.Default.Api,
                        color = AiPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.ApiDesigner) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "Architecture Decisions",
                        value = "${decisions.size}",
                        subtitle = "Documented ADRs",
                        icon = Icons.Default.Article,
                        color = StatusAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.ArchitectureDecisions) }
                    )
                    AdminStatCard(
                        title = "UML Diagrams",
                        value = "6",
                        subtitle = "Class, Sequence & Comp",
                        icon = Icons.Default.Schema,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.UmlStudio) }
                    )
                }
            }

            // 3. Quick Actions for Architect
            item {
                Text(
                    text = "Architectural Studio Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        ActionChipButton(
                            title = "Suggest Architecture (AI)",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { viewModel.navigateTo(Screen.SuggestArchitecture(pId)) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "UML Studio",
                            icon = Icons.Default.Schema,
                            onClick = { viewModel.navigateTo(Screen.UmlStudio) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Database Designer",
                            icon = Icons.Default.Storage,
                            onClick = { viewModel.navigateTo(Screen.DatabaseDesigner) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "API Designer",
                            icon = Icons.Default.Api,
                            onClick = { viewModel.navigateTo(Screen.ApiDesigner) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "ADR Log",
                            icon = Icons.Default.Article,
                            onClick = { viewModel.navigateTo(Screen.ArchitectureDecisions) }
                        )
                    }
                }
            }

            // 4. Architecture Decisions & Component Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Architectural Decisions (ADR)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.navigateTo(Screen.ArchitectureDecisions) }) {
                        Text("View ADRs")
                    }
                }

                if (decisions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No architecture decisions logged yet. Record an ADR to formalize technical trade-offs.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        decisions.take(3).forEach { adr ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo(Screen.ArchitectureDecisions) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${adr.code}: ${adr.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(adr.status) },
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = adr.decision,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
