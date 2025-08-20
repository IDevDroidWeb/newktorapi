package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class Chat(
    @BsonId @Contextual
    val id: String = ObjectId().toHexString(),
    @Contextual val senderId: String,
    @Contextual val receiverId: String,
    @Contextual val openedTime: LocalDateTime = LocalDateTime.now(),
    @Contextual val roomId: String
)