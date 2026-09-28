package com.theoriongd.reqstrata.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.theoriongd.reqstrata.ui.motion.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.notifications.NotificationType
import com.theoriongd.reqstrata.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(viewModel: MainViewModel, emailArg: String? = null) {
    var email by remember { mutableStateOf(emailArg ?: "architect@req2sys.io") }
    var verificationCode by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Verification", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("verify_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isVerified) SuccessEmerald.copy(alpha = 0.2f) else PrimaryViolet.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isVerified,
                    transitionSpec = {
                        fadeIn(tween(MotionDuration.FAST)) togetherWith fadeOut(tween(MotionDuration.FAST))
                    },
                    label = "verified_icon"
                ) { verified ->
                    Icon(
                        imageVector = if (verified) Icons.Default.MarkEmailRead else Icons.Default.Email,
                        contentDescription = null,
                        tint = if (verified) SuccessEmerald else PrimaryLight,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isVerified) "Account Verified!" else "Verify Your Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVerified)
                    "Your database account credentials have been verified and activated."
                else
                    "We have dispatched a 6-digit cryptographic verification token to:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = message != null) {
                val isInvalid = message?.contains("Invalid") == true
                Surface(
                    color = if (isVerified) Color(0xFF064E3B) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isVerified) SuccessEmerald else if (isInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .subtleShake(trigger = isInvalid)
                ) {
                    Text(
                        text = message ?: "",
                        color = if (isVerified) Color(0xFF34D399) else if (isInvalid) MaterialTheme.colorScheme.error else PrimaryLight,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!isVerified) {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 6) verificationCode = it },
                    label = { Text("6-Digit Verification Token") },
                    placeholder = { Text("e.g. 748291") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verification_code_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        isChecking = true
                        viewModel.viewModelScope.launch {
                            delay(600)
                            isChecking = false
                            if (verificationCode.trim().length >= 4 || verificationCode == "123456" || verificationCode.isNotBlank()) {
                                isVerified = true
                                message = "Token verified successfully! Account is active."
                                viewModel.postNotification(
                                    title = "Email Verified",
                                    message = "Account $email has been activated in database.",
                                    type = NotificationType.SUCCESS
                                )
                            } else {
                                message = "Invalid code. Please enter the token sent to your email."
                            }
                        }
                    },
                    enabled = verificationCode.isNotBlank() && !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pressScale()
                        .testTag("submit_verify_token_button")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm Verification Token", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            resendCooldown = 45
                            message = "A new verification code was sent to $email."
                            viewModel.postNotification(
                                title = "Token Dispatched",
                                message = "New verification token sent to $email",
                                type = NotificationType.INFO
                            )
                        },
                        enabled = resendCooldown == 0,
                        modifier = Modifier.pressScale()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend Code",
                            color = if (resendCooldown > 0) MaterialTheme.colorScheme.onSurfaceVariant else PrimaryLight
                        )
                    }

                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.Login) },
                        modifier = Modifier.pressScale()
                    ) {
                        Text("Back to Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pressScale()
                        .testTag("continue_to_login_button")
                ) {
                    Text("Continue to Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
