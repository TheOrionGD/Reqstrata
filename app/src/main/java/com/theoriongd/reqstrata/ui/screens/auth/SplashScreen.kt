package com.theoriongd.reqstrata.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.components.background.MobiusSpaceBackground
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.theme.PrimaryLight
import com.theoriongd.reqstrata.ui.theme.PrimaryViolet
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import com.theoriongd.reqstrata.ui.motion.MotionDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val reduceMotion = com.theoriongd.reqstrata.ui.motion.LocalReduceMotion.current
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(MotionDuration.SHIMMER, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Entrance fade & subtle scale
    var isReady by remember { mutableStateOf(false) }
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0f,
        animationSpec = tween(MotionDuration.EMPHASIS, easing = LinearOutSlowInEasing),
        label = "entrance_alpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (isReady) 1f else 0.92f,
        animationSpec = tween(MotionDuration.EMPHASIS, easing = FastOutSlowInEasing),
        label = "entrance_scale"
    )

    var statusText by remember { mutableStateOf("Initializing system environment...") }

    LaunchedEffect(Unit) {
        isReady = true
        delay(400)
        statusText = "Restoring active security session..."
        val user = viewModel.authRepo.getCurrentUser()

        delay(400)
        if (user != null) {
            statusText = "Resolving project memberships for ${user.fullName}..."
            val projects = viewModel.projectRepo.getAllProjects().firstOrNull() ?: emptyList()
            delay(300)
            if (projects.isNotEmpty()) {
                val lastProject = projects.first()
                val role = viewModel.projectRepo.getUserRole(lastProject.id, user.id)
                viewModel.switchRoleForTesting(role)
                viewModel.navigateTo(viewModel.getRoleDefaultScreen(role))
            } else {
                viewModel.navigateTo(Screen.ProjectSelection)
            }
        } else {
            statusText = "Starting onboarding workflow..."
            delay(300)
            viewModel.navigateTo(Screen.Onboarding)
        }
    }

    val activeScale = if (reduceMotion) 1f else (entranceScale * (if (isReady) pulseScale else 1f))
    val activeAlpha = if (reduceMotion) 1f else entranceAlpha

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // 3D Mobius space background — full intensity on splash
        MobiusSpaceBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 220,
            rotationSpeed = 1f,
            intensity = 1f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer {
                    alpha = activeAlpha
                    scaleX = activeScale
                    scaleY = activeScale
                }
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryViolet, PrimaryLight)
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Reqstrata Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Reqstrata",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Next-Generation Enterprise Software Lifecycle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.animation.AnimatedContent(
                targetState = statusText,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(tween(MotionDuration.FAST)) togetherWith
                        androidx.compose.animation.fadeOut(tween(MotionDuration.FAST))
                },
                label = "splash_status_text"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
