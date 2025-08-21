package com.dto.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UserResponseDto(
    val id: String,
    val profilePicture: String? = null,
    val name: String,
    val phone: String,
    val email: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val accountType: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val numberOfAds: Int,
    val joinTime: String,
    val status: String,
    val subscriptionPlanId: String? = null,
    val isPhoneVerified: Boolean,
    val isEmailVerified: Boolean
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val profilePicture: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)