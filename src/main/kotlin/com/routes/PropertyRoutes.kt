package com.routes

import com.dto.property.*
import com.services.PropertyService
import com.utils.Constants
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.propertyRoutes() {
    val propertyService = PropertyService()

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

        // Protected routes
        authenticate("auth-jwt") {
            post {
                try {
                    val userId = call.getUserId()
                    val request = call.receive<CreatePropertyRequest>()
                    val property = propertyService.createProperty(request, userId)
                    call.respondSuccess(property, "Property created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create property")
                }
            }

            put("/{id}") {
                try {
                    val userId = call.getUserId()
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid property ID")
                    val request = call.receive<UpdatePropertyRequest>()
                    val property = propertyService.updateProperty(id, request, userId)
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