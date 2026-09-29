package com.theoriongd.reqstrata.ui.screens.team
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.local.entity.UserEntity
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.motion.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val scrollState = rememberScrollState()

    // Hierarchical role delegation:
    // ADMIN (Owner) -> can create ARCHITECT, BUSINESS_ANALYST, DEVELOPER, TESTER
    // ARCHITECT -> can create DEVELOPER, TESTER
    // BUSINESS_ANALYST -> can create DEVELOPER, TESTER
    // Others -> cannot create subordinate roles
    val projectRoles = remember(currentRole) {
        when (currentRole) {
            ProjectRole.ADMIN -> listOf(
                ProjectRole.ARCHITECT,
                ProjectRole.BUSINESS_ANALYST,
                ProjectRole.DEVELOPER,
                ProjectRole.TESTER
            )
            ProjectRole.ARCHITECT -> listOf(
                ProjectRole.DEVELOPER,
                ProjectRole.TESTER
            )
            ProjectRole.BUSINESS_ANALYST -> listOf(
                ProjectRole.DEVELOPER,
                ProjectRole.TESTER
            )
            else -> emptyList()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("Welcome@2026") }
    var confirmPassword by remember { mutableStateOf("Welcome@2026") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedRole by remember(projectRoles) {
        mutableStateOf(projectRoles.firstOrNull() ?: ProjectRole.DEVELOPER)
    }

    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdUserCredentials by remember { mutableStateOf<Pair<UserEntity, String>?>(null) }

    fun generatePassword(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#"
        return (1..10).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hierarchical User Provisioning",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Authority Tier: ${currentRole.title} Delegation",
                            color = Color(0xFF8B5CF6),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .testTag("invite_back_btn")
                            .pressScale()
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
            // User Persona & Hierarchy Card
            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF27272A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(pressedScale = 0.99f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF6D28D9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName ?: "I").take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.fullName ?: "Inviting Officer",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = currentRole.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Workspace: ${project?.name ?: "Current Workspace"} (${user?.tenantName ?: "Tenant Isolated"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hierarchical Scope Notice
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when (currentRole) {
                                ProjectRole.ADMIN -> "Project Owner Hierarchy Authority"
                                ProjectRole.ARCHITECT -> "Architect Technical Delegation"
                                ProjectRole.BUSINESS_ANALYST -> "Business Analyst Delivery Delegation"
                                else -> "Read-Only Authority Tier"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = when (currentRole) {
                                ProjectRole.ADMIN -> "As Project Owner, you can provision Tier 2 roles (Architect, BA) and Tier 3 execution roles (Developer, Tester)."
                                ProjectRole.ARCHITECT -> "As Lead Architect, you can provision Tier 3 execution team roles (Developer, QA Tester) for technical delivery."
                                ProjectRole.BUSINESS_ANALYST -> "As Business Analyst, you can provision Tier 3 execution team roles (Developer, QA Tester) for requirement delivery."
                                else -> "Your current role cannot create subordinate users. Only Project Owners, Architects, and BAs possess role delegation authority."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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

            if (projectRoles.isEmpty()) {
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Your role '${currentRole.title}' does not have hierarchical delegation privileges to create users.",
                        color = Color(0xFFA1A1AA),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Input Fields
                Text("Member Full Name *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
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

                Text("Member Email (Username for Login) *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. j.hayes@company.io") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Initial Password *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = {
                        val pass = generatePassword()
                        password = pass
                        confirmPassword = pass
                    }) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Generate", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    placeholder = { Text("min 6 characters") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = Color(0xFFA1A1AA)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Confirm Password *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    placeholder = { Text("Confirm initial password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_confirm_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Role Selection
                Text("Select Subordinate Role in Hierarchy *", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                projectRoles.forEach { role ->
                    val isSelected = selectedRole == role
                    val badgeColor = when (role) {
                        ProjectRole.ADMIN -> Color(0xFF6D28D9)
                        ProjectRole.BUSINESS_ANALYST -> Color(0xFF10B981)
                        ProjectRole.ARCHITECT -> Color(0xFF8B5CF6)
                        ProjectRole.DEVELOPER -> Color(0xFFF59E0B)
                        ProjectRole.TESTER -> Color(0xFFEC4899)
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
                            .pressScale(pressedScale = 0.98f) { selectedRole = role }
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
                        if (name.isBlank()) {
                            errorMessage = "Please enter member's full name."
                            return@Button
                        }
                        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        val proj = project ?: run {
                            errorMessage = "No active project selected."
                            return@Button
                        }

                        isSending = true
                        errorMessage = null

                        viewModel.createHierarchicalUser(
                            name = name,
                            email = email,
                            password = password,
                            role = selectedRole,
                            projectId = proj.id,
                            onSuccess = { createdUser ->
                                isSending = false
                                createdUserCredentials = Pair(createdUser, password)
                                name = ""
                                email = ""
                            },
                            onError = { err ->
                                isSending = false
                                errorMessage = err
                            }
                        )
                    },
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pressScale()
                        .testTag("send_invitation_btn")
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating & Provisioning Role...", color = Color.White)
                    } else {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create & Provision Role (${selectedRole.title})", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Success Credentials Dialog
    if (createdUserCredentials != null) {
        val (createdUser, pass) = createdUserCredentials!!
        AlertDialog(
            onDismissRequest = { createdUserCredentials = null },
            confirmButton = {
                Button(
                    onClick = { createdUserCredentials = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text("Role Provisioned Successfully!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "The user account has been created in the SQLite database and bound to the workspace with immediate active login rights.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF27272A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Name: ${createdUser.fullName}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Username / Email: ${createdUser.email}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                            Text("Password: $pass", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("Assigned Role: ${createdUser.titleOrRole}", style = MaterialTheme.typography.labelSmall)
                            Text("Provisioned By: ${user?.fullName} (${currentRole.title})", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                        }
                    }
                    Text(
                        "The person can now log in immediately using the email and password above.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        )
    }
}
