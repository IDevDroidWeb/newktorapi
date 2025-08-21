package com.routes

import com.dto.upload.SubscriptionPlanUploadRequest
import com.dto.upload.UploadContext
import com.services.FileUploadService
import com.services.SubscriptionPlanService
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
data class CreateSubscriptionPlanRequest(
    val image: String,
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val features: List<String>,
    val suitableAr: String,
    val suitableEn: String,
    val discountValue: Double = 0.0
)

fun Route.subscriptionPlanRoutes() {
    val subscriptionPlanService = SubscriptionPlanService()
    val fileUploadService = FileUploadService()

    route("/subscription-plans") {
        get {
            try {
                val plans = subscriptionPlanService.getAllPlans()
                call.respondSuccess(plans, "Subscription plans retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get subscription plans")
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("Invalid plan ID")
                val plan = subscriptionPlanService.getPlanById(id)
                call.respondSuccess(plan, "Subscription plan found")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Subscription plan not found")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val multipart = call.receiveMultipart()

                    var planData: SubscriptionPlanUploadRequest? = null
                    var imageFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    planData = Json.decodeFromString<SubscriptionPlanUploadRequest>(part.value)
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

                    val data = planData ?: throw IllegalArgumentException("Plan data is required")
                    val image = imageFile ?: throw IllegalArgumentException("Plan image is required")

                    // Upload plan image
                    val uploadResult = fileUploadService.uploadSingleFile(image, UploadContext.SUBSCRIPTION_PLAN_IMAGE)
                    if (uploadResult.isFailure) {
                        throw IllegalArgumentException("Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }

                    val uploadedImage = uploadResult.getOrThrow()

                    // Create request object for service
                    val createRequest = com.routes.CreateSubscriptionPlanRequest(
                        image = uploadedImage.url,
                        nameAr = data.nameAr,
                        nameEn = data.nameEn,
                        price = data.price,
                        features = data.features,
                        suitableAr = data.suitableAr,
                        suitableEn = data.suitableEn,
                        discountValue = data.discountValue
                    )

                    val plan = subscriptionPlanService.createPlan(createRequest)
                    call.respondSuccess(plan, "Subscription plan created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create subscription plan")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid plan ID")

                    val multipart = call.receiveMultipart()

                    var updateData: SubscriptionPlanUploadRequest? = null
                    var newImageFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    updateData = Json.decodeFromString<SubscriptionPlanUploadRequest>(part.value)
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
                    updates["nameAr"] = data.nameAr
                    updates["nameEn"] = data.nameEn
                    updates["price"] = data.price
                    updates["features"] = data.features
                    updates["suitableAr"] = data.suitableAr
                    updates["suitableEn"] = data.suitableEn
                    updates["discountValue"] = data.discountValue

                    // Upload new image if provided
                    if (newImageFile != null) {
                        val uploadResult = fileUploadService.uploadSingleFile(newImageFile!!, UploadContext.SUBSCRIPTION_PLAN_IMAGE)
                        if (uploadResult.isFailure) {
                            throw IllegalArgumentException("Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                        }
                        updates["image"] = uploadResult.getOrThrow().url
                    }

                    val plan = subscriptionPlanService.updatePlan(id, updates)
                    call.respondSuccess(plan, "Subscription plan updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update subscription plan")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid plan ID")
                    val deleted = subscriptionPlanService.deletePlan(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Subscription plan deleted successfully")
                    } else {
                        call.respondError("Failed to delete subscription plan")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete subscription plan")
                }
            }
        }
    }
}