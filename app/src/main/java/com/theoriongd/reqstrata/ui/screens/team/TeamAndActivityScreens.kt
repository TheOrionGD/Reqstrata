package com.theoriongd.reqstrata.ui.screens.team

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.theoriongd.reqstrata.data.local.entity.NotificationEntity
import com.theoriongd.reqstrata.data.local.entity.ProjectMemberEntity
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.components.RoleBadge
import com.theoriongd.reqstrata.ui.motion.MotionSpec
import com.theoriongd.reqstrata.ui.motion.MotionTransition
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val members by viewModel.projectRepo.getMembers(project?.id ?: "").collectAsState(initial = emptyList())

    var showInviteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team & Role Governance", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.InviteMember) },
                        modifier = Modifier.testTag("team_invite_member_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Invite Member", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.InviteMember) },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                modifier = Modifier.testTag("team_invite_member_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Invite Member")
            }
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.RoleManagement) }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Project-Level Role Governance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Manage project-scoped RBAC authorities and permissions", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Manage Roles", tint = Color(0xFF8B5CF6))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Project Members (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    MemberCard(member = member, viewModel = viewModel)
                }
            }
        }
    }

    if (showInviteDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(ProjectRole.DEVELOPER) }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Project Member") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Project Role:", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
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
                        val p = project ?: return@Button
                        if (name.isNotBlank() && email.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.projectRepo.addMember(
                                    projectId = p.id,
                                    name = name,
                                    email = email,
                                    role = selectedRole,
                                    actor = viewModel.currentUser.value?.fullName ?: "Admin"
                                )
                                showInviteDialog = false
                            }
                        }
                    }
                ) { Text("Invite") }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MemberCard(member: ProjectMemberEntity, viewModel: MainViewModel) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { viewModel.navigateTo(Screen.MemberDetail(member.id)) }
            .testTag("member_card_${member.userEmail}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF6D28D9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(member.userName, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text(member.userEmail, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                }
            }

            Box {
                Surface(
                    color = Color(0xFF18181B),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .pressScale()
                        .clickable { showRoleMenu = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(
                            targetState = member.role,
                            transitionSpec = {
                                MotionTransition.crossfadeEnter() togetherWith MotionTransition.crossfadeExit()
                            },
                            label = "role_change_transition"
                        ) { roleStr ->
                            RoleBadge(role = ProjectRole.fromString(roleStr))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Edit Role", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                DropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    ProjectRole.entries.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.title) },
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.projectRepo.updateMemberRole(
                                        member = member,
                                        newRole = r,
                                        actor = viewModel.currentUser.value?.fullName ?: "Admin"
                                    )
                                    showRoleMenu = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val notifs by viewModel.notifRepo.getNotifications(user?.id ?: "").collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        user?.let { u ->
                            viewModel.viewModelScope.launch {
                                viewModel.notifRepo.markAllAsRead(u.id)
                            }
                        }
                    }) {
                        Text("Mark all read", color = Color(0xFF8B5CF6))
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
        ) {
            if (notifs.isEmpty()) {
                EmptyStateView(
                    title = "All Caught Up",
                    message = "No unread alerts or notifications. You will receive notifications when requirements change or tests fail.",
                    icon = Icons.Default.NotificationsNone
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notifs, key = { it.id }) { notif ->
                        NotificationCard(notif = notif, dateFormat = dateFormat, onClick = {
                            viewModel.viewModelScope.launch {
                                viewModel.notifRepo.markAsRead(notif.id)
                                if (notif.targetType == "REQUIREMENT") {
                                    viewModel.navigateTo(Screen.RequirementDetail(notif.targetId))
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notif: NotificationEntity,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (notif.isRead) Color(0xFF27272A) else Color(0xFF2E1065),
        animationSpec = MotionSpec.standardTween(),
        label = "notif_bg_color"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notif.title, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                Text(dateFormat.format(Date(notif.createdAt)), style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(notif.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val activities by viewModel.actRepo.getActivities(project?.id ?: "").collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Audit Log", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        ) {
            Text("Immutable Lifecycle Event Audit Trail", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))

            if (activities.isEmpty()) {
                EmptyStateView(
                    title = "No Activity Logged Yet",
                    message = "Actions taken in this project will appear here with full actor and timestamp audits.",
                    icon = Icons.Default.History
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activities, key = { it.id }) { act ->
                        Surface(
                            color = Color(0xFF27272A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(act.action, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.titleSmall)
                                    Text(dateFormat.format(Date(act.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(act.details, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Actor: ${act.actorName} • Target: ${act.targetType}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }

    val matchedReqs = reqs.filter { query.isNotBlank() && (it.title.contains(query, true) || it.code.contains(query, true) || it.description.contains(query, true)) }
    val matchedArchs = archs.filter { query.isNotBlank() && (it.name.contains(query, true) || it.code.contains(query, true)) }
    val matchedApis = apis.filter { query.isNotBlank() && (it.path.contains(query, true) || it.description.contains(query, true)) }
    val matchedTasks = tasks.filter { query.isNotBlank() && (it.title.contains(query, true) || it.code.contains(query, true)) }
    val matchedTests = tests.filter { query.isNotBlank() && (it.title.contains(query, true) || it.code.contains(query, true)) }

    val totalMatches = matchedReqs.size + matchedArchs.size + matchedApis.size + matchedTasks.size + matchedTests.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Project Search", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search across all project artifacts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (query.isBlank()) {
                Text("Type to search across Requirements, Architecture, APIs, Tasks, and Tests.", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.bodyMedium)
            } else if (totalMatches == 0) {
                Text("No matching artifacts found for '$query'.", color = Color(0xFFF87171), style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Found $totalMatches artifacts:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(matchedReqs) { r ->
                        SearchResultItem("REQUIREMENT", r.code, r.title) {
                            viewModel.navigateTo(Screen.RequirementDetail(r.id))
                        }
                    }
                    items(matchedArchs) { a ->
                        SearchResultItem("ARCHITECTURE", a.code, a.name) {
                            viewModel.navigateTo(Screen.ArchitectureWorkspace)
                        }
                    }
                    items(matchedApis) { ep ->
                        SearchResultItem("API", "${ep.method} ${ep.path}", ep.description) {
                            viewModel.navigateTo(Screen.ApiDesigner)
                        }
                    }
                    items(matchedTasks) { t ->
                        SearchResultItem("TASK", t.code, t.title) {
                            viewModel.navigateTo(Screen.TaskManagement)
                        }
                    }
                    items(matchedTests) { tc ->
                        SearchResultItem("TEST CASE", tc.code, tc.title) {
                            viewModel.navigateTo(Screen.TestingWorkspace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(type: String, code: String, title: String, onClick: () -> Unit) {
    Surface(
        color = Color(0xFF27272A),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                        Text(type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(code, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFAFAFA), maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
