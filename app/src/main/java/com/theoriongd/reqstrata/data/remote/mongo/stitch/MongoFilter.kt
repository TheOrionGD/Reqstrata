package com.theoriongd.reqstrata.data.remote.mongo.stitch

/**
 * MongoDB Query Filter representation for Stitch / Realm collections.
 */
sealed class MongoFilter {
    abstract fun matches(doc: MongoDocument): Boolean

    object Empty : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean = true
    }

    data class Eq(val field: String, val value: Any?) : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean {
            val docVal = doc.get(field)
            if (value == null) return docVal == null
            return docVal?.toString() == value.toString()
        }
    }

    data class Ne(val field: String, val value: Any?) : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean {
            val docVal = doc.get(field)
            return docVal?.toString() != value?.toString()
        }
    }

    data class InList(val field: String, val values: List<Any?>) : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean {
            val docVal = doc.get(field)?.toString()
            return values.any { it?.toString() == docVal }
        }
    }

    data class And(val filters: List<MongoFilter>) : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean =
            filters.all { it.matches(doc) }
    }

    data class Or(val filters: List<MongoFilter>) : MongoFilter() {
        override fun matches(doc: MongoDocument): Boolean =
            filters.any { it.matches(doc) }
    }

    companion object {
        fun empty(): MongoFilter = Empty
        fun eq(field: String, value: Any?): MongoFilter = Eq(field, value)
        fun ne(field: String, value: Any?): MongoFilter = Ne(field, value)
        fun inList(field: String, values: List<Any?>): MongoFilter = InList(field, values)
        fun and(vararg filters: MongoFilter): MongoFilter = And(filters.toList())
        fun or(vararg filters: MongoFilter): MongoFilter = Or(filters.toList())
    }
}

data class MongoChangeEvent(
    val operationType: String, // insert, update, replace, delete
    val documentId: String,
    val collectionName: String,
    val fullDocument: MongoDocument? = null,
    val timestamp: Long = System.currentTimeMillis()
)
