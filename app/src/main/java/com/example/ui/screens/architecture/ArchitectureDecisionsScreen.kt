package com.example.ui.screens.architecture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.entity.ArchitectureDecisionEntity
import com.example.ui.MainViewModel
import com.example.ui.notifications.NotificationType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureDecisionsScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val decisions by viewModel.archRepo.getDecisions(project?.id ?: "").collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredDecisions = remember(decisions, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") decisions
        else decisions.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Architecture Decision Records (ADR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(project?.name ?: "System Architecture", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("adr_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Decision Record", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                text = { Text("Log ADR", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_adr_fab")
            )
        },
        containerColor = Color(0xFF09090B)
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
                // Explanatory Card
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Architectural Governance & Trade-offs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "ADRs capture significant architectural decisions along with their context, trade-offs, and consequences, serving as permanent technical memory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "ACCEPTED" to "Accepted", "PROPOSED" to "Proposed", "REJECTED" to "Rejected").forEach { (key, label) ->
                        val isSelected = selectedStatusFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
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

            if (filteredDecisions.isEmpty()) {
                item {
                    Surface(
                        color = Color(0xFF27272A).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Architecture Decisions Logged", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Record technology selections, concurrency patterns, and database choices.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                        }
                    }
                }
            }

            items(filteredDecisions, key = { it.id }) { adr ->
                val statusColor = when (adr.status.uppercase()) {
                    "ACCEPTED" -> Color(0xFF10B981)
                    "PROPOSED" -> Color(0xFFF59E0B)
                    "REJECTED" -> Color(0xFFEF4444)
                    else -> Color(0xFF8B5CF6)
                }

                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier.fillMaxWidth().testTag("adr_card_${adr.title}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(adr.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Surface(
                                color = statusColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = adr.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Context & Problem:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        Text(adr.context, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Decision:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(adr.decision, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Consequences & Trade-offs:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Text(adr.consequences, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(adr.status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                            Text(dateFormat.format(Date(adr.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF71717A))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Add ADR Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var contextText by remember { mutableStateOf("") }
        var decisionText by remember { mutableStateOf("") }
        var consequencesText by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("Accepted") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Architecture Decision Record") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("ADR Title (e.g. ADR-001: Event Sourcing for Ledger)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contextText,
                        onValueChange = { contextText = it },
                        label = { Text("Context & Constraints") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = decisionText,
                        onValueChange = { decisionText = it },
                        label = { Text("Selected Decision") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = consequencesText,
                        onValueChange = { consequencesText = it },
                        label = { Text("Consequences & Trade-offs") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (title.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.archRepo.addDecision(
                                    projectId = p.id,
                                    title = title,
                                    context = contextText,
                                    decision = decisionText,
                                    consequences = consequencesText,
                                    actor = user?.fullName ?: "Architect"
                                )
                                viewModel.postNotification(
                                    title = "Architecture Decision Logged",
                                    message = "Recorded ADR: '$title' in project architecture memory.",
                                    type = NotificationType.SUCCESS
                                )
                                showAddDialog = false
                            }
                        }
                    }
                ) { Text("Save ADR") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
