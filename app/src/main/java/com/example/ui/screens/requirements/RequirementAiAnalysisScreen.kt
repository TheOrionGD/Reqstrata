package com.example.ui.screens.requirements

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewModelScope
import com.example.data.remote.GeminiApiClient
import com.example.domain.model.RequirementQuality
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SectionHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementAiAnalysisScreen(
    viewModel: MainViewModel,
    reqId: String
) {
    val req by viewModel.reqRepo.getRequirement(reqId).collectAsState(initial = null)
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<RequirementQuality?>(null) }
    var acceptedSuggestions by remember { mutableStateOf(setOf<String>()) }
    var rejectedSuggestions by remember { mutableStateOf(setOf<String>()) }

    val scrollState = rememberScrollState()

    fun runAnalysis() {
        val r = req ?: return
        isAnalyzing = true
        viewModel.viewModelScope.launch {
            val res = GeminiApiClient.analyzeRequirementQuality(
                title = r.title,
                description = r.description,
                type = r.type
            )
            analysisResult = res
            isAnalyzing = false
        }
    }

    LaunchedEffect(reqId) {
        if (analysisResult == null) {
            runAnalysis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Requirement Quality Engine", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(req?.code ?: "Quality & Testability Audit", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    com.example.ui.theme.ThemeToggleIconButton(viewModel = viewModel)
                    IconButton(
                        onClick = { runAnalysis() },
                        enabled = !isAnalyzing,
                        modifier = Modifier.testTag("reanalyze_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-analyze with Gemini", tint = MaterialTheme.colorScheme.primary)
                    }
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
            req?.let { r ->
                // Summary Header
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
                            Text(r.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                                Text(r.status, color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(r.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isAnalyzing) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gemini is auditing requirement completeness, ambiguity, consistency, and testability...", color = Color(0xFFFAFAFA), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (analysisResult != null) {
                val q = analysisResult!!

                // Score Cards Grid
                SectionHeader(title = "Quality Dimensions Scorecard")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityMetricCard(title = "Completeness", score = q.completeness, modifier = Modifier.weight(1f))
                    QualityMetricCard(title = "Consistency", score = q.consistency, modifier = Modifier.weight(1f))
                    QualityMetricCard(title = "Testability", score = q.testability, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ambiguity & Quality Issues
                SectionHeader(title = "Detected Quality Issues (${q.issues.size})")
                if (q.issues.isEmpty()) {
                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No major quality issues detected. Specification is clear and structured.", color = Color(0xFF34D399), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    for (issue in q.issues) {
                        Surface(
                            color = Color(0xFF7F1D1D).copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Quality Alert", fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5), style = MaterialTheme.typography.labelMedium)
                                    Text(issue, color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Missing Information / Boundary Issues
                SectionHeader(title = "Missing Information & Boundary Constraints (${q.missingInformation.size})")
                if (q.missingInformation.isEmpty()) {
                    Text("No missing boundary information identified.", color = Color(0xFF71717A), style = MaterialTheme.typography.bodySmall)
                } else {
                    for (info in q.missingInformation) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(info, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actionable AI Suggestions
                SectionHeader(title = "AI Refinement Suggestions (${q.suggestions.size})")
                q.suggestions.forEach { suggestion ->
                    val isAccepted = acceptedSuggestions.contains(suggestion)
                    val isRejected = rejectedSuggestions.contains(suggestion)

                    Surface(
                        color = if (isAccepted) Color(0xFF064E3B).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isAccepted) Color(0xFF10B981) else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(suggestion, color = Color.White, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        acceptedSuggestions = acceptedSuggestions + suggestion
                                        rejectedSuggestions = rejectedSuggestions - suggestion
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAccepted) Color(0xFF10B981) else Color(0xFF6D28D9)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAccepted) "Accepted" else "Accept Suggestion", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = {
                                        rejectedSuggestions = rejectedSuggestions + suggestion
                                        acceptedSuggestions = acceptedSuggestions - suggestion
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isRejected) "Rejected" else "Dismiss", color = Color(0xFFA1A1AA), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Edit Requirement & Re-Analyze
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CreateEditRequirement(reqId)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_req_from_ai_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Requirement")
                    }

                    OutlinedButton(
                        onClick = { runAnalysis() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Analyze")
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun QualityMetricCard(
    title: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> Color(0xFF10B981)
        score >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
            Spacer(modifier = Modifier.height(4.dp))
            Text("$score%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
