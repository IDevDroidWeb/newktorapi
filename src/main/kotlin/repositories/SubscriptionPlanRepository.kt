package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.SubscriptionPlan
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class SubscriptionPlanRepository {
    private val collection = DatabaseConfig.database.getCollection<SubscriptionPlan>(Constants.COLLECTION_SUBSCRIPTION_PLANS)

    suspend fun create(plan: SubscriptionPlan): SubscriptionPlan {
        collection.insertOne(plan)
        return plan
    }

    suspend fun findById(id: ObjectId): SubscriptionPlan? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findAll(): List<SubscriptionPlan> {
        return collection.find().toList()
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