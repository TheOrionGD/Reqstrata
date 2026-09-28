package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.notifications.NotificationType
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(viewModel: MainViewModel, emailArg: String? = null) {
    var email by remember { mutableStateOf(emailArg ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("reset_back_button")
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
            Icon(
                Icons.Default.LockReset,
                contentDescription = null,
                tint = PrimaryLight,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Set New Security Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Enter your registered database account email and new password credentials.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = statusMessage != null) {
                Surface(
                    color = if (isSuccess) Color(0xFF064E3B) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSuccess) SuccessEmerald else MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = if (isSuccess) Color(0xFF34D399) else Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; statusMessage = null },
                label = { Text("Registered Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; statusMessage = null },
                label = { Text("New Password (min 6 characters)") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_new_password_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; statusMessage = null },
                label = { Text("Confirm New Password") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_confirm_password_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        statusMessage = "Please provide your registered account email."
                        return@Button
                    }
                    if (newPassword.length < 6) {
                        statusMessage = "New password must be at least 6 characters long."
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        statusMessage = "New password and confirmation password do not match."
                        return@Button
                    }

                    isSubmitting = true
                    viewModel.viewModelScope.launch {
                        val res = viewModel.authRepo.resetPassword(email.trim(), newPassword)
                        isSubmitting = false
                        if (res.isSuccess) {
                            isSuccess = true
                            statusMessage = "Password successfully reset in database! You may now sign in."
                            viewModel.postNotification(
                                title = "Password Reset Successful",
                                message = "Security credentials updated for $email",
                                type = NotificationType.SUCCESS
                            )
                        } else {
                            isSuccess = false
                            statusMessage = res.exceptionOrNull()?.message ?: "Failed to reset password."
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_password_submit_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Update Database Password", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.navigateTo(Screen.Login) },
                modifier = Modifier.testTag("back_to_login_btn")
            ) {
                Text("Return to Sign In", color = PrimaryLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}
