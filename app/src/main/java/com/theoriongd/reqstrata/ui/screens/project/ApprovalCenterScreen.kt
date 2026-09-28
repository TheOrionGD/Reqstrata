package com.theoriongd.reqstrata.ui.screens.project

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
import com.theoriongd.reqstrata.data.local.entity.RequirementEntity
import com.theoriongd.reqstrata.domain.model.RequirementStatus
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.theme.PrimaryPurple
import com.theoriongd.reqstrata.ui.theme.StatusAmber
import com.theoriongd.reqstrata.ui.theme.StatusError
import com.theoriongd.reqstrata.ui.theme.StatusSuccess

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.components.RoleBadge
import com.theoriongd.reqstrata.ui.motion.MotionTransition
import com.theoriongd.reqstrata.ui.motion.ReqstrataTabRow
import com.theoriongd.reqstrata.ui.motion.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalCenterScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val pId = project?.id ?: ""
    val requirements by viewModel.reqRepo.getRequirements(pId).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }

    val pendingList = requirements.filter { it.status.equals("Under Review", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true) }
    val approvedList = requirements.filter { it.status.equals("Approved", ignoreCase = true) }

    val tabs = listOf(
        "Pending Review (${pendingList.size})",
        "Approved (${approvedList.size})",
        "All (${requirements.size})"
    )

    var showActionDialog by remember { mutableStateOf<RequirementEntity?>(null) }
    var reviewNote by remember { mutableStateOf("") }

    val canApprove = currentRole in listOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Governance & Approval Center", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "Reviewer: ${currentUser?.fullName ?: "User"} • ${currentRole.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User Authority Banner
            Surface(
                color = Color(0xFF27272A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleBadge(role = currentRole)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (canApprove) "Authorized Approval Officer" else "Observer / Read-Only Authority",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (canApprove) Color(0xFF34D399) else Color(0xFFA1A1AA)
                        )
                        Text(
                            text = if (canApprove) "Signed reviews are cryptographically recorded in audit logs" else "Only Admin & Lead BA roles have sign-off clearance",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            ReqstrataTabRow(
                selectedTabIndex = selectedTab,
                tabs = tabs,
                onTabSelected = { selectedTab = it }
            )

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    MotionTransition.crossfadeEnter() togetherWith MotionTransition.crossfadeExit()
                },
                label = "approval_tab_content"
            ) { targetIndex ->
                val displayedList = when (targetIndex) {
                    0 -> pendingList
                    1 -> approvedList
                    else -> requirements
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
                            Text("No items under review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("All requirements in this category have been processed.", color = Color(0xFFA1A1AA))
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
                                    .pressScale()
                                    .clickable { if (canApprove) showActionDialog = req else viewModel.navigateTo(Screen.RequirementDetail(req.id)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A))
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
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
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
                                        color = Color(0xFFA1A1AA),
                                        maxLines = 2
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
                                            color = Color(0xFF71717A)
                                        )
                                        if (canApprove) {
                                            TextButton(
                                                onClick = { showActionDialog = req },
                                                modifier = Modifier.pressScale()
                                            ) {
                                                Text("Review Decision", color = Color(0xFF8B5CF6))
                                            }
                                        }
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
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                        modifier = Modifier.pressScale()
                    ) {
                        Text("Approve")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { showActionDialog = null }, modifier = Modifier.pressScale()) {
                            Text("Cancel")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.updateRequirementStatus(targetReq.id, RequirementStatus.REJECTED, reviewNote)
                                showActionDialog = null
                                reviewNote = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                            modifier = Modifier.pressScale()
                        ) {
                            Text("Reject")
                        }
                    }
                }
            )
        }
    }
}
