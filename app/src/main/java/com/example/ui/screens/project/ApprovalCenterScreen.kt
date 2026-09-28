package com.example.ui.screens.project

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.RequirementEntity
import com.example.domain.model.RequirementStatus
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalCenterScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val pId = project?.id ?: ""
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Review", "Approved", "All Governance")

    val pendingList = requirements.filter { it.status.equals("Under Review", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true) }
    val approvedList = requirements.filter { it.status.equals("Approved", ignoreCase = true) }

    val displayedList = when (selectedTab) {
        0 -> pendingList
        1 -> approvedList
        else -> requirements
    }

    var showActionDialog by remember { mutableStateOf<RequirementEntity?>(null) }
    var reviewNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Governance & Approval Center") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            val count = when (index) {
                                0 -> pendingList.size
                                1 -> approvedList.size
                                else -> requirements.size
                            }
                            Text("$title ($count)")
                        }
                    )
                }
            }

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No items under review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("All requirements in this category have been processed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedList, key = { it.id }) { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showActionDialog = req },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${req.code}: ${req.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(req.status) },
                                        leadingIcon = {
                                            val iconColor = when (req.status.lowercase()) {
                                                "approved" -> StatusSuccess
                                                "under review" -> StatusAmber
                                                "rejected" -> StatusError
                                                else -> PrimaryPurple
                                            }
                                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(iconColor))
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = req.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Author: ${req.authorName} • Priority: ${req.priority}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = { showActionDialog = req }) {
                                        Text("Review Decision")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Decision Dialog
        showActionDialog?.let { targetReq ->
            AlertDialog(
                onDismissRequest = { showActionDialog = null },
                title = { Text("Review ${targetReq.code}") },
                text = {
                    Column {
                        Text(text = targetReq.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = targetReq.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = reviewNote,
                            onValueChange = { reviewNote = it },
                            label = { Text("Approval / Rejection Rationale") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateRequirementStatus(targetReq.id, RequirementStatus.APPROVED, reviewNote)
                            showActionDialog = null
                            reviewNote = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                    ) {
                        Text("Approve")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateRequirementStatus(targetReq.id, RequirementStatus.REJECTED, reviewNote)
                            showActionDialog = null
                            reviewNote = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                    ) {
                        Text("Reject")
                    }
                }
            )
        }
    }
}
