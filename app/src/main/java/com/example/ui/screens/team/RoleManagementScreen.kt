package com.example.ui.screens.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.entity.ProjectMemberEntity
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.notifications.NotificationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManagementScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val members by viewModel.projectRepo.getMembers(project?.id ?: "").collectAsState(initial = emptyList())
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedMemberForEdit by remember { mutableStateOf<ProjectMemberEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Project Role Governance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Project-Level Role-Based Access Control (RBAC)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("role_mgmt_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Explanatory Banner
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Project-Scoped Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Roles are strictly project-scoped. For example, a user may be a Principal Architect in Project A while acting as a Tester in Project B.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }

            // Role Specifications
            item {
                Text("Role Hierarchy & Authority", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            items(ProjectRole.entries) { role ->
                val assignedCount = members.count { it.role.equals(role.name, ignoreCase = true) }
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(role.title, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.titleSmall)
                            Surface(
                                color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "$assignedCount members",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(role.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            item {
                Text("Assigned Team Members (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            items(members, key = { it.id }) { member ->
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.MemberDetail(member.id)) }
                        .testTag("role_member_${member.userEmail}")
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
                            Text(member.userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.userName, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text(member.userEmail, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                        Surface(
                            color = Color(0xFF18181B),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                if (currentRole == ProjectRole.ADMIN) {
                                    selectedMemberForEdit = member
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ProjectRole.fromString(member.role).title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B5CF6)
                                )
                                if (currentRole == ProjectRole.ADMIN) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Role Reassignment Dialog
    selectedMemberForEdit?.let { targetMember ->
        var newRole by remember { mutableStateOf(ProjectRole.fromString(targetMember.role)) }
        AlertDialog(
            onDismissRequest = { selectedMemberForEdit = null },
            title = { Text("Reassign Project Role") },
            text = {
                Column {
                    Text("Select new authority level for ${targetMember.userName}:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(10.dp))
                    ProjectRole.entries.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newRole = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = newRole == r, onClick = { newRole = r })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(r.title, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(r.description, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.projectRepo.updateMemberRole(
                                member = targetMember,
                                newRole = newRole,
                                actor = viewModel.currentUser.value?.fullName ?: "Admin"
                            )
                            viewModel.postNotification(
                                title = "Role Reassigned",
                                message = "${targetMember.userName} updated to ${newRole.title}",
                                type = NotificationType.SUCCESS
                            )
                            selectedMemberForEdit = null
                        }
                    }
                ) { Text("Save Role") }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForEdit = null }) { Text("Cancel") }
            }
        )
    }
}
