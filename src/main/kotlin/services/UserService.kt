package com.services

import com.dto.user.UpdateUserRequest
import com.dto.user.UserResponseDto
import com.models.User
import com.repositories.UserRepository
import com.utils.toIsoString
import org.bson.types.ObjectId

class UserService {
    private val userRepository = UserRepository()

    suspend fun getUserById(id: ObjectId): UserResponseDto {
        val user = userRepository.findById(id)
            ?: throw NoSuchElementException("User not found")
        return user.toResponseDto()
    }

    suspend fun updateUser(id: ObjectId, request: UpdateUserRequest): UserResponseDto {
        val updates = mutableMapOf<String, Any>()

        request.name?.let { updates["name"] = it }
        request.email?.let {
            // Check if email already exists for another user
            val existingUser = userRepository.findByEmail(it)
            if (existingUser != null && existingUser.id != id) {
                throw IllegalArgumentException("Email already exists")
            }
            updates["email"] = it
        }
        request.dateOfBirth?.let { updates["dateOfBirth"] = it }
        request.gender?.let { updates["gender"] = it }
        request.address?.let { updates["address"] = it }
        request.latitude?.let { updates["latitude"] = it }
        request.longitude?.let { updates["longitude"] = it }

        if (updates.isEmpty()) {
            throw IllegalArgumentException("No fields to update")
        }

        val updated = userRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update user")
        }

        return getUserById(id)
    }

    suspend fun deleteUser(id: ObjectId): Boolean {
        return userRepository.delete(id)
    }

    suspend fun updateUserStatus(id: ObjectId, status: String): Boolean {
        return userRepository.updateStatus(id, status)
    }

    suspend fun getAllUsers(page: Int, limit: Int): Pair<List<UserResponseDto>, Long> {
        val users = userRepository.findAll(page, limit)
        val total = userRepository.countAll()
        return users.map { it.toResponseDto() } to total
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