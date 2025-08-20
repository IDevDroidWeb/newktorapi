package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class Property(
    @BsonId @Contextual
    val id: String = ObjectId().toHexString(),
    val images: List<String> = emptyList(),
    val video: String? = null,
    val title: String,
    val description: String,
    @Contextual val categoryId: String,
    val categoryName: String,
    @Contextual val propertyTypeId: String,
    val propertyTypeName: String,
    @Contextual val specifications: List<String> = emptyList(),
    val area: Double,
    val rooms: Byte,
    val baths: Byte,
    val price: Double,
    val locationString: String,
    val latitude: Double,
    val longitude: Double,
    @Contextual val uploadTime: LocalDateTime = LocalDateTime.now(),
    @Contextual val expireTime: LocalDateTime = LocalDateTime.now().plusDays(30),
    @Contextual val ownerId: String,
    @Contextual val countryId: String,
    @Contextual val governorateId: String,
    val featured: Boolean = false,
    val pinned: Boolean = false,
    val status: String = "active", // "active", "inactive"
    val sold: Boolean = false
)