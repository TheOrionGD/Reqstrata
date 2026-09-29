package com.theoriongd.reqstrata.ui.screens.requirements
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.theoriongd.reqstrata.data.local.entity.Converters
import com.theoriongd.reqstrata.data.local.entity.RequirementEntity
import com.theoriongd.reqstrata.data.local.entity.RequirementVersionEntity
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.motion.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementComparisonScreen(
    viewModel: MainViewModel,
    reqId: String,
    v1Num: Int = 1,
    v2Num: Int = 2
) {
    val scrollState = rememberScrollState()
    var requirement by remember { mutableStateOf<RequirementEntity?>(null) }
    var versions by remember { mutableStateOf<List<RequirementVersionEntity>>(emptyList()) }

    var selectedLeftVersion by remember { mutableIntStateOf(v1Num) }
    var selectedRightVersion by remember { mutableIntStateOf(v2Num) }

    LaunchedEffect(reqId) {
        val req = viewModel.reqRepo.getRequirementDirect(reqId)
        requirement = req
        val vList = viewModel.reqRepo.getVersions(reqId).firstOrNull() ?: emptyList()
        versions = vList
        if (vList.size >= 2) {
            selectedLeftVersion = vList.getOrNull(vList.size - 2)?.versionNumber ?: 1
            selectedRightVersion = vList.lastOrNull()?.versionNumber ?: (selectedLeftVersion + 1)
        } else if (vList.isNotEmpty()) {
            selectedLeftVersion = vList.first().versionNumber
            selectedRightVersion = req?.version ?: 1
        }
    }

    val leftVersion = versions.firstOrNull { it.versionNumber == selectedLeftVersion }
    val rightVersion = versions.firstOrNull { it.versionNumber == selectedRightVersion }
    val currentReq = requirement

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Version Comparison: ${currentReq?.code ?: reqId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "v$selectedLeftVersion vs v$selectedRightVersion (Audit Diff Analysis)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("diff_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    com.theoriongd.reqstrata.ui.theme.ThemeToggleIconButton(viewModel = viewModel)
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
            // Version Selectors Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Base Version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Version v$selectedLeftVersion",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = "Versus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp).padding(horizontal = 4.dp)
                    )

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Target Version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "Version v$selectedRightVersion",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Change Summary Pill
            Surface(
                color = Color(0xFF6D28D9).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF6D28D9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diff engine detects changes in title, priority, specification narrative, and acceptance criteria.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Title Diff
            DiffSectionHeader(title = "Specification Title")
            DiffComparisonRow(
                leftLabel = "v$selectedLeftVersion",
                leftText = leftVersion?.title ?: currentReq?.title ?: "N/A",
                rightLabel = "v$selectedRightVersion",
                rightText = rightVersion?.title ?: currentReq?.title ?: "N/A"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Priority Diff
            DiffSectionHeader(title = "Priority Ranking")
            DiffComparisonRow(
                leftLabel = "v$selectedLeftVersion",
                leftText = currentReq?.priority ?: "HIGH",
                rightLabel = "v$selectedRightVersion",
                rightText = currentReq?.priority ?: "HIGH"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Specification Narrative Diff
            DiffSectionHeader(title = "Detailed Description Narrative")
            DiffComparisonRow(
                leftLabel = "v$selectedLeftVersion Content",
                leftText = leftVersion?.description ?: currentReq?.description ?: "No previous snapshot",
                rightLabel = "v$selectedRightVersion Content",
                rightText = rightVersion?.description ?: currentReq?.description ?: "Current active version",
                isDescription = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Acceptance Criteria Diff
            val leftCriteria = Converters().toStringList(leftVersion?.acceptanceCriteriaJson ?: "[]")
            val rightCriteria = Converters().toStringList(rightVersion?.acceptanceCriteriaJson ?: currentReq?.acceptanceCriteriaJson ?: "[]")

            DiffSectionHeader(title = "Acceptance Criteria (${leftCriteria.size} vs ${rightCriteria.size})")

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Target Version Statements (v$selectedRightVersion):", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (rightCriteria.isEmpty()) {
                        Text("No criteria defined", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
                    } else {
                        rightCriteria.forEachIndexed { i, c ->
                            val isAdded = !leftCriteria.contains(c)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isAdded) Color(0xFF10B981) else Color(0xFF8B5CF6), androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${i + 1}. $c",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isAdded) Color(0xFF34D399) else Color.White
                                )
                                if (isAdded) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "NEW",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DiffSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun DiffComparisonRow(
    leftLabel: String,
    leftText: String,
    rightLabel: String,
    rightText: String,
    isDescription: Boolean = false
) {
    val isModified = leftText.trim() != rightText.trim()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .expandableContentMotion(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Column (Previous)
        Surface(
            color = if (isModified) Color(0xFF7F1D1D).copy(alpha = 0.2f) else Color(0xFF27272A),
            border = BorderStroke(1.dp, if (isModified) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF3F3F46)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(leftLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = leftText,
                    style = if (isDescription) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                    color = if (isModified) Color(0xFFFCA5A5) else Color.White
                )
            }
        }

        // Right Column (Current / Target)
        Surface(
            color = if (isModified) Color(0xFF064E3B).copy(alpha = 0.2f) else Color(0xFF27272A),
            border = BorderStroke(1.dp, if (isModified) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF3F3F46)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rightLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    if (isModified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(MODIFIED)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rightText,
                    style = if (isDescription) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                    color = if (isModified) Color(0xFF86EFAC) else Color.White
                )
            }
        }
    }
}
