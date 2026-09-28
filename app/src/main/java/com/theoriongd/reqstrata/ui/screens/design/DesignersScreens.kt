package com.theoriongd.reqstrata.ui.screens.design

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.data.local.entity.ApiEndpointEntity
import com.theoriongd.reqstrata.data.local.entity.DatabaseEntityRecord
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.components.EmptyStateView
import com.theoriongd.reqstrata.ui.motion.MotionCopyButton
import com.theoriongd.reqstrata.ui.motion.MotionSpec
import com.theoriongd.reqstrata.ui.motion.pressScale
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseDesignerScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val entities by viewModel.dbDesignRepo.getEntities(project?.id ?: "").collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Schema & ERD", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Entity", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entity")
            }
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Relational Storage Architecture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Normalized schemas, foreign keys, and indexes", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (entities.isEmpty()) {
                EmptyStateView(
                    title = "No Database Entities",
                    message = "Define database tables or let Gemini suggest normalized relational entities.",
                    icon = Icons.Default.Storage,
                    buttonText = "Add Table / Entity",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(entities, key = { it.id }) { entity ->
                        DatabaseEntityCard(entity = entity)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var entityName by remember { mutableStateOf("") }
        var entityDesc by remember { mutableStateOf("") }
        var fieldName1 by remember { mutableStateOf("id") }
        var fieldType1 by remember { mutableStateOf("UUID (PK)") }
        var fieldName2 by remember { mutableStateOf("name") }
        var fieldType2 by remember { mutableStateOf("VARCHAR(255)") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Database Table") },
            text = {
                Column {
                    OutlinedTextField(value = entityName, onValueChange = { entityName = it }, label = { Text("Table Name (e.g. students)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = entityDesc, onValueChange = { entityDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Initial Columns:", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = fieldName1, onValueChange = { fieldName1 = it }, label = { Text("Column 1") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = fieldName2, onValueChange = { fieldName2 = it }, label = { Text("Column 2") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (entityName.isNotBlank()) {
                            val fields = JSONArray().apply {
                                put(JSONObject().put("name", fieldName1).put("type", fieldType1).put("pk", true))
                                put(JSONObject().put("name", fieldName2).put("type", fieldType2).put("nullable", false))
                            }
                            viewModel.viewModelScope.launch {
                                viewModel.dbDesignRepo.addEntity(
                                    projectId = p.id,
                                    name = entityName,
                                    description = entityDesc,
                                    fieldsJson = fields.toString(),
                                    relationshipsJson = "[]",
                                    actor = viewModel.currentUser.value?.fullName ?: "Architect"
                                )
                                showAddDialog = false
                            }
                        }
                    }
                ) { Text("Save Table") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DatabaseEntityCard(entity: DatabaseEntityRecord) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
            .clickable { expanded = !expanded }
            .animateContentSize(MotionSpec.contentSizeSpec())
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(entity.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(4.dp)) {
                    Text(entity.code, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            if (entity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(entity.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fields table
            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val fieldsArray = try { JSONArray(entity.fieldsJson) } catch (e: Exception) { null }
                    if (fieldsArray != null && fieldsArray.length() > 0) {
                        val countToShow = if (expanded) fieldsArray.length() else minOf(2, fieldsArray.length())
                        for (i in 0 until countToShow) {
                            val f = fieldsArray.getJSONObject(i)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (f.optBoolean("pk", false)) {
                                        Icon(Icons.Default.VpnKey, contentDescription = "Primary Key", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(f.optString("name", "field"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                Text(f.optString("type", "TEXT"), style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                            }
                        }
                        if (fieldsArray.length() > 2) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (expanded) "▲ Show fewer fields" else "▼ Show all (${fieldsArray.length()}) fields",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA78BFA)
                            )
                        }
                    } else {
                        Text("id (UUID PK), created_at (TIMESTAMP)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDesignerScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val endpoints by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Designer", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Endpoint", tint = Color(0xFF8B5CF6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6D28D9),
                contentColor = Color.White,
                modifier = Modifier.pressScale()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Endpoint")
            }
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Http, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("RESTful Endpoints & Schema Contracts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Specification linked directly to software requirements", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (endpoints.isEmpty()) {
                EmptyStateView(
                    title = "No API Endpoints",
                    message = "Design RESTful contracts, payloads, and authorization requirements for client integration.",
                    icon = Icons.Default.Http,
                    buttonText = "Add Endpoint",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(endpoints, key = { it.id }) { ep ->
                        ApiEndpointCard(endpoint = ep)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var method by remember { mutableStateOf("GET") }
        var path by remember { mutableStateOf("/api/v1/") }
        var desc by remember { mutableStateOf("") }
        var authReq by remember { mutableStateOf(true) }
        var selectedReqId by remember { mutableStateOf(reqs.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add API Endpoint") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("GET", "POST", "PUT", "DELETE").forEach { m ->
                            FilterChip(
                                selected = method == m,
                                onClick = { method = m },
                                label = { Text(m) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("Path (e.g. /api/v1/courses)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = authReq, onCheckedChange = { authReq = it })
                        Text("Requires Authentication Token (Bearer)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = project ?: return@Button
                        if (path.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.apiRepo.addEndpoint(
                                    projectId = p.id,
                                    method = method,
                                    path = path,
                                    description = desc,
                                    authRequired = authReq,
                                    reqSchema = "{}",
                                    respSchema = "{}",
                                    linkedReqId = selectedReqId,
                                    actor = viewModel.currentUser.value?.fullName ?: "Architect"
                                )
                                showAddDialog = false
                            }
                        }
                    },
                    modifier = Modifier.pressScale()
                ) { Text("Save Endpoint") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ApiEndpointCard(endpoint: ApiEndpointEntity) {
    val methodColor = when (endpoint.method.uppercase()) {
        "GET" -> Color(0xFF10B981)
        "POST" -> Color(0xFF8B5CF6)
        "PUT" -> Color(0xFFF59E0B)
        "DELETE" -> Color(0xFFEF4444)
        else -> Color(0xFFA78BFA)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = methodColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, methodColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(endpoint.method, fontWeight = FontWeight.Bold, color = methodColor, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(endpoint.path, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    MotionCopyButton(textToCopy = endpoint.path)
                }
                if (endpoint.authRequired) {
                    Icon(Icons.Default.Lock, contentDescription = "Auth Required", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(endpoint.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA1A1AA))

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF18181B),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Request: ${endpoint.requestSchema} • Response: ${endpoint.responseSchema}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF71717A),
                        modifier = Modifier.weight(1f)
                    )
                    MotionCopyButton(textToCopy = "{ \"request\": ${endpoint.requestSchema}, \"response\": ${endpoint.responseSchema} }")
                }
            }
        }
    }
}
