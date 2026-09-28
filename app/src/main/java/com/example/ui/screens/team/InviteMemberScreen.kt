package com.example.ui.screens.team

import androidx.compose.animation.*
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
import com.example.domain.model.ProjectRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.notifications.NotificationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(ProjectRole.DEVELOPER) }
    var invitationSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    val projectRoles = listOf(
        ProjectRole.BUSINESS_ANALYST,
        ProjectRole.ARCHITECT,
        ProjectRole.DEVELOPER,
        ProjectRole.TESTER
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Invite Project Member", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(project?.name ?: "Team Governance", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("invite_back_btn")
                    ) {
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
            // Header Info Card
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Project-Level Role Invitation", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                        Text("Invitees receive access to workspace screens matching their assigned role.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Success Banner
            AnimatedVisibility(visible = invitationSuccess) {
                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Invitation dispatched successfully! Member added to project database.", color = Color(0xFFA7F3D0), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(errorMessage ?: "", color = Color(0xFFFCA5A5), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Input Fields
            Text("Invitee Full Name", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                placeholder = { Text("e.g. Jordan Hayes") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invite_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Invitee Email Address *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = { Text("e.g. j.hayes@company.io") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invite_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Role Selection
            Text("Select Project Role *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            projectRoles.forEach { role ->
                val isSelected = selectedRole == role
                val badgeColor = when (role) {
                    ProjectRole.BUSINESS_ANALYST -> Color(0xFF10B981)
                    ProjectRole.ARCHITECT -> Color(0xFF8B5CF6)
                    ProjectRole.DEVELOPER -> Color(0xFFF59E0B)
                    ProjectRole.TESTER -> Color(0xFFEC4899)
                    else -> Color(0xFF8B5CF6)
                }

                Surface(
                    color = if (isSelected) badgeColor.copy(alpha = 0.15f) else Color(0xFF27272A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) badgeColor else Color(0xFF3F3F46)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedRole = role }
                        .testTag("invite_role_${role.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedRole = role },
                            colors = RadioButtonDefaults.colors(selectedColor = badgeColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(role.title, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFFFAFAFA), style = MaterialTheme.typography.titleSmall)
                            Text(role.description, color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || !email.contains("@")) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }
                    val proj = project ?: return@Button
                    val actor = user?.fullName ?: "Project Admin"
                    isSending = true

                    viewModel.viewModelScope.launch {
                        viewModel.projectRepo.addMember(
                            projectId = proj.id,
                            name = if (name.isBlank()) email.substringBefore("@").replace(".", " ").capitalize() else name,
                            email = email,
                            role = selectedRole,
                            actor = actor
                        )
                        viewModel.postNotification(
                            title = "Team Member Invited",
                            message = "Sent project invitation to $email as ${selectedRole.title}",
                            type = NotificationType.SUCCESS
                        )
                        isSending = false
                        invitationSuccess = true
                        name = ""
                        email = ""
                    }
                },
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_invitation_btn")
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dispatching Invitation...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Project Invitation", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
