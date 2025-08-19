package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class Story(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val mediaUrl: String,
    val storyType: String, // "image", "video"
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val ownerName: String,
    @Contextual val ownerId: ObjectId,
    @Contextual val propertyId: ObjectId? = null,
    @Contextual val uploadTime: LocalDateTime = LocalDateTime.now(),
    @Contextual val expireTime: LocalDateTime = LocalDateTime.now().plusHours(24)
)