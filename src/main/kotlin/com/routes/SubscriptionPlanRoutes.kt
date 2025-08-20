package com.routes

import com.services.SubscriptionPlanService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

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
                    val request = call.receive<CreateSubscriptionPlanRequest>()
                    val plan = subscriptionPlanService.createPlan(request)
                    call.respondSuccess(plan, "Subscription plan created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create subscription plan")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid plan ID")
                    val request = call.receive<CreateSubscriptionPlanRequest>()

                    val updates = mapOf(
                        "image" to request.image,
                        "nameAr" to request.nameAr,
                        "nameEn" to request.nameEn,
                        "price" to request.price,
                        "features" to request.features,
                        "suitableAr" to request.suitableAr,
                        "suitableEn" to request.suitableEn,
                        "discountValue" to request.discountValue
                    )

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