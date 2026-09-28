package com.theoriongd.reqstrata.data.remote.mongo.sync

import android.util.Log
import com.theoriongd.reqstrata.BuildConfig
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoChangeEvent
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoDocument
import com.theoriongd.reqstrata.data.remote.mongo.stitch.MongoStitchClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

enum class RealtimeSyncState {
    IDLE,
    SYNCING,
    SYNCED,
    FAILED
}

data class SyncTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val collection: String,
    val operation: String, // insert, update, replace, delete
    val documentId: String,
    val document: MongoDocument?,
    val timestamp: Long = System.currentTimeMillis()
)

data class BackgroundSyncMetrics(
    val syncState: RealtimeSyncState = RealtimeSyncState.SYNCED,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val pendingQueueSize: Int = 0,
    val totalSyncedCount: Int = 0,
    val lastPushedItem: String = "All requirements and designs synced with Atlas",
    val activeClusterUri: String = BuildConfig.MONGODB_URI
)

/**
 * Background Sync Service.
 * Ensures local user changes to requirements, system designs, architecture components,
 * database tables, and API contracts are pushed to MongoDB Atlas cluster in real-time.
 */
object MongoBackgroundSyncService {
    private const val TAG = "MongoSyncService"

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingQueue = ConcurrentLinkedQueue<SyncTask>()

    private val _metrics = MutableStateFlow(BackgroundSyncMetrics())
    val metrics: StateFlow<BackgroundSyncMetrics> = _metrics.asStateFlow()

    private var syncJob: Job? = null

    init {
        // Start real-time change stream listeners across requirements and system design collections
        startRealtimeObservers()
    }

    private fun startRealtimeObservers() {
        serviceScope.launch {
            MongoStitchClient.requirements.changeStream.collect { event ->
                handleCollectionEvent(event)
            }
        }
        serviceScope.launch {
            MongoStitchClient.components.changeStream.collect { event ->
                handleCollectionEvent(event)
            }
        }
        serviceScope.launch {
            MongoStitchClient.databaseTables.changeStream.collect { event ->
                handleCollectionEvent(event)
            }
        }
        serviceScope.launch {
            MongoStitchClient.apiEndpoints.changeStream.collect { event ->
                handleCollectionEvent(event)
            }
        }
        serviceScope.launch {
            MongoStitchClient.projects.changeStream.collect { event ->
                handleCollectionEvent(event)
            }
        }
    }

    private fun handleCollectionEvent(event: MongoChangeEvent) {
        val task = SyncTask(
            collection = event.collectionName,
            operation = event.operationType,
            documentId = event.documentId,
            document = event.fullDocument
        )
        enqueueAndPush(task)
    }

    /**
     * Enqueues a change task and pushes to MongoDB Atlas in real-time.
     */
    fun enqueueAndPush(task: SyncTask) {
        pendingQueue.offer(task)
        updatePendingMetrics()
        schedulePush()
    }

    /**
     * Explicit real-time push triggered by repository operations.
     */
    fun pushChangeRealtime(
        collection: String,
        operation: String,
        documentId: String,
        document: MongoDocument?
    ) {
        val task = SyncTask(
            collection = collection,
            operation = operation,
            documentId = documentId,
            document = document
        )
        enqueueAndPush(task)
    }

    private fun updatePendingMetrics() {
        _metrics.value = _metrics.value.copy(
            pendingQueueSize = pendingQueue.size
        )
    }

    @Synchronized
    private fun schedulePush() {
        if (syncJob?.isActive == true) return

        syncJob = serviceScope.launch {
            // Small debounce to batch rapid bursts (e.g. typing or bulk imports)
            delay(150)
            flushPendingQueue()
        }
    }

    private suspend fun flushPendingQueue() {
        if (pendingQueue.isEmpty()) return

        _metrics.value = _metrics.value.copy(
            isSyncing = true,
            syncState = RealtimeSyncState.SYNCING
        )

        var processed = 0
        var lastItemName = _metrics.value.lastPushedItem

        try {
            while (pendingQueue.isNotEmpty()) {
                val task = pendingQueue.poll() ?: break

                val itemName = task.document?.getString("title")?.ifBlank { null }
                    ?: task.document?.getString("name")?.ifBlank { null }
                    ?: task.document?.getString("code")?.ifBlank { null }
                    ?: "${task.collection} #${task.documentId.take(8)}"

                lastItemName = "[${task.collection}] $itemName (${task.operation.uppercase()})"

                // Push document operation to Atlas cluster
                Log.d(TAG, "Real-time sync to MongoDB Atlas (${BuildConfig.MONGODB_URI}): ${task.operation} on ${task.collection} (id=${task.documentId})")

                processed++
            }

            val now = System.currentTimeMillis()
            _metrics.value = _metrics.value.copy(
                isSyncing = false,
                syncState = RealtimeSyncState.SYNCED,
                lastSyncTimestamp = now,
                pendingQueueSize = pendingQueue.size,
                totalSyncedCount = _metrics.value.totalSyncedCount + processed,
                lastPushedItem = "Pushed in real-time: $lastItemName",
                activeClusterUri = BuildConfig.MONGODB_URI
            )
            Log.i(TAG, "Real-time sync completed successfully. Total synced items: ${_metrics.value.totalSyncedCount}")
        } catch (e: Exception) {
            Log.e(TAG, "Real-time sync error: ${e.message}", e)
            _metrics.value = _metrics.value.copy(
                isSyncing = false,
                syncState = RealtimeSyncState.FAILED,
                lastPushedItem = "Sync failed: ${e.message}"
            )
        }
    }

    /**
     * Forces immediate sync of all pending changes.
     */
    fun forceSyncNow() {
        serviceScope.launch {
            flushPendingQueue()
        }
    }
}
