package com.example.data.remote.mongo.stitch

import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * MongoDB Stitch / Realm Client.
 * Connects to MongoDB Atlas using the provided URI and credentials from BuildConfig (via ENV).
 * Manages database collections and provides document-level access.
 */
object MongoStitchClient {

    // Configuration injected from ENV via BuildConfig
    val username: String = BuildConfig.MONGODB_USERNAME
    val password: String = BuildConfig.MONGODB_PASSWORD
    val uri: String = BuildConfig.MONGODB_URI
    val cluster: String by lazy {
        try {
            val atIdx = uri.indexOf("@")
            if (atIdx != -1) {
                val sub = uri.substring(atIdx + 1)
                val slashIdx = sub.indexOf("/")
                val qIdx = sub.indexOf("?")
                val end = when {
                    slashIdx != -1 -> slashIdx
                    qIdx != -1 -> qIdx
                    else -> sub.length
                }
                sub.substring(0, end)
            } else {
                "hellotheoriongd.rbxbuxe.mongodb.net"
            }
        } catch (e: Exception) {
            "hellotheoriongd.rbxbuxe.mongodb.net"
        }
    }
    const val databaseName: String = "requirement2system"

    // Collection names
    const val COLL_PROJECTS = "projects"
    const val COLL_REQUIREMENTS = "requirements"
    const val COLL_REQUIREMENT_VERSIONS = "requirement_versions"
    const val COLL_USE_CASES = "use_cases"
    const val COLL_COMPONENTS = "architecture_components"
    const val COLL_DECISIONS = "architecture_decisions"
    const val COLL_DATABASE_TABLES = "database_tables"
    const val COLL_API_ENDPOINTS = "api_endpoints"
    const val COLL_TEST_SUITES = "test_suites"
    const val COLL_TEST_CASES = "test_cases"
    const val COLL_USERS = "users"
    const val COLL_PROJECT_MEMBERS = "project_members"
    const val COLL_ACTIVITY_LOGS = "activity_logs"
    const val COLL_NOTIFICATIONS = "notifications"
    const val COLL_TRACEABILITY = "traceability_links"
    const val COLL_SETTINGS = "settings"

    private val collections = ConcurrentHashMap<String, MongoDocumentCollection>()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun getCollection(name: String): MongoDocumentCollection {
        return collections.getOrPut(name) { MongoDocumentCollection(name) }
    }

    val projects: MongoDocumentCollection get() = getCollection(COLL_PROJECTS)
    val requirements: MongoDocumentCollection get() = getCollection(COLL_REQUIREMENTS)
    val requirementVersions: MongoDocumentCollection get() = getCollection(COLL_REQUIREMENT_VERSIONS)
    val useCases: MongoDocumentCollection get() = getCollection(COLL_USE_CASES)
    val components: MongoDocumentCollection get() = getCollection(COLL_COMPONENTS)
    val decisions: MongoDocumentCollection get() = getCollection(COLL_DECISIONS)
    val databaseTables: MongoDocumentCollection get() = getCollection(COLL_DATABASE_TABLES)
    val apiEndpoints: MongoDocumentCollection get() = getCollection(COLL_API_ENDPOINTS)
    val testSuites: MongoDocumentCollection get() = getCollection(COLL_TEST_SUITES)
    val testCases: MongoDocumentCollection get() = getCollection(COLL_TEST_CASES)
    val users: MongoDocumentCollection get() = getCollection(COLL_USERS)
    val projectMembers: MongoDocumentCollection get() = getCollection(COLL_PROJECT_MEMBERS)
    val activityLogs: MongoDocumentCollection get() = getCollection(COLL_ACTIVITY_LOGS)
    val notifications: MongoDocumentCollection get() = getCollection(COLL_NOTIFICATIONS)
    val traceability: MongoDocumentCollection get() = getCollection(COLL_TRACEABILITY)
    val settings: MongoDocumentCollection get() = getCollection(COLL_SETTINGS)

    fun ping(): Boolean {
        return uri.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    }
}
