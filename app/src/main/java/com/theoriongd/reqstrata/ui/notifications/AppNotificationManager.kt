package com.theoriongd.reqstrata.ui.notifications

import android.content.Context
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Data model for in-app notification alerts displayed across screens.
 */
data class InAppNotificationData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.SUCCESS,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Severity / Status type for application and sync alerts.
 */
enum class NotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ALERT
}

/**
 * AppNotificationManager provides backward-compatible delegation to
 * CentralizedNotificationService for all existing UI callers.
 */
object AppNotificationManager {
    const val CHANNEL_ID = CentralizedNotificationService.CHANNEL_ID

    val inAppNotification: StateFlow<InAppNotificationData?>
        get() = CentralizedNotificationService.inAppNotification

    fun initChannel(context: Context) {
        CentralizedNotificationService.init(context)
    }

    fun notify(
        context: Context,
        title: String,
        message: String,
        type: NotificationType = NotificationType.SUCCESS
    ) {
        CentralizedNotificationService.notify(
            context = context,
            title = title,
            message = message,
            type = type,
            triggerPush = true
        )
    }

    fun showInApp(
        title: String,
        message: String,
        type: NotificationType = NotificationType.SUCCESS
    ) {
        CentralizedNotificationService.showInApp(title, message, type)
    }

    fun dismissInApp() {
        CentralizedNotificationService.dismissInApp()
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        CentralizedNotificationService.showSystemNotification(context, title, message, notificationId)
    }
}
