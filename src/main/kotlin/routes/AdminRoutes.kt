package com.routes

import com.services.UserService
import com.services.PropertyService
import com.utils.Constants
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserStatusRequest(
    val status: String
)

@Serializable
data class UpdatePropertyStatusRequest(
    val status: String
)

fun Route.adminRoutes() {
    val userService = UserService()
    val propertyService = PropertyService()

    authenticate("auth-jwt") {
        route("/admin") {
            // User management
            route("/users") {
                get {
                    try {
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                        val (users, total) = userService.getAllUsers(page, limit)
                        call.respondPaginated(users, page, limit, total.toInt(), "Users retrieved")
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to get users")
                    }
                }

                put("/{id}/status") {
                    try {
                        val id = call.parameters["id"]?.toObjectId()
                            ?: throw IllegalArgumentException("Invalid user ID")
                        val request = call.receive<UpdateUserStatusRequest>()

                        val updated = userService.updateUserStatus(id, request.status)
                        if (updated) {
                            call.respondSuccess(mapOf("updated" to true), "User status updated successfully")
                        } else {
                            call.respondError("Failed to update user status")
                        }
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to update user status")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toObjectId()
                            ?: throw IllegalArgumentException("Invalid user ID")
                        val deleted = userService.deleteUser(id)
                        if (deleted) {
                            call.respondSuccess(mapOf("deleted" to true), "User deleted successfully")
                        } else {
                            call.respondError("Failed to delete user")
                        }
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to delete user")
                    }
                }
            }

            // Property management
            route("/properties") {
                get {
                    try {
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                        val (properties, total) = propertyService.getAllProperties(page, limit)
                        call.respondPaginated(properties, page, limit, total.toInt(), "Properties retrieved")
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to get properties")
                    }
                }

                put("/{id}/status") {
                    try {
                        val id = call.parameters["id"]?.toObjectId()
                            ?: throw IllegalArgumentException("Invalid property ID")
                        val request = call.receive<UpdatePropertyStatusRequest>()

                        val updated = propertyService.updatePropertyStatus(id, request.status)
                        if (updated) {
                            call.respondSuccess(mapOf("updated" to true), "Property status updated successfully")
                        } else {
                            call.respondError("Failed to update property status")
                        }
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to update property status")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toObjectId()
                            ?: throw IllegalArgumentException("Invalid property ID")
                        val deleted = propertyService.adminDeleteProperty(id)
                        if (deleted) {
                            call.respondSuccess(mapOf("deleted" to true), "Property deleted successfully")
                        } else {
                            call.respondError("Failed to delete property")
                        }
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to delete property")
                    }
                }
            }
        }
    }
}