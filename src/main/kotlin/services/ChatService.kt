package com.services

import com.models.Chat
import com.models.ChatRoom
import com.models.Message
import com.repositories.ChatRepository
import com.repositories.ChatRoomRepository
import com.routes.SendMessageRequest
import com.utils.Constants
import org.bson.types.ObjectId
import java.time.LocalDateTime

class ChatService {
    private val chatRepository = ChatRepository()
    private val chatRoomRepository = ChatRoomRepository()

    suspend fun createOrGetChat(senderId: ObjectId, receiverId: ObjectId): Chat {
        // Check if chat already exists between users
        val existingChat = chatRepository.findBetweenUsers(senderId, receiverId)
        if (existingChat != null) {
            return existingChat
        }

        // Create new chat room
        val chatRoom = ChatRoom()
        val createdRoom = chatRoomRepository.create(chatRoom)

        // Create new chat
        val chat = Chat(
            senderId = senderId,
            receiverId = receiverId,
            roomId = createdRoom.id
        )

        return chatRepository.create(chat)
    }

    suspend fun getChatById(id: ObjectId, userId: ObjectId): Chat {
        val chat = chatRepository.findById(id)
            ?: throw NoSuchElementException("Chat not found")

        // Verify user is part of this chat
        if (chat.senderId != userId && chat.receiverId != userId) {
            throw IllegalArgumentException("Access denied")
        }

        return chat
    }

    suspend fun getUserChats(userId: ObjectId, page: Int, limit: Int): Pair<List<Chat>, Long> {
        val chats = chatRepository.findByUserId(userId, page, limit)
        // Approximate total for pagination
        val total = chats.size.toLong()
        return chats to total
    }

    suspend fun sendMessage(chatId: ObjectId, senderId: ObjectId, request: SendMessageRequest): Message {
        val chat = getChatById(chatId, senderId)

        if (!listOf(Constants.MESSAGE_TYPE_TEXT, Constants.MESSAGE_TYPE_IMAGE, Constants.MESSAGE_TYPE_VIDEO).contains(request.type)) {
            throw IllegalArgumentException("Invalid message type")
        }

        if (request.type == Constants.MESSAGE_TYPE_TEXT && request.text.isNullOrBlank()) {
            throw IllegalArgumentException("Text is required for text messages")
        }

        if (request.type != Constants.MESSAGE_TYPE_TEXT && request.mediaUrl.isNullOrBlank()) {
            throw IllegalArgumentException("Media URL is required for media messages")
        }

        val message = Message(
            text = request.text,
            type = request.type,
            mediaUrl = request.mediaUrl,
            senderId = senderId
        )

        val added = chatRoomRepository.addMessage(chat.roomId, message)
        if (!added) {
            throw IllegalStateException("Failed to send message")
        }

        return message
    }

    suspend fun markMessagesAsRead(chatId: ObjectId, userId: ObjectId): Boolean {
        val chat = getChatById(chatId, userId)
        return chatRoomRepository.markMessagesAsRead(chat.roomId, userId)
    }
}