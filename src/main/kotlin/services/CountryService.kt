package com.services

import com.models.Country
import com.repositories.CountryRepository
import org.bson.types.ObjectId

class CountryService {
    private val countryRepository = CountryRepository()

    suspend fun createCountry(nameAr: String, nameEn: String, flagImage: String): Country {
        val country = Country(
            nameAr = nameAr,
            nameEn = nameEn,
            flagImage = flagImage
        )
        return countryRepository.create(country)
    }

    suspend fun getCountryById(id: ObjectId): Country {
        return countryRepository.findById(id)
            ?: throw NoSuchElementException("Country not found")
    }

    suspend fun getAllCountries(): List<Country> {
        return countryRepository.findAll()
    }

    suspend fun updateCountry(id: ObjectId, updates: Map<String, Any>): Country {
        val updated = countryRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update country")
        }
        return getCountryById(id)
    }

    suspend fun deleteCountry(id: ObjectId): Boolean {
        return countryRepository.delete(id)
    }
}