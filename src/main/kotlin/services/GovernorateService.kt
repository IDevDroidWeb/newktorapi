package com.services

import com.models.Governorate
import com.repositories.GovernorateRepository
import org.bson.types.ObjectId

class GovernorateService {
    private val governorateRepository = GovernorateRepository()

    suspend fun createGovernorate(countryId: ObjectId, nameAr: String, nameEn: String): Governorate {
        val governorate = Governorate(
            countryId = countryId,
            nameAr = nameAr,
            nameEn = nameEn
        )
        return governorateRepository.create(governorate)
    }

    suspend fun getGovernorateById(id: ObjectId): Governorate {
        return governorateRepository.findById(id)
            ?: throw NoSuchElementException("Governorate not found")
    }

    suspend fun getGovernoratesByCountry(countryId: ObjectId): List<Governorate> {
        return governorateRepository.findByCountryId(countryId)
    }

    suspend fun getAllGovernorates(): List<Governorate> {
        return governorateRepository.findAll()
    }

    suspend fun updateGovernorate(id: ObjectId, updates: Map<String, Any>): Governorate {
        val updated = governorateRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update governorate")
        }
        return getGovernorateById(id)
    }

    suspend fun deleteGovernorate(id: ObjectId): Boolean {
        return governorateRepository.delete(id)
    }
}