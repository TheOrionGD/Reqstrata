package com.example.ui.screens.project

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSettingsScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val scrollState = rememberScrollState()

    var name by remember(project) { mutableStateOf(project?.name ?: "") }
    var description by remember(project) { mutableStateOf(project?.description ?: "") }
    var domain by remember(project) { mutableStateOf(project?.domain ?: "") }
    var techStack by remember(project) { mutableStateOf(project?.techStack ?: "") }
    var methodology by remember(project) { mutableStateOf(project?.methodology ?: "Agile / Scrum") }
    var status by remember(project) { mutableStateOf(project?.status ?: "Active") }

    var showArchiveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "In Progress", "Review", "Completed", "Archived")

    // Confirmation dialog for Archive
    if (showArchiveConfirmation) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Archive, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archive Project?", color = Color.White)
                }
            },
            text = {
                Text(
                    "Are you sure you want to archive '${project?.name}'? Archived projects become read-only for non-admin team members.",
                    color = Color(0xFFFAFAFA)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.archiveCurrentProject()
                        showArchiveConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.testTag("confirm_archive_project_btn")
                ) {
                    Text("Archive Project", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF27272A)
        )
    }

    // Confirmation dialog for Permanent Delete
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Project Permanently?", color = Color.White)
                }
            },
            text = {
                Text(
                    "This action CANNOT be undone. All requirements, architecture models, test cases, and cloud synchronizations for '${project?.name}' will be destroyed permanently.",
                    color = Color(0xFFFCA5A5)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        project?.let { p ->
                            viewModel.deleteProject(p)
                            showDeleteConfirmation = false
                            viewModel.navigateTo(Screen.ProjectSelection)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_delete_permanent_btn")
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
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
                        Text("Project Settings", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(project?.name ?: "Configuration", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.RoleManagement) },
                        modifier = Modifier.testTag("settings_permissions_btn")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Permissions & Roles", tint = Color(0xFF8B5CF6))
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
            // General Information Section
            SectionHeader(title = "General Configuration")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_project_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Project Description") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_project_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Domain / Industry") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tech & Methodology
            SectionHeader(title = "Engineering Preferences")

            OutlinedTextField(
                value = techStack,
                onValueChange = { techStack = it },
                label = { Text("Technology Preferences") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = methodology,
                onValueChange = { methodology = it },
                label = { Text("Software Methodology") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Project Status
            SectionHeader(title = "Lifecycle Status")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { opt ->
                    val isSelected = status.equals(opt, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { status = opt },
                        label = { Text(opt) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6D28D9),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF27272A),
                            labelColor = Color(0xFFA1A1AA)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Project Governance & Permissions
            SectionHeader(title = "Project Permissions & Roles")

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Project-Level Role Governance", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                        Text("Manage permissions for BA, Architect, Developer, Tester roles", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    }
                    TextButton(onClick = { viewModel.navigateTo(Screen.RoleManagement) }) {
                        Text("Configure", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action
            Button(
                onClick = {
                    viewModel.updateProjectSettings(
                        name = name,
                        description = description,
                        domain = domain,
                        techStack = techStack,
                        methodology = methodology,
                        status = status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_project_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Project Settings", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dangerous Operations Section
            SectionHeader(title = "Dangerous Operations")

            Surface(
                color = Color(0xFF7F1D1D).copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Archive Project", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Make project read-only and hide from active workspace", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFCA5A5))
                        }
                        OutlinedButton(
                            onClick = { showArchiveConfirmation = true },
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                            modifier = Modifier.testTag("archive_project_btn")
                        ) {
                            Text("Archive")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEF4444).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Delete Project Permanently", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Text("Irreversible deletion of all requirements and artifacts", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFCA5A5))
                        }
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.testTag("delete_project_permanent_btn")
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
