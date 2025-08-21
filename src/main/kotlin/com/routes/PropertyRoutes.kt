package com.routes

import com.dto.property.*
import com.dto.upload.*
import com.services.FileUploadService
import com.services.PropertyService
import com.utils.Constants
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.propertyRoutes() {
    val propertyService = PropertyService()
    val fileUploadService = FileUploadService()

    route("/properties") {
        // Public routes
        get("/search") {
            try {
                val query = call.request.queryParameters["query"]
                val categoryId = call.request.queryParameters["categoryId"]
                val propertyTypeId = call.request.queryParameters["propertyTypeId"]
                val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()
                val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()
                val rooms = call.request.queryParameters["rooms"]?.toByteOrNull()
                val baths = call.request.queryParameters["baths"]?.toByteOrNull()
                val minArea = call.request.queryParameters["minArea"]?.toDoubleOrNull()
                val maxArea = call.request.queryParameters["maxArea"]?.toDoubleOrNull()
                val governorateId = call.request.queryParameters["governorateId"]
                val countryId = call.request.queryParameters["countryId"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                val searchRequest = PropertySearchRequest(
                    query = query,
                    categoryId = categoryId,
                    propertyTypeId = propertyTypeId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    rooms = rooms,
                    baths = baths,
                    minArea = minArea,
                    maxArea = maxArea,
                    governorateId = governorateId,
                    countryId = countryId,
                    page = page,
                    limit = limit
                )

                val (properties, total) = propertyService.searchProperties(searchRequest)
                call.respondPaginated(properties, page, limit, total.toInt(), "Properties found")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Search failed")
            }
        }

        get("/featured") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val properties = propertyService.getFeaturedProperties(limit)
                call.respondSuccess(properties, "Featured properties retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get featured properties")
            }
        }

        get("/pinned") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val properties = propertyService.getPinnedProperties(limit)
                call.respondSuccess(properties, "Pinned properties retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get pinned properties")
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("Invalid property ID")
                val property = propertyService.getPropertyById(id)
                call.respondSuccess(property, "Property found")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Property not found")
            }
        }

        // Protected routes with file upload support
        authenticate("auth-jwt") {
            post {
                try {
                    val userId = call.getUserId()
                    val multipart = call.receiveMultipart()

                    var propertyData: PropertyUploadRequest? = null
                    val imageFiles = mutableListOf<PartData.FileItem>()
                    var videoFile: PartData.FileItem? = null

                    // Parse multipart data
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    propertyData = Json.decodeFromString<PropertyUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                when (part.name) {
                                    "images" -> imageFiles.add(part)
                                    "video" -> videoFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    // Validate required data
                    val data = propertyData ?: throw IllegalArgumentException("Property data is required")
                    if (imageFiles.isEmpty()) {
                        throw IllegalArgumentException("At least one image is required")
                    }

                    // Upload images
                    val imageUploadResult = fileUploadService.uploadFiles(imageFiles, UploadContext.PROPERTY_IMAGES)
                    if (imageUploadResult.isFailure) {
                        throw IllegalArgumentException("Image upload failed: ${imageUploadResult.exceptionOrNull()?.message}")
                    }
                    val uploadedImages = imageUploadResult.getOrThrow()

                    // Upload video if provided
                    var uploadedVideo: UploadedFile? = null
                    if (videoFile != null) {
                        val videoUploadResult = fileUploadService.uploadSingleFile(videoFile!!, UploadContext.PROPERTY_VIDEO)
                        if (videoUploadResult.isFailure) {
                            // Rollback images
                            fileUploadService.deleteFiles(uploadedImages.map { it.url })
                            throw IllegalArgumentException("Video upload failed: ${videoUploadResult.exceptionOrNull()?.message}")
                        }
                        uploadedVideo = videoUploadResult.getOrThrow()
                    }

                    // Create property request with uploaded file URLs
                    val createRequest = CreatePropertyRequest(
                        images = uploadedImages.map { it.url },
                        video = uploadedVideo?.url,
                        title = data.title,
                        description = data.description,
                        categoryId = data.categoryId,
                        categoryName = data.categoryName,
                        propertyTypeId = data.propertyTypeId,
                        propertyTypeName = data.propertyTypeName,
                        specifications = data.specifications,
                        area = data.area,
                        rooms = data.rooms,
                        baths = data.baths,
                        price = data.price,
                        locationString = data.locationString,
                        latitude = data.latitude,
                        longitude = data.longitude,
                        countryId = data.countryId,
                        governorateId = data.governorateId
                    )

                    val property = propertyService.createProperty(createRequest, userId)
                    call.respondSuccess(property, "Property created successfully with ${uploadedImages.size} images${if (uploadedVideo != null) " and 1 video" else ""}")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create property")
                }
            }

            put("/{id}") {
                try {
                    val userId = call.getUserId()
                    val propertyId = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid property ID")

                    val multipart = call.receiveMultipart()

                    var updateData: UpdatePropertyRequest? = null
                    val newImageFiles = mutableListOf<PartData.FileItem>()
                    var newVideoFile: PartData.FileItem? = null

                    // Parse multipart data
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    updateData = Json.decodeFromString<UpdatePropertyRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                when (part.name) {
                                    "newImages" -> newImageFiles.add(part)
                                    "newVideo" -> newVideoFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = updateData ?: throw IllegalArgumentException("Update data is required")

                    // Upload new images if provided
                    var newImageUrls: List<String> = emptyList()
                    if (newImageFiles.isNotEmpty()) {
                        val imageUploadResult = fileUploadService.uploadFiles(newImageFiles, UploadContext.PROPERTY_IMAGES)
                        if (imageUploadResult.isFailure) {
                            throw IllegalArgumentException("Image upload failed: ${imageUploadResult.exceptionOrNull()?.message}")
                        }
                        newImageUrls = imageUploadResult.getOrThrow().map { it.url }
                    }

                    // Upload new video if provided
                    var newVideoUrl: String? = null
                    if (newVideoFile != null) {
                        val videoUploadResult = fileUploadService.uploadSingleFile(newVideoFile!!, UploadContext.PROPERTY_VIDEO)
                        if (videoUploadResult.isFailure) {
                            // Rollback new images
                            if (newImageUrls.isNotEmpty()) {
                                fileUploadService.deleteFiles(newImageUrls)
                            }
                            throw IllegalArgumentException("Video upload failed: ${videoUploadResult.exceptionOrNull()?.message}")
                        }
                        newVideoUrl = videoUploadResult.getOrThrow().url
                    }

                    // Merge new images with existing ones (if keeping existing)
                    val finalImages = when {
                        newImageUrls.isNotEmpty() && data.replaceImages == true -> newImageUrls
                        newImageUrls.isNotEmpty() -> (data.images ?: emptyList()) + newImageUrls
                        else -> data.images ?: emptyList()
                    }

                    val finalVideo = newVideoUrl ?: data.video

                    val finalUpdateRequest = data.copy(
                        images = finalImages.takeIf { it.isNotEmpty() },
                        video = finalVideo
                    )

                    val property = propertyService.updateProperty(propertyId, finalUpdateRequest, userId)
                    call.respondSuccess(property, "Property updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update property")
                }
            }

            delete("/{id}") {
                try {
                    val userId = call.getUserId()
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid property ID")
                    val deleted = propertyService.deleteProperty(id, userId)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Property deleted successfully")
                    } else {
                        call.respondError("Failed to delete property")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete property")
                }
            }

            get("/my") {
                try {
                    val userId = call.getUserId()
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                    val (properties, total) = propertyService.getUserProperties(userId, page, limit)
                    call.respondPaginated(properties, page, limit, total.toInt(), "User properties retrieved")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to get user properties")
                }
            }
        }
    }
}