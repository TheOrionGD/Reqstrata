package com.theoriongd.reqstrata.ui.screens.requirements

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.theoriongd.reqstrata.domain.model.RequirementPriority
import com.theoriongd.reqstrata.domain.model.RequirementStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.components.MetricCard
import com.theoriongd.reqstrata.ui.components.PriorityBadge
import com.theoriongd.reqstrata.ui.components.SectionHeader
import com.theoriongd.reqstrata.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessAnalystDashboardScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val activeRole by viewModel.currentRole.collectAsState()

    val totalReqs = reqs.size
    val pendingReview = reqs.count { it.status.equals("Under Review", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true) }
    val approved = reqs.count { it.status.equals("Approved", ignoreCase = true) }
    val rejected = reqs.count { it.status.equals("Rejected", ignoreCase = true) }
    val highPriority = reqs.count { it.priority.equals("Critical", ignoreCase = true) || it.priority.equals("High", ignoreCase = true) }

    // Ambiguity / Quality metrics
    val ambiguousReqs = reqs.filter { req ->
        val desc = req.description.lowercase()
        desc.contains("quick") || desc.contains("fast") || desc.contains("user-friendly") || desc.contains("etc") || desc.contains("easy")
    }
    val nonTestableReqs = reqs.filter { it.acceptanceCriteriaJson.isBlank() || it.acceptanceCriteriaJson == "[]" }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val displayedReqs = remember(reqs, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> reqs.filter { it.status.equals("Under Review", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true) }
            "APPROVED" -> reqs.filter { it.status.equals("Approved", ignoreCase = true) }
            "REJECTED" -> reqs.filter { it.status.equals("Rejected", ignoreCase = true) }
            "AMBIGUOUS" -> ambiguousReqs
            "NON_TESTABLE" -> nonTestableReqs
            "HIGH_PRIORITY" -> reqs.filter { it.priority.equals("Critical", ignoreCase = true) || it.priority.equals("High", ignoreCase = true) }
            else -> reqs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business Analyst Workspace", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(project?.name ?: "Requirements Intelligence", color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ProjectSelection) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Projects", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    com.theoriongd.reqstrata.ui.theme.ThemeToggleIconButton(viewModel = viewModel)
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ProjectAiAssistant) },
                        modifier = Modifier.testTag("ba_ai_assistant_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.UseCases) },
                        modifier = Modifier.testTag("ba_use_cases_btn")
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = "Use Cases", tint = Color(0xFF34D399))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.NewRequirementForm) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Author Requirement", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("ba_create_req_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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

                // Role Pill Banner
                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF10B981), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lead Business Analyst Role", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                            Text("Requirement specification, ambiguity remediation, and use case synthesis", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA7F3D0))
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                SectionHeader(title = "Analyst Quick Actions")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.NewRequirementForm) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("ba_quick_create_req_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Req", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.GenerateRequirements(project?.id)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("ba_quick_gen_reqs_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Elicit", color = Color.Black, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.UseCases) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("ba_quick_usecases_btn")
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use Cases", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Metric Summary Cards
            item {
                SectionHeader(title = "Requirement Quality Health")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Total Reqs",
                        value = "$totalReqs",
                        subtitle = "$approved Approved",
                        icon = Icons.Default.Description,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFilter = "ALL" }
                    )
                    MetricCard(
                        title = "Pending Review",
                        value = "$pendingReview",
                        subtitle = "Needs Sign-off",
                        icon = Icons.Default.HourglassTop,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFilter = "PENDING" }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Ambiguous Reqs",
                        value = "${ambiguousReqs.size}",
                        subtitle = "Vague terms detected",
                        icon = Icons.Default.WarningAmber,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFilter = "AMBIGUOUS" }
                    )
                    MetricCard(
                        title = "Non-Testable",
                        value = "${nonTestableReqs.size}",
                        subtitle = "Missing criteria",
                        icon = Icons.AutoMirrored.Filled.Rule,
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFilter = "NON_TESTABLE" }
                    )
                }
            }

            // Status Filter Chips
            item {
                SectionHeader(title = "Filter Requirements by Health Status")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All ($totalReqs)") }
                    )
                    FilterChip(
                        selected = selectedFilter == "AMBIGUOUS",
                        onClick = { selectedFilter = if (selectedFilter == "AMBIGUOUS") "ALL" else "AMBIGUOUS" },
                        label = { Text("Ambiguous (${ambiguousReqs.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "NON_TESTABLE",
                        onClick = { selectedFilter = if (selectedFilter == "NON_TESTABLE") "ALL" else "NON_TESTABLE" },
                        label = { Text("Non-Testable (${nonTestableReqs.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = if (selectedFilter == "PENDING") "ALL" else "PENDING" },
                        label = { Text("Pending ($pendingReview)") }
                    )
                }
            }

            // Requirement List items
            if (displayedReqs.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Requirements in this Filter",
                        message = "All requirements in this category have been addressed or none exist yet.",
                        icon = Icons.Default.CheckCircle
                    )
                }
            } else {
                items(displayedReqs, key = { it.id }) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.RequirementDetail(req.id)) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = req.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("v${req.version}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                                StatusBadge(status = req.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = req.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    PriorityBadge(priority = req.priority)
                                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = req.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.navigateTo(Screen.RequirementAiAnalysis(req.id)) },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Quality", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
