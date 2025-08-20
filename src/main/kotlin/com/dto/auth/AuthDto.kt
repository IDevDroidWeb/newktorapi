package com.dto.auth

import com.dto.user.UserResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String, // phone or email
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponseDto
)

@Serializable
data class RegisterStep1Request(
    val phone: String
)

@Serializable
data class RegisterStep1Response(
    val message: String,
    val tempToken: String
)

@Serializable
data class RegisterStep2Request(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterStep3Request(
    val profilePicture: String? = null
)

@Serializable
data class RegisterFinalResponse(
    val userId: String,
    val token: String,
    val user: UserResponseDto
)

@Serializable
data class VerifyPhoneRequest(
    val code: String,
    val tempToken: String
)