package com.theoriongd.reqstrata.ui.screens.ai
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.data.remote.gemini.GeminiRetrofitClient
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.theme.ThemeToggleIconButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SystemDesignChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = GeminiRetrofitClient.MODEL_PRO
)

/**
 * Chat-like interface that allows users to send project requirement descriptions
 * to the integrated Gemini API and display the generated system design suggestions in a scrollable view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementSystemDesignChatScreen(viewModel: MainViewModel) {
    val project by viewModel.currentProject.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputDescription by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            SystemDesignChatMessage(
                isUser = false,
                text = "Welcome to the Reqstrata Design Studio! Send any software requirement description or user story below. I will analyze your requirements and synthesize comprehensive system design suggestions including architectural style, modular components, database schemas, and RESTful API specifications."
            )
        )
    }

    val requirementTemplates = listOf(
        "Secure User Authentication with OAuth2, JWT & Refresh Tokens",
        "High-Concurrency Order Processing with Inventory Reservation",
        "Real-Time Collaborative Document Editing with Offline Sync",
        "Financial Payment Gateway Reconciliation with Audit Logging"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF6D28D9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "System Design Copilot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${project?.name ?: "All Projects"} • Gemini 3.1 Pro & Retrofit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Requirement Quick Prompt Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Quick Requirement Starters:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(requirementTemplates) { template ->
                        SuggestionChip(
                            onClick = { inputDescription = template },
                            label = { Text(template, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                            icon = {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }

            // Scrollable Conversation View
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        UserRequirementMessageBubble(msg)
                    } else {
                        SystemDesignSuggestionMessageBubble(
                            message = msg,
                            onSaveToDatabase = {
                                val currentProj = project
                                if (currentProj != null) {
                                    viewModel.viewModelScope.launch {
                                        viewModel.docRepo.saveDocument(
                                            projectId = currentProj.id,
                                            docType = "SDD",
                                            title = "System Design Suggestion - ${SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date())}",
                                            markdown = msg.text,
                                            author = "Gemini Retrofit Copilot"
                                        )
                                        viewModel.postNotification(
                                            title = "System Design Persisted",
                                            message = "Saved generated system design suggestion into Room database documentation store."
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                if (isGenerating) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Gemini Retrofit Client analyzing requirements & synthesizing system design...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputDescription,
                        onValueChange = { inputDescription = it },
                        placeholder = { Text("Enter project requirement description or user story...") },
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_requirement_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val desc = inputDescription.trim()
                            if (desc.isNotBlank()) {
                                messages.add(SystemDesignChatMessage(isUser = true, text = desc))
                                inputDescription = ""
                                isGenerating = true

                                val projectCtx = "Project: ${project?.name ?: "Enterprise System"}\nDomain: ${project?.domain ?: "SaaS"}"

                                scope.launch {
                                    viewModel.startLoading(
                                        operation = "Gemini Retrofit API",
                                        message = "Generating high-level system design suggestion...",
                                        isAi = true
                                    )

                                    val result = GeminiRetrofitClient.generateSystemDesignSuggestions(
                                        requirementText = desc,
                                        projectContext = projectCtx
                                    )

                                    val responseText = result.getOrElse {
                                        "Error communicating with Gemini API: ${it.message}\n\nFalling back to deterministic System Design Template:\n\n### 1. Recommended Architecture Style\nClean Architecture with Domain-Driven Design (DDD) & Event-Driven messaging.\n\n### 2. Core Components\n- **Presentation Layer**: Jetpack Compose UI & ViewModels\n- **Domain Layer**: UseCases, Entities, and Repository Interfaces\n- **Data Layer**: Room SQLite Database, Retrofit REST Client, and MongoDB Atlas Sync\n\n### 3. Database Schema\n- `RequirementRecord` (id: String PK, code: String, title: String, priority: String)\n- `DesignComponent` (id: String PK, layer: String, responsibilities: String)\n\n### 4. RESTful API Contracts\n- `POST /api/v1/requirements`: Create requirement specification\n- `GET /api/v1/system-design/{id}`: Fetch architectural components"
                                    }

                                    messages.add(
                                        SystemDesignChatMessage(
                                            isUser = false,
                                            text = responseText
                                        )
                                    )
                                    isGenerating = false
                                    viewModel.stopLoading()
                                }
                            }
                        },
                        enabled = !isGenerating && inputDescription.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (inputDescription.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .testTag("chat_send_requirement_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputDescription.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserRequirementMessageBubble(message: SystemDesignChatMessage) {
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Requirement Description", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SystemDesignSuggestionMessageBubble(
    message: SystemDesignChatMessage,
    onSaveToDatabase: () -> Unit
) {
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini System Design Suggestion",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Retrofit Service",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Button(
                            onClick = onSaveToDatabase,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Design to Room DB", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
