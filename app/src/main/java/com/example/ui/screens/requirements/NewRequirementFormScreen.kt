package com.example.ui.screens.requirements

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.RequirementPriority
import com.example.domain.model.RequirementQuality
import com.example.domain.model.RequirementType
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.notifications.NotificationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequirementFormScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val project by viewModel.currentProject.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(RequirementPriority.HIGH) }
    var selectedType by remember { mutableStateOf(RequirementType.FUNCTIONAL) }
    var criteriaList by remember { mutableStateOf(mutableListOf<String>()) }
    var newCriterionText by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isAnalyzingQuality by remember { mutableStateOf(false) }
    var qualityResult by remember { mutableStateOf<RequirementQuality?>(null) }
    var isListeningVoice by remember { mutableStateOf(false) }

    // Voice-to-Text Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningVoice = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenTextList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = spokenTextList?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                description = if (description.isBlank()) {
                    spoken.trim()
                } else {
                    "${description.trim()} ${spoken.trim()}"
                }
                validationError = null
                viewModel.postNotification(
                    title = "Voice Input Recorded",
                    message = "Transcribed speech into description successfully.",
                    type = NotificationType.INFO
                )
            }
        }
    }

    // Permission launcher for Microphone (RECORD_AUDIO)
    val requestRecordAudioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListeningVoice = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate requirement specification...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                isListeningVoice = false
                Toast.makeText(context, "Voice dictation unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice dictation", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceDictation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            isListeningVoice = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate requirement specification...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                isListeningVoice = false
                Toast.makeText(context, "Speech recognition unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Requirement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "MongoDB Atlas Real-time Persistence",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MongoDB Live",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                },
                actions = {
                    com.example.ui.theme.ThemeToggleIconButton(viewModel = viewModel)
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
            // Validation error banner
            AnimatedVisibility(visible = validationError != null) {
                Surface(
                    color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validationError ?: "",
                            color = Color(0xFFFCA5A5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // --- 1. TITLE FIELD ---
            Text(
                text = "Requirement Title *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                placeholder = { Text("e.g., Real-time Automated Payment Reconciliation") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_req_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- 2. PRIORITY LEVEL SELECTOR ---
            Text(
                text = "Priority Level *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Determines release scheduling, risk ranking, and team assignment",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A1AA)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementPriority.entries.forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val (color, label) = when (priority) {
                        RequirementPriority.CRITICAL -> Pair(Color(0xFFEF4444), "Critical")
                        RequirementPriority.HIGH -> Pair(Color(0xFFF59E0B), "High")
                        RequirementPriority.MEDIUM -> Pair(Color(0xFF8B5CF6), "Medium")
                        RequirementPriority.LOW -> Pair(Color(0xFFA1A1AA), "Low")
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedPriority = priority }
                            .testTag("priority_chip_${priority.name.lowercase()}"),
                        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- 3. CATEGORY / TYPE ---
            Text(
                text = "Requirement Classification",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    RequirementType.FUNCTIONAL,
                    RequirementType.NON_FUNCTIONAL,
                    RequirementType.SECURITY,
                    RequirementType.PERFORMANCE
                ).forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- 4. DETAILED DESCRIPTION & VOICE-TO-TEXT DICTATION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detailed Description *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { startVoiceDictation() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListeningVoice) Color(0xFFEF4444) else Color(0xFF6D28D9).copy(alpha = 0.25f),
                        contentColor = if (isListeningVoice) Color.White else Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isListeningVoice) Color(0xFFEF4444) else Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("voice_to_text_button")
                ) {
                    Icon(
                        imageVector = if (isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Dictate with Microphone",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isListeningVoice) "Listening..." else "Dictate (Voice)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tap 'Dictate' to speak requirement specifications directly",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isListeningVoice) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                )
                Text(
                    text = "${description.length} chars | ${description.split("\\s+".toRegex()).count { it.isNotBlank() }} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF71717A)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    validationError = null
                },
                placeholder = {
                    Text(
                        "The system shall provide automated reconciliation of merchant transactions against bank clearing houses within 5 minutes of batch settlement..."
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { startVoiceDictation() },
                        modifier = Modifier.testTag("dictate_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Dictate Description",
                            tint = if (isListeningVoice) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                        )
                    }
                },
                minLines = 5,
                maxLines = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_req_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3F3F46),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- 5. ACCEPTANCE CRITERIA BUILDER ---
            Text(
                text = "Acceptance Criteria (Gherkin / Testable Statements)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (criteriaList.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No criteria added yet. Add testable Given-When-Then statements below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                criteriaList.forEachIndexed { index, criterion ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = criterion,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val list = criteriaList.toMutableList()
                                    list.removeAt(index)
                                    criteriaList = list
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove criterion",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCriterionText,
                    onValueChange = { newCriterionText = it },
                    placeholder = { Text("Given authorized merchant, When settlement runs, Then...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_criterion_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3F3F46),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newCriterionText.isNotBlank()) {
                            val list = criteriaList.toMutableList()
                            list.add(newCriterionText.trim())
                            criteriaList = list
                            newCriterionText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(52.dp).testTag("add_criterion_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 6. SUBMIT BUTTON INTEGRATED WITH MONGODB REPOSITORY ---
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please specify a requirement title."
                        return@Button
                    }
                    if (description.isBlank()) {
                        validationError = "Detailed specification description is required."
                        return@Button
                    }

                    val currentUser = user ?: return@Button
                    val currentProj = project ?: return@Button

                    isSubmitting = true
                    viewModel.viewModelScope.launch {
                        try {
                            // 1. Create requirement via MongoDB-integrated repository
                            val created = viewModel.reqRepo.createRequirement(
                                projectId = currentProj.id,
                                title = title,
                                description = description,
                                type = selectedType,
                                priority = selectedPriority,
                                acceptanceCriteria = criteriaList,
                                author = currentUser
                            )

                            // 2. Dispatch In-App Notification and native Phone Notification Center
                            viewModel.postNotification(
                                title = "Requirement Document Committed",
                                message = "${created.code}: '${created.title}' (${created.priority}) synced to MongoDB Atlas.",
                                type = NotificationType.SUCCESS
                            )

                            // 3. Navigate back to Project Dashboard to view the live card
                            viewModel.navigateTo(Screen.ProjectDashboard)
                        } catch (e: Exception) {
                            validationError = "Failed to commit requirement: ${e.message}"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_requirement_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Pushing to MongoDB Atlas...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commit to MongoDB Repository",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
