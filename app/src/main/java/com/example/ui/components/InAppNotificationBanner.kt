package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.notifications.InAppNotificationData
import com.example.ui.notifications.NotificationType

@Composable
fun InAppNotificationBanner(
    notification: InAppNotificationData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .statusBarsPadding()
    ) {
        notification?.let { item ->
            val (icon, bgGradient, iconTint) = when (item.type) {
                NotificationType.SUCCESS -> Triple(
                    Icons.Default.CheckCircle,
                    listOf(Color(0xFF064E3B), Color(0xFF18181B)),
                    Color(0xFF34D399)
                )
                NotificationType.ALERT -> Triple(
                    Icons.Default.Warning,
                    listOf(Color(0xFF450A0A), Color(0xFF18181B)),
                    Color(0xFFF87171)
                )
                NotificationType.WARNING -> Triple(
                    Icons.Default.WarningAmber,
                    listOf(Color(0xFF451A03), Color(0xFF18181B)),
                    Color(0xFFFBBF24)
                )
                NotificationType.INFO -> Triple(
                    Icons.Default.NotificationsActive,
                    listOf(Color(0xFF2E1065), Color(0xFF18181B)),
                    Color(0xFFA78BFA)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismiss() }
                    .testTag("in_app_notification_banner"),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .background(Brush.horizontalGradient(bgGradient))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(iconTint.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
