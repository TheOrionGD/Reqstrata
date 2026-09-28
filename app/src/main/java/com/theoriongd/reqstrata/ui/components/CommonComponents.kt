package com.theoriongd.reqstrata.ui.components

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import com.theoriongd.reqstrata.ui.motion.pressScale
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "APPROVED", "PASSED", "COMPLETED", "ACTIVE" -> Pair(Color(0xFF064E3B), Color(0xFF34D399))
        "UNDER REVIEW", "IN PROGRESS", "IN REVIEW", "PENDING", "NEEDS REVIEW" -> Pair(Color(0xFF451A03), Color(0xFFFBBF24))
        "DRAFT", "BACKLOG", "READY" -> Pair(Color(0xFF27272A), Color(0xFFA1A1AA))
        "FAILED", "REJECTED", "BLOCKED", "CRITICAL" -> Pair(Color(0xFF450A0A), Color(0xFFF87171))
        "DEPRECATED", "SKIPPED", "WARNING" -> Pair(Color(0xFF451A03), Color(0xFFFBBF24))
        "AI GENERATED", "AI_GENERATED" -> Pair(Color(0xFF3B0764), Color(0xFFA78BFA))
        else -> Pair(Color(0xFF27272A), Color(0xFFA1A1AA))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (priority.uppercase()) {
        "CRITICAL" -> Pair(ErrorRed, Icons.Default.Warning)
        "HIGH" -> Pair(WarningAmber, Icons.Default.ArrowUpward)
        "MEDIUM" -> Pair(SecondaryAmber, Icons.Default.Remove)
        else -> Pair(Color(0xFFA1A1AA), Icons.Default.ArrowDownward)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = priority,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priority,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RoleBadge(
    role: ProjectRole,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (role) {
        ProjectRole.ADMIN -> Pair(PrimaryViolet, "Admin")
        ProjectRole.BUSINESS_ANALYST -> Pair(PrimaryLight, "Analyst")
        ProjectRole.ARCHITECT -> Pair(AiAccent, "Architect")
        ProjectRole.DEVELOPER -> Pair(SuccessEmerald, "Developer")
        ProjectRole.TESTER -> Pair(SecondaryAmber, "Tester")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.pressScale(onClick = onClick)
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AiActionCard(
    title: String,
    description: String,
    buttonText: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            PrimaryLight.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isLoading) {
                    Modifier.pressScale(pressedScale = 0.99f, onClick = onClick)
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            PrimaryViolet,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(animationSpec = com.theoriongd.reqstrata.ui.motion.MotionSpec.fastTween()) togetherWith
                            androidx.compose.animation.fadeOut(animationSpec = com.theoriongd.reqstrata.ui.motion.MotionSpec.fastTween())
                    },
                    label = "ai_button_loading_content"
                ) { loading ->
                    if (loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing with Gemini...")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TraceabilityChainView(
    reqCode: String,
    useCaseCode: String?,
    archCode: String?,
    apiCode: String?,
    taskCode: String?,
    testCode: String?,
    modifier: Modifier = Modifier
) {
    val items = listOfNotNull(
        Pair("REQ", reqCode),
        useCaseCode?.let { Pair("UC", it) },
        archCode?.let { Pair("ARCH", it) },
        apiCode?.let { Pair("API", it) },
        taskCode?.let { Pair("DEV", it) },
        testCode?.let { Pair("TEST", it) }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, (type, code) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLight
                )
            }
            if (index < items.size - 1) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(14.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
