package com.theoriongd.reqstrata.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.theoriongd.reqstrata.data.remote.GeminiApiClient
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.components.background.MobiusSpaceBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ChatbotRole(
    val title: String,
    val model: String,
    val systemInstruction: String,
    val iconColor: Color
) {
    ARCHITECT(
        title = "Systems Architect",
        model = GeminiApiClient.MODEL_PRO,
        systemInstruction = "You are a Principal Enterprise Systems Architect. You specialize in software architecture patterns, component design, security threat modeling, API standards, and ADR tradeoff analysis. Use precise architectural terminology.",
        iconColor = Color(0xFF7C3AED)
    ),
    REQUIREMENTS_ANALYST(
        title = "Requirements Analyst",
        model = GeminiApiClient.MODEL_FLASH,
        systemInstruction = "You are a Lead Requirements Engineer and Business Analyst. You focus on functional completeness, user stories, acceptance criteria, testability, and identifying ambiguity or missing edge cases.",
        iconColor = Color(0xFF6D28D9)
    ),
    FAST_QA_BOT(
        title = "Fast QA & Sprint Bot",
        model = GeminiApiClient.MODEL_FLASH_LITE,
        systemInstruction = "You are a Fast QA & Agile Tester. You provide rapid feedback, test assertions, boundary test scenarios, and quick requirement clarifications with ultra-low latency.",
        iconColor = Color(0xFF10B981)
    )
}

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val roleName: String? = null,
    val modelUsed: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAiAssistantScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val reqs by viewModel.reqRepo.getRequirements(project?.id ?: "").collectAsState(initial = emptyList())
    val archs by viewModel.archRepo.getComponents(project?.id ?: "").collectAsState(initial = emptyList())
    val apis by viewModel.apiRepo.getEndpoints(project?.id ?: "").collectAsState(initial = emptyList())
    val tests by viewModel.testRepo.getTestCases(project?.id ?: "").collectAsState(initial = emptyList())

    var selectedRole by remember { mutableStateOf(ChatbotRole.REQUIREMENTS_ANALYST) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Hello! I am your AI Engineering Copilot for '${project?.name ?: "Current Workspace"}'. I can analyze requirements coverage, verify architectural decisions, test readiness, or generate design specifications. Choose a specialized bot role above to guide my focus.",
                roleName = ChatbotRole.REQUIREMENTS_ANALYST.title,
                modelUsed = ChatbotRole.REQUIREMENTS_ANALYST.model
            )
        )
    }

    var inputQuery by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Which requirements lack test coverage?",
        "Summarize the active system architecture",
        "Flag ambiguous requirement wording",
        "List core REST API endpoints and contracts"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(selectedRole.iconColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Engineering Copilot", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${selectedRole.title} • ${selectedRole.model}", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                        }
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
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MobiusSpaceBackground(
                modifier = Modifier.fillMaxSize(),
                particleCount = 140,
                rotationSpeed = 0.75f,
                intensity = 0.5f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
            // Role Switcher
            Text(
                text = "Specialized AI Persona & Model:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA1A1AA)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ChatbotRole.entries) { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role.title) },
                        leadingIcon = if (selectedRole == role) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick queries row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickQuestions) { q ->
                    SuggestionChip(
                        onClick = { inputQuery = q },
                        label = { Text(q, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Conversation Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isSending) {
                    item {
                        Surface(
                            color = Color(0xFF27272A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 64.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF8B5CF6), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${selectedRole.title} thinking with ${selectedRole.model}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask about requirements, APIs, tests...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val q = inputQuery.trim()
                        if (q.isNotBlank()) {
                            messages.add(ChatMessage(isUser = true, text = q))
                            inputQuery = ""
                            isSending = true

                            val currentRoleSnapshot = selectedRole
                            val contextSummary = """
                                Project: ${project?.name}
                                Domain: ${project?.domain}
                                Requirements: ${reqs.joinToString("; ") { "${it.code}: ${it.title} [${it.status}]" }}
                                Architecture: ${archs.joinToString("; ") { "${it.code}: ${it.name} (${it.layerOrModule})" }}
                                APIs: ${apis.joinToString("; ") { "${it.method} ${it.path}" }}
                                Tests: ${tests.joinToString("; ") { "${it.code}: ${it.title}" }}
                            """.trimIndent()

                            viewModel.viewModelScope.launch {
                                val reply = GeminiApiClient.askProjectAssistant(
                                    query = q,
                                    projectContext = contextSummary,
                                    model = currentRoleSnapshot.model,
                                    systemInstruction = currentRoleSnapshot.systemInstruction
                                )
                                messages.add(
                                    ChatMessage(
                                        isUser = false,
                                        text = reply,
                                        roleName = currentRoleSnapshot.title,
                                        modelUsed = currentRoleSnapshot.model
                                    )
                                )
                                isSending = false
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF6D28D9), CircleShape)
                        .testTag("ai_assistant_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
        } // end Box
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            if (!message.isUser && message.roleName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                ) {
                    Text(
                        text = message.roleName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )
                    message.modelUsed?.let { m ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF18181B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = m,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA1A1AA),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                color = if (message.isUser) Color(0xFF6D28D9) else Color(0xFF27272A),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (message.isUser) 14.dp else 2.dp,
                    bottomEnd = if (message.isUser) 2.dp else 14.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        color = if (message.isUser) Color.White.copy(alpha = 0.7f) else Color(0xFF71717A),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
