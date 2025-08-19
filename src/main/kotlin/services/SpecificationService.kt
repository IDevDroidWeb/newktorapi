package com.services

import com.models.Specification
import com.repositories.SpecificationRepository
import org.bson.types.ObjectId

class SpecificationService {
    private val specificationRepository = SpecificationRepository()

    suspend fun createSpecification(nameAr: String, nameEn: String, iconImage: String): Specification {
        val specification = Specification(
            nameAr = nameAr,
            nameEn = nameEn,
            iconImage = iconImage
        )
        return specificationRepository.create(specification)
    }

    suspend fun getSpecificationById(id: ObjectId): Specification {
        return specificationRepository.findById(id)
            ?: throw NoSuchElementException("Specification not found")
    }

    suspend fun getAllSpecifications(): List<Specification> {
        return specificationRepository.findAll()
    }

    suspend fun updateSpecification(id: ObjectId, updates: Map<String, Any>): Specification {
        val updated = specificationRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update specification")
        }
        return getSpecificationById(id)
    }

    suspend fun deleteSpecification(id: ObjectId): Boolean {
        return specificationRepository.delete(id)
    }
}