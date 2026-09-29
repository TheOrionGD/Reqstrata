package com.theoriongd.reqstrata.ui.screens.profile
import androidx.compose.material.icons.automirrored.filled.*

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
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile & Role Identity", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                // User Profile Icon (Matching App Logo)
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.theoriongd.reqstrata.R.drawable.ic_app_logo),
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
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
                        Text("Data Boundary: Encrypted Local Workspace Isolation", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                        Text("Cross-Tenant Access: Strictly Denied & Isolated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            "Cloud & Local Data Synchronization" to true
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
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
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
    val scrollState = rememberScrollState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var aiAutoSaveEnabled by remember { mutableStateOf(false) }
    var highDetailAnalysis by remember { mutableStateOf(true) }
    var highContrastMode by remember { mutableStateOf(false) }
    var largeTouchTargets by remember { mutableStateOf(false) }
    var selectedAnimationPreset by remember { mutableStateOf("Normal (250ms)") }
    var selectedTextScale by remember { mutableStateOf("Standard (100%)") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                .verticalScroll(scrollState)
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

                    // Dynamic Wallpaper Accent Option
                    val isDynamicActive = dynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                    Surface(
                        color = if (isDynamicActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isDynamicActive) 2.dp else 1.dp,
                            if (isDynamicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { viewModel.setDynamicColors(true) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dynamic Wallpaper Accent",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isDynamicActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDynamicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDynamicActive) "Currently Active • Synchronized with Android OS Wallpaper" else "Tap to sync theme with phone wallpaper",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isDynamicActive) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text("Preset Engineering Palettes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppColorPalette.entries.forEach { palette ->
                            val isSelected = !dynamicColors && currentPalette == palette
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
                            Text("Disables non-essential slide and scale transitions for vestibular accessibility", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("Spring compression physics on cards, buttons, and actionable items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = tactileFeedback,
                            onCheckedChange = { viewModel.setTactileFeedback(it) },
                            modifier = Modifier.testTag("settings_tactile_feedback_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Contrast Borders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Strengthens boundary definition around interactive cards and text fields", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = highContrastMode,
                            onCheckedChange = { highContrastMode = it },
                            modifier = Modifier.testTag("settings_high_contrast_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Large Touch Targets", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Enforces 48dp minimum hit targets for improved motor accessibility", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = largeTouchTargets,
                            onCheckedChange = { largeTouchTargets = it },
                            modifier = Modifier.testTag("settings_large_touch_targets_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    Text("Animation Speed Scale", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Fast (150ms)", "Normal (250ms)", "Relaxed (400ms)").forEach { preset ->
                            val isSelected = selectedAnimationPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAnimationPreset = preset },
                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Text Readability Scaling", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Standard (100%)", "Large (115%)", "Extra Large (130%)").forEach { scale ->
                            val isSelected = selectedTextScale == scale
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTextScale = scale },
                                label = { Text(scale, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
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
