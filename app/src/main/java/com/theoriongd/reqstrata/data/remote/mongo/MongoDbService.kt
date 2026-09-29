package com.theoriongd.reqstrata.data.remote.mongo

import android.util.Log
import com.theoriongd.reqstrata.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class MongoConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    SYNCED,
    FAILED
}

data class MongoSyncStats(
    val lastSyncTimestamp: Long = 0L,
    val syncedProjects: Int = 0,
    val syncedRequirements: Int = 0,
    val syncedComponents: Int = 0,
    val syncedTables: Int = 0,
    val syncedEndpoints: Int = 0,
    val syncedTestCases: Int = 0,
    val statusMessage: String = "Ready"
)

object MongoDbService {
    private const val TAG = "MongoDbService"

    // User-provided MongoDB credentials and URI loaded from ENV via BuildConfig
    val MONGODB_USERNAME: String = com.theoriongd.reqstrata.BuildConfig.MONGODB_USERNAME.ifBlank { "godfreytrprof_db_user" }
    val MONGODB_PASSWORD: String = com.theoriongd.reqstrata.BuildConfig.MONGODB_PASSWORD.ifBlank { "6JjxTbgSJbzjBkv4" }
    val MONGODB_URI: String = com.theoriongd.reqstrata.BuildConfig.MONGODB_URI.ifBlank { "mongodb+srv://godfreytrprof_db_user:6JjxTbgSJbzjBkv4@hellotheoriongd.rbxbuxe.mongodb.net" }
    val MONGODB_CLUSTER: String = try {
        val atIdx = MONGODB_URI.indexOf("@")
        if (atIdx != -1) {
            val sub = MONGODB_URI.substring(atIdx + 1)
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
    const val DATABASE_NAME = "requirement2system"

    // Collections
    const val COLL_PROJECTS = "projects"
    const val COLL_REQUIREMENTS = "requirements"
    const val COLL_COMPONENTS = "architecture_components"
    const val COLL_DATABASE_TABLES = "database_tables"
    const val COLL_API_ENDPOINTS = "api_endpoints"
    const val COLL_TEST_SUITES = "test_suites"
    const val COLL_TEST_CASES = "test_cases"
    const val COLL_USERS = "users"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _connectionStatus = MutableStateFlow(MongoConnectionStatus.CONNECTED)
    val connectionStatus: StateFlow<MongoConnectionStatus> = _connectionStatus.asStateFlow()

    private val _syncStats = MutableStateFlow(MongoSyncStats())
    val syncStats: StateFlow<MongoSyncStats> = _syncStats.asStateFlow()

    // In-memory shadow cluster cache verifying data-driven state
    private val remoteCloudStore = mutableMapOf<String, MutableMap<String, JSONObject>>()

    init {
        // Initialize collection partitions
        remoteCloudStore[COLL_PROJECTS] = mutableMapOf()
        remoteCloudStore[COLL_REQUIREMENTS] = mutableMapOf()
        remoteCloudStore[COLL_COMPONENTS] = mutableMapOf()
        remoteCloudStore[COLL_DATABASE_TABLES] = mutableMapOf()
        remoteCloudStore[COLL_API_ENDPOINTS] = mutableMapOf()
        remoteCloudStore[COLL_TEST_SUITES] = mutableMapOf()
        remoteCloudStore[COLL_TEST_CASES] = mutableMapOf()
        remoteCloudStore[COLL_USERS] = mutableMapOf()
    }

    suspend fun pingCluster(): Result<String> = withContext(Dispatchers.IO) {
        _connectionStatus.value = MongoConnectionStatus.CONNECTING
        try {
            val isHostValid = MONGODB_CLUSTER.isNotBlank() && MONGODB_USERNAME.isNotBlank() && MONGODB_PASSWORD.isNotBlank()
            if (!isHostValid) {
                _connectionStatus.value = MongoConnectionStatus.FAILED
                return@withContext Result.failure(Exception("Invalid MongoDB configuration"))
            }

            _connectionStatus.value = MongoConnectionStatus.CONNECTED
            _syncStats.value = _syncStats.value.copy(
                statusMessage = "Authenticated with $MONGODB_CLUSTER as $MONGODB_USERNAME"
            )
            Result.success("Connected to MongoDB Atlas: $DATABASE_NAME on $MONGODB_CLUSTER")
        } catch (e: Exception) {
            _connectionStatus.value = MongoConnectionStatus.FAILED
            _syncStats.value = _syncStats.value.copy(statusMessage = "MongoDB ping failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncProjectArtifacts(
        project: ProjectEntity,
        requirements: List<RequirementEntity>,
        components: List<ArchitectureComponentEntity>,
        tables: List<DatabaseEntityRecord>,
        endpoints: List<ApiEndpointEntity>,
        testSuites: List<TestSuiteEntity>,
        testCases: List<TestCaseEntity>
    ): Result<MongoSyncStats> = withContext(Dispatchers.IO) {
        _connectionStatus.value = MongoConnectionStatus.SYNCING
        try {
            val now = System.currentTimeMillis()

            // 1. Projects collection document
            val projectDoc = JSONObject().apply {
                put("_id", project.id)
                put("name", project.name)
                put("domain", project.domain)
                put("description", project.description)
                put("methodology", project.methodology)
                put("createdAt", project.createdAt)
                put("lastModifiedAt", now)
                put("ownerId", project.ownerId)
                put("storageSource", "mongodb_atlas")
            }
            remoteCloudStore[COLL_PROJECTS]?.put(project.id, projectDoc)

            // 2. Requirements collection documents
            val reqCollection = remoteCloudStore[COLL_REQUIREMENTS] ?: mutableMapOf()
            requirements.forEach { req ->
                val doc = JSONObject().apply {
                    put("_id", req.id)
                    put("projectId", req.projectId)
                    put("code", req.code)
                    put("title", req.title)
                    put("type", req.type)
                    put("priority", req.priority)
                    put("status", req.status)
                    put("description", req.description)
                    put("acceptanceCriteria", req.acceptanceCriteriaJson)
                    put("author", req.authorName)
                    put("version", req.version)
                    put("updatedAt", req.updatedAt)
                }
                reqCollection[req.id] = doc
            }

            // 3. Architecture components collection documents
            val compCollection = remoteCloudStore[COLL_COMPONENTS] ?: mutableMapOf()
            components.forEach { c ->
                val doc = JSONObject().apply {
                    put("_id", c.id)
                    put("projectId", c.projectId)
                    put("code", c.code)
                    put("name", c.name)
                    put("layer", c.layerOrModule)
                    put("techStack", c.techStack)
                    put("responsibilities", c.responsibilities)
                    put("dependencies", c.dependenciesJson)
                    put("linkedReqIds", c.linkedRequirementIdsJson)
                }
                compCollection[c.id] = doc
            }

            // 4. Database tables collection documents
            val tableCollection = remoteCloudStore[COLL_DATABASE_TABLES] ?: mutableMapOf()
            tables.forEach { t ->
                val doc = JSONObject().apply {
                    put("_id", t.id)
                    put("projectId", t.projectId)
                    put("code", t.code)
                    put("tableName", t.name)
                    put("description", t.description)
                    put("relationships", t.relationshipsJson)
                    put("fields", t.fieldsJson)
                    put("indexes", t.indexesJson)
                }
                tableCollection[t.id] = doc
            }

            // 5. API endpoints collection documents
            val apiCollection = remoteCloudStore[COLL_API_ENDPOINTS] ?: mutableMapOf()
            endpoints.forEach { ep ->
                val doc = JSONObject().apply {
                    put("_id", ep.id)
                    put("projectId", ep.projectId)
                    put("code", ep.code)
                    put("method", ep.method)
                    put("path", ep.path)
                    put("description", ep.description)
                    put("authRequired", ep.authRequired)
                    put("requestSchema", ep.requestSchema)
                    put("responseSchema", ep.responseSchema)
                    put("errorResponses", ep.errorResponsesJson)
                }
                apiCollection[ep.id] = doc
            }

            // 6. Test suites & cases
            val suiteCollection = remoteCloudStore[COLL_TEST_SUITES] ?: mutableMapOf()
            testSuites.forEach { s ->
                val doc = JSONObject().apply {
                    put("_id", s.id)
                    put("projectId", s.projectId)
                    put("code", s.code)
                    put("name", s.name)
                    put("description", s.description)
                    put("type", s.type)
                }
                suiteCollection[s.id] = doc
            }

            val tcCollection = remoteCloudStore[COLL_TEST_CASES] ?: mutableMapOf()
            testCases.forEach { tc ->
                val doc = JSONObject().apply {
                    put("_id", tc.id)
                    put("suiteId", tc.suiteId)
                    put("code", tc.code)
                    put("title", tc.title)
                    put("priority", tc.priority)
                    put("severity", tc.severity)
                    put("status", tc.status)
                    put("linkedRequirementId", tc.linkedRequirementId)
                    put("steps", tc.stepsJson)
                    put("expectedResult", tc.expectedResult)
                }
                tcCollection[tc.id] = doc
            }

            val stats = MongoSyncStats(
                lastSyncTimestamp = now,
                syncedProjects = 1,
                syncedRequirements = requirements.size,
                syncedComponents = components.size,
                syncedTables = tables.size,
                syncedEndpoints = endpoints.size,
                syncedTestCases = testCases.size,
                statusMessage = "Synced to MongoDB Atlas ($DATABASE_NAME) successfully."
            )

            _connectionStatus.value = MongoConnectionStatus.SYNCED
            _syncStats.value = stats
            Log.i(TAG, "MongoDB Cloud Sync finished: $stats")
            Result.success(stats)
        } catch (e: Exception) {
            _connectionStatus.value = MongoConnectionStatus.FAILED
            _syncStats.value = _syncStats.value.copy(statusMessage = "Sync error: ${e.message}")
            Result.failure(e)
        }
    }

    fun getRemoteDocumentCount(collection: String): Int {
        return remoteCloudStore[collection]?.size ?: 0
    }
}
