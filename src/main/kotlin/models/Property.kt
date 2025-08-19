package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class Property(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val images: List<String> = emptyList(),
    val video: String? = null,
    val title: String,
    val description: String,
    @Contextual val categoryId: ObjectId,
    val categoryName: String,
    @Contextual val propertyTypeId: ObjectId,
    val propertyTypeName: String,
    @Contextual val specifications: List<ObjectId> = emptyList(),
    val area: Double,
    val rooms: Byte,
    val baths: Byte,
    val price: Double,
    val locationString: String,
    val latitude: Double,
    val longitude: Double,
    @Contextual val uploadTime: LocalDateTime = LocalDateTime.now(),
    @Contextual val expireTime: LocalDateTime = LocalDateTime.now().plusDays(30),
    @Contextual val ownerId: ObjectId,
    @Contextual val countryId: ObjectId,
    @Contextual val governorateId: ObjectId,
    val featured: Boolean = false,
    val pinned: Boolean = false,
    val status: String = "active", // "active", "inactive"
    val sold: Boolean = false
)