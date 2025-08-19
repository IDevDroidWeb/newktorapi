package com.services

import com.dto.property.*
import com.models.Property
import com.repositories.PropertyRepository
import com.repositories.UserRepository
import com.utils.toIsoString
import com.utils.toObjectId
import org.bson.types.ObjectId

class PropertyService {
    private val propertyRepository = PropertyRepository()
    private val userRepository = UserRepository()

    suspend fun createProperty(request: CreatePropertyRequest, ownerId: ObjectId): PropertyResponseDto {
        val property = Property(
            images = request.images,
            video = request.video,
            title = request.title,
            description = request.description,
            categoryId = request.categoryId.toObjectId() ?: throw IllegalArgumentException("Invalid category ID"),
            categoryName = request.categoryName,
            propertyTypeId = request.propertyTypeId.toObjectId() ?: throw IllegalArgumentException("Invalid property type ID"),
            propertyTypeName = request.propertyTypeName,
            specifications = request.specifications.mapNotNull { it.toObjectId() },
            area = request.area,
            rooms = request.rooms,
            baths = request.baths,
            price = request.price,
            locationString = request.locationString,
            latitude = request.latitude,
            longitude = request.longitude,
            ownerId = ownerId,
            countryId = request.countryId.toObjectId() ?: throw IllegalArgumentException("Invalid country ID"),
            governorateId = request.governorateId.toObjectId() ?: throw IllegalArgumentException("Invalid governorate ID")
        )

        val createdProperty = propertyRepository.create(property)

        // Increment user's ad count
        userRepository.incrementAdCount(ownerId)

        return createdProperty.toResponseDto()
    }

    suspend fun getPropertyById(id: ObjectId): PropertyResponseDto {
        val property = propertyRepository.findById(id)
            ?: throw NoSuchElementException("Property not found")
        return property.toResponseDto()
    }

    suspend fun updateProperty(id: ObjectId, request: UpdatePropertyRequest, ownerId: ObjectId): PropertyResponseDto {
        val existing = propertyRepository.findById(id)
            ?: throw NoSuchElementException("Property not found")

        if (existing.ownerId != ownerId) {
            throw IllegalArgumentException("You can only update your own properties")
        }

        val updates = mutableMapOf<String, Any>()

        request.images?.let { updates["images"] = it }
        request.video?.let { updates["video"] = it }
        request.title?.let { updates["title"] = it }
        request.description?.let { updates["description"] = it }
        request.categoryId?.let {
            val objId = it.toObjectId() ?: throw IllegalArgumentException("Invalid category ID")
            updates["categoryId"] = objId
        }
        request.categoryName?.let { updates["categoryName"] = it }
        request.propertyTypeId?.let {
            val objId = it.toObjectId() ?: throw IllegalArgumentException("Invalid property type ID")
            updates["propertyTypeId"] = objId
        }
        request.propertyTypeName?.let { updates["propertyTypeName"] = it }
        request.specifications?.let { updates["specifications"] = it.mapNotNull { spec -> spec.toObjectId() } }
        request.area?.let { updates["area"] = it }
        request.rooms?.let { updates["rooms"] = it }
        request.baths?.let { updates["baths"] = it }
        request.price?.let { updates["price"] = it }
        request.locationString?.let { updates["locationString"] = it }
        request.latitude?.let { updates["latitude"] = it }
        request.longitude?.let { updates["longitude"] = it }
        request.countryId?.let {
            val objId = it.toObjectId() ?: throw IllegalArgumentException("Invalid country ID")
            updates["countryId"] = objId
        }
        request.governorateId?.let {
            val objId = it.toObjectId() ?: throw IllegalArgumentException("Invalid governorate ID")
            updates["governorateId"] = objId
        }
        request.featured?.let { updates["featured"] = it }
        request.pinned?.let { updates["pinned"] = it }
        request.status?.let { updates["status"] = it }
        request.sold?.let { updates["sold"] = it }

        if (updates.isEmpty()) {
            throw IllegalArgumentException("No fields to update")
        }

        val updated = propertyRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update property")
        }

        return getPropertyById(id)
    }

    suspend fun deleteProperty(id: ObjectId, ownerId: ObjectId): Boolean {
        val existing = propertyRepository.findById(id)
            ?: throw NoSuchElementException("Property not found")

        if (existing.ownerId != ownerId) {
            throw IllegalArgumentException("You can only delete your own properties")
        }

        val deleted = propertyRepository.delete(id)
        if (deleted) {
            // Decrement user's ad count
            userRepository.decrementAdCount(ownerId)
        }
        return deleted
    }

    suspend fun searchProperties(request: PropertySearchRequest): Pair<List<PropertyResponseDto>, Long> {
        val properties = propertyRepository.search(
            query = request.query,
            categoryId = request.categoryId?.toObjectId(),
            propertyTypeId = request.propertyTypeId?.toObjectId(),
            minPrice = request.minPrice,
            maxPrice = request.maxPrice,
            rooms = request.rooms,
            baths = request.baths,
            minArea = request.minArea,
            maxArea = request.maxArea,
            governorateId = request.governorateId?.toObjectId(),
            countryId = request.countryId?.toObjectId(),
            page = request.page,
            limit = request.limit
        )

        val total = propertyRepository.countSearch(
            query = request.query,
            categoryId = request.categoryId?.toObjectId(),
            propertyTypeId = request.propertyTypeId?.toObjectId(),
            minPrice = request.minPrice,
            maxPrice = request.maxPrice,
            rooms = request.rooms,
            baths = request.baths,
            minArea = request.minArea,
            maxArea = request.maxArea,
            governorateId = request.governorateId?.toObjectId(),
            countryId = request.countryId?.toObjectId()
        )

        return properties.map { it.toResponseDto() } to total
    }

    suspend fun getUserProperties(ownerId: ObjectId, page: Int, limit: Int): Pair<List<PropertyResponseDto>, Long> {
        val properties = propertyRepository.findByOwnerId(ownerId, page, limit)
        val user = userRepository.findById(ownerId)
        val total = user?.numberOfAds?.toLong() ?: 0L

        return properties.map { it.toResponseDto() } to total
    }

    suspend fun getFeaturedProperties(limit: Int): List<PropertyResponseDto> {
        return propertyRepository.findFeatured(limit).map { it.toResponseDto() }
    }

    suspend fun getPinnedProperties(limit: Int): List<PropertyResponseDto> {
        return propertyRepository.findPinned(limit).map { it.toResponseDto() }
    }

    private fun Property.toResponseDto() = PropertyResponseDto(
        id = id.toString(),
        images = images,
        video = video,
        title = title,
        description = description,
        categoryId = categoryId.toString(),
        categoryName = categoryName,
        propertyTypeId = propertyTypeId.toString(),
        propertyTypeName = propertyTypeName,
        specifications = specifications.map { it.toString() },
        area = area,
        rooms = rooms,
        baths = baths,
        price = price,
        locationString = locationString,
        latitude = latitude,
        longitude = longitude,
        uploadTime = uploadTime.toIsoString(),
        expireTime = expireTime.toIsoString(),
        ownerId = ownerId.toString(),
        countryId = countryId.toString(),
        governorateId = governorateId.toString(),
        featured = featured,
        pinned = pinned,
        status = status,
        sold = sold
    )

    suspend fun getAllProperties(page: Int, limit: Int): Pair<List<PropertyResponseDto>, Long> {
        val properties = propertyRepository.findAll(page, limit)
        val total = propertyRepository.countAll()
        return properties.map { it.toResponseDto() } to total
    }

    suspend fun updatePropertyStatus(id: ObjectId, status: String): Boolean {
        return propertyRepository.updateStatus(id, status)
    }

    suspend fun adminDeleteProperty(id: ObjectId): Boolean {
        val property = propertyRepository.findById(id)
        if (property != null) {
            // Decrement user's ad count
            userRepository.decrementAdCount(property.ownerId)
        }
        return propertyRepository.delete(id)
    }
}