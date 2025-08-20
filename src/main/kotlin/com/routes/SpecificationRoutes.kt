package com.routes

import com.services.SpecificationService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateSpecificationRequest(
    val nameAr: String,
    val nameEn: String,
    val iconImage: String
)

fun Route.specificationRoutes() {
    val specificationService = SpecificationService() // ✅ Service declared here

    route("/specifications") {
        get {
            try {
                val specifications = specificationService.getAllSpecifications()
                call.respondSuccess(specifications, "Specifications retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get specifications")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val request = call.receive<CreateSpecificationRequest>()
                    val specification = specificationService.createSpecification(
                        request.nameAr,
                        request.nameEn,
                        request.iconImage
                    )
                    call.respondSuccess(specification, "Specification created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create specification")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid specification ID")
                    val request = call.receive<CreateSpecificationRequest>()

                    val updates = mapOf(
                        "nameAr" to request.nameAr,
                        "nameEn" to request.nameEn,
                        "iconImage" to request.iconImage
                    )

                    val specification = specificationService.updateSpecification(id, updates)
                    call.respondSuccess(specification, "Specification updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update specification")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid specification ID")
                    val deleted = specificationService.deleteSpecification(id) // ✅ Now works
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