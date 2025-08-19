package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.User
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class UserRepository {
    private val collection = DatabaseConfig.database.getCollection<User>(Constants.COLLECTION_USERS)

    suspend fun create(user: User): User {
        collection.insertOne(user)
        return user
    }

    suspend fun findById(id: ObjectId): User? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findByPhone(phone: String): User? {
        return collection.find(Filters.eq("phone", phone)).firstOrNull()
    }

    suspend fun findByEmail(email: String): User? {
        return collection.find(Filters.eq("email", email)).firstOrNull()
    }

    suspend fun findByPhoneOrEmail(identifier: String): User? {
        return collection.find(
            Filters.or(
                Filters.eq("phone", identifier),
                Filters.eq("email", identifier)
            )
        ).firstOrNull()
    }

    suspend fun updateById(id: ObjectId, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(updates.map { Updates.set(it.key, it.value) })
        val result = collection.updateOne(Filters.eq("_id", id), updateDoc)
        return result.modifiedCount > 0
    }

    suspend fun updateStatus(id: ObjectId, status: String): Boolean {
        return updateById(id, mapOf("status" to status))
    }

    suspend fun incrementAdCount(id: ObjectId): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.inc("numberOfAds", 1)
        )
        return result.modifiedCount > 0
    }

    suspend fun decrementAdCount(id: ObjectId): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.inc("numberOfAds", -1)
        )
        return result.modifiedCount > 0
    }

    suspend fun findAll(page: Int, limit: Int): List<User> {
        return collection.find()
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
    }

    suspend fun countAll(): Long {
        return collection.countDocuments()
    }

    suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}