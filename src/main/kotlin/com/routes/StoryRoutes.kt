package com.routes

import com.services.StoryService
import com.utils.Constants
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateStoryRequest(
    val mediaUrl: String,
    val storyType: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val propertyId: String? = null
)

fun Route.storyRoutes() {
    val storyService = StoryService()

    route("/stories") {
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                val (stories, total) = storyService.getActiveStories(page, limit)
                call.respondPaginated(stories, page, limit, total.toInt(), "Active stories retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get stories")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val userId = call.getUserId()
                    val request = call.receive<CreateStoryRequest>()
                    val story = storyService.createStory(request, userId)
                    call.respondSuccess(story, "Story created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create story")
                }
            }

            get("/my") {
                try {
                    val userId = call.getUserId()
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                    val (stories, total) = storyService.getUserStories(userId, page, limit)
                    call.respondPaginated(stories, page, limit, total.toInt(), "User stories retrieved")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to get user stories")
                }
            }

            delete("/{id}") {
                try {
                    val userId = call.getUserId()
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid story ID")
                    val deleted = storyService.deleteStory(id, userId)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Story deleted successfully")
                    } else {
                        call.respondError("Failed to delete story")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete story")
                }
            }
        }
    }
}