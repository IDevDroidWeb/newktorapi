package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class User(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val picture: String? = null,
    val name: String,
    val phone: String,
    val email: String,
    val passwordHash: String,
    val dateOfBirth: String? = null,
    val gender: String? = null, // "male", "female", "other"
    val accountType: String = "person", // "person", "company"
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val numberOfAds: Int = 0,
    @Contextual val joinTime: LocalDateTime = LocalDateTime.now(),
    val status: String = "active", // "active", "inactive", "banned"
    @Contextual val subscriptionPlanId: ObjectId? = null,
    val isPhoneVerified: Boolean = false,
    val isEmailVerified: Boolean = false
)