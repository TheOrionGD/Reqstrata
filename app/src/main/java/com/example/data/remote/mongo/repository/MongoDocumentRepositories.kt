package com.example.data.remote.mongo.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.mongo.stitch.MongoDocument
import com.example.data.remote.mongo.stitch.MongoFilter
import com.example.data.remote.mongo.stitch.MongoStitchClient
import com.example.data.remote.mongo.sync.MongoBackgroundSyncService
import com.example.domain.auth.CentralizedAuthorizationManager
import com.example.domain.model.ProjectModule
import com.example.domain.model.ProjectRole
import com.example.domain.model.RequirementPriority
import com.example.domain.model.RequirementStatus
import com.example.domain.model.RequirementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * MongoDB Document Repository Layer.
 * Connects to MongoDB Atlas using the provided URI and credentials from BuildConfig,
 * executing MongoDB document operations (insertOne, replaceOne, deleteOne, find, count)
 * and triggering real-time background sync.
 */

class MongoRequirementDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val collection = stitch.requirements
    private val versionsCollection = stitch.requirementVersions
    private val useCasesCollection = stitch.useCases

    fun getRequirements(projectId: String): Flow<List<RequirementEntity>> {
        return collection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { MongoDocument.toRequirement(it) }
        }
    }

    fun getRequirement(id: String): Flow<RequirementEntity?> {
        return collection.find(MongoFilter.eq("_id", id)).map { docs ->
            docs.firstOrNull()?.let { MongoDocument.toRequirement(it) }
        }
    }

    suspend fun getRequirementDirect(id: String): RequirementEntity? {
        val doc = collection.findById(id) ?: collection.findOne(MongoFilter.eq("_id", id))
        return doc?.let { MongoDocument.toRequirement(it) }
    }

    fun getVersions(reqId: String): Flow<List<RequirementVersionEntity>> {
        return versionsCollection.find(MongoFilter.eq("requirementId", reqId)).map { docs ->
            docs.map { doc ->
                RequirementVersionEntity(
                    id = doc.getString("_id"),
                    requirementId = doc.getString("requirementId"),
                    versionNumber = doc.getInt("versionNumber", 1),
                    title = doc.getString("title"),
                    description = doc.getString("description"),
                    acceptanceCriteriaJson = doc.getString("acceptanceCriteriaJson", "[]"),
                    changedBy = doc.getString("changedBy"),
                    changeReason = doc.getString("changeReason"),
                    timestamp = doc.getLong("timestamp", System.currentTimeMillis())
                )
            }
        }
    }

    fun getUseCases(projectId: String): Flow<List<UseCaseEntity>> {
        return useCasesCollection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { doc ->
                UseCaseEntity(
                    id = doc.getString("_id"),
                    projectId = doc.getString("projectId"),
                    code = doc.getString("code"),
                    requirementId = doc.getString("requirementId"),
                    name = doc.getString("name"),
                    actor = doc.getString("actor"),
                    goal = doc.getString("goal"),
                    preconditions = doc.getString("preconditions"),
                    postconditions = doc.getString("postconditions"),
                    mainFlowJson = doc.getString("mainFlowJson", "[]"),
                    altFlowsJson = doc.getString("altFlowsJson", "[]"),
                    exceptionFlowsJson = doc.getString("exceptionFlowsJson", "[]")
                )
            }
        }
    }

    suspend fun createRequirement(
        projectId: String,
        title: String,
        description: String,
        type: RequirementType,
        priority: RequirementPriority,
        acceptanceCriteria: List<String>,
        author: UserEntity
    ): RequirementEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = author.id,
        module = ProjectModule.REQUIREMENTS,
        actionName = "Create Requirement in MongoDB",
        projectId = projectId
    ) {
        val count = collection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "REQ-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()
        val criteriaJson = JSONArray(acceptanceCriteria).toString()

        val req = RequirementEntity(
            id = id,
            projectId = projectId,
            code = code,
            title = title.trim(),
            description = description.trim(),
            type = type.displayName,
            priority = priority.displayName,
            status = RequirementStatus.DRAFT.displayName,
            version = 1,
            authorId = author.id,
            authorName = author.fullName,
            acceptanceCriteriaJson = criteriaJson
        )

        // 1. Execute MongoDB Document operation: insertOne
        val mongoDoc = MongoDocument.fromRequirement(req)
        collection.insertOne(mongoDoc)

        // 2. Insert initial version document
        val versionDoc = MongoDocument()
            .put("_id", UUID.randomUUID().toString())
            .put("requirementId", id)
            .put("versionNumber", 1)
            .put("title", req.title)
            .put("description", req.description)
            .put("acceptanceCriteriaJson", criteriaJson)
            .put("changedBy", author.fullName)
            .put("changeReason", "Initial requirement authored")
            .put("timestamp", System.currentTimeMillis())
        versionsCollection.insertOne(versionDoc)

        // 3. Push change to Atlas in real-time via Background Sync Service
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "insert",
            documentId = id,
            document = mongoDoc
        )

        db?.requirementDao()?.insertRequirement(req)
        req
    }

    suspend fun updateRequirement(
        current: RequirementEntity,
        newTitle: String,
        newDescription: String,
        newType: RequirementType,
        newPriority: RequirementPriority,
        newCriteria: List<String>,
        changeReason: String,
        editor: UserEntity
    ) = CentralizedAuthorizationManager.authorizeMutation(
        userId = editor.id,
        module = ProjectModule.REQUIREMENTS,
        actionName = "Update Requirement in MongoDB",
        projectId = current.projectId
    ) {
        val newVersion = current.version + 1
        val criteriaJson = JSONArray(newCriteria).toString()

        val updated = current.copy(
            title = newTitle.trim(),
            description = newDescription.trim(),
            type = newType.displayName,
            priority = newPriority.displayName,
            version = newVersion,
            acceptanceCriteriaJson = criteriaJson,
            status = RequirementStatus.UNDER_REVIEW.displayName,
            updatedAt = System.currentTimeMillis()
        )

        // 1. Execute MongoDB Document operation: replaceOne
        val mongoDoc = MongoDocument.fromRequirement(updated)
        collection.replaceOne(MongoFilter.eq("_id", current.id), mongoDoc, upsert = true)

        // 2. Add version document
        val versionDoc = MongoDocument()
            .put("_id", UUID.randomUUID().toString())
            .put("requirementId", current.id)
            .put("versionNumber", newVersion)
            .put("title", newTitle.trim())
            .put("description", newDescription.trim())
            .put("acceptanceCriteriaJson", criteriaJson)
            .put("changedBy", editor.fullName)
            .put("changeReason", changeReason)
            .put("timestamp", System.currentTimeMillis())
        versionsCollection.insertOne(versionDoc)

        // 3. Trigger real-time background sync to Atlas
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "replace",
            documentId = current.id,
            document = mongoDoc
        )

        db?.requirementDao()?.updateRequirement(updated)
    }

    suspend fun setApprovalStatus(
        req: RequirementEntity,
        status: RequirementStatus,
        notes: String,
        reviewer: UserEntity
    ) = CentralizedAuthorizationManager.authorizeAction(
        userId = reviewer.id,
        allowedRoles = setOf(ProjectRole.ADMIN, ProjectRole.BUSINESS_ANALYST, ProjectRole.ARCHITECT),
        actionName = "Approve Requirement in MongoDB",
        module = ProjectModule.REQUIREMENTS,
        projectId = req.projectId
    ) {
        val updated = req.copy(
            status = status.displayName,
            approvalNotes = notes,
            updatedAt = System.currentTimeMillis()
        )
        val mongoDoc = MongoDocument.fromRequirement(updated)
        collection.replaceOne(MongoFilter.eq("_id", req.id), mongoDoc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_REQUIREMENTS,
            operation = "update",
            documentId = req.id,
            document = mongoDoc
        )

        db?.requirementDao()?.updateRequirement(updated)
    }

    suspend fun deleteRequirement(id: String, actorUserId: String? = null, projectId: String? = null) {
        CentralizedAuthorizationManager.authorizeAction(
            userId = actorUserId,
            allowedRoles = setOf(ProjectRole.ADMIN),
            actionName = "Delete Requirement in MongoDB",
            module = ProjectModule.REQUIREMENTS,
            projectId = projectId
        ) {
            collection.deleteOne(MongoFilter.eq("_id", id))
            MongoBackgroundSyncService.pushChangeRealtime(
                collection = MongoStitchClient.COLL_REQUIREMENTS,
                operation = "delete",
                documentId = id,
                document = null
            )
            db?.requirementDao()?.deleteRequirement(id)
        }
    }

    suspend fun addUseCase(
        projectId: String,
        reqId: String,
        name: String,
        actor: String,
        goal: String,
        preconditions: String,
        postconditions: String,
        mainFlow: List<String>,
        altFlows: List<String>,
        exceptionFlows: List<String>,
        authorUserId: String? = null
    ): UseCaseEntity = CentralizedAuthorizationManager.authorizeMutation(
        userId = authorUserId,
        module = ProjectModule.USE_CASES,
        actionName = "Add Use Case in MongoDB",
        projectId = projectId
    ) {
        val count = useCasesCollection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "UC-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val uc = UseCaseEntity(
            id = id,
            projectId = projectId,
            code = code,
            requirementId = reqId,
            name = name,
            actor = actor,
            goal = goal,
            preconditions = preconditions,
            postconditions = postconditions,
            mainFlowJson = JSONArray(mainFlow).toString(),
            altFlowsJson = JSONArray(altFlows).toString(),
            exceptionFlowsJson = JSONArray(exceptionFlows).toString()
        )

        val doc = MongoDocument()
            .put("_id", id)
            .put("projectId", projectId)
            .put("code", code)
            .put("requirementId", reqId)
            .put("name", name)
            .put("actor", actor)
            .put("goal", goal)
            .put("preconditions", preconditions)
            .put("postconditions", postconditions)
            .put("mainFlowJson", uc.mainFlowJson)
            .put("altFlowsJson", uc.altFlowsJson)
            .put("exceptionFlowsJson", uc.exceptionFlowsJson)
        useCasesCollection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_USE_CASES,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.requirementDao()?.insertUseCase(uc)
        uc
    }
}

class MongoArchitectureDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val compCollection = stitch.components
    private val decCollection = stitch.decisions

    fun getComponents(projectId: String): Flow<List<ArchitectureComponentEntity>> {
        return compCollection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { MongoDocument.toComponent(it) }
        }
    }

    suspend fun addComponent(
        projectId: String,
        name: String,
        style: String,
        layer: String,
        responsibilities: String,
        techStack: String,
        dependencies: List<String>,
        linkedReqIds: List<String>,
        actor: String
    ): ArchitectureComponentEntity {
        val count = compCollection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "ARCH-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val comp = ArchitectureComponentEntity(
            id = id,
            projectId = projectId,
            code = code,
            name = name.trim(),
            style = style,
            layerOrModule = layer,
            responsibilities = responsibilities,
            techStack = techStack,
            dependenciesJson = JSONArray(dependencies).toString(),
            linkedRequirementIdsJson = JSONArray(linkedReqIds).toString()
        )

        // Execute MongoDB Document operation: insertOne
        val mongoDoc = MongoDocument.fromComponent(comp)
        compCollection.insertOne(mongoDoc)

        // Real-time push to MongoDB Atlas
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_COMPONENTS,
            operation = "insert",
            documentId = id,
            document = mongoDoc
        )

        db?.architectureDao()?.insertComponent(comp)
        return comp
    }

    suspend fun deleteComponent(id: String) {
        compCollection.deleteOne(MongoFilter.eq("_id", id))
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_COMPONENTS,
            operation = "delete",
            documentId = id,
            document = null
        )
        db?.architectureDao()?.deleteComponent(id)
    }

    fun getDecisions(projectId: String): Flow<List<ArchitectureDecisionEntity>> {
        return decCollection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { doc ->
                ArchitectureDecisionEntity(
                    id = doc.getString("_id"),
                    projectId = doc.getString("projectId"),
                    code = doc.getString("code"),
                    title = doc.getString("title"),
                    status = doc.getString("status", "Accepted"),
                    context = doc.getString("context"),
                    decision = doc.getString("decision"),
                    consequences = doc.getString("consequences"),
                    timestamp = doc.getLong("timestamp", System.currentTimeMillis())
                )
            }
        }
    }

    suspend fun addDecision(
        projectId: String,
        title: String,
        context: String,
        decision: String,
        consequences: String,
        actor: String
    ): ArchitectureDecisionEntity {
        val count = decCollection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "ADR-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val adr = ArchitectureDecisionEntity(
            id = id,
            projectId = projectId,
            code = code,
            title = title.trim(),
            context = context,
            decision = decision,
            consequences = consequences
        )

        val doc = MongoDocument()
            .put("_id", id)
            .put("projectId", projectId)
            .put("code", code)
            .put("title", adr.title)
            .put("context", context)
            .put("decision", decision)
            .put("consequences", consequences)
            .put("timestamp", adr.timestamp)
        decCollection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_DECISIONS,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.architectureDao()?.insertDecision(adr)
        return adr
    }
}

class MongoDatabaseDesignDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val collection = stitch.databaseTables

    fun getEntities(projectId: String): Flow<List<DatabaseEntityRecord>> {
        return collection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { MongoDocument.toDatabaseEntity(it) }
        }
    }

    suspend fun addEntity(
        projectId: String,
        name: String,
        description: String,
        fieldsJson: String,
        relationshipsJson: String,
        indexesJson: String = "[]",
        actor: String
    ): DatabaseEntityRecord {
        val count = collection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "DB-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val entity = DatabaseEntityRecord(
            id = id,
            projectId = projectId,
            code = code,
            name = name.trim(),
            description = description.trim(),
            fieldsJson = fieldsJson,
            relationshipsJson = relationshipsJson,
            indexesJson = indexesJson
        )

        val doc = MongoDocument.fromDatabaseEntity(entity)
        collection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_DATABASE_TABLES,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.databaseDesignDao()?.insertEntity(entity)
        return entity
    }

    suspend fun deleteEntity(id: String) {
        collection.deleteOne(MongoFilter.eq("_id", id))
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_DATABASE_TABLES,
            operation = "delete",
            documentId = id,
            document = null
        )
        db?.databaseDesignDao()?.deleteEntity(id)
    }
}

class MongoApiDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val collection = stitch.apiEndpoints

    fun getEndpoints(projectId: String): Flow<List<ApiEndpointEntity>> {
        return collection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { MongoDocument.toApiEndpoint(it) }
        }
    }

    suspend fun addEndpoint(
        projectId: String,
        method: String,
        path: String,
        description: String,
        authRequired: Boolean,
        reqSchema: String,
        respSchema: String,
        linkedReqId: String,
        actor: String
    ): ApiEndpointEntity {
        val count = collection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "API-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val endpoint = ApiEndpointEntity(
            id = id,
            projectId = projectId,
            code = code,
            method = method.uppercase(),
            path = if (path.startsWith("/")) path else "/$path",
            description = description.trim(),
            authRequired = authRequired,
            requestSchema = reqSchema,
            responseSchema = respSchema,
            linkedRequirementId = linkedReqId
        )

        val doc = MongoDocument.fromApiEndpoint(endpoint)
        collection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_API_ENDPOINTS,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.apiDao()?.insertEndpoint(endpoint)
        return endpoint
    }

    suspend fun deleteEndpoint(id: String) {
        collection.deleteOne(MongoFilter.eq("_id", id))
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_API_ENDPOINTS,
            operation = "delete",
            documentId = id,
            document = null
        )
        db?.apiDao()?.deleteEndpoint(id)
    }
}

class MongoTestDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val suiteCollection = stitch.testSuites
    private val tcCollection = stitch.testCases

    fun getSuites(projectId: String): Flow<List<TestSuiteEntity>> {
        return suiteCollection.find(MongoFilter.eq("projectId", projectId)).map { docs ->
            docs.map { MongoDocument.toTestSuite(it) }
        }
    }

    fun getTestCases(suiteId: String): Flow<List<TestCaseEntity>> {
        return tcCollection.find(MongoFilter.eq("suiteId", suiteId)).map { docs ->
            docs.map { MongoDocument.toTestCase(it) }
        }
    }

    suspend fun addTestSuite(
        projectId: String,
        name: String,
        type: String,
        description: String
    ): TestSuiteEntity {
        val count = suiteCollection.countDocuments(MongoFilter.eq("projectId", projectId))
        val code = "TS-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val suite = TestSuiteEntity(
            id = id,
            projectId = projectId,
            code = code,
            name = name.trim(),
            type = type,
            description = description.trim()
        )

        val doc = MongoDocument.fromTestSuite(suite)
        suiteCollection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_TEST_SUITES,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.testDao()?.insertSuite(suite)
        return suite
    }

    suspend fun addTestCase(
        projectId: String,
        suiteId: String,
        title: String,
        preconditions: String,
        testData: String = "{}",
        steps: List<String>,
        expectedResult: String,
        priority: String,
        severity: String,
        linkedReqId: String?
    ): TestCaseEntity {
        val count = tcCollection.countDocuments(MongoFilter.eq("suiteId", suiteId))
        val code = "TC-%03d".format(count + 1)
        val id = UUID.randomUUID().toString()

        val tc = TestCaseEntity(
            id = id,
            projectId = projectId,
            code = code,
            suiteId = suiteId,
            linkedRequirementId = linkedReqId ?: "",
            title = title.trim(),
            preconditions = preconditions.trim(),
            testData = testData,
            stepsJson = JSONArray(steps).toString(),
            expectedResult = expectedResult.trim(),
            priority = priority,
            severity = severity,
            status = "Active"
        )

        val doc = MongoDocument.fromTestCase(tc)
        tcCollection.insertOne(doc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_TEST_CASES,
            operation = "insert",
            documentId = id,
            document = doc
        )

        db?.testDao()?.insertTestCase(tc)
        return tc
    }
}

class MongoProjectDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val collection = stitch.projects
    private val membersCollection = stitch.projectMembers

    fun getAllProjects(): Flow<List<ProjectEntity>> {
        return collection.find().map { docs ->
            docs.map { MongoDocument.toProject(it) }
        }
    }

    fun getProject(id: String): Flow<ProjectEntity?> {
        return collection.find(MongoFilter.eq("_id", id)).map { docs ->
            docs.firstOrNull()?.let { MongoDocument.toProject(it) }
        }
    }

    suspend fun getProjectDirect(id: String): ProjectEntity? {
        val doc = collection.findById(id) ?: collection.findOne(MongoFilter.eq("_id", id))
        return doc?.let { MongoDocument.toProject(it) }
    }

    suspend fun createProject(
        name: String,
        description: String,
        domain: String,
        projectType: String,
        techStack: String,
        methodology: String,
        visibility: String,
        owner: UserEntity
    ): ProjectEntity = CentralizedAuthorizationManager.authorizeAction(
        userId = owner.id,
        allowedRoles = setOf(ProjectRole.ADMIN),
        actionName = "Create Project in MongoDB",
        module = ProjectModule.PROJECT_MANAGEMENT
    ) {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name.trim(),
            description = description.trim(),
            domain = domain.trim(),
            projectType = projectType.trim(),
            techStack = techStack.trim(),
            methodology = methodology.trim(),
            visibility = visibility,
            ownerId = owner.id
        )

        val doc = MongoDocument.fromProject(project)
        collection.insertOne(doc)

        val member = ProjectMemberEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            userId = owner.id,
            userName = owner.fullName,
            userEmail = owner.email,
            role = ProjectRole.ADMIN.name
        )
        val memberDoc = MongoDocument()
            .put("_id", member.id)
            .put("projectId", projectId)
            .put("userId", owner.id)
            .put("userName", owner.fullName)
            .put("userEmail", owner.email)
            .put("role", member.role)
        membersCollection.insertOne(memberDoc)

        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_PROJECTS,
            operation = "insert",
            documentId = projectId,
            document = doc
        )

        db?.projectDao()?.insertProject(project)
        db?.projectDao()?.insertMember(member)
        project
    }

    suspend fun deleteProject(projectId: String, actorUserId: String? = null) {
        CentralizedAuthorizationManager.authorizeAction(
            userId = actorUserId,
            allowedRoles = setOf(ProjectRole.ADMIN),
            actionName = "Delete Project in MongoDB",
            module = ProjectModule.PROJECT_MANAGEMENT,
            projectId = projectId
        ) {
            collection.deleteOne(MongoFilter.eq("_id", projectId))
            MongoBackgroundSyncService.pushChangeRealtime(
                collection = MongoStitchClient.COLL_PROJECTS,
                operation = "delete",
                documentId = projectId,
                document = null
            )
            db?.projectDao()?.deleteProject(projectId)
        }
    }

    suspend fun getUserRole(projectId: String, userId: String): ProjectRole {
        val doc = membersCollection.findOne(
            MongoFilter.and(
                MongoFilter.eq("projectId", projectId),
                MongoFilter.eq("userId", userId)
            )
        )
        val roleStr = doc?.getString("role")
        return ProjectRole.fromString(roleStr)
    }
}
