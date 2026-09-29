package com.theoriongd.reqstrata.ui.screens.team
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.data.local.entity.ProjectMemberEntity
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.*
import com.theoriongd.reqstrata.ui.notifications.NotificationType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(viewModel: MainViewModel, memberId: String) {
    val project by viewModel.currentProject.collectAsState()
    val members by viewModel.projectRepo.getMembers(project?.id ?: "").collectAsState(initial = emptyList())
    val currentRole by viewModel.currentRole.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    val member = members.firstOrNull { it.id == memberId || it.userId == memberId }

    var showRoleDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Team Member Profile", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(project?.name ?: "Member Directory", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .testTag("member_detail_back_btn")
                            .pressScale()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (currentRole == ProjectRole.ADMIN && member != null) {
                        IconButton(
                            onClick = { showRoleDialog = true },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = "Change Role", tint = Color(0xFF8B5CF6))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        if (member == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Member record not found in project context", color = Color(0xFFA1A1AA))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Viewer Persona / Tenant Separation Badge
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(pressedScale = 0.99f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF6D28D9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (activeUser?.fullName ?: "V").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Viewing as: ${activeUser?.fullName ?: "User"} (${currentRole.title})",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Workspace: ${project?.name ?: "Current"} (Tenant Isolated)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Avatar Card
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF6D28D9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.userName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = member.userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = member.userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA1A1AA)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val roleEnum = ProjectRole.fromString(member.role)
                Surface(
                    color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                ) {
                    Text(
                        text = roleEnum.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Specs Card
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(pressedScale = 0.99f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Project Membership Details", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow(label = "Project Role", value = roleEnum.title)
                        DetailRow(label = "Role Responsibility", value = roleEnum.description)
                        DetailRow(label = "Member Since", value = dateFormat.format(Date(member.joinedAt)))
                        DetailRow(label = "Access Status", value = "Active Database Credentials")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions Card
                if (currentRole == ProjectRole.ADMIN) {
                    Surface(
                        color = Color(0xFF27272A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Administrative Operations", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showRoleDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale()
                                    .testTag("change_role_btn")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Modify Project Role")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showRemoveDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale()
                                    .testTag("remove_member_btn")
                            ) {
                                Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove from Project")
                            }
                        }
                    }
                }
            }
        }
    }

    // Role Selection Dialog
    if (showRoleDialog && member != null) {
        var selectedRole by remember { mutableStateOf(ProjectRole.fromString(member.role)) }
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Change Project Role for ${member.userName}") },
            text = {
                Column {
                    ProjectRole.entries.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRole = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedRole == r, onClick = { selectedRole = r })
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
                                member = member,
                                newRole = selectedRole,
                                actor = viewModel.currentUser.value?.fullName ?: "Admin"
                            )
                            viewModel.postNotification(
                                title = "Project Role Updated",
                                message = "${member.userName} is now assigned as ${selectedRole.title}",
                                type = NotificationType.SUCCESS
                            )
                            showRoleDialog = false
                        }
                    },
                    modifier = Modifier.pressScale()
                ) { Text("Confirm Role") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRoleDialog = false },
                    modifier = Modifier.pressScale()
                ) { Text("Cancel") }
            }
        )
    }

    // Remove Confirmation Dialog
    if (showRemoveDialog && member != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Member from Project?") },
            text = { Text("Are you sure you want to remove ${member.userName} from this project? Their assigned tasks will remain preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.projectRepo.removeMember(
                                member = member,
                                actor = viewModel.currentUser.value?.fullName ?: "Admin"
                            )
                            viewModel.postNotification(
                                title = "Member Removed",
                                message = "${member.userName} removed from project.",
                                type = NotificationType.WARNING
                            )
                            showRemoveDialog = false
                            viewModel.navigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                    modifier = Modifier.pressScale()
                ) { Text("Remove", color = Color.White) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false },
                    modifier = Modifier.pressScale()
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
