package com.theoriongd.reqstrata.ui.screens.architecture

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
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.components.SectionHeader

import com.theoriongd.reqstrata.ui.motion.MotionExpandableCard
import com.theoriongd.reqstrata.ui.motion.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureComponentDetailScreen(
    viewModel: MainViewModel,
    componentId: String
) {
    val project by viewModel.currentProject.collectAsState()
    val components by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val comp = components.firstOrNull { it.id == componentId }

    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val dbEntities by viewModel.dbDesignRepo.getEntities(project?.id ?: "").collectAsState(initial = emptyList())
    val tasks by viewModel.taskRepo.getTasks(project?.id ?: "").collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(comp?.code ?: "Component Detail", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(comp?.name ?: "Architecture Module", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("comp_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.UmlStudio) }) {
                        Icon(Icons.Default.AccountTree, contentDescription = "UML Diagram", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        if (comp == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Architecture component not found", color = Color(0xFFA1A1AA))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main Header
                Surface(
                    color = Color(0xFF27272A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(comp.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.titleSmall)
                            Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                                Text(comp.layerOrModule, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA78BFA), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(comp.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                                Text("Style: ${comp.style}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFAFAFA), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                                Text("Tech: ${comp.techStack}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // Responsibilities & Purpose
                MotionExpandableCard(
                    title = "Core Purpose & Responsibilities",
                    icon = Icons.Default.Info
                ) {
                    Text(comp.responsibilities, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                // Dependencies
                val dependencies = Converters().toStringList(comp.dependenciesJson)
                MotionExpandableCard(
                    title = "Module Dependencies (${dependencies.size})",
                    icon = Icons.Default.Extension
                ) {
                    if (dependencies.isEmpty()) {
                        Text("No direct external dependencies declared.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dependencies.forEach { dep ->
                                Surface(
                                    color = Color(0xFF18181B),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF3F3F46))
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Extension, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(dep, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Related REST APIs
                MotionExpandableCard(
                    title = "Associated API Endpoints (${apis.size})",
                    icon = Icons.Default.Http
                ) {
                    if (apis.isEmpty()) {
                        Text("No REST endpoints mapped directly to this component.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                    } else {
                        apis.take(4).forEach { api ->
                            Surface(
                                color = Color(0xFF18181B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .pressScale()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (api.method == "GET") Color(0xFF6D28D9) else Color(0xFF10B981),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(api.method, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(api.path, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Database Entities
                MotionExpandableCard(
                    title = "Underlying Database Entities (${dbEntities.size})",
                    icon = Icons.Default.Storage
                ) {
                    if (dbEntities.isEmpty()) {
                        Text("No database schema entities mapped.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                    } else {
                        dbEntities.take(4).forEach { entity ->
                            Surface(
                                color = Color(0xFF18181B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .pressScale()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(entity.name, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• ${entity.description.take(30)}", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Linked Requirements
                val linkedReqIds = Converters().toStringList(comp.linkedRequirementIdsJson).toSet()
                val linkedReqs = reqs.filter { linkedReqIds.contains(it.id) || linkedReqIds.contains(it.code) }
                MotionExpandableCard(
                    title = "Realizes Requirements (${linkedReqs.size})",
                    icon = Icons.Default.AssignmentTurnedIn
                ) {
                    if (linkedReqs.isEmpty()) {
                        Text("Component realizes system-wide architectural invariants.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                    } else {
                        linkedReqs.forEach { r ->
                            Surface(
                                color = Color(0xFF18181B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .pressScale()
                                    .clickable { viewModel.navigateTo(Screen.RequirementDetail(r.id)) }
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.code, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(r.title, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Linked Implementation Tasks
                MotionExpandableCard(
                    title = "Implementation Tasks (${tasks.size})",
                    icon = Icons.Default.Engineering
                ) {
                    if (tasks.isEmpty()) {
                        Text("No implementation tasks assigned to this module.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                    } else {
                        tasks.take(4).forEach { task ->
                            Surface(
                                color = Color(0xFF18181B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .pressScale()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(task.code, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(task.title, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text(task.status, color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
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
