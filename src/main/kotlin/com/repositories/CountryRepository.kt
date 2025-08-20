package com.repositories

import com.config.DatabaseConfig
import com.models.Country
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates

class CountryRepository {
    private val collection = DatabaseConfig.database.getCollection<Country>(Constants.COLLECTION_COUNTRIES)

    suspend fun create(country: Country): Country {
        collection.insertOne(country)
        return country
    }

    suspend fun findById(id: String): Country? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findAll(): List<Country> {
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