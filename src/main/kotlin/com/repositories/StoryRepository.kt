package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.Story
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import java.time.LocalDateTime

class StoryRepository {
    private val collection = DatabaseConfig.database.getCollection<Story>(Constants.COLLECTION_STORIES)

    suspend fun create(story: Story): Story {
        collection.insertOne(story)
        return story
    }

    suspend fun findById(id: String): Story? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findActiveStories(page: Int, limit: Int): List<Story> {
        return collection.find(Filters.gte("expireTime", LocalDateTime.now()))
            .sort(Sorts.descending("uploadTime"))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
    }

    suspend fun findByOwnerId(ownerId: String, page: Int, limit: Int): List<Story> {
        return collection.find(
            Filters.and(
                Filters.eq("ownerId", ownerId),
                Filters.gte("expireTime", LocalDateTime.now())
            )
        )
            .sort(Sorts.descending("uploadTime"))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
    }

    suspend fun countActiveStories(): Long {
        return collection.countDocuments(Filters.gte("expireTime", LocalDateTime.now()))
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

    suspend fun deleteExpiredStories(): Long {
        val result = collection.deleteMany(Filters.lt("expireTime", LocalDateTime.now()))
        return result.deletedCount
    }
}