package com.theoriongd.reqstrata.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.theoriongd.reqstrata.data.remote.mongo.repository.MongoUserDocumentRepository
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import com.theoriongd.reqstrata.domain.auth.CentralizedAuthorizationManager
import com.theoriongd.reqstrata.domain.model.PermissionLevel
import com.theoriongd.reqstrata.domain.model.ProjectAccessPolicy
import com.theoriongd.reqstrata.domain.model.ProjectModule
import com.theoriongd.reqstrata.domain.model.ProjectRole
import com.theoriongd.reqstrata.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to check and verify user authorization directly against
 * the MongoDB Atlas user collection.
 */
object MongoAuthorizationService {
    val userRepository: MongoUserDocumentRepository get() = CentralizedAuthorizationManager.userRepository

    suspend fun getVerifiedRoleFromMongo(userId: String, projectId: String? = null): ProjectRole =
        CentralizedAuthorizationManager.getVerifiedRole(userId, projectId)

    fun mapStringToRole(roleStr: String): ProjectRole =
        CentralizedAuthorizationManager.userRepository.mapStringToRole(roleStr)
}

/**
 * Route-Level Authorization Decorator.
 * Restricts access to sensitive project management, architecture, requirements,
 * and administration pages based on the user's role stored in the MongoDB user collection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorizedRoute(
    requiredRoles: Set<ProjectRole>,
    viewModel: MainViewModel,
    routeTitle: String = "Protected Resource",
    onNavigateBack: () -> Unit = { viewModel.navigateBack() },
    content: @Composable () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val localRole by viewModel.currentRole.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()

    var mongoRole by remember(currentUser?.id, currentProject?.id) { mutableStateOf<ProjectRole?>(null) }
    var isCheckingMongo by remember(currentUser?.id, currentProject?.id) { mutableStateOf(true) }

    LaunchedEffect(currentUser?.id, currentProject?.id) {
        val uid = currentUser?.id
        if (uid != null) {
            val verified = CentralizedAuthorizationManager.getVerifiedRole(uid, currentProject?.id)
            mongoRole = verified
            isCheckingMongo = false
        } else {
            isCheckingMongo = false
        }
    }

    // Role verified from MongoDB user collection, falling back to local authenticated role
    val effectiveRole = mongoRole ?: localRole
    val isAuthorized = effectiveRole in requiredRoles || effectiveRole == ProjectRole.ADMIN

    if (isCheckingMongo) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Verifying role authority...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (isAuthorized) {
        // User has the required role
        content()
    } else {
        // Display dedicated role-separated boundary page instead of redirecting
        RoleRestrictedAccessScreen(
            routeTitle = routeTitle,
            currentRole = effectiveRole,
            requiredRoles = requiredRoles,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * Dedicated Role-Separated Boundary Page displayed when a user attempts
 * to access a page outside their authorized role persona.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleRestrictedAccessScreen(
    routeTitle: String,
    currentRole: ProjectRole,
    requiredRoles: Set<ProjectRole>,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = { viewModel.navigateBack() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Access Boundary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Role-Based Separation Enforced", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Restricted Destination",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = routeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your Current Persona:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentRole.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Authorized Roles:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = requiredRoles.joinToString(", ") { it.title },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Text(
                        text = "Under Reqstrata's multi-persona architecture, this screen is isolated to specific engineering responsibilities to prevent out-of-scope modifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo(viewModel.getRoleDefaultScreen(currentRole)) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open My ${currentRole.title} Dashboard", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}

/**
 * Route-Level Authorization Decorator for NavGraphBuilder.
 * Automatically wraps destination composable with MongoDB role verification.
 */
fun NavGraphBuilder.authorizedComposable(
    route: String,
    requiredRoles: Set<ProjectRole>,
    viewModel: MainViewModel,
    routeTitle: String = "Protected Screen",
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks
    ) { backStackEntry ->
        AuthorizedRoute(
            requiredRoles = requiredRoles,
            viewModel = viewModel,
            routeTitle = routeTitle,
            onNavigateBack = { viewModel.navigateBack() }
        ) {
            content(backStackEntry)
        }
    }
}

/**
 * Route-Level Authorization Decorator by Module and PermissionLevel.
 */
fun NavGraphBuilder.authorizedModuleComposable(
    route: String,
    module: ProjectModule,
    requiredLevel: PermissionLevel = PermissionLevel.FULL,
    viewModel: MainViewModel,
    routeTitle: String = module.displayName,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    val allowedRoles = ProjectRole.entries.filter { role ->
        when (requiredLevel) {
            PermissionLevel.NONE -> true
            PermissionLevel.VIEW -> ProjectAccessPolicy.canRead(role, module)
            PermissionLevel.FULL -> ProjectAccessPolicy.canWrite(role, module)
        }
    }.toSet()

    authorizedComposable(
        route = route,
        requiredRoles = allowedRoles,
        viewModel = viewModel,
        routeTitle = routeTitle,
        arguments = arguments,
        deepLinks = deepLinks,
        content = content
    )
}
