package com.routes

import com.services.OnboardingService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

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
                    val request = call.receive<CreateOnboardingRequest>()
                    val item = onboardingService.createOnboarding(
                        request.image,
                        request.titleAr,
                        request.titleEn,
                        request.descriptionAr,
                        request.descriptionEn,
                        request.order
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
                    val request = call.receive<CreateOnboardingRequest>()

                    val updates = mapOf(
                        "image" to request.image,
                        "titleAr" to request.titleAr,
                        "titleEn" to request.titleEn,
                        "descriptionAr" to request.descriptionAr,
                        "descriptionEn" to request.descriptionEn,
                        "order" to request.order
                    )

                    val item = onboardingService.updateOnboarding(id, updates)
                    call.respondSuccess(item, "Onboarding item updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update onboarding item")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid specification ID")
                    val deleted = onboardingService.deleteOnboarding(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Specification deleted successfully")
                    } else {
                        call.respondError("Failed to delete specification")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete specification")
                }
            }
        }
    }
}