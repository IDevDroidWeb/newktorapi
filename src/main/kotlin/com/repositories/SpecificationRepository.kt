package com.repositories

import com.config.DatabaseConfig
import com.models.Specification
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates

class SpecificationRepository {
    private val collection = DatabaseConfig.database.getCollection<Specification>(Constants.COLLECTION_SPECIFICATIONS)

    suspend fun create(specification: Specification): Specification {
        collection.insertOne(specification)
        return specification
    }

    suspend fun findById(id: String): Specification? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findByIds(ids: List<ObjectId>): List<Specification> {
        return collection.find(Filters.`in`("_id", ids)).toList()
    }

    suspend fun findAll(): List<Specification> {
        return collection.find().toList()
    }

    suspend fun updateById(id: String, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(updates.map { Updates.set(it.key, it.value) })
        val result = collection.updateOne(Filters.eq("_id", id), updateDoc)
        return result.modifiedCount > 0
    }

    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}