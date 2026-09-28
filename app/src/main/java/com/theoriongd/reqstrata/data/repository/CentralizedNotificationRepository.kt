package com.theoriongd.reqstrata.data.repository

import android.util.Log
import com.theoriongd.reqstrata.data.local.AppDatabase
import com.theoriongd.reqstrata.data.local.entity.NotificationEntity
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoFilter
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Centralized Notification Repository.
 * Handles both local Room persistence and remote MongoDB Atlas synchronization
 * for application notifications, lifecycle alerts, and background sync events.
 */
class CentralizedNotificationRepository(
    private val db: AppDatabase,
    private val stitch: MongoStitchClient = MongoStitchClient
) {
    private val notifDao = db.notificationDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mongoCollection = stitch.notifications

    private companion object {
        const val TAG = "CentralizedNotifRepo"
    }

    fun getNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notifDao.getNotifications(userId).flowOn(Dispatchers.IO)
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notifDao.getUnreadCount(userId).flowOn(Dispatchers.IO)
    }

    suspend fun sendNotification(
        projectId: String,
        recipientUserId: String,
        title: String,
        message: String,
        targetType: String = "ALERT",
        targetId: String = ""
    ): NotificationEntity {
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            recipientUserId = recipientUserId,
            title = title,
            message = message,
            targetType = targetType,
            targetId = targetId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )

        // 1. Save in local Room database
        notifDao.insertNotification(notif)

        // 2. Synchronize to MongoDB Atlas notifications collection in background
        scope.launch {
            try {
                val doc = MongoDocument()
                    .put("_id", notif.id)
                    .put("projectId", notif.projectId)
                    .put("recipientUserId", notif.recipientUserId)
                    .put("title", notif.title)
                    .put("message", notif.message)
                    .put("targetType", notif.targetType)
                    .put("targetId", notif.targetId)
                    .put("isRead", notif.isRead)
                    .put("createdAt", notif.createdAt)
                    .put("__mongo_collection", "notifications")

                mongoCollection.insertOne(doc)
                Log.d(TAG, "Notification synced to MongoDB Atlas: ${notif.title}")
            } catch (e: Exception) {
                Log.w(TAG, "MongoDB notification sync deferred: ${e.message}")
            }
        }

        return notif
    }

    suspend fun markAsRead(id: String) {
        notifDao.markAsRead(id)
        scope.launch {
            try {
                mongoCollection.updateOne(
                    filter = MongoFilter.eq("_id", id),
                    updates = MongoDocument().put("isRead", true)
                )
            } catch (e: Exception) {
                Log.w(TAG, "MongoDB markAsRead sync deferred: ${e.message}")
            }
        }
    }

    suspend fun markAllAsRead(userId: String) {
        notifDao.markAllAsRead(userId)
        scope.launch {
            try {
                val userDocs = mongoCollection.findDirect(MongoFilter.eq("recipientUserId", userId))
                for (doc in userDocs) {
                    mongoCollection.updateOne(
                        filter = MongoFilter.eq("_id", doc.id),
                        updates = MongoDocument().put("isRead", true)
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "MongoDB markAllAsRead sync deferred: ${e.message}")
            }
        }
    }
}
