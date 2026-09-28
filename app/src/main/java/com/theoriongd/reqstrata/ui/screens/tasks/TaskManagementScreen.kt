package com.theoriongd.reqstrata.ui.screens.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.theoriongd.reqstrata.data.local.entity.TaskEntity
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.domain.model.TaskStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.components.PriorityBadge
import com.theoriongd.reqstrata.ui.components.StatusBadge
import com.theoriongd.reqstrata.ui.motion.AiGenerationMotionContainer
import com.theoriongd.reqstrata.ui.motion.MotionTransition
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val tasks by viewModel.taskRepo.getTasks(project?.id ?: "").collectAsState(initial = emptyList())
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter { task ->
        selectedStatus == null || task.status.equals(selectedStatus, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Task Workspace", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Task", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
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

            // Status filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("All Tasks (${tasks.size})") }
                    )
                }
                items(TaskStatus.entries) { st ->
                    val count = tasks.count { it.status.equals(st.displayName, ignoreCase = true) }
                    FilterChip(
                        selected = selectedStatus == st.displayName,
                        onClick = { selectedStatus = if (selectedStatus == st.displayName) null else st.displayName },
                        label = { Text("${st.displayName} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTasks.isEmpty()) {
                EmptyStateView(
                    title = "No Tasks in Pipeline",
                    message = "Create technical implementation tasks linked to approved requirements and architecture components.",
                    icon = Icons.Default.Engineering,
                    buttonText = "Create Task",
                    onButtonClick = { showAddTaskDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(task = task, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("High") }
        var estHours by remember { mutableIntStateOf(4) }
        var selectedReqId by remember { mutableStateOf(reqs.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Create Development Task") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Implementation Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = estHours.toString(), onValueChange = { estHours = it.toIntOrNull() ?: 4 }, label = { Text("Estimated Effort (Hours)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (title.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.taskRepo.createTask(
                                    projectId = p.id,
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    assigneeName = viewModel.currentUser.value?.fullName ?: "Developer",
                                    linkedReqId = selectedReqId,
                                    linkedCompId = "ARCH-001",
                                    linkedApiId = "API-001",
                                    estHours = estHours,
                                    actor = viewModel.currentUser.value?.fullName ?: "Developer"
                                )
                                showAddTaskDialog = false
                            }
                        }
                    }
                ) { Text("Create Task") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TaskItemCard(task: TaskEntity, viewModel: MainViewModel) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var aiGuidanceText by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = task.priority)
                }

                Box {
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .pressScale()
                            .clickable { showStatusMenu = true }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            AnimatedContent(
                                targetState = task.status,
                                transitionSpec = {
                                    MotionTransition.crossfadeEnter() togetherWith MotionTransition.crossfadeExit()
                                },
                                label = "task_status_transition"
                            ) { st ->
                                StatusBadge(status = st)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change Status", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        TaskStatus.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.displayName) },
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.taskRepo.updateTaskStatus(
                                            task = task,
                                            newStatus = st,
                                            actor = viewModel.currentUser.value?.fullName ?: "Developer"
                                        )
                                        showStatusMenu = false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(task.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Assigned: ${task.assigneeName} • ${task.estimatedHours}h est", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))

                OutlinedButton(
                    onClick = { showAiAssistantDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                    modifier = Modifier.pressScale()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Guidance", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (showAiAssistantDialog) {
        AlertDialog(
            onDismissRequest = { showAiAssistantDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Dev Guidance: ${task.code}")
                }
            },
            text = {
                Column {
                    Text("Ask Gemini for contextual implementation assistance:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                isAiLoading = true
                                viewModel.viewModelScope.launch {
                                    val prompt = "Task: ${task.title}\nDescription: ${task.description}\nSuggest 4 clear technical implementation steps and edge cases in Kotlin."
                                    val res = GeminiApiClient.askProjectAssistant(prompt, "Project Task Context")
                                    aiGuidanceText = res
                                    isAiLoading = false
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Impl Steps", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                isAiLoading = true
                                viewModel.viewModelScope.launch {
                                    val prompt = "Generate sample idiomatic Kotlin code snippet and unit test for: ${task.title}"
                                    val res = GeminiApiClient.askProjectAssistant(prompt, "Project Task Context")
                                    aiGuidanceText = res
                                    isAiLoading = false
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pseudocode", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AiGenerationMotionContainer(
                        isGenerating = isAiLoading,
                        hasResult = aiGuidanceText != null,
                        generationSubtitle = "Synthesizing developer guidance with Gemini...",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        aiGuidanceText?.let { text ->
                            Surface(
                                color = Color(0xFF18181B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiAssistantDialog = false }, modifier = Modifier.pressScale()) { Text("Close") }
            }
        )
    }
}
