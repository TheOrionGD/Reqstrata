package com.theoriongd.reqstrata.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class GlobalLoadingState(
    val isLoading: Boolean = false,
    val operation: String = "",
    val message: String = "",
    val isAi: Boolean = false
)

/**
 * Global Loading State Handler Overlay providing dynamic feedback while AI is processing
 * requests or while data is being fetched / committed to Room Database.
 */
@Composable
fun GlobalLoadingOverlay(
    loadingState: GlobalLoadingState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = loadingState.isLoading,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                color = if (loadingState.isAi) Color(0xFF2E1065) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (loadingState.isAi) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .testTag("global_loading_indicator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (loadingState.isAi) Color(0xFF7C3AED).copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = if (loadingState.isAi) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (loadingState.isAi) Icons.Default.AutoAwesome else Icons.Default.Storage,
                                contentDescription = null,
                                tint = if (loadingState.isAi) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = loadingState.operation.ifBlank { if (loadingState.isAi) "Gemini AI" else "Database Operation" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (loadingState.isAi) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = loadingState.message.ifBlank { "Processing request..." },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
