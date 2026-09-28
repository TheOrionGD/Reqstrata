package com.example.data.remote.mongo.model

import com.example.data.local.entity.*
import com.example.data.remote.mongo.stitch.MongoDocument
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * MongoDB Document-Compatible Kotlin Data Classes.
 * Represents core system entities stored in MongoDB Atlas collections.
 * Compatible with BSON/JSON document representations, Stitch/Realm, and Room local entities.
 */

// 1. Project Entity
data class Project(
    val _id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val domain: String,
    val projectType: String = "Web Application",
    val techStack: String = "Kotlin / Jetpack Compose",
    val methodology: String = "Agile",
    val visibility: String = "Private",
    val status: String = "Active", // Active, Archived
    val ownerId: String,
    val teamMemberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    fun toMongoDocument(): MongoDocument {
        return MongoDocument()
            .put("_id", _id)
            .put("name", name)
            .put("description", description)
            .put("domain", domain)
            .put("projectType", projectType)
            .put("techStack", techStack)
            .put("methodology", methodology)
            .put("visibility", visibility)
            .put("status", status)
            .put("ownerId", ownerId)
            .put("teamMemberIds", teamMemberIds)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("metadata", JSONObject(metadata).toString())
            .put("__mongo_collection", "projects")
    }

    fun toEntity(): ProjectEntity {
        return ProjectEntity(
            id = _id,
            name = name,
            description = description,
            domain = domain,
            projectType = projectType,
            techStack = techStack,
            methodology = methodology,
            visibility = visibility,
            status = status,
            ownerId = ownerId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): Project {
            val metaStr = doc.getString("metadata", "{}")
            val metaMap = mutableMapOf<String, String>()
            try {
                val j = JSONObject(metaStr)
                j.keys().forEach { k -> metaMap[k] = j.optString(k) }
            } catch (_: Exception) {}

            return Project(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
                name = doc.getString("name"),
                description = doc.getString("description"),
                domain = doc.getString("domain"),
                projectType = doc.getString("projectType", "Web Application"),
                techStack = doc.getString("techStack", "Kotlin / Jetpack Compose"),
                methodology = doc.getString("methodology", "Agile"),
                visibility = doc.getString("visibility", "Private"),
                status = doc.getString("status", "Active"),
                ownerId = doc.getString("ownerId"),
                teamMemberIds = doc.getList("teamMemberIds"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis()),
                metadata = metaMap
            )
        }

        fun fromEntity(entity: ProjectEntity): Project {
            return Project(
                _id = entity.id,
                name = entity.name,
                description = entity.description,
                domain = entity.domain,
                projectType = entity.projectType,
                techStack = entity.techStack,
                methodology = entity.methodology,
                visibility = entity.visibility,
                status = entity.status,
                ownerId = entity.ownerId,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}

// 2. Requirement Entity
data class Requirement(
    val _id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val code: String, // e.g. REQ-001
    val title: String,
    val description: String,
    val type: String = "Functional", // Functional, Non-Functional, Security, etc.
    val priority: String = "Medium", // Critical, High, Medium, Low
    val status: String = "Draft", // Draft, Under Review, Approved, Rejected, Deprecated
    val version: Int = 1,
    val authorId: String,
    val authorName: String,
    val acceptanceCriteria: List<String> = emptyList(),
    val aiQualityScore: Map<String, Int> = emptyMap(),
    val dependencies: List<String> = emptyList(),
    val linkedUseCaseIds: List<String> = emptyList(),
    val approvalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMongoDocument(): MongoDocument {
        val aiScoreJson = JSONObject()
        aiQualityScore.forEach { (k, v) -> aiScoreJson.put(k, v) }

        return MongoDocument()
            .put("_id", _id)
            .put("projectId", projectId)
            .put("code", code)
            .put("title", title)
            .put("description", description)
            .put("type", type)
            .put("priority", priority)
            .put("status", status)
            .put("version", version)
            .put("authorId", authorId)
            .put("authorName", authorName)
            .put("acceptanceCriteria", JSONArray(acceptanceCriteria).toString())
            .put("aiQualityScore", aiScoreJson.toString())
            .put("dependencies", JSONArray(dependencies).toString())
            .put("linkedUseCaseIds", JSONArray(linkedUseCaseIds).toString())
            .put("approvalNotes", approvalNotes)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("__mongo_collection", "requirements")
    }

    fun toEntity(): RequirementEntity {
        val aiScoreJson = JSONObject()
        aiQualityScore.forEach { (k, v) -> aiScoreJson.put(k, v) }

        return RequirementEntity(
            id = _id,
            projectId = projectId,
            code = code,
            title = title,
            description = description,
            type = type,
            priority = priority,
            status = status,
            version = version,
            authorId = authorId,
            authorName = authorName,
            acceptanceCriteriaJson = JSONArray(acceptanceCriteria).toString(),
            aiQualityJson = aiScoreJson.toString(),
            dependenciesJson = JSONArray(dependencies).toString(),
            approvalNotes = approvalNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): Requirement {
            val acList = doc.getList("acceptanceCriteria")
            val depList = doc.getList("dependencies")
            val ucList = doc.getList("linkedUseCaseIds")

            val aiScoreMap = mutableMapOf<String, Int>()
            val aiScoreStr = doc.getString("aiQualityScore", "{}")
            try {
                val j = JSONObject(aiScoreStr)
                j.keys().forEach { k -> aiScoreMap[k] = j.optInt(k, 0) }
            } catch (_: Exception) {}

            return Requirement(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
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
                acceptanceCriteria = acList,
                aiQualityScore = aiScoreMap,
                dependencies = depList,
                linkedUseCaseIds = ucList,
                approvalNotes = doc.getString("approvalNotes", ""),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromEntity(entity: RequirementEntity): Requirement {
            val acList = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.acceptanceCriteriaJson)
                for (i in 0 until arr.length()) acList.add(arr.optString(i))
            } catch (_: Exception) {}

            val depList = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.dependenciesJson)
                for (i in 0 until arr.length()) depList.add(arr.optString(i))
            } catch (_: Exception) {}

            val scoreMap = mutableMapOf<String, Int>()
            try {
                val j = JSONObject(entity.aiQualityJson)
                j.keys().forEach { k -> scoreMap[k] = j.optInt(k, 0) }
            } catch (_: Exception) {}

            return Requirement(
                _id = entity.id,
                projectId = entity.projectId,
                code = entity.code,
                title = entity.title,
                description = entity.description,
                type = entity.type,
                priority = entity.priority,
                status = entity.status,
                version = entity.version,
                authorId = entity.authorId,
                authorName = entity.authorName,
                acceptanceCriteria = acList,
                aiQualityScore = scoreMap,
                dependencies = depList,
                approvalNotes = entity.approvalNotes,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}

// 3. UseCase Entity
data class UseCase(
    val _id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val requirementId: String = "",
    val code: String, // e.g. UC-001
    val name: String,
    val actor: String,
    val goal: String,
    val preconditions: String = "",
    val postconditions: String = "",
    val mainFlow: List<String> = emptyList(),
    val alternativeFlows: List<String> = emptyList(),
    val exceptionFlows: List<String> = emptyList(),
    val status: String = "Approved",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMongoDocument(): MongoDocument {
        return MongoDocument()
            .put("_id", _id)
            .put("projectId", projectId)
            .put("requirementId", requirementId)
            .put("code", code)
            .put("name", name)
            .put("actor", actor)
            .put("goal", goal)
            .put("preconditions", preconditions)
            .put("postconditions", postconditions)
            .put("mainFlow", JSONArray(mainFlow).toString())
            .put("alternativeFlows", JSONArray(alternativeFlows).toString())
            .put("exceptionFlows", JSONArray(exceptionFlows).toString())
            .put("status", status)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("__mongo_collection", "use_cases")
    }

    fun toEntity(): UseCaseEntity {
        return UseCaseEntity(
            id = _id,
            projectId = projectId,
            code = code,
            requirementId = requirementId,
            name = name,
            actor = actor,
            goal = goal,
            preconditions = preconditions,
            postconditions = postconditions,
            mainFlowJson = JSONArray(mainFlow).toString(),
            altFlowsJson = JSONArray(alternativeFlows).toString(),
            exceptionFlowsJson = JSONArray(exceptionFlows).toString(),
            status = status,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): UseCase {
            return UseCase(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
                projectId = doc.getString("projectId"),
                requirementId = doc.getString("requirementId", ""),
                code = doc.getString("code"),
                name = doc.getString("name"),
                actor = doc.getString("actor"),
                goal = doc.getString("goal"),
                preconditions = doc.getString("preconditions", ""),
                postconditions = doc.getString("postconditions", ""),
                mainFlow = doc.getList("mainFlow"),
                alternativeFlows = doc.getList("alternativeFlows"),
                exceptionFlows = doc.getList("exceptionFlows"),
                status = doc.getString("status", "Approved"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromEntity(entity: UseCaseEntity): UseCase {
            val mf = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.mainFlowJson)
                for (i in 0 until arr.length()) mf.add(arr.optString(i))
            } catch (_: Exception) {}

            val af = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.altFlowsJson)
                for (i in 0 until arr.length()) af.add(arr.optString(i))
            } catch (_: Exception) {}

            val ef = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.exceptionFlowsJson)
                for (i in 0 until arr.length()) ef.add(arr.optString(i))
            } catch (_: Exception) {}

            return UseCase(
                _id = entity.id,
                projectId = entity.projectId,
                requirementId = entity.requirementId,
                code = entity.code,
                name = entity.name,
                actor = entity.actor,
                goal = entity.goal,
                preconditions = entity.preconditions,
                postconditions = entity.postconditions,
                mainFlow = mf,
                alternativeFlows = af,
                exceptionFlows = ef,
                status = entity.status,
                createdAt = entity.createdAt
            )
        }
    }
}

// 4. ArchitectureDecision Entity (ADR)
data class ArchitectureDecision(
    val _id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val code: String, // e.g. ADR-001
    val title: String,
    val status: String = "Accepted", // Proposed, Accepted, Superseded, Rejected
    val context: String,
    val decision: String,
    val consequences: String,
    val authorId: String = "",
    val supersededBy: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMongoDocument(): MongoDocument {
        return MongoDocument()
            .put("_id", _id)
            .put("projectId", projectId)
            .put("code", code)
            .put("title", title)
            .put("status", status)
            .put("context", context)
            .put("decision", decision)
            .put("consequences", consequences)
            .put("authorId", authorId)
            .put("supersededBy", supersededBy)
            .put("timestamp", timestamp)
            .put("updatedAt", updatedAt)
            .put("__mongo_collection", "architecture_decisions")
    }

    fun toEntity(): ArchitectureDecisionEntity {
        return ArchitectureDecisionEntity(
            id = _id,
            projectId = projectId,
            code = code,
            title = title,
            status = status,
            context = context,
            decision = decision,
            consequences = consequences,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): ArchitectureDecision {
            return ArchitectureDecision(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                title = doc.getString("title"),
                status = doc.getString("status", "Accepted"),
                context = doc.getString("context"),
                decision = doc.getString("decision"),
                consequences = doc.getString("consequences"),
                authorId = doc.getString("authorId", ""),
                supersededBy = doc.getString("supersededBy").ifBlank { null },
                timestamp = doc.getLong("timestamp", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromEntity(entity: ArchitectureDecisionEntity): ArchitectureDecision {
            return ArchitectureDecision(
                _id = entity.id,
                projectId = entity.projectId,
                code = entity.code,
                title = entity.title,
                status = entity.status,
                context = entity.context,
                decision = entity.decision,
                consequences = entity.consequences,
                timestamp = entity.timestamp
            )
        }
    }
}

// 5. APIEndpoint Entity
data class APIEndpoint(
    val _id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val code: String, // e.g. API-001
    val method: String = "GET", // GET, POST, PUT, DELETE, PATCH
    val path: String = "/",
    val description: String,
    val authRequired: Boolean = true,
    val requestSchema: String = "{}",
    val responseSchema: String = "{}",
    val validationRules: List<String> = emptyList(),
    val errorResponses: List<String> = emptyList(),
    val linkedRequirementId: String = "",
    val status: String = "Approved",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMongoDocument(): MongoDocument {
        return MongoDocument()
            .put("_id", _id)
            .put("projectId", projectId)
            .put("code", code)
            .put("method", method)
            .put("path", path)
            .put("description", description)
            .put("authRequired", authRequired)
            .put("requestSchema", requestSchema)
            .put("responseSchema", responseSchema)
            .put("validationRules", JSONArray(validationRules).toString())
            .put("errorResponses", JSONArray(errorResponses).toString())
            .put("linkedRequirementId", linkedRequirementId)
            .put("status", status)
            .put("tags", JSONArray(tags).toString())
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("__mongo_collection", "api_endpoints")
    }

    fun toEntity(): ApiEndpointEntity {
        return ApiEndpointEntity(
            id = _id,
            projectId = projectId,
            code = code,
            method = method,
            path = path,
            description = description,
            authRequired = authRequired,
            requestSchema = requestSchema,
            responseSchema = responseSchema,
            validationRulesJson = JSONArray(validationRules).toString(),
            errorResponsesJson = JSONArray(errorResponses).toString(),
            linkedRequirementId = linkedRequirementId,
            status = status,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): APIEndpoint {
            return APIEndpoint(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                method = doc.getString("method", "GET"),
                path = doc.getString("path", "/"),
                description = doc.getString("description"),
                authRequired = doc.getBoolean("authRequired", true),
                requestSchema = doc.getString("requestSchema", "{}"),
                responseSchema = doc.getString("responseSchema", "{}"),
                validationRules = doc.getList("validationRules"),
                errorResponses = doc.getList("errorResponses"),
                linkedRequirementId = doc.getString("linkedRequirementId", ""),
                status = doc.getString("status", "Approved"),
                tags = doc.getList("tags"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromEntity(entity: ApiEndpointEntity): APIEndpoint {
            val vr = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.validationRulesJson)
                for (i in 0 until arr.length()) vr.add(arr.optString(i))
            } catch (_: Exception) {}

            val er = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.errorResponsesJson)
                for (i in 0 until arr.length()) er.add(arr.optString(i))
            } catch (_: Exception) {}

            return APIEndpoint(
                _id = entity.id,
                projectId = entity.projectId,
                code = entity.code,
                method = entity.method,
                path = entity.path,
                description = entity.description,
                authRequired = entity.authRequired,
                requestSchema = entity.requestSchema,
                responseSchema = entity.responseSchema,
                validationRules = vr,
                errorResponses = er,
                linkedRequirementId = entity.linkedRequirementId,
                status = entity.status,
                createdAt = entity.createdAt
            )
        }
    }
}

// 6. Task Entity (Development Task)
data class Task(
    val _id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val code: String, // e.g. DEV-001
    val title: String,
    val description: String,
    val priority: String = "Medium", // Critical, High, Medium, Low
    val status: String = "Backlog", // Backlog, Ready, In Progress, Blocked, In Review, Completed
    val assigneeId: String = "",
    val assigneeName: String = "Unassigned",
    val linkedRequirementId: String = "",
    val linkedComponentId: String = "",
    val linkedApiId: String = "",
    val estimatedHours: Int = 4,
    val actualHours: Int = 0,
    val technicalNotes: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMongoDocument(): MongoDocument {
        return MongoDocument()
            .put("_id", _id)
            .put("projectId", projectId)
            .put("code", code)
            .put("title", title)
            .put("description", description)
            .put("priority", priority)
            .put("status", status)
            .put("assigneeId", assigneeId)
            .put("assigneeName", assigneeName)
            .put("linkedRequirementId", linkedRequirementId)
            .put("linkedComponentId", linkedComponentId)
            .put("linkedApiId", linkedApiId)
            .put("estimatedHours", estimatedHours)
            .put("actualHours", actualHours)
            .put("technicalNotes", technicalNotes)
            .put("tags", JSONArray(tags).toString())
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("__mongo_collection", "development_tasks")
    }

    fun toEntity(): TaskEntity {
        return TaskEntity(
            id = _id,
            projectId = projectId,
            code = code,
            title = title,
            description = description,
            priority = priority,
            status = status,
            assigneeId = assigneeId,
            assigneeName = assigneeName,
            linkedRequirementId = linkedRequirementId,
            linkedComponentId = linkedComponentId,
            linkedApiId = linkedApiId,
            estimatedHours = estimatedHours,
            technicalNotes = technicalNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromMongoDocument(doc: MongoDocument): Task {
            return Task(
                _id = doc.getString("_id", UUID.randomUUID().toString()),
                projectId = doc.getString("projectId"),
                code = doc.getString("code"),
                title = doc.getString("title"),
                description = doc.getString("description"),
                priority = doc.getString("priority", "Medium"),
                status = doc.getString("status", "Backlog"),
                assigneeId = doc.getString("assigneeId", ""),
                assigneeName = doc.getString("assigneeName", "Unassigned"),
                linkedRequirementId = doc.getString("linkedRequirementId", ""),
                linkedComponentId = doc.getString("linkedComponentId", ""),
                linkedApiId = doc.getString("linkedApiId", ""),
                estimatedHours = doc.getInt("estimatedHours", 4),
                actualHours = doc.getInt("actualHours", 0),
                technicalNotes = doc.getString("technicalNotes", ""),
                tags = doc.getList("tags"),
                createdAt = doc.getLong("createdAt", System.currentTimeMillis()),
                updatedAt = doc.getLong("updatedAt", System.currentTimeMillis())
            )
        }

        fun fromEntity(entity: TaskEntity): Task {
            return Task(
                _id = entity.id,
                projectId = entity.projectId,
                code = entity.code,
                title = entity.title,
                description = entity.description,
                priority = entity.priority,
                status = entity.status,
                assigneeId = entity.assigneeId,
                assigneeName = entity.assigneeName,
                linkedRequirementId = entity.linkedRequirementId,
                linkedComponentId = entity.linkedComponentId,
                linkedApiId = entity.linkedApiId,
                estimatedHours = entity.estimatedHours,
                technicalNotes = entity.technicalNotes,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}
