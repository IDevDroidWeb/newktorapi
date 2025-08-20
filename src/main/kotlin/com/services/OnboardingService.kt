package com.services

import com.models.Onboarding
import com.repositories.OnboardingRepository
import org.bson.types.ObjectId

class OnboardingService {
    private val onboardingRepository = OnboardingRepository()

    suspend fun createOnboarding(
        image: String,
        titleAr: String,
        titleEn: String,
        descriptionAr: String,
        descriptionEn: String,
        order: Int
    ): Onboarding {
        val onboarding = Onboarding(
            image = image,
            titleAr = titleAr,
            titleEn = titleEn,
            descriptionAr = descriptionAr,
            descriptionEn = descriptionEn,
            order = order
        )
        return onboardingRepository.create(onboarding)
    }

    suspend fun getOnboardingById(id: String): Onboarding {
        return onboardingRepository.findById(id)
            ?: throw NoSuchElementException("Onboarding item not found")
    }

    suspend fun getAllOnboarding(): List<Onboarding> {
        return onboardingRepository.findAll()
    }

    suspend fun updateOnboarding(id: String, updates: Map<String, Any>): Onboarding {
        val updated = onboardingRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update onboarding item")
        }
        return getOnboardingById(id)
    }

    suspend fun deleteOnboarding(id: String): Boolean {
        return onboardingRepository.delete(id)
    }
}