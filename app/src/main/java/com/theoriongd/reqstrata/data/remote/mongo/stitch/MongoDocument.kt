package com.theoriongd.reqstrata.data.remote.mongo.stitch

import com.theoriongd.reqstrata.data.local.entity.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * MongoDB Stitch / Realm Document Model.
 * Represents BSON/JSON documents stored in MongoDB Atlas collections.
 */
class MongoDocument(initialFields: Map<String, Any?> = emptyMap()) {
    private val fields = mutableMapOf<String, Any?>()

    init {
        fields.putAll(initialFields)
    }

    val id: String
        get() = fields["_id"]?.toString() ?: ""

    fun put(key: String, value: Any?): MongoDocument {
        fields[key] = value
        return this
    }

    fun get(key: String): Any? = fields[key]

    fun getString(key: String, default: String = ""): String =
        fields[key]?.toString() ?: default

    fun getLong(key: String, default: Long = 0L): Long {
        val v = fields[key] ?: return default
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: default
            else -> default
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        val v = fields[key] ?: return default
        return when (v) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: default
            else -> default
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        val v = fields[key] ?: return default
        return when (v) {
            is Boolean -> v
            is String -> v.toBoolean()
            is Number -> v.toInt() != 0
            else -> default
        }
    }

    fun getList(key: String): List<String> {
        val v = fields[key] ?: return emptyList()
        return when (v) {
            is List<*> -> v.mapNotNull { it?.toString() }
            is JSONArray -> {
                val list = mutableListOf<String>()
                for (i in 0 until v.length()) {
                    list.add(v.optString(i))
                }
                list
            }
            is String -> {
                try {
                    val arr = JSONArray(v)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.optString(i))
                    }
                    list
                } catch (e: Exception) {
                    listOf(v)
                }
            }
            else -> emptyList()
        }
    }

    fun toMap(): Map<String, Any?> = fields.toMap()

    fun toJson(): JSONObject {
        val json = JSONObject()
        for ((k, v) in fields) {
            when (v) {
                null -> json.put(k, JSONObject.NULL)
                is MongoDocument -> json.put(k, v.toJson())
                is List<*> -> json.put(k, JSONArray(v))
                else -> json.put(k, v)
            }
        }
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): MongoDocument {
            val doc = MongoDocument()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                doc.put(key, json.opt(key))
            }
            return doc
        }

        // --- Entity Converters for MongoDB Document Operations ---

        fun fromProject(project: ProjectEntity): MongoDocument {
            return MongoDocument()
                .put("_id", project.id)
                .put("name", project.name)
                .put("description", project.description)
                .put("domain", project.domain)
                .put("projectType", project.projectType)
                .put("techStack", project.techStack)
                .put("methodology", project.methodology)
                .put("visibility", project.visibility)
                .put("status", project.status)
                .put("ownerId", project.ownerId)
                .put("createdAt", project.createdAt)
                .put("updatedAt", project.updatedAt)
                .put("__realm_version", 1)
        }

        fun toProject(doc: MongoDocument): ProjectEntity {
            return ProjectEntity(
                id = doc.getString("_id"),
                name = doc.getString("name"),
                description = doc.getString("description"),
                domain = doc.getString("domain"),
                projectType = doc.getString("projectType", "Web Application"),
                techStack = doc.getString("techStack", "Kotlin / Jetpack Compose"),
                methodology = doc.getString("methodology", "Agile"),
                visibility = doc.getString("visibility", "Private"),
                status = doc.getString("status", "Active"),
                ownerId = doc.getString("ownerId"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromRequirement(req: RequirementEntity): MongoDocument {
            return MongoDocument()
                .put("_id", req.id)
                .put("projectId", req.projectId)
                .put("code", req.code)
                .put("title", req.title)
                .put("description", req.description)
                .put("type", req.type)
                .put("priority", req.priority)
                .put("status", req.status)
                .put("version", req.version)
                .put("authorId", req.authorId)
                .put("authorName", req.authorName)
                .put("acceptanceCriteriaJson", req.acceptanceCriteriaJson)
                .put("aiQualityJson", req.aiQualityJson)
                .put("dependenciesJson", req.dependenciesJson)
                .put("approvalNotes", req.approvalNotes)
                .put("createdAt", req.createdAt)
                .put("updatedAt", req.updatedAt)
                .put("__realm_collection", "requirements")
        }

        fun toRequirement(doc: MongoDocument): RequirementEntity {
            return RequirementEntity(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                title = doc.getString("title"),
                description = doc.getString("description"),
                type = doc.getString("type", "Functional"),
                priority = doc.getString("priority", "Medium"),
                status = doc.getString("status", "Draft"),
                version = doc.getInt("version", 1),
                authorId = doc.getString("authorId"),
                authorName = doc.getString("authorName"),
                acceptanceCriteriaJson = doc.getString("acceptanceCriteriaJson", "[]"),
                aiQualityJson = doc.getString("aiQualityJson", "{}"),
                dependenciesJson = doc.getString("dependenciesJson", "[]"),
                approvalNotes = doc.getString("approvalNotes", ""),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromComponent(comp: ArchitectureComponentEntity): MongoDocument {
            return MongoDocument()
                .put("_id", comp.id)
                .put("projectId", comp.projectId)
                .put("code", comp.code)
                .put("name", comp.name)
                .put("style", comp.style)
                .put("layerOrModule", comp.layerOrModule)
                .put("responsibilities", comp.responsibilities)
                .put("techStack", comp.techStack)
                .put("dependenciesJson", comp.dependenciesJson)
                .put("linkedRequirementIdsJson", comp.linkedRequirementIdsJson)
                .put("status", comp.status)
                .put("createdAt", comp.createdAt)
        }

        fun toComponent(doc: MongoDocument): ArchitectureComponentEntity {
            return ArchitectureComponentEntity(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                name = doc.getString("name"),
                style = doc.getString("style", "Microservices"),
                layerOrModule = doc.getString("layerOrModule", "Backend"),
                responsibilities = doc.getString("responsibilities"),
                techStack = doc.getString("techStack"),
                dependenciesJson = doc.getString("dependenciesJson", "[]"),
                linkedRequirementIdsJson = doc.getString("linkedRequirementIdsJson", "[]"),
                status = doc.getString("status", "Approved"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromDatabaseEntity(table: DatabaseEntityRecord): MongoDocument {
            return MongoDocument()
                .put("_id", table.id)
                .put("projectId", table.projectId)
                .put("code", table.code)
                .put("name", table.name)
                .put("description", table.description)
                .put("fieldsJson", table.fieldsJson)
                .put("relationshipsJson", table.relationshipsJson)
                .put("indexesJson", table.indexesJson)
                .put("status", table.status)
                .put("createdAt", table.createdAt)
        }

        fun toDatabaseEntity(doc: MongoDocument): DatabaseEntityRecord {
            return DatabaseEntityRecord(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                name = doc.getString("name"),
                description = doc.getString("description"),
                fieldsJson = doc.getString("fieldsJson", "[]"),
                relationshipsJson = doc.getString("relationshipsJson", "[]"),
                indexesJson = doc.getString("indexesJson", "[]"),
                status = doc.getString("status", "Approved"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromApiEndpoint(endpoint: ApiEndpointEntity): MongoDocument {
            return MongoDocument()
                .put("_id", endpoint.id)
                .put("projectId", endpoint.projectId)
                .put("code", endpoint.code)
                .put("method", endpoint.method)
                .put("path", endpoint.path)
                .put("description", endpoint.description)
                .put("authRequired", endpoint.authRequired)
                .put("requestSchema", endpoint.requestSchema)
                .put("responseSchema", endpoint.responseSchema)
                .put("validationRulesJson", endpoint.validationRulesJson)
                .put("errorResponsesJson", endpoint.errorResponsesJson)
                .put("linkedRequirementId", endpoint.linkedRequirementId)
                .put("status", endpoint.status)
                .put("createdAt", endpoint.createdAt)
        }

        fun toApiEndpoint(doc: MongoDocument): ApiEndpointEntity {
            return ApiEndpointEntity(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                method = doc.getString("method", "GET"),
                path = doc.getString("path", "/"),
                description = doc.getString("description"),
                authRequired = doc.getBoolean("authRequired", true),
                requestSchema = doc.getString("requestSchema", "{}"),
                responseSchema = doc.getString("responseSchema", "{}"),
                validationRulesJson = doc.getString("validationRulesJson", "[]"),
                errorResponsesJson = doc.getString("errorResponsesJson", "[]"),
                linkedRequirementId = doc.getString("linkedRequirementId", ""),
                status = doc.getString("status", "Approved"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromTestSuite(suite: TestSuiteEntity): MongoDocument {
            return MongoDocument()
                .put("_id", suite.id)
                .put("projectId", suite.projectId)
                .put("code", suite.code)
                .put("name", suite.name)
                .put("type", suite.type)
                .put("description", suite.description)
                .put("createdAt", suite.createdAt)
        }

        fun toTestSuite(doc: MongoDocument): TestSuiteEntity {
            return TestSuiteEntity(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                name = doc.getString("name"),
                type = doc.getString("type", "Functional"),
                description = doc.getString("description"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromTestCase(testCase: TestCaseEntity): MongoDocument {
            return MongoDocument()
                .put("_id", testCase.id)
                .put("projectId", testCase.projectId)
                .put("code", testCase.code)
                .put("suiteId", testCase.suiteId)
                .put("linkedRequirementId", testCase.linkedRequirementId)
                .put("title", testCase.title)
                .put("preconditions", testCase.preconditions)
                .put("testData", testCase.testData)
                .put("stepsJson", testCase.stepsJson)
                .put("expectedResult", testCase.expectedResult)
                .put("priority", testCase.priority)
                .put("severity", testCase.severity)
                .put("status", testCase.status)
                .put("createdAt", testCase.createdAt)
        }

        fun toTestCase(doc: MongoDocument): TestCaseEntity {
            return TestCaseEntity(
                id = doc.getString("_id"),
                projectId = doc.getString("projectId", "default"),
                code = doc.getString("code"),
                suiteId = doc.getString("suiteId"),
                linkedRequirementId = doc.getString("linkedRequirementId", ""),
                title = doc.getString("title"),
                preconditions = doc.getString("preconditions"),
                testData = doc.getString("testData", "{}"),
                stepsJson = doc.getString("stepsJson", "[]"),
                expectedResult = doc.getString("expectedResult"),
                priority = doc.getString("priority", "Medium"),
                severity = doc.getString("severity", "Major"),
                status = doc.getString("status", "Active"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }

        fun fromUser(user: UserEntity): MongoDocument {
            return MongoDocument()
                .put("_id", user.id)
                .put("fullName", user.fullName)
                .put("email", user.email)
                .put("passwordHash", user.passwordHash)
                .put("titleOrRole", user.titleOrRole)
                .put("role", user.titleOrRole)
                .put("avatarColor", user.avatarColor)
                .put("status", user.status)
                .put("tenantId", user.tenantId)
                .put("tenantName", user.tenantName)
                .put("createdAt", user.createdAt)
        }

        fun toUser(doc: MongoDocument): UserEntity {
            return UserEntity(
                id = doc.getString("_id"),
                fullName = doc.getString("fullName"),
                email = doc.getString("email"),
                passwordHash = doc.getString("passwordHash"),
                titleOrRole = doc.getString("role").ifBlank { doc.getString("titleOrRole", "Software Engineer") },
                avatarColor = doc.getLong("avatarColor", 0xFF6D28D9),
                status = doc.getString("status", "ACTIVE"),
                tenantId = doc.getString("tenantId", "tenant_default"),
                tenantName = doc.getString("tenantName", "Enterprise Core Workspace"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
