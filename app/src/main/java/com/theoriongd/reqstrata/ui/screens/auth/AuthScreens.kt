package com.theoriongd.reqstrata.ui.screens.auth
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.theoriongd.reqstrata.ui.motion.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.theme.*
import com.theoriongd.reqstrata.ui.components.background.MobiusSpaceBackground
import com.theoriongd.reqstrata.ui.components.mobius.MobiusRibbonHeroVisual
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        Triple(
            "Transform Requirements",
            "Convert natural-language software requirements into validated, structured software engineering artifacts with Gemini AI.",
            Icons.Default.Description
        ),
        Triple(
            "Design Systems with AI",
            "Synthesize production architectures, interactive UML diagrams, normalized database schemas, and clean RESTful API specifications.",
            Icons.Default.AccountTree
        ),
        Triple(
            "Validate and Trace",
            "Maintain bidirectional end-to-end traceability from Requirement to Task, Test Case, and Automated Documentation.",
            Icons.Default.Verified
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MobiusSpaceBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 180,
            rotationSpeed = 0.9f,
            intensity = 0.65f,
            showRibbon = false
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // App Header Brand
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.theoriongd.reqstrata.R.drawable.ic_app_logo),
                contentDescription = "Reqstrata Logo",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Reqstrata",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Software Lifecycle Platform",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val (title, description, icon) = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Production 3D Futuristic Digital Möbius Ribbon Hero Visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MobiusRibbonHeroVisual(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .aspectRatio(1.18f)
                            .heightIn(max = 330.dp),
                        pageIndex = pageIndex,
                        badgeIcon = icon
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }

        // Page Indicator
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = tween(MotionDuration.STANDARD),
                    label = "indicator_width"
                )
                val color by animateColorAsState(
                    targetValue = if (isSelected) PrimaryLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    animationSpec = tween(MotionDuration.STANDARD),
                    label = "indicator_color"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < 2) {
                TextButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier
                        .pressScale()
                        .testTag("skip_button")
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .pressScale()
                        .testTag("next_button")
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .pressScale()
                        .testTag("get_started_button")
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
    } // end Box
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("admin@req2sys.io") }
    var password by remember { mutableStateOf("Admin123!") }
    var passwordVisible by remember { mutableStateOf(false) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        MobiusSpaceBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 160,
            rotationSpeed = 0.85f,
            intensity = 0.55f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Logo
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    Brush.linearGradient(listOf(PrimaryViolet, PrimaryLight)),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to Req2System AI",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Sign in to access your engineering projects",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = expandVertically(tween(MotionDuration.FAST)) + fadeIn(tween(MotionDuration.FAST)),
            exit = shrinkVertically(tween(MotionDuration.FAST)) + fadeOut(tween(MotionDuration.FAST))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .subtleShake(trigger = errorMessage != null)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryLight) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryLight) },
            trailingIcon = {
                MotionPasswordToggle(
                    isVisible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible }
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.navigateTo(Screen.ForgotPassword) },
                modifier = Modifier.pressScale()
            ) {
                Text("Forgot Password?", color = PrimaryLight, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pressScale()
                .testTag("login_button")
        ) {
            Text("Sign In (Database Auth)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { viewModel.navigateTo(Screen.Register) }) {
                Text("Register", color = PrimaryLight, fontWeight = FontWeight.Bold)
            }
        }
    }
    } // end Box
}

@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        MobiusSpaceBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 140,
            rotationSpeed = 0.85f,
            intensity = 0.55f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = "Project Owner Registration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Create an autonomous workspace as Project Owner & Administrator",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role Policy & Hierarchy Notice Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Role: Project Owner / Administrator (Tier 1)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Public registration is strictly for Project Owners / Workspace Administrators. Other roles (Architects, Business Analysts, Developers, and Testers) are provisioned through the workspace team hierarchy by authorized leads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = expandVertically(tween(MotionDuration.FAST)) + fadeIn(tween(MotionDuration.FAST)),
            exit = shrinkVertically(tween(MotionDuration.FAST)) + fadeOut(tween(MotionDuration.FAST))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .subtleShake(trigger = errorMessage != null)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearMessages() },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryLight) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryLight) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input")
        )

        // Automated Tenant Workspace Preview
        val showTenantPreview = email.contains("@") && email.contains(".")
        AnimatedVisibility(
            visible = showTenantPreview,
            enter = expandVertically(tween(MotionDuration.FAST)) + fadeIn(tween(MotionDuration.FAST)),
            exit = shrinkVertically(tween(MotionDuration.FAST)) + fadeOut(tween(MotionDuration.FAST))
        ) {
            val domainPart = email.trim().lowercase().substringAfter("@", "").substringBefore(".")
            val predictedTenantName = if (domainPart.isNotBlank() && domainPart !in listOf("gmail", "yahoo", "outlook", "hotmail", "icloud", "mail")) {
                "${domainPart.replaceFirstChar { it.uppercase() }} Enterprise Workspace"
            } else if (fullName.isNotBlank()) {
                "${fullName.trim().substringBefore(" ")}'s Autonomous Workspace"
            } else {
                "Automated Isolated Workspace"
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessEmerald.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = SuccessEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Separated Tenant: $predictedTenantName",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Zero Admin Approval Required • Instant Active Provisioning",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessEmerald
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = { Text("Password (min 6 chars)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryLight) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; viewModel.clearMessages() },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryLight) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register(fullName, email, password, confirmPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pressScale()
                .testTag("register_submit_button")
        ) {
            Text("Register as Project Owner & Admin", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Invited by a lead as an Architect, BA, Developer, or Tester?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier.pressScale()
                ) {
                    Text("Sign In with Provisioned Credentials", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already registered as Owner?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(
                onClick = { viewModel.navigateTo(Screen.Login) },
                modifier = Modifier.pressScale()
            ) {
                Text("Sign In", color = PrimaryLight, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(20.dp))

        // Enterprise Tenant Separation Request Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(pressedScale = 0.99f)
                .testTag("tenant_separation_request_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need Dedicated Tenant Separation?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enterprise Workspace & User Provisioning",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Request a dedicated tenant workspace partition with isolated database boundaries, custom organization policies, and pre-provisioned team member accounts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.navigateTo(Screen.TenantSeparationRegister) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLight.copy(alpha = 0.18f),
                        contentColor = PrimaryLight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .pressScale()
                        .testTag("open_tenant_separation_register_btn")
                ) {
                    Icon(
                        Icons.Default.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Register Tenant Separation & Provision Users",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
    } // end Box
}

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var resetSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MobiusSpaceBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 130,
            rotationSpeed = 0.8f,
            intensity = 0.5f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reset Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Enter your registered email to reset your security credentials", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = resetSuccess,
            enter = expandVertically(tween(MotionDuration.FAST)) + fadeIn(tween(MotionDuration.FAST)),
            exit = shrinkVertically(tween(MotionDuration.FAST)) + fadeOut(tween(MotionDuration.FAST))
        ) {
            Surface(
                color = Color(0xFF064E3B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    "Password updated successfully! You can now log in.",
                    color = Color(0xFF34D399),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Registered Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.viewModelScope.launch {
                    val res = viewModel.authRepo.resetPassword(email, newPassword)
                    if (res.isSuccess) {
                        resetSuccess = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pressScale()
        ) {
            Text("Update Password")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.navigateTo(Screen.ResetPassword(email)) },
                modifier = Modifier.pressScale()
            ) {
                Text("Dedicated Reset Form", color = PrimaryLight, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(
                onClick = { viewModel.navigateTo(Screen.EmailVerification(email)) },
                modifier = Modifier.pressScale()
            ) {
                Text("Verify Email Token", color = SuccessEmerald, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.navigateTo(Screen.Login) },
            modifier = Modifier.pressScale()
        ) {
            Text("Return to Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    } // end Box
}
