package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.Chat
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class ChatRepository {
    private val collection = DatabaseConfig.database.getCollection<Chat>(Constants.COLLECTION_CHATS)

    suspend fun create(chat: Chat): Chat {
        collection.insertOne(chat)
        return chat
    }

    suspend fun findById(id: ObjectId): Chat? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findByUserId(userId: ObjectId, page: Int, limit: Int): List<Chat> {
        return collection.find(
            Filters.or(
                Filters.eq("senderId", userId),
                Filters.eq("receiverId", userId)
            )
        )
            .sort(Sorts.descending("openedTime"))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
    }

    suspend fun findBetweenUsers(user1Id: ObjectId, user2Id: ObjectId): Chat? {
        return collection.find(
            Filters.or(
                Filters.and(
                    Filters.eq("senderId", user1Id),
                    Filters.eq("receiverId", user2Id)
                ),
                Filters.and(
                    Filters.eq("senderId", user2Id),
                    Filters.eq("receiverId", user1Id)
                )
            )
        ).firstOrNull()
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