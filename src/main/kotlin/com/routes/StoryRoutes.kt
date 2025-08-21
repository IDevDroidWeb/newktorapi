package com.routes

import com.dto.upload.*
import com.services.StoryService
import com.services.FileUploadService
import com.utils.Constants
import com.utils.FileValidators
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.storyRoutes() {
    val storyService = StoryService()
    val fileUploadService = FileUploadService()

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
                    val multipart = call.receiveMultipart()

                    var storyData: StoryUploadRequest? = null
                    var mediaFile: PartData.FileItem? = null

                    // Parse multipart data
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    storyData = Json.decodeFromString<StoryUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "media") {
                                    mediaFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    // Validate required data
                    val data = storyData ?: throw IllegalArgumentException("Story data is required")
                    val media = mediaFile ?: throw IllegalArgumentException("Media file is required")

                    // Upload media file
                    val uploadResult = fileUploadService.uploadSingleFile(media, UploadContext.STORY_MEDIA)
                    if (uploadResult.isFailure) {
                        throw IllegalArgumentException("Media upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }

                    val uploadedMedia = uploadResult.getOrThrow()

                    // Determine story type based on uploaded file
                    val storyType = if (FileValidators.isImageFile(uploadedMedia.mimeType)) {
                        "image"
                    } else {
                        "video"
                    }

                    // Create story request with uploaded media URL
                    val createRequest = CreateStoryRequest(
                        mediaUrl = uploadedMedia.url,
                        storyType = storyType,
                        titleAr = data.titleAr,
                        titleEn = data.titleEn,
                        descriptionAr = data.descriptionAr,
                        descriptionEn = data.descriptionEn,
                        propertyId = data.propertyId
                    )

                    val story = storyService.createStory(createRequest, userId)
                    call.respondSuccess(story, "Story created successfully with ${storyType}")
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

// DTO for internal story creation
@kotlinx.serialization.Serializable
data class CreateStoryRequest(
    val mediaUrl: String,
    val storyType: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val propertyId: String? = null
)