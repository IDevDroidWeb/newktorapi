package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.ChatRoom
import com.models.Message
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId

class ChatRoomRepository {
    private val collection = DatabaseConfig.database.getCollection<ChatRoom>(Constants.COLLECTION_CHAT_ROOMS)

    suspend fun create(chatRoom: ChatRoom): ChatRoom {
        collection.insertOne(chatRoom)
        return chatRoom
    }

    suspend fun findById(id: String): ChatRoom? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun addMessage(roomId: String, message: Message): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", roomId),
            Updates.push("messages", message)
        )
        return result.modifiedCount > 0
    }

    suspend fun markMessagesAsRead(roomId: String, senderId: String): Boolean {
        val result = collection.updateMany(
            Filters.and(
                Filters.eq("_id", roomId),
                Filters.eq("messages.senderId", senderId)
            ),
            Updates.set("messages.$.readStatus", true)
        )
        return result.modifiedCount > 0
    }

    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}