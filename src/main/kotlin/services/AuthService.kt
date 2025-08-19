package com.services

import com.config.AuthConfig
import com.dto.auth.*
import com.dto.user.UserResponseDto
import com.models.User
import com.repositories.UserRepository
import com.utils.Constants
import com.utils.toIsoString
import org.mindrot.jbcrypt.BCrypt
import org.bson.types.ObjectId
import java.time.LocalDateTime

class AuthService {
    private val userRepository = UserRepository()
    private val smsService = SmsService()

    private val pendingRegistrations = mutableMapOf<String, PendingRegistration>()

    data class PendingRegistration(
        val phone: String,
        val verificationCode: String,
        val expiryTime: LocalDateTime,
        var name: String? = null,
        var email: String? = null,
        var passwordHash: String? = null,
        var profilePicture: String? = null
    )

    suspend fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByPhoneOrEmail(request.identifier)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (user.status != Constants.USER_STATUS_ACTIVE) {
            throw IllegalArgumentException("Account is not active")
        }

        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        // Send SMS if login with phone number
        if (request.identifier == user.phone && user.isPhoneVerified) {
            smsService.sendVerificationCode(user.phone)
        }

        val token = AuthConfig.generateToken(user.id, user.phone)
        return LoginResponse(token, user.toResponseDto())
    }

    suspend fun registerStep1(request: RegisterStep1Request): RegisterStep1Response {
        // Check if phone already exists
        val existingUser = userRepository.findByPhone(request.phone)
        if (existingUser != null) {
            throw IllegalArgumentException("Phone number already registered")
        }

        // Generate verification code
        val code = generateVerificationCode()

        // Store pending registration
        pendingRegistrations[request.phone] = PendingRegistration(
            phone = request.phone,
            verificationCode = code,
            expiryTime = LocalDateTime.now().plusMinutes(10)
        )

        // Send SMS
        smsService.sendVerificationCode(request.phone, code)

        val tempToken = AuthConfig.generateTempToken(request.phone)

        return RegisterStep1Response(
            message = "Verification code sent to your phone",
            tempToken = tempToken
        )
    }

    suspend fun verifyPhone(request: VerifyPhoneRequest, phone: String): Boolean {
        val pending = pendingRegistrations[phone]
            ?: throw IllegalArgumentException("No pending registration found")

        if (LocalDateTime.now().isAfter(pending.expiryTime)) {
            pendingRegistrations.remove(phone)
            throw IllegalArgumentException("Verification code expired")
        }

        if (request.code != pending.verificationCode) {
            throw IllegalArgumentException("Invalid verification code")
        }

        return true
    }

    suspend fun registerStep2(request: RegisterStep2Request, phone: String): Boolean {
        val pending = pendingRegistrations[phone]
            ?: throw IllegalArgumentException("No pending registration found or phone not verified")

        // Check if email already exists
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) {
            throw IllegalArgumentException("Email already registered")
        }

        // Update pending registration
        pending.name = request.name
        pending.email = request.email
        pending.passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

        return true
    }

    suspend fun registerStep3(request: RegisterStep3Request, phone: String): Boolean {
        val pending = pendingRegistrations[phone]
            ?: throw IllegalArgumentException("No pending registration found")

        pending.profilePicture = request.profilePicture
        return true
    }

    suspend fun registerFinal(phone: String): RegisterFinalResponse {
        val pending = pendingRegistrations[phone]
            ?: throw IllegalArgumentException("No pending registration found")

        if (pending.name == null || pending.email == null || pending.passwordHash == null) {
            throw IllegalArgumentException("Registration not complete")
        }

        val user = User(
            name = pending.name!!,
            phone = pending.phone,
            email = pending.email!!,
            passwordHash = pending.passwordHash!!,
            picture = pending.profilePicture,
            isPhoneVerified = true,
            status = Constants.USER_STATUS_ACTIVE
        )

        val createdUser = userRepository.create(user)

        // Clean up pending registration
        pendingRegistrations.remove(phone)

        val token = AuthConfig.generateToken(createdUser.id, createdUser.phone)

        return RegisterFinalResponse(
            userId = createdUser.id.toString(),
            token = token,
            user = createdUser.toResponseDto()
        )
    }

    private fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }

    private fun User.toResponseDto() = UserResponseDto(
        id = id.toString(),
        picture = picture,
        name = name,
        phone = phone,
        email = email,
        dateOfBirth = dateOfBirth,
        gender = gender,
        accountType = accountType,
        address = address,
        latitude = latitude,
        longitude = longitude,
        numberOfAds = numberOfAds,
        joinTime = joinTime.toIsoString(),
        status = status,
        subscriptionPlanId = subscriptionPlanId?.toString(),
        isPhoneVerified = isPhoneVerified,
        isEmailVerified = isEmailVerified
    )
}