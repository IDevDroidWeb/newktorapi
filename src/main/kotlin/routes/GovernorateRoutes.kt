package com.routes

import com.services.GovernorateService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateGovernorateRequest(
    val countryId: String,
    val nameAr: String,
    val nameEn: String
)

@Serializable
data class UpdateGovernorateRequest(
    val countryId: String? = null,
    val nameAr: String? = null,
    val nameEn: String? = null
)

fun Route.governorateRoutes() {
    val governorateService = GovernorateService()

    route("/governorates") {
        get {
            try {
                val countryId = call.request.queryParameters["countryId"]

                val governorates = if (countryId != null) {
                    val objId = countryId.toObjectId()
                        ?: throw IllegalArgumentException("Invalid country ID")
                    governorateService.getGovernoratesByCountry(objId)
                } else {
                    governorateService.getAllGovernorates()
                }

                call.respondSuccess(governorates, "Governorates retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get governorates")
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toObjectId()
                    ?: throw IllegalArgumentException("Invalid governorate ID")
                val governorate = governorateService.getGovernorateById(id)
                call.respondSuccess(governorate, "Governorate found")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Governorate not found")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val request = call.receive<CreateGovernorateRequest>()
                    val countryId = request.countryId.toObjectId()
                        ?: throw IllegalArgumentException("Invalid country ID")

                    val governorate = governorateService.createGovernorate(
                        countryId,
                        request.nameAr,
                        request.nameEn
                    )
                    call.respondSuccess(governorate, "Governorate created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create governorate")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toObjectId()
                        ?: throw IllegalArgumentException("Invalid governorate ID")
                    val request = call.receive<UpdateGovernorateRequest>()

                    val updates = mutableMapOf<String, Any>()
                    request.countryId?.let {
                        val objId = it.toObjectId() ?: throw IllegalArgumentException("Invalid country ID")
                        updates["countryId"] = objId
                    }
                    request.nameAr?.let { updates["nameAr"] = it }
                    request.nameEn?.let { updates["nameEn"] = it }

                    if (updates.isEmpty()) {
                        call.respondError("No fields to update")
                        return@put
                    }

                    val governorate = governorateService.updateGovernorate(id, updates)
                    call.respondSuccess(governorate, "Governorate updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update governorate")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toObjectId()
                        ?: throw IllegalArgumentException("Invalid governorate ID")
                    val deleted = governorateService.deleteGovernorate(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Governorate deleted successfully")
                    } else {
                        call.respondError("Failed to delete governorate")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete governorate")
                }
            }
        }
    }
}