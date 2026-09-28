package com.example.data.remote.mongo.stitch

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

/**
 * MongoDB Stitch / Realm Document Collection.
 * Executes MongoDB document operations (insertOne, replaceOne, updateOne, deleteOne, find, count).
 * Provides live change streams (watch) and reactive Flow streams.
 */
class MongoDocumentCollection(val name: String) {

    // Document store keyed by _id
    private val documents = ConcurrentHashMap<String, MongoDocument>()
    private val _documentsFlow = MutableStateFlow<List<MongoDocument>>(emptyList())
    private val _changeStream = MutableSharedFlow<MongoChangeEvent>(extraBufferCapacity = 64)

    val changeStream: Flow<MongoChangeEvent> = _changeStream.asSharedFlow()

    private fun notifyChanged() {
        _documentsFlow.value = documents.values.toList()
    }

    fun insertOne(document: MongoDocument): Result<MongoDocument> {
        val id = document.id.ifBlank {
            val genId = java.util.UUID.randomUUID().toString()
            document.put("_id", genId)
            genId
        }
        documents[id] = document
        notifyChanged()
        _changeStream.tryEmit(
            MongoChangeEvent(
                operationType = "insert",
                documentId = id,
                collectionName = name,
                fullDocument = document
            )
        )
        return Result.success(document)
    }

    fun insertMany(docs: List<MongoDocument>): Result<List<MongoDocument>> {
        for (d in docs) {
            val id = d.id.ifBlank {
                val genId = java.util.UUID.randomUUID().toString()
                d.put("_id", genId)
                genId
            }
            documents[id] = d
            _changeStream.tryEmit(
                MongoChangeEvent(
                    operationType = "insert",
                    documentId = id,
                    collectionName = name,
                    fullDocument = d
                )
            )
        }
        notifyChanged()
        return Result.success(docs)
    }

    fun find(filter: MongoFilter = MongoFilter.empty()): Flow<List<MongoDocument>> {
        return _documentsFlow.map { list ->
            list.filter { filter.matches(it) }
        }
    }

    fun findDirect(filter: MongoFilter = MongoFilter.empty()): List<MongoDocument> {
        return documents.values.filter { filter.matches(it) }
    }

    fun findOne(filter: MongoFilter): MongoDocument? {
        return documents.values.firstOrNull { filter.matches(it) }
    }

    fun findById(id: String): MongoDocument? {
        return documents[id]
    }

    fun replaceOne(
        filter: MongoFilter,
        replacement: MongoDocument,
        upsert: Boolean = true
    ): Boolean {
        val existing = documents.values.firstOrNull { filter.matches(it) }
        val id = existing?.id ?: replacement.id.ifBlank {
            java.util.UUID.randomUUID().toString().also { replacement.put("_id", it) }
        }

        if (existing != null || upsert) {
            documents[id] = replacement
            notifyChanged()
            _changeStream.tryEmit(
                MongoChangeEvent(
                    operationType = if (existing != null) "replace" else "insert",
                    documentId = id,
                    collectionName = name,
                    fullDocument = replacement
                )
            )
            return true
        }
        return false
    }

    fun updateOne(filter: MongoFilter, updates: MongoDocument): Boolean {
        val existing = documents.values.firstOrNull { filter.matches(it) } ?: return false
        for ((k, v) in updates.toMap()) {
            if (k != "_id") {
                existing.put(k, v)
            }
        }
        notifyChanged()
        _changeStream.tryEmit(
            MongoChangeEvent(
                operationType = "update",
                documentId = existing.id,
                collectionName = name,
                fullDocument = existing
            )
        )
        return true
    }

    fun deleteOne(filter: MongoFilter): Boolean {
        val existing = documents.values.firstOrNull { filter.matches(it) } ?: return false
        val removed = documents.remove(existing.id) != null
        if (removed) {
            notifyChanged()
            _changeStream.tryEmit(
                MongoChangeEvent(
                    operationType = "delete",
                    documentId = existing.id,
                    collectionName = name,
                    fullDocument = null
                )
            )
        }
        return removed
    }

    fun deleteById(id: String): Boolean {
        val removed = documents.remove(id) != null
        if (removed) {
            notifyChanged()
            _changeStream.tryEmit(
                MongoChangeEvent(
                    operationType = "delete",
                    documentId = id,
                    collectionName = name,
                    fullDocument = null
                )
            )
        }
        return removed
    }

    fun deleteMany(filter: MongoFilter): Int {
        val toRemove = documents.values.filter { filter.matches(it) }
        var count = 0
        for (item in toRemove) {
            if (documents.remove(item.id) != null) {
                count++
                _changeStream.tryEmit(
                    MongoChangeEvent(
                        operationType = "delete",
                        documentId = item.id,
                        collectionName = name,
                        fullDocument = null
                    )
                )
            }
        }
        if (count > 0) {
            notifyChanged()
        }
        return count
    }

    fun countDocuments(filter: MongoFilter = MongoFilter.empty()): Long {
        return documents.values.count { filter.matches(it) }.toLong()
    }

    fun clear() {
        documents.clear()
        notifyChanged()
    }
}
