package com.theoriongd.reqstrata.data.remote.mongo.repository

import com.theoriongd.reqstrata.data.local.AppDatabase
import com.theoriongd.reqstrata.data.local.entity.UserEntity
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import com.theoriongd.reqstrata.data.remote.mongo.sync.MongoBackgroundSyncService
import com.theoriongd.reqstrata.domain.model.ProjectRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * MongoDB User Document Repository.
 * Handles storage, query, role synchronization, and profile persistence
 * directly against the MongoDB Atlas `users` collection.
 */
class MongoUserDocumentRepository(
    private val stitch: MongoStitchClient = MongoStitchClient,
    private val db: AppDatabase? = null
) {
    private val collection = stitch.users
    private val projectMembersCollection = stitch.projectMembers

    fun getAllUsers(): Flow<List<UserEntity>> {
        return collection.find().map { docs ->
            docs.map { MongoDocument.toUser(it) }
        }
    }

    suspend fun getAllUsersDirect(): List<UserEntity> {
        val docs = collection.findDirect()
        return if (docs.isNotEmpty()) {
            docs.map { MongoDocument.toUser(it) }
        } else {
            db?.userDao()?.getAllUsersDirect() ?: emptyList()
        }
    }

    suspend fun findById(userId: String): UserEntity? {
        val doc = collection.findById(userId) ?: collection.findOne(MongoFilter.eq("_id", userId))
        if (doc != null) {
            return MongoDocument.toUser(doc)
        }
        return db?.userDao()?.getUserById(userId)
    }

    suspend fun findByEmail(email: String): UserEntity? {
        val normalized = email.trim().lowercase()
        val doc = collection.findOne(MongoFilter.eq("email", normalized))
        if (doc != null) {
            return MongoDocument.toUser(doc)
        }
        return db?.userDao()?.getUserByEmail(normalized)
    }

    suspend fun saveUser(user: UserEntity): UserEntity {
        val doc = MongoDocument.fromUser(user)
        collection.replaceOne(MongoFilter.eq("_id", user.id), doc, upsert = true)
        MongoBackgroundSyncService.pushChangeRealtime(
            collection = MongoStitchClient.COLL_USERS,
            operation = "replace",
            documentId = user.id,
            document = doc
        )
        db?.userDao()?.insertUser(user)
        return user
    }

    suspend fun saveUsers(users: List<UserEntity>) {
        for (user in users) {
            val doc = MongoDocument.fromUser(user)
            collection.replaceOne(MongoFilter.eq("_id", user.id), doc, upsert = true)
        }
    }

    /**
     * Resolves the verified role for a user directly from MongoDB.
     * Evaluates project-level role first (project_members collection),
     * then the global user role (users collection), and lastly falls back to local database.
     */
    suspend fun getVerifiedRole(userId: String, projectId: String? = null): ProjectRole {
        // 1. Check project-level assignment in MongoDB projectMembers collection
        if (!projectId.isNullOrBlank()) {
            val memberDoc = projectMembersCollection.findOne(
                MongoFilter.and(
                    MongoFilter.eq("projectId", projectId),
                    MongoFilter.eq("userId", userId)
                )
            )
            if (memberDoc != null) {
                val roleStr = memberDoc.getString("role")
                if (roleStr.isNotBlank()) {
                    return ProjectRole.fromString(roleStr)
                }
            }
        }

        // 2. Check MongoDB users collection
        val userDoc = collection.findById(userId)
            ?: collection.findOne(MongoFilter.eq("_id", userId))
            ?: collection.findOne(MongoFilter.eq("email", userId))

        if (userDoc != null) {
            val roleStr = userDoc.getString("role").ifBlank {
                userDoc.getString("titleOrRole")
            }
            if (roleStr.isNotBlank()) {
                return mapStringToRole(roleStr)
            }
        }

        // 3. Fallback to Room DB if MongoDB collection is not yet populated
        val localUser = db?.userDao()?.getUserById(userId)
            ?: db?.userDao()?.getUserByEmail(userId)
        if (localUser != null) {
            return mapStringToRole(localUser.titleOrRole)
        }

        return ProjectRole.DEVELOPER
    }

    fun mapStringToRole(roleStr: String): ProjectRole {
        val lower = roleStr.lowercase()
        return when {
            lower.contains("admin") || lower.contains("owner") || lower.contains("director") -> ProjectRole.ADMIN
            lower.contains("tester") || lower.contains("qa") || lower.contains("quality") || lower.contains("test") -> ProjectRole.TESTER
            lower.contains("architect") || lower.contains("architecture") -> ProjectRole.ARCHITECT
            lower.contains("analyst") || lower.contains("requirement") || lower.contains("ba") || lower.contains("business") -> ProjectRole.BUSINESS_ANALYST
            lower.contains("manager") -> ProjectRole.ADMIN
            lower.contains("developer") || lower.contains("engineer") || lower.contains("dev") -> ProjectRole.DEVELOPER
            else -> ProjectRole.DEVELOPER
        }
    }
}
