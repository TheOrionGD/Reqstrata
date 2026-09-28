package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ProjectAccessPolicy
import com.example.domain.model.ProjectRole
import com.example.domain.model.RoleNavigationItem
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.AiPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

/**
 * Dynamic Role-Specific Navigation Drawer.
 * Renders role-specific workspace navigation destinations based on the user's active project role.
 */
@Composable
fun RoleWorkspaceNavigationDrawer(
    viewModel: MainViewModel,
    drawerState: DrawerState,
    content: @Composable () -> Unit
) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val navItems = remember(activeRole) {
        ProjectAccessPolicy.getNavigationItems(activeRole)
    }

    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Header: Project & User Role Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryPurple.copy(alpha = 0.1f))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (project?.name?.take(1) ?: "P").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project?.name ?: "No Project Selected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = project?.domain ?: "Enterprise AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AssistChip(
                        onClick = {},
                        label = { Text(activeRole.title) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentUser?.fullName ?: "Authenticated User",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Navigation Items
                Spacer(modifier = Modifier.height(8.dp))
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                viewModel.navigateTo(item.screen)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                // Bottom actions
                NavigationDrawerItem(
                    label = { Text("Switch Project") },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            viewModel.navigateTo(Screen.ProjectSelection)
                        }
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        content = content
    )
}
