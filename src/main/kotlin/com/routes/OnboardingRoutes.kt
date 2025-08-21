package com.routes

import com.dto.upload.OnboardingUploadRequest
import com.dto.upload.UploadContext
import com.services.FileUploadService
import com.services.OnboardingService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateOnboardingRequest(
    val image: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val order: Int = 0
)

fun Route.onboardingRoutes() {
    val onboardingService = OnboardingService()
    val fileUploadService = FileUploadService()

    route("/onboarding") {
        get {
            try {
                val items = onboardingService.getAllOnboarding()
                call.respondSuccess(items, "Onboarding items retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get onboarding items")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val multipart = call.receiveMultipart()

                    var onboardingData: OnboardingUploadRequest? = null
                    var imageFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    onboardingData = Json.decodeFromString<OnboardingUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "image") {
                                    imageFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = onboardingData ?: throw IllegalArgumentException("Onboarding data is required")
                    val image = imageFile ?: throw IllegalArgumentException("Image is required")

                    // Upload image
                    val uploadResult = fileUploadService.uploadSingleFile(image, UploadContext.ONBOARDING_IMAGE)
                    if (uploadResult.isFailure) {
                        throw IllegalArgumentException("Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }

                    val uploadedImage = uploadResult.getOrThrow()

                    val item = onboardingService.createOnboarding(
                        uploadedImage.url,
                        data.titleAr,
                        data.titleEn,
                        data.descriptionAr,
                        data.descriptionEn,
                        data.order
                    )
                    call.respondSuccess(item, "Onboarding item created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create onboarding item")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid onboarding ID")

                    val multipart = call.receiveMultipart()

                    var updateData: OnboardingUploadRequest? = null
                    var newImageFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    updateData = Json.decodeFromString<OnboardingUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "image") {
                                    newImageFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = updateData ?: throw IllegalArgumentException("Update data is required")

                    val updates = mutableMapOf<String, Any>()
                    updates["titleAr"] = data.titleAr
                    updates["titleEn"] = data.titleEn
                    updates["descriptionAr"] = data.descriptionAr
                    updates["descriptionEn"] = data.descriptionEn
                    updates["order"] = data.order

                    // Upload new image if provided
                    if (newImageFile != null) {
                        val uploadResult = fileUploadService.uploadSingleFile(newImageFile!!, UploadContext.ONBOARDING_IMAGE)
                        if (uploadResult.isFailure) {
                            throw IllegalArgumentException("Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                        }
                        updates["image"] = uploadResult.getOrThrow().url
                    }

                    val item = onboardingService.updateOnboarding(id, updates)
                    call.respondSuccess(item, "Onboarding item updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update onboarding item")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid onboarding ID")
                    val deleted = onboardingService.deleteOnboarding(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Onboarding item deleted successfully")
                    } else {
                        call.respondError("Failed to delete onboarding item")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete onboarding item")
                }
            }
        }
    }
}