package com.theoriongd.reqstrata.ui.screens.auth
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.data.repository.TenantMemberRegistrationItem
import com.theoriongd.reqstrata.data.repository.TenantSeparationRequest
import com.theoriongd.reqstrata.data.repository.TenantSeparationResult
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import com.theoriongd.reqstrata.ui.Screen
import com.theoriongd.reqstrata.ui.motion.pressScale
import com.theoriongd.reqstrata.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TenantSeparationRegisterScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()

    // 1. Tenant Details
    var tenantName by remember { mutableStateOf("") }
    var tenantSlug by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("Enterprise SaaS") }
    var selectedIsolationMode by remember { mutableStateOf("Dedicated Sovereign Partition") }
    var selectedRegion by remember { mutableStateOf("Local Sovereign Partition") }
    var defaultProjectName by remember { mutableStateOf("") }

    // 2. Tenant Administrator
    var adminFullName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminTitle by remember { mutableStateOf("Tenant Administrator") }
    var adminPassword by remember { mutableStateOf("") }
    var adminConfirmPassword by remember { mutableStateOf("") }
    var showAdminPassword by remember { mutableStateOf(false) }

    // 3. Team Members Creation
    var memberList by remember {
        mutableStateOf(
            listOf(
                TenantMemberRegistrationItem(
                    id = UUID.randomUUID().toString(),
                    fullName = "Elena Rostova",
                    email = "elena.rostova@enterprise.internal",
                    role = ProjectRole.ARCHITECT,
                    password = "SecurePass@2026"
                ),
                TenantMemberRegistrationItem(
                    id = UUID.randomUUID().toString(),
                    fullName = "Marcus Vance",
                    email = "marcus.vance@enterprise.internal",
                    role = ProjectRole.BUSINESS_ANALYST,
                    password = "SecurePass@2026"
                )
            )
        )
    }

    // 4. Governance & Policies
    var strictIsolation by remember { mutableStateOf(true) }
    var immediateActivation by remember { mutableStateOf(true) }
    var immutableAuditTrail by remember { mutableStateOf(true) }

    // State & Feedback
    var isSubmitting by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var successResult by remember { mutableStateOf<TenantSeparationResult?>(null) }

    val industries = listOf(
        "Enterprise SaaS",
        "FinTech & Banking",
        "Healthcare / HIPAA",
        "Defense & Aerospace",
        "E-Commerce & Retail",
        "Telecommunications",
        "Energy & Utilities"
    )

    val isolationModes = listOf(
        Triple(
            "Dedicated Sovereign Partition",
            "Zero Cross-Tenant Leakage",
            "Complete local Room database boundary partition with strict isolation filters."
        ),
        Triple(
            "Encrypted Enterprise Multi-Tenant",
            "Cryptographically Segregated",
            "Row-level multi-tenancy with tenant identification tags and client-side access control."
        ),
        Triple(
            "Regulated Compliance Boundary",
            "Audit & Compliance Ready",
            "Isolated tenant envelope adhering to SOC2 Type II, HIPAA, and ISO 27001 policies."
        )
    )

    val regions = listOf(
        "Local Sovereign Partition",
        "US-East (N. Virginia)",
        "EU-Central (Frankfurt)",
        "AP-South (Mumbai)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tenant Separation Registration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Isolated Enterprise Workspace & User Provisioning",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .testTag("tenant_separation_back_btn")
                            .pressScale()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enterprise Isolation Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enterprise Tenant Separation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instantly provision an isolated tenant partition with strict boundaries, custom initial users, and immediate active access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error display
            AnimatedVisibility(visible = localError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = localError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ==========================================
            // STEP 1: TENANT & ORGANIZATION PROFILE
            // ==========================================
            SectionHeader(
                number = "1",
                title = "Tenant Organization Profile",
                subtitle = "Define your isolated workspace boundary and naming"
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tenantName,
                onValueChange = {
                    tenantName = it
                    localError = null
                    if (tenantSlug.isBlank() || tenantSlug == tenantName.take(it.length - 1).lowercase().replace(" ", "-")) {
                        tenantSlug = it.trim().lowercase().replace(Regex("[^a-z0-9]"), "-").take(24)
                    }
                    if (defaultProjectName.isBlank() || defaultProjectName.endsWith(" Core System")) {
                        defaultProjectName = if (it.isNotBlank()) "${it.trim()} Core System" else ""
                    }
                },
                label = { Text("Organization / Tenant Name *") },
                placeholder = { Text("e.g. Apex Global Solutions") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryLight) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tenant_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tenantSlug,
                onValueChange = {
                    tenantSlug = it.lowercase().replace(Regex("[^a-z0-9_-]"), "")
                    localError = null
                },
                label = { Text("Tenant Identifier / Slug *") },
                placeholder = { Text("e.g. apex-global") },
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = PrimaryLight) },
                supportingText = {
                    val previewId = if (tenantSlug.startsWith("tenant_")) tenantSlug else "tenant_${tenantSlug.ifBlank { "example" }}"
                    Text("Partition Key: $previewId", color = PrimaryLight)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tenant_slug_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Industry Domain",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                industries.forEach { ind ->
                    FilterChip(
                        selected = selectedIndustry == ind,
                        onClick = { selectedIndustry = ind },
                        label = { Text(ind, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryLight.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tenant Isolation Mode",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                isolationModes.forEach { (mode, badge, desc) ->
                    val isSelected = selectedIsolationMode == mode
                    Surface(
                        color = if (isSelected) PrimaryLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIsolationMode = mode }
                            .pressScale(pressedScale = 0.99f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedIsolationMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = SuccessEmerald.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SuccessEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Primary Region / Data Residency",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                regions.forEach { reg ->
                    FilterChip(
                        selected = selectedRegion == reg,
                        onClick = { selectedRegion = reg },
                        label = { Text(reg, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryLight.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = defaultProjectName,
                onValueChange = { defaultProjectName = it },
                label = { Text("Initial Workspace Project Name") },
                placeholder = { Text("e.g. Apex Core Architecture System") },
                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = PrimaryLight) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tenant_default_project_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // STEP 2: PRIMARY TENANT ADMINISTRATOR
            // ==========================================
            SectionHeader(
                number = "2",
                title = "Primary Tenant Administrator",
                subtitle = "This user will have full administrative rights over the isolated tenant"
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = adminFullName,
                onValueChange = { adminFullName = it; localError = null },
                label = { Text("Admin Full Name *") },
                placeholder = { Text("e.g. Dr. Jordan Vance") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryLight) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adminEmail,
                onValueChange = { adminEmail = it; localError = null },
                label = { Text("Admin Corporate Email *") },
                placeholder = { Text("admin@organization.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryLight) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adminTitle,
                onValueChange = { adminTitle = it },
                label = { Text("Official Job Title") },
                placeholder = { Text("e.g. Head of Engineering / Tenant Admin") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryLight) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adminPassword,
                onValueChange = { adminPassword = it; localError = null },
                label = { Text("Admin Password (min 6 chars) *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryLight) },
                trailingIcon = {
                    IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                        Icon(
                            if (showAdminPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showAdminPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adminConfirmPassword,
                onValueChange = { adminConfirmPassword = it; localError = null },
                label = { Text("Confirm Admin Password *") },
                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryLight) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_confirm_password_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // STEP 3: CREATE TENANT USERS (MULTI-USER PROVISIONING)
            // ==========================================
            SectionHeader(
                number = "3",
                title = "Create Tenant Users & Team Members",
                subtitle = "Pre-provision accounts that belong strictly to this tenant partition"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Provisioned Accounts: ${memberList.size + 1} (1 Admin + ${memberList.size} Members)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLight
                )
                TextButton(
                    onClick = {
                        val domain = if (adminEmail.contains("@")) adminEmail.substringAfter("@") else "enterprise.internal"
                        memberList = listOf(
                            TenantMemberRegistrationItem(
                                fullName = "Elena Rostova",
                                email = "elena.rostova@$domain",
                                role = ProjectRole.ARCHITECT,
                                password = "SecurePass@2026"
                            ),
                            TenantMemberRegistrationItem(
                                fullName = "Marcus Vance",
                                email = "marcus.vance@$domain",
                                role = ProjectRole.BUSINESS_ANALYST,
                                password = "SecurePass@2026"
                            ),
                            TenantMemberRegistrationItem(
                                fullName = "Devon Lin",
                                email = "devon.lin@$domain",
                                role = ProjectRole.DEVELOPER,
                                password = "SecurePass@2026"
                            ),
                            TenantMemberRegistrationItem(
                                fullName = "Sarah Connor",
                                email = "sarah.connor@$domain",
                                role = ProjectRole.TESTER,
                                password = "SecurePass@2026"
                            )
                        )
                    },
                    modifier = Modifier.pressScale()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pre-fill 4 Roles", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                memberList.forEachIndexed { index, member ->
                    MemberProvisionCard(
                        index = index + 1,
                        member = member,
                        onUpdate = { updated ->
                            memberList = memberList.toMutableList().also { it[index] = updated }
                        },
                        onDelete = {
                            memberList = memberList.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    memberList = memberList + TenantMemberRegistrationItem(
                        id = UUID.randomUUID().toString(),
                        fullName = "",
                        email = "",
                        role = ProjectRole.DEVELOPER,
                        password = "SecurePass@2026"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .pressScale()
                    .testTag("add_tenant_user_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, PrimaryLight)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another User to Tenant", color = PrimaryLight, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // STEP 4: GOVERNANCE & POLICIES
            // ==========================================
            SectionHeader(
                number = "4",
                title = "Separation Policies & Governance",
                subtitle = "Security controls applied across all users in this tenant"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enforce Strict Tenant Boundary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Deny all cross-tenant project querying and member leakage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = strictIsolation, onCheckedChange = { strictIsolation = it })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instant Active Provisioning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Zero manual admin approval required. Immediate ACTIVE database state.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = immediateActivation, onCheckedChange = { immediateActivation = it })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Immutable Audit & Security Trail", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Capture activity logs and partition events for regulatory auditing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = immutableAuditTrail, onCheckedChange = { immutableAuditTrail = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SUBMIT & PROVISION BUTTON
            // ==========================================
            Button(
                onClick = {
                    // Validation
                    if (tenantName.isBlank()) {
                        localError = "Please specify Organization / Tenant Name."
                        return@Button
                    }
                    if (tenantSlug.isBlank()) {
                        localError = "Please specify a Tenant Identifier / Slug."
                        return@Button
                    }
                    if (adminFullName.isBlank()) {
                        localError = "Please enter Tenant Administrator full name."
                        return@Button
                    }
                    if (adminEmail.isBlank() || !adminEmail.contains("@") || !adminEmail.contains(".")) {
                        localError = "Please enter a valid administrator email address."
                        return@Button
                    }
                    if (adminPassword.length < 6) {
                        localError = "Admin password must be at least 6 characters."
                        return@Button
                    }
                    if (adminPassword != adminConfirmPassword) {
                        localError = "Admin passwords do not match."
                        return@Button
                    }

                    // Check member rows
                    for (i in memberList.indices) {
                        val m = memberList[i]
                        if (m.fullName.isNotBlank() || m.email.isNotBlank()) {
                            if (m.fullName.isBlank()) {
                                localError = "User #${i + 1} requires a full name."
                                return@Button
                            }
                            if (m.email.isBlank() || !m.email.contains("@") || !m.email.contains(".")) {
                                localError = "User #${i + 1} (${m.fullName}) has an invalid email."
                                return@Button
                            }
                            if (m.password.length < 6) {
                                localError = "User #${i + 1} password must be at least 6 characters."
                                return@Button
                            }
                        }
                    }

                    isSubmitting = true
                    localError = null

                    val request = TenantSeparationRequest(
                        tenantName = tenantName,
                        tenantSlug = tenantSlug,
                        isolationMode = selectedIsolationMode,
                        industry = selectedIndustry,
                        primaryRegion = selectedRegion,
                        defaultProjectName = defaultProjectName,
                        adminFullName = adminFullName,
                        adminEmail = adminEmail,
                        adminPassword = adminPassword,
                        adminTitle = adminTitle,
                        initialUsers = memberList.filter { it.fullName.isNotBlank() && it.email.isNotBlank() },
                        strictIsolation = strictIsolation
                    )

                    viewModel.registerTenantSeparation(
                        request = request,
                        onSuccess = { res ->
                            isSubmitting = false
                            successResult = res
                        },
                        onError = { err ->
                            isSubmitting = false
                            localError = err
                        }
                    )
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .pressScale()
                    .testTag("submit_tenant_separation_btn")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Provisioning Isolated Partition & Accounts...")
                } else {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Provision Separated Tenant (${memberList.size + 1} Accounts)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // ==========================================
    // SUCCESS PROVISIONING DIALOG
    // ==========================================
    if (successResult != null) {
        val res = successResult!!
        AlertDialog(
            onDismissRequest = { /* forced action */ },
            confirmButton = {
                Button(
                    onClick = {
                        successResult = null
                        viewModel.navigateTo(Screen.ProjectSelection)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("enter_tenant_workspace_btn")
                ) {
                    Text("Enter Isolated Tenant Workspace", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SuccessEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Tenant Partition Provisioned!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tenant '${res.tenantName}' has been partitioned with dedicated Room database encapsulation.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tenant ID: ${res.tenantId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = PrimaryLight)
                            Text("Default Project: ${res.defaultProject.name}", style = MaterialTheme.typography.labelSmall)
                            Text("Admin: ${res.adminUser.fullName} (${res.adminUser.email})", style = MaterialTheme.typography.labelSmall)
                            Text("Provisioned Users: ${res.createdUsers.size + 1} active accounts", style = MaterialTheme.typography.labelSmall, color = SuccessEmerald)
                        }
                    }

                    if (res.createdUsers.isNotEmpty()) {
                        Text(
                            text = "Created Team Accounts:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        res.createdUsers.forEach { u ->
                            Text(
                                text = "• ${u.fullName} (${u.email}) — ${u.titleOrRole}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(number: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberProvisionCard(
    index: Int,
    member: TenantMemberRegistrationItem,
    onUpdate: (TenantMemberRegistrationItem) -> Unit,
    onDelete: () -> Unit
) {
    var expandedRoleDropdown by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryLight.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "User #$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (member.fullName.isNotBlank()) member.fullName else "New User",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove user",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = member.fullName,
                    onValueChange = { onUpdate(member.copy(fullName = it)) },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Jane Architect") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = member.email,
                    onValueChange = { onUpdate(member.copy(email = it)) },
                    label = { Text("Email *") },
                    placeholder = { Text("jane@org.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Role Picker
                ExposedDropdownMenuBox(
                    expanded = expandedRoleDropdown,
                    onExpandedChange = { expandedRoleDropdown = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = member.role.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoleDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRoleDropdown,
                        onDismissRequest = { expandedRoleDropdown = false }
                    ) {
                        listOf(
                            ProjectRole.ADMIN,
                            ProjectRole.ARCHITECT,
                            ProjectRole.BUSINESS_ANALYST,
                            ProjectRole.DEVELOPER,
                            ProjectRole.TESTER
                        ).forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(r.title, fontWeight = FontWeight.Bold)
                                        Text(r.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    onUpdate(member.copy(role = r))
                                    expandedRoleDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = member.password,
                    onValueChange = { onUpdate(member.copy(password = it)) },
                    label = { Text("Initial Password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
