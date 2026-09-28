package com.example.ui.screens.auth

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
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    var statusText by remember { mutableStateOf("Initializing system environment...") }

    LaunchedEffect(Unit) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
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
                    contentDescription = "Requirement2System Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Requirement2System AI",
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

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
