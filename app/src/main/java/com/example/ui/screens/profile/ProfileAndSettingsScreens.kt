package com.example.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", color = MaterialTheme.colorScheme.onSurface) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            user?.let { u ->
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(u.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(u.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(u.titleOrRole, style = MaterialTheme.typography.labelSmall, color = PrimaryLight)

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Security & Account Credentials", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Authentication: Local Encrypted Hash & JWT Session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Identity Token: Verified", style = MaterialTheme.typography.bodySmall, color = SuccessEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Automated Tenant Separation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                color = Color(0xFF064E3B),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Automated Active",
                                    color = Color(0xFF34D399),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Workspace: ${u.tenantName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Tenant ID: ${u.tenantId}", style = MaterialTheme.typography.bodySmall, color = PrimaryLight)
                        Text("Approval Gate: Zero Admin Approval (Auto-Separated)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                        Text("Isolation: Full Local Room & Mongo Data Boundaries", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Workspace & AI Preferences", color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
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
                                    .androidx.compose.foundation.clickable { viewModel.setColorPalette(palette) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .androidx.compose.ui.draw.clip(CircleShape)
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sync All Artifacts", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { viewModel.pingMongoCluster() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ping Cluster", style = MaterialTheme.typography.labelMedium)
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
        }
    }
}
