package com.example.ui.screens.project

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.RequirementEntity
import com.example.domain.model.ProjectRole
import com.example.domain.model.RequirementPriority
import com.example.domain.model.RequirementStatus
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.notifications.NotificationType

enum class DashboardTab(val title: String, val testTag: String) {
    ACTIVE("Active", "dashboard_top_tab_active"),
    IN_PROGRESS("In Progress", "dashboard_top_tab_in_progress"),
    COMPLETED("Completed", "dashboard_top_tab_completed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val activeAccounts by viewModel.activeDatabaseAccounts.collectAsState()

    // Live requirement stream
    val reqList by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val mongoStatus by viewModel.mongoStatus.collectAsState()
    val syncMetrics by viewModel.backgroundSyncMetrics.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf<RequirementPriority?>(null) }
    var selectedTopTab by remember { mutableStateOf(DashboardTab.ACTIVE) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }

    // Counts per status category
    val activeCount = remember(reqList) {
        reqList.count {
            val s = it.status.uppercase()
            s in listOf("ACTIVE", "APPROVED", "DRAFT", "UNDER REVIEW", "NEW") && s !in listOf("COMPLETED", "DONE", "IN PROGRESS")
        }
    }
    val inProgressCount = remember(reqList) {
        reqList.count {
            val s = it.status.uppercase()
            s in listOf("IN PROGRESS", "IN_PROGRESS", "IMPLEMENTING", "UNDER REVIEW", "IN REVIEW")
        }
    }
    val completedCount = remember(reqList) {
        reqList.count {
            val s = it.status.uppercase()
            s in listOf("COMPLETED", "DONE", "VERIFIED", "RELEASED", "CLOSED")
        }
    }

    // Filter requirements according to the selected top-level tab, search query, and priority
    val filteredRequirements = remember(reqList, searchQuery, selectedPriorityFilter, selectedTopTab) {
        reqList.filter { req ->
            val matchesSearch = searchQuery.isBlank() ||
                    req.title.contains(searchQuery, ignoreCase = true) ||
                    req.code.contains(searchQuery, ignoreCase = true) ||
                    req.description.contains(searchQuery, ignoreCase = true)

            val matchesPriority = selectedPriorityFilter == null ||
                    req.priority.equals(selectedPriorityFilter?.displayName, ignoreCase = true)

            val s = req.status.uppercase()
            val matchesStatusTab = when (selectedTopTab) {
                DashboardTab.ACTIVE -> s in listOf("ACTIVE", "APPROVED", "DRAFT", "UNDER REVIEW", "NEW") && s !in listOf("COMPLETED", "DONE", "IN PROGRESS")
                DashboardTab.IN_PROGRESS -> s in listOf("IN PROGRESS", "IN_PROGRESS", "IMPLEMENTING", "UNDER REVIEW", "IN REVIEW")
                DashboardTab.COMPLETED -> s in listOf("COMPLETED", "DONE", "VERIFIED", "RELEASED", "CLOSED")
            }

            matchesSearch && matchesPriority && matchesStatusTab
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.name ?: "Project Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Active Role: ${currentRole.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8B5CF6)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF6D28D9).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { showRoleSwitcherDialog = true }
                            ) {
                                Text(
                                    text = "SWITCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // In-app / Phone notification trigger test
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Notifications) },
                        modifier = Modifier.testTag("notifications_icon_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color(0xFFEF4444)) {
                                    Text("Live", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }

                    // Navigation to Project Overview details
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ProjectOverview) },
                        modifier = Modifier.testTag("project_overview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Project Settings & Architecture",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.NewRequirementForm) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Requirement") },
                text = { Text("Add Requirement", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("dashboard_add_req_fab")
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TOP-LEVEL TAB ROW FOR REQUIREMENT STATUS FILTERING ---
            TabRow(
                selectedTabIndex = selectedTopTab.ordinal,
                containerColor = Color(0xFF18181B),
                contentColor = Color(0xFF8B5CF6),
                indicator = { tabPositions ->
                    if (selectedTopTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTopTab.ordinal]),
                            color = Color(0xFF8B5CF6),
                            height = 3.dp
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = Color(0xFF27272A), thickness = 1.dp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_top_tab_row")
            ) {
                DashboardTab.entries.forEach { tab ->
                    val isSelected = selectedTopTab == tab
                    val count = when (tab) {
                        DashboardTab.ACTIVE -> activeCount
                        DashboardTab.IN_PROGRESS -> inProgressCount
                        DashboardTab.COMPLETED -> completedCount
                    }
                    val badgeColor = when (tab) {
                        DashboardTab.ACTIVE -> Color(0xFF10B981)
                        DashboardTab.IN_PROGRESS -> Color(0xFFF59E0B)
                        DashboardTab.COMPLETED -> Color(0xFF8B5CF6)
                    }

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTopTab = tab },
                        modifier = Modifier.testTag(tab.testTag),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isSelected) badgeColor.copy(alpha = 0.25f) else Color(0xFF27272A),
                                    shape = CircleShape,
                                    border = if (isSelected) BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f)) else null
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) badgeColor else Color(0xFF71717A),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // --- 1. HERO METRICS & MONGODB SYNC STATUS ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                DashboardSummaryMetricsHeader(
                    totalCount = reqList.size,
                    activeCount = reqList.count { it.status.equals("Active", true) || it.status.equals("Approved", true) },
                    criticalCount = reqList.count { it.priority.equals("Critical", true) || it.priority.equals("High", true) },
                    isSyncing = syncMetrics.isSyncing,
                    lastSynced = syncMetrics.lastPushedItem,
                    onSyncNow = { viewModel.triggerRealtimeSync() }
                )
            }

            // --- 2. ROLE-BASED QUICK WORKSPACE DOCK ---
            item {
                RoleBasedWorkspaceDock(
                    currentRole = currentRole,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }

            // --- 3. SEARCH & FILTER CONTROLS ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search active specifications, IDs, or keywords...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B5CF6))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFA1A1AA))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF3F3F46),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Priority filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPriorityFilter == null,
                            onClick = { selectedPriorityFilter = null },
                            label = { Text("All Priorities", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6D28D9),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF27272A),
                                labelColor = Color(0xFFA1A1AA)
                            )
                        )
                        RequirementPriority.entries.forEach { priority ->
                            val isSelected = selectedPriorityFilter == priority
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPriorityFilter = if (isSelected) null else priority },
                                label = { Text(priority.displayName, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6D28D9),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF27272A),
                                    labelColor = Color(0xFFA1A1AA)
                                )
                            )
                        }
                    }
                }
            }

            // --- 4. SECTION TITLE ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Requirement Documents (${filteredRequirements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { viewModel.navigateTo(Screen.NewRequirementForm) }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // --- 5. EMPTY STATE ---
            if (filteredRequirements.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        color = Color(0xFF27272A).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF3F3F46))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF3F3F46), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Active Requirements Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create and author your first formal requirement specification with instant MongoDB synchronization.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA1A1AA),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { viewModel.navigateTo(Screen.NewRequirementForm) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Author New Requirement")
                            }
                        }
                    }
                }
            }

            // --- 6. LAZY LIST OF REQUIREMENT SUMMARY CARDS WITH TRANSITIONS ---
            items(
                items = filteredRequirements,
                key = { it.id }
            ) { req ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                            slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    RequirementDocumentSummaryCard(
                        requirement = req,
                        onViewDetail = {
                            viewModel.navigateTo(Screen.RequirementDetail(req.id))
                        },
                        onEdit = {
                            viewModel.navigateTo(Screen.CreateEditRequirement(req.id))
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
        }
    }

    // Role Switcher Dialog (no Firebase Auth required)
    if (showRoleSwitcherDialog) {
        ActiveAccountRoleSwitcherDialog(
            activeAccounts = activeAccounts,
            currentRole = currentRole,
            onSelectAccount = { selectedUser ->
                viewModel.loginWithActiveAccount(selectedUser)
                showRoleSwitcherDialog = false
            },
            onDismiss = { showRoleSwitcherDialog = false }
        )
    }
}

/**
 * Summary metrics banner displaying active document counts and real-time MongoDB status.
 */
@Composable
fun DashboardSummaryMetricsHeader(
    totalCount: Int,
    activeCount: Int,
    criticalCount: Int,
    isSyncing: Boolean,
    lastSynced: String,
    onSyncNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF18181B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF27272A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isSyncing) Color(0xFFF59E0B) else Color(0xFF10B981),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSyncing) "Atlas Syncing in Progress" else "MongoDB Atlas Live",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSyncing) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                }

                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onSyncNow() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Now",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    title = "Total Specs",
                    value = totalCount.toString(),
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = "Active / Appr",
                    value = activeCount.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = "High / Crit",
                    value = criticalCount.toString(),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricPill(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF27272A),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA1A1AA)
            )
        }
    }
}

/**
 * Requirement Document Summary Card designed according to Material 3 guidelines.
 */
@Composable
fun RequirementDocumentSummaryCard(
    requirement: RequirementEntity,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit
) {
    val priorityColor = when (requirement.priority.uppercase()) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF59E0B)
        "MEDIUM" -> Color(0xFF8B5CF6)
        else -> Color(0xFFA1A1AA)
    }

    val statusColor = when (requirement.status.uppercase()) {
        "APPROVED", "ACTIVE" -> Color(0xFF10B981)
        "UNDER REVIEW" -> Color(0xFFF59E0B)
        "DRAFT" -> Color(0xFF71717A)
        else -> Color(0xFFA1A1AA)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onViewDetail() }
            .testTag("req_card_${requirement.code}"),
        color = Color(0xFF27272A),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF27272A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Code, Version, Priority Pill, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF6D28D9).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = requirement.code,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF27272A),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "v${requirement.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF27272A),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = requirement.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority Pill
                    Surface(
                        color = priorityColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = requirement.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status Pill
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = requirement.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = requirement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Detailed Description snippet
            Text(
                text = requirement.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = null,
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = requirement.authorName.ifBlank { "Architect" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF71717A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(
                        onClick = onViewDetail,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("View Specs", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Role-Based Navigation Dock facilitating fast switching between role workspaces.
 */
@Composable
fun RoleBasedWorkspaceDock(
    currentRole: ProjectRole,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF27272A),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF27272A))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Role Workspaces (${currentRole.title})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkspaceTile(
                    title = "Architecture",
                    icon = Icons.Default.Hub,
                    onClick = { onNavigate(Screen.ArchitectureWorkspace) },
                    modifier = Modifier.weight(1f)
                )
                WorkspaceTile(
                    title = "Tasks",
                    icon = Icons.Default.Checklist,
                    onClick = { onNavigate(Screen.TaskManagement) },
                    modifier = Modifier.weight(1f)
                )
                WorkspaceTile(
                    title = "Testing",
                    icon = Icons.Default.FactCheck,
                    onClick = { onNavigate(Screen.TestingWorkspace) },
                    modifier = Modifier.weight(1f)
                )
                WorkspaceTile(
                    title = "Database",
                    icon = Icons.Default.Storage,
                    onClick = { onNavigate(Screen.DatabaseDesigner) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WorkspaceTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color(0xFF27272A),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

/**
 * Role Switcher Dialog for active database accounts (No Firebase Auth needed).
 */
@Composable
fun ActiveAccountRoleSwitcherDialog(
    activeAccounts: List<com.example.data.local.entity.UserEntity>,
    currentRole: ProjectRole,
    onSelectAccount: (com.example.data.local.entity.UserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Switch Active Role Account", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "No Firebase Auth required — authenticated directly against active database records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeAccounts.forEach { account ->
                    val isCurrent = account.titleOrRole.contains(currentRole.name, true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectAccount(account) },
                        color = if (isCurrent) Color(0xFF6D28D9).copy(alpha = 0.25f) else Color(0xFF27272A),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) Color(0xFF6D28D9) else Color(0xFF3F3F46)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(account.avatarColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.fullName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.fullName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${account.titleOrRole} • Active DB Account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF10B981)
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF8B5CF6))
            }
        },
        containerColor = Color(0xFF18181B)
    )
}
