package com.routes

import com.services.ChatService
import com.utils.Constants
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondPaginated
import com.utils.ResponseWrapper.respondSuccess
import com.utils.getUserId
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    val receiverId: String
)

@Serializable
data class SendMessageRequest(
    val text: String? = null,
    val type: String,
    val mediaUrl: String? = null
)

fun Route.chatRoutes() {
    val chatService = ChatService()

    authenticate("auth-jwt") {
        route("/chats") {
            get {
                try {
                    val userId = call.getUserId()
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(Constants.MAX_LIMIT) ?: Constants.DEFAULT_LIMIT

                    val (chats, total) = chatService.getUserChats(userId, page, limit)
                    call.respondPaginated(chats, page, limit, total.toInt(), "User chats retrieved")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to get chats")
                }
            }

            post {
                try {
                    val userId = call.getUserId()
                    val request = call.receive<CreateChatRequest>()
                    val receiverId = request.receiverId
                        ?: throw IllegalArgumentException("Invalid receiver ID")

                    val chat = chatService.createOrGetChat(userId, receiverId)
                    call.respondSuccess(chat, "Chat created/retrieved successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create chat")
                }
            }

            get("/{id}") {
                try {
                    val userId = call.getUserId()
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid chat ID")
                    val chat = chatService.getChatById(id, userId)
                    call.respondSuccess(chat, "Chat retrieved")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Chat not found")
                }
            }

            route("/{id}/messages") {
                post {
                    try {
                        val userId = call.getUserId()
                        val chatId = call.parameters["id"]
                            ?: throw IllegalArgumentException("Invalid chat ID")
                        val request = call.receive<SendMessageRequest>()

                        val message = chatService.sendMessage(chatId, userId, request)
                        call.respondSuccess(message, "Message sent successfully")
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to send message")
                    }
                }

                post("/read") {
                    try {
                        val userId = call.getUserId()
                        val chatId = call.parameters["id"]
                            ?: throw IllegalArgumentException("Invalid chat ID")

                        val marked = chatService.markMessagesAsRead(chatId, userId)
                        if (marked) {
                            call.respondSuccess(mapOf("marked" to true), "Messages marked as read")
                        } else {
                            call.respondError("Failed to mark messages as read")
                        }
                    } catch (e: Exception) {
                        call.respondError(e.message ?: "Failed to mark messages as read")
                    }
                }
            }
        }
    }
}