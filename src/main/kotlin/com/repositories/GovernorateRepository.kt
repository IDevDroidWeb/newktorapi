package com.repositories

import com.config.DatabaseConfig
import com.models.Governorate
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates

class GovernorateRepository {
    private val collection = DatabaseConfig.database.getCollection<Governorate>(Constants.COLLECTION_GOVERNORATES)

    suspend fun create(governorate: Governorate): Governorate {
        collection.insertOne(governorate)
        return governorate
    }

    suspend fun findById(id: String): Governorate? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findByCountryId(countryId: String): List<Governorate> {
        return collection.find(Filters.eq("countryId", countryId)).toList()
    }

    suspend fun findAll(): List<Governorate> {
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