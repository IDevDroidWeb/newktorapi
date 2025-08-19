package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.Onboarding
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class OnboardingRepository {
    private val collection = DatabaseConfig.database.getCollection<Onboarding>(Constants.COLLECTION_ONBOARDING)

    suspend fun create(onboarding: Onboarding): Onboarding {
        collection.insertOne(onboarding)
        return onboarding
    }

    suspend fun findById(id: ObjectId): Onboarding? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findAll(): List<Onboarding> {
        return collection.find().sort(Sorts.ascending("order")).toList()
    }

    suspend fun updateById(id: ObjectId, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(updates.map { Updates.set(it.key, it.value) })
        val result = collection.updateOne(Filters.eq("_id", id), updateDoc)
        return result.modifiedCount > 0
    }

    suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}