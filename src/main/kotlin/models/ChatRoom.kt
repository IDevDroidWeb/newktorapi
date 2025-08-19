package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class ChatRoom(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val messages: List<Message> = emptyList()
)

@Serializable
data class Message(
    @Contextual val id: ObjectId = ObjectId(),
    val text: String? = null,
    @Contextual val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: String, // "text", "image", "video"
    val readStatus: Boolean = false,
    val mediaUrl: String? = null,
    @Contextual val senderId: ObjectId
)