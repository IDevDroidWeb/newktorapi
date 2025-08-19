package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class Chat(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    @Contextual val senderId: ObjectId,
    @Contextual val receiverId: ObjectId,
    @Contextual val openedTime: LocalDateTime = LocalDateTime.now(),
    @Contextual val roomId: ObjectId
)