package com.routes

import com.dto.user.UpdateUserRequest
import com.dto.upload.UploadContext
import com.services.UserService
import com.services.FileUploadService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.userRoutes() {
    val userService = UserService()
    val fileUploadService = FileUploadService()

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
                    val contentType = call.request.contentType()

                    if (contentType.match("multipart/form-data")) {
                        // Handle multipart request with optional profile picture
                        val multipart = call.receiveMultipart()

                        var updateData: UpdateUserRequest? = null
                        var profilePictureFile: PartData.FileItem? = null

                        multipart.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    if (part.name == "data") {
                                        updateData = Json.decodeFromString<UpdateUserRequest>(part.value)
                                    }
                                }
                                is PartData.FileItem -> {
                                    if (part.name == "profilePicture") {
                                        profilePictureFile = part
                                    }
                                }
                                else -> {}
                            }
                            part.dispose()
                        }

                        val data = updateData ?: UpdateUserRequest()

                        // Upload profile picture if provided
                        var profilePictureUrl: String? = null
                        if (profilePictureFile != null) {
                            val uploadResult = fileUploadService.uploadSingleFile(
                                profilePictureFile!!,
                                UploadContext.USER_PROFILE
                            )
                            if (uploadResult.isFailure) {
                                throw IllegalArgumentException("Profile picture upload failed: ${uploadResult.exceptionOrNull()?.message}")
                            }
                            profilePictureUrl = uploadResult.getOrThrow().url
                        }

                        val finalUpdateRequest = data.copy(
                            picture = profilePictureUrl ?: data.picture
                        )

                        val updatedUser = userService.updateUser(userId, finalUpdateRequest)
                        call.respondSuccess(updatedUser, "Profile updated successfully${if (profilePictureUrl != null) " with new profile picture" else ""}")

                    } else {
                        // Handle regular JSON request (no file upload)
                        val request = call.receive<UpdateUserRequest>()
                        val updatedUser = userService.updateUser(userId, request)
                        call.respondSuccess(updatedUser, "Profile updated successfully")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update profile")
                }
            }

            get("/{id}") {
                try {
                    val id = call.parameters["id"]
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