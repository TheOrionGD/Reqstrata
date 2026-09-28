package com.theoriongd.reqstrata.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.local.entity.RequirementEntity
import com.theoriongd.reqstrata.domain.model.RequirementPriority
import com.theoriongd.reqstrata.domain.model.RequirementType
import com.theoriongd.reqstrata.ui.MainViewModel
import kotlinx.coroutines.launch

/**
 * Data entry form component to capture software requirements, including fields for
 * title, description, and priority level, utilizing Room database to save these locally.
 */
@Composable
fun RequirementDataEntryForm(
    viewModel: MainViewModel,
    projectId: String,
    modifier: Modifier = Modifier,
    initialTitle: String = "",
    initialDescription: String = "",
    initialPriority: RequirementPriority = RequirementPriority.HIGH,
    initialType: RequirementType = RequirementType.FUNCTIONAL,
    onRequirementSaved: (RequirementEntity) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var priority by remember { mutableStateOf(initialPriority) }
    var type by remember { mutableStateOf(initialType) }

    var criteriaList by remember { mutableStateOf(listOf<String>()) }
    var criterionInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Capture Software Requirement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Stored locally in SQLite Room Database",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Local Room DB",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error validation alert
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // --- 1. TITLE FIELD ---
            Text(
                text = "Requirement Title *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                placeholder = { Text("e.g., User Authentication via OAuth 2.0 / OIDC") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_requirement_title"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- 2. PRIORITY LEVEL SELECTOR ---
            Text(
                text = "Priority Level *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementPriority.entries.forEach { p ->
                    val isSelected = priority == p
                    val chipColor = when (p) {
                        RequirementPriority.CRITICAL -> Color(0xFFEF4444)
                        RequirementPriority.HIGH -> Color(0xFFF59E0B)
                        RequirementPriority.MEDIUM -> Color(0xFF8B5CF6)
                        RequirementPriority.LOW -> Color(0xFF10B981)
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { priority = p }
                            .testTag("priority_opt_${p.name.lowercase()}"),
                        color = if (isSelected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) chipColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(chipColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = p.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. REQUIREMENT TYPE / CLASSIFICATION ---
            Text(
                text = "Requirement Classification",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    RequirementType.FUNCTIONAL,
                    RequirementType.NON_FUNCTIONAL,
                    RequirementType.SECURITY,
                    RequirementType.PERFORMANCE
                ).forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(t.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 4. DESCRIPTION FIELD ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Specification Description *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${description.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    errorMessage = null
                },
                placeholder = {
                    Text("The system shall verify JWT access tokens against the authorization server and extract tenant claims...")
                },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_requirement_description"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- 5. ACCEPTANCE CRITERIA (OPTIONAL) ---
            Text(
                text = "Acceptance Criteria (${criteriaList.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            criteriaList.forEachIndexed { idx, criterion ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = criterion,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                criteriaList = criteriaList.filterIndexed { i, _ -> i != idx }
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = criterionInput,
                    onValueChange = { criterionInput = it },
                    placeholder = { Text("Given [context], When [action], Then [outcome]...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (criterionInput.isNotBlank()) {
                            criteriaList = criteriaList + criterionInput.trim()
                            criterionInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 6. ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Requirement title cannot be empty."
                            return@Button
                        }
                        if (description.isBlank()) {
                            errorMessage = "Requirement description is required."
                            return@Button
                        }

                        val user = currentUser
                        if (user == null) {
                            errorMessage = "User session expired. Please sign in."
                            return@Button
                        }

                        isSaving = true
                        coroutineScope.launch {
                            viewModel.startLoading(
                                operation = "Local Room Database",
                                message = "Saving requirement '${title.trim()}' locally...",
                                isAi = false
                            )
                            try {
                                val savedEntity = viewModel.reqRepo.createRequirement(
                                    projectId = projectId,
                                    title = title.trim(),
                                    description = description.trim(),
                                    type = type,
                                    priority = priority,
                                    acceptanceCriteria = criteriaList,
                                    author = user
                                )

                                viewModel.postNotification(
                                    title = "Requirement Saved Locally",
                                    message = "Committed ${savedEntity.code}: '${savedEntity.title}' into local Room database.",
                                    type = com.theoriongd.reqstrata.ui.notifications.NotificationType.SUCCESS
                                )

                                onRequirementSaved(savedEntity)
                            } catch (e: Exception) {
                                errorMessage = "Failed to save to database: ${e.message}"
                            } finally {
                                isSaving = false
                                viewModel.stopLoading()
                            }
                        }
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_requirement_local_btn")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Room...")
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Local Room DB")
                    }
                }
            }
        }
    }
}
