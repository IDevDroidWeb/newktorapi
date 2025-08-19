package com.services

import com.models.Story
import com.repositories.StoryRepository
import com.repositories.UserRepository
import com.routes.CreateStoryRequest
import com.utils.Constants
import com.utils.toObjectId
import org.bson.types.ObjectId

class StoryService {
    private val storyRepository = StoryRepository()
    private val userRepository = UserRepository()

    suspend fun createStory(request: CreateStoryRequest, ownerId: ObjectId): Story {
        val user = userRepository.findById(ownerId)
            ?: throw NoSuchElementException("User not found")

        if (!listOf(Constants.STORY_TYPE_IMAGE, Constants.STORY_TYPE_VIDEO).contains(request.storyType)) {
            throw IllegalArgumentException("Invalid story type")
        }

        val story = Story(
            mediaUrl = request.mediaUrl,
            storyType = request.storyType,
            titleAr = request.titleAr,
            titleEn = request.titleEn,
            descriptionAr = request.descriptionAr,
            descriptionEn = request.descriptionEn,
            ownerName = user.name,
            ownerId = ownerId,
            propertyId = request.propertyId?.toObjectId()
        )

        return storyRepository.create(story)
    }

    suspend fun getActiveStories(page: Int, limit: Int): Pair<List<Story>, Long> {
        val stories = storyRepository.findActiveStories(page, limit)
        val total = storyRepository.countActiveStories()
        return stories to total
    }

    suspend fun getUserStories(ownerId: ObjectId, page: Int, limit: Int): Pair<List<Story>, Long> {
        val stories = storyRepository.findByOwnerId(ownerId, page, limit)
        // For user stories, we approximate the total
        val total = stories.size.toLong()
        return stories to total
    }

    suspend fun deleteStory(id: ObjectId, ownerId: ObjectId): Boolean {
        val story = storyRepository.findById(id)
            ?: throw NoSuchElementException("Story not found")

        if (story.ownerId != ownerId) {
            throw IllegalArgumentException("You can only delete your own stories")
        }

        return storyRepository.delete(id)
    }

    suspend fun deleteExpiredStories(): Long {
        return storyRepository.deleteExpiredStories()
    }
}