package com.routes

import com.dto.user.UpdateUserRequest
import com.services.UserService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    val userService = UserService()

    authenticate("auth-jwt") {
        route("/users") {
            get("/me") {
                try {
                    val userId = call.getUserId()
                    val user = userService.getUserById(userId)
                    call.respondSuccess(user, "User profile retrieved")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to get user profile")
                }
            }

            put("/me") {
                try {
                    val userId = call.getUserId()
                    val request = call.receive<UpdateUserRequest>()
                    val updatedUser = userService.updateUser(userId, request)
                    call.respondSuccess(updatedUser, "Profile updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update profile")
                }
            }

            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toObjectId()
                        ?: throw IllegalArgumentException("Invalid user ID")
                    val user = userService.getUserById(id)
                    call.respondSuccess(user, "User found")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "User not found")
                }
            }
        }
    }
}