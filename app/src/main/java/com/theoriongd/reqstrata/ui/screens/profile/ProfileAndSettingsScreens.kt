package com.theoriongd.reqstrata.ui.screens.profile

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.RoleBadge
import com.theoriongd.reqstrata.ui.components.SectionHeader
import com.theoriongd.reqstrata.ui.motion.MotionExpandableCard
import com.theoriongd.reqstrata.ui.motion.MotionTransition
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val activeAccounts by viewModel.activeDatabaseAccounts.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile & Role Identity", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            user?.let { u ->
                // User Avatar and Primary Details
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(u.avatarColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = u.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(u.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(u.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoleBadge(role = activeRole)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(u.titleOrRole, style = MaterialTheme.typography.labelSmall, color = PrimaryLight, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                // Automated Tenant Separation Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SuccessEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Isolated Workspace Tenant", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                color = Color(0xFF064E3B),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Separated Tenant",
                                    color = Color(0xFF34D399),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Organization / Tenant: ${u.tenantName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Tenant ID: ${u.tenantId}", style = MaterialTheme.typography.bodySmall, color = PrimaryLight)
                        Text("Data Boundary: Independent Room SQLite & MongoDB Collections", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                        Text("Cross-Tenant Access: Strictly Denied & Isolated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Active Account & User Separation Switcher
                MotionExpandableCard(
                    title = "Switch Active Account / Role (${activeAccounts.size})",
                    icon = Icons.Default.SwitchAccount
                ) {
                    Text(
                        text = "Reqstrata maintains isolated user accounts across roles. Switch accounts to experience the system through different engineering perspectives:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    activeAccounts.forEach { account ->
                        val isCurrent = account.id == u.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .pressScale()
                                .clickable { viewModel.loginWithActiveAccount(account) },
                            color = if (isCurrent) Color(0xFF6D28D9).copy(alpha = 0.25f) else Color(0xFF18181B),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) Color(0xFF6D28D9) else Color(0xFF3F3F46)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(account.avatarColor), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = account.fullName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.fullName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${account.titleOrRole} • ${account.email}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                                if (isCurrent) {
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Current Permissions & Role Authority Matrix
                MotionExpandableCard(
                    title = "Role Permissions: ${activeRole.title}",
                    icon = Icons.Default.Shield
                ) {
                    val permissions = when (activeRole) {
                        ProjectRole.ADMIN -> listOf(
                            "Full Project Governance & Policy Administration" to true,
                            "Invite & Manage Team Member Access" to true,
                            "Final Approval Authority for Requirements" to true,
                            "Architecture & Design Studio Authoring" to true,
                            "Direct MongoDB Atlas Real-Time Push" to true
                        )
                        ProjectRole.BUSINESS_ANALYST -> listOf(
                            "Requirements Specification & Version Authoring" to true,
                            "AI Use Case & Acceptance Criteria Generation" to true,
                            "Submit Specifications for Formal Review" to true,
                            "Code Architecture & Schema Modification" to false,
                            "Direct Project Member Removal" to false
                        )
                        ProjectRole.ARCHITECT -> listOf(
                            "Component Hierarchy & UML Diagram Studio" to true,
                            "Database ERD & Entity Schema Designer" to true,
                            "RESTful API Endpoint Contract Authoring" to true,
                            "Architecture Decision Record (ADR) Signing" to true,
                            "User Role & Billing Governance" to false
                        )
                        ProjectRole.DEVELOPER -> listOf(
                            "Sprint Task Execution & Status Updates" to true,
                            "Contextual AI Guidance & Snippet Generation" to true,
                            "View Linked Requirement & API Contracts" to true,
                            "Execute Test Suites & Report Defects" to true,
                            "Requirement Deletion or Rejection" to false
                        )
                        ProjectRole.TESTER -> listOf(
                            "QA Test Suite & Test Case Authoring" to true,
                            "Interactive Test Runner & Verification" to true,
                            "Record System Defects & Blockers" to true,
                            "Change Impact & Traceability Verification" to true,
                            "Architecture Component Modification" to false
                        )
                    }

                    permissions.forEach { (perm, granted) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (granted) Color(0xFF10B981) else Color(0xFF71717A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = perm,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (granted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Security & Session Info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Session & Account Security", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Authentication: Local Encrypted Hash & JWT Session Token", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Device Encryption: Active (Android Keystore Standard)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                    }
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale()
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Workspace & AI Preferences", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale()
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var aiAutoSaveEnabled by remember { mutableStateOf(false) }
    var highDetailAnalysis by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "AI Preferences")

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Deep Architectural Analysis", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Requests full trade-off evaluations from Gemini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = highDetailAnalysis, onCheckedChange = { highDetailAnalysis = it })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Save AI Suggestions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Disabled by default: User must review suggestions before persisting", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = aiAutoSaveEnabled, onCheckedChange = { aiAutoSaveEnabled = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Material 3 Theme & Appearance")

            val isDark by viewModel.isDarkMode.collectAsState()
            val dynamicColors by viewModel.useDynamicColors.collectAsState()
            val currentPalette by viewModel.selectedColorPalette.collectAsState()

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dark Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                if (isDark) "Active: Dark Slate Engineering mode" else "Active: Bright Clean Studio mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dynamic Wallpaper Colors", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Material You system palette on Android 12+", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = dynamicColors,
                            onCheckedChange = { viewModel.toggleDynamicColors() },
                            modifier = Modifier.testTag("settings_dynamic_color_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Text("Accent Color Palette", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppColorPalette.entries.forEach { palette ->
                            val isSelected = currentPalette == palette
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setColorPalette(palette) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) palette.primaryDark else palette.primaryLight)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        palette.displayName.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "MongoDB Atlas Cloud Database")

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "URI: mongodb+srv://...hellotheoriongd.rbxbuxe.mongodb.net",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Authenticated User: godfreytrprof_db_user | Database: requirement2system",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.syncCurrentProjectToMongo() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier
                                .weight(1f)
                                .pressScale()
                        ) {
                            Text("Sync All Artifacts", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { viewModel.pingMongoCluster() },
                            modifier = Modifier
                                .weight(1f)
                                .pressScale()
                        ) {
                            Text("Ping Cluster", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Motion & Accessibility")

            val reduceMotion by viewModel.reduceMotion.collectAsState()
            val tactileFeedback by viewModel.tactileFeedback.collectAsState()

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduce Motion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Disables non-essential slide and scale transitions for accessibility", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) },
                            modifier = Modifier.testTag("settings_reduce_motion_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Interactive Tactile Feedback", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Short spring compression on cards and actionable elements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = tactileFeedback,
                            onCheckedChange = { viewModel.setTactileFeedback(it) },
                            modifier = Modifier.testTag("settings_tactile_feedback_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "In-App Notifications")

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lifecycle Alert Notifications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Notify when requirements update or tests fail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
