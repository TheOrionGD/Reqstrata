package com.example.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.remote.mongo.sync.MongoBackgroundSyncService
import com.example.data.remote.mongo.sync.RealtimeSyncState
import com.example.data.repository.CentralizedNotificationRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Centralized Notification Service.
 * Coordinates:
 * 1. In-app banner alerts with auto-dismiss
 * 2. Native Android system local push notifications
 * 3. Deep integration with MongoDB background sync events (triggers alerts on Atlas push/completion/failure)
 * 4. Persistence into CentralizedNotificationRepository
 */
object CentralizedNotificationService {
    private const val TAG = "CentralizedNotifService"

    const val CHANNEL_ID = "req2system_alerts"
    private const val CHANNEL_NAME = "System Requirements & Lifecycle Alerts"
    private const val CHANNEL_DESC = "Notifications for requirement authoring, approvals, and real-time MongoDB Atlas sync"

    private val _inAppNotification = MutableStateFlow<InAppNotificationData?>(null)
    val inAppNotification: StateFlow<InAppNotificationData?> = _inAppNotification.asStateFlow()

    private var dismissJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var appContext: Context? = null
    private var notificationRepository: CentralizedNotificationRepository? = null

    private var lastObservedSyncTimestamp: Long = 0L
    private var isSyncObserverInitialized = false

    fun init(context: Context, repository: CentralizedNotificationRepository? = null) {
        appContext = context.applicationContext
        notificationRepository = repository
        initChannel(context)
        startMongoSyncEventObserver()
    }

    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Integrates with existing MongoDB Background Sync Service events.
     * Listens to real-time sync metrics and emits alerts/push notifications.
     */
    private fun startMongoSyncEventObserver() {
        if (isSyncObserverInitialized) return
        isSyncObserverInitialized = true

        serviceScope.launch(Dispatchers.IO) {
            MongoBackgroundSyncService.metrics.collect { metrics ->
                // Check if a new sync event completed
                if (metrics.lastSyncTimestamp > lastObservedSyncTimestamp && metrics.totalSyncedCount > 0) {
                    lastObservedSyncTimestamp = metrics.lastSyncTimestamp

                    when (metrics.syncState) {
                        RealtimeSyncState.SYNCED -> {
                            val alertTitle = "MongoDB Atlas Synced"
                            val alertMessage = metrics.lastPushedItem.ifBlank { "All changes synchronized with MongoDB Atlas." }

                            withContext(Dispatchers.Main) {
                                showInApp(
                                    title = alertTitle,
                                    message = alertMessage,
                                    type = NotificationType.SUCCESS
                                )

                                appContext?.let { ctx ->
                                    showSystemNotification(
                                        context = ctx,
                                        title = alertTitle,
                                        message = alertMessage,
                                        notificationId = 1001
                                    )
                                }
                            }
                        }
                        RealtimeSyncState.FAILED -> {
                            val alertTitle = "MongoDB Sync Warning"
                            val alertMessage = metrics.lastPushedItem.ifBlank { "Sync to Atlas encountered an issue. Changes stored locally." }

                            withContext(Dispatchers.Main) {
                                showInApp(
                                    title = alertTitle,
                                    message = alertMessage,
                                    type = NotificationType.WARNING
                                )

                                appContext?.let { ctx ->
                                    showSystemNotification(
                                        context = ctx,
                                        title = alertTitle,
                                        message = alertMessage,
                                        notificationId = 1002
                                    )
                                }
                            }
                        }
                        else -> { /* IDLE or SYNCING */ }
                    }
                }
            }
        }
    }

    /**
     * Dispatch both in-app banner and local push notification.
     */
    fun notify(
        context: Context? = appContext,
        title: String,
        message: String,
        type: NotificationType = NotificationType.SUCCESS,
        triggerPush: Boolean = true,
        projectId: String = "global",
        userId: String? = null
    ) {
        // 1. Show in-app banner alert
        showInApp(title, message, type)

        // 2. Trigger native Android notification
        if (triggerPush) {
            val ctx = context ?: appContext
            ctx?.let { showSystemNotification(it, title, message) }
        }

        // 3. Record in repository if available
        if (userId != null && notificationRepository != null) {
            serviceScope.launch(Dispatchers.IO) {
                try {
                    notificationRepository?.sendNotification(
                        projectId = projectId,
                        recipientUserId = userId,
                        title = title,
                        message = message,
                        targetType = type.name
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to record notification: ${e.message}")
                }
            }
        }
    }

    fun showInApp(
        title: String,
        message: String,
        type: NotificationType = NotificationType.SUCCESS,
        durationMs: Long = 4500L
    ) {
        dismissJob?.cancel()
        _inAppNotification.value = InAppNotificationData(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type
        )

        dismissJob = serviceScope.launch {
            delay(durationMs)
            _inAppNotification.value = null
        }
    }

    fun dismissInApp() {
        dismissJob?.cancel()
        _inAppNotification.value = null
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            initChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    return
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.w(TAG, "NotificationManagerCompat error: ${e.message}")
        }
    }
}
