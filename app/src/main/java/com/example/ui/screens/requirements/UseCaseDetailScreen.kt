package com.example.ui.screens.requirements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.Converters
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ThemeToggleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseCaseDetailScreen(
    useCaseId: String,
    viewModel: MainViewModel
) {
    val project by viewModel.currentProject.collectAsState()
    val useCases by viewModel.reqRepo.getUseCases(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())
    val useCase = useCases.find { it.id == useCaseId }

    val linkedReq by viewModel.reqRepo.getRequirement(useCase?.requirementId ?: "").collectAsState(initial = null)
    val relatedTests = tests.filter { it.linkedRequirementId == useCase?.requirementId }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            useCase?.code ?: "Use Case Detail",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            useCase?.name ?: "System Specification",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("uc_detail_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    ThemeToggleIconButton(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (useCase == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Use case not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header Card (Overview)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                useCase.code,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Actor: ${useCase.actor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            useCase.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Goal: ${useCase.goal}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preconditions & Postconditions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Preconditions",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                useCase.preconditions.ifBlank { "User is authenticated and authorized" },
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Postconditions",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                useCase.postconditions.ifBlank { "System state updated and audit log recorded" },
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Flow
                val mainFlow = Converters().toStringList(useCase.mainFlowJson)
                SectionHeader(title = "Main Operational Flow (${mainFlow.size} Steps)")
                if (mainFlow.isEmpty()) {
                    Text("No main flow steps defined.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    mainFlow.forEachIndexed { idx, step ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${idx + 1}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(step, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Alternative Flows
                val altFlows = Converters().toStringList(useCase.altFlowsJson)
                SectionHeader(title = "Alternative Flows (${altFlows.size})")
                if (altFlows.isEmpty()) {
                    Text("No alternative branch flows defined.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    altFlows.forEachIndexed { idx, alt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Text("A${idx + 1}:", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(alt, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Exceptions
                val excFlows = Converters().toStringList(useCase.exceptionFlowsJson)
                SectionHeader(title = "Exceptions & Error Handling (${excFlows.size})")
                if (excFlows.isEmpty()) {
                    Text("No explicit exception conditions defined.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    excFlows.forEachIndexed { idx, exc ->
                        Surface(
                            color = Color(0xFF7F1D1D).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Text("E${idx + 1}:", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(exc, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Linked Requirement
                SectionHeader(title = "Linked Requirement Document")
                linkedReq?.let { req ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.RequirementDetail(req.id)) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${req.code}: ${req.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                                Text(req.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                } ?: Text("No linked requirement attached.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(18.dp))

                // Related QA Test Cases
                SectionHeader(title = "Associated Test Cases (${relatedTests.size})")
                if (relatedTests.isEmpty()) {
                    Text("No test cases currently verify this use case.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    relatedTests.forEach { test ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${test.code}: ${test.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                    Text("Severity: ${test.severity}", color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                                }
                                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        test.expectedResult.take(24),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
