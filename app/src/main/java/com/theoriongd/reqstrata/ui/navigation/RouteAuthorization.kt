package com.theoriongd.reqstrata.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
                    text = "Verifying MongoDB User Collection Permissions...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (isAuthorized) {
        // User has the required role in MongoDB user collection
        content()
    } else {
        // Access Denied / Restricted
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Access Restricted") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Access Restricted",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Route '$routeTitle' requires administrative authorization.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Role comparison card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Role (MongoDB):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(effectiveRole.title) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Security,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Required Roles:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = requiredRoles.joinToString(", ") { it.title },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Button(
                            onClick = {
                                val target = viewModel.getRoleDefaultScreen(effectiveRole)
                                viewModel.navigateTo(target)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to My ${effectiveRole.title} Dashboard")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Return to Previous Screen")
                        }
                    }
                }
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
