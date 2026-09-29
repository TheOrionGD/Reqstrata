package com.theoriongd.reqstrata.ui.screens.tasks
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.domain.model.TaskStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.MotionSpec
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.screens.project.ActionChipButton
import com.theoriongd.reqstrata.ui.screens.project.AdminStatCard
import com.theoriongd.reqstrata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val pId = project?.id ?: ""
    var bannerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bannerVisible = true }

    // Real Data Flows from Repositories
    val tasks by viewModel.taskRepo.getTasks(pId).collectAsState(initial = emptyList())
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())
    val components by viewModel.archRepo.getComponents(pId).collectAsState(initial = emptyList())
    val endpoints by viewModel.apiRepo.getEndpoints(pId).collectAsState(initial = emptyList())

    val myTasks = tasks.filter {
        it.assigneeId == currentUser?.id || it.assigneeName.equals(currentUser?.fullName, ignoreCase = true)
    }
    val targetTasks = if (myTasks.isNotEmpty()) myTasks else tasks

    val inProgressTasks = tasks.count { it.status.equals("In Progress", ignoreCase = true) }
    val completedTasks = tasks.count { it.status.equals("Completed", ignoreCase = true) }
    val blockedTasks = tasks.count { it.status.equals("Blocked", ignoreCase = true) }
    val highPriorityTasks = tasks.count { it.priority.equals("Critical", ignoreCase = true) || it.priority.equals("High", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Developer Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Software Engineer & Implementation Workspace",
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
                    IconButton(onClick = { viewModel.navigateTo(Screen.DeveloperAiAssistant) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coding Guidance")
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
            // 1. Welcome & Implementation Banner
            item {
                AnimatedVisibility(
                    visible = bannerVisible,
                    enter = fadeIn(MotionSpec.emphasisTween()) + slideInVertically(
                        animationSpec = MotionSpec.emphasisTween(),
                        initialOffsetY = { -it / 3 }
                    )
                ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.1f))
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
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Developer"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tasks.size} total tasks • $inProgressTasks in development • Tech: ${project?.techStack ?: "Modern Stack"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                } // AnimatedVisibility
            }

            // 2. Task Workload & Progress Metrics
            item {
                Text(
                    text = "Development Workload & Backlog",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "In Progress",
                        value = "$inProgressTasks",
                        subtitle = "Active sprint tasks",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        color = PrimaryPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                    )
                    AdminStatCard(
                        title = "Completed",
                        value = "$completedTasks",
                        subtitle = "Finished implementation",
                        icon = Icons.Default.CheckCircle,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(
                        title = "High Priority",
                        value = "$highPriorityTasks",
                        subtitle = "Critical & High items",
                        icon = Icons.Default.PriorityHigh,
                        color = if (highPriorityTasks > 0) StatusError else StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                    )
                    AdminStatCard(
                        title = "Blocked Tasks",
                        value = "$blockedTasks",
                        subtitle = if (blockedTasks > 0) "Needs resolution" else "No impediments",
                        icon = Icons.Default.Block,
                        color = if (blockedTasks > 0) StatusError else StatusSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                    )
                }
            }

            // 3. Quick Actions for Developer
            item {
                Text(
                    text = "Engineering Tools & Context",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        ActionChipButton(
                            title = "AI Coding Guidance",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { viewModel.navigateTo(Screen.DeveloperAiAssistant) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Task Board (Kanban)",
                            icon = Icons.Default.ViewKanban,
                            onClick = { viewModel.navigateTo(Screen.TaskManagement) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "API Specifications",
                            icon = Icons.Default.Api,
                            onClick = { viewModel.navigateTo(Screen.ApiDesigner) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Architecture Modules",
                            icon = Icons.Default.AccountTree,
                            onClick = { viewModel.navigateTo(Screen.ArchitectureWorkspace) }
                        )
                    }
                    item {
                        ActionChipButton(
                            title = "Traceability RTM",
                            icon = Icons.Default.Hub,
                            onClick = { viewModel.navigateTo(Screen.TraceabilityMatrix) }
                        )
                    }
                }
            }

            // 4. Current Tasks List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sprint Tasks & Requirements Context",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.navigateTo(Screen.TaskManagement) }) {
                        Text("View All")
                    }
                }

                if (targetTasks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No tasks found in sprint backlog. Use the Task Board to create development items.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        targetTasks.take(4).forEach { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale()
                                    .clickable { viewModel.navigateTo(Screen.TaskManagement) },
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
                                            text = "${task.code}: ${task.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Status: ${task.status} • Est: ${task.estimatedHours}h • Assignee: ${task.assigneeName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(task.priority) },
                                        modifier = Modifier.height(24.dp)
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
