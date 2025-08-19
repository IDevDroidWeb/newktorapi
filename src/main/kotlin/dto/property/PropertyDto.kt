package com.dto.property

import kotlinx.serialization.Serializable

@Serializable
data class PropertyResponseDto(
    val id: String,
    val images: List<String>,
    val video: String? = null,
    val title: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val propertyTypeId: String,
    val propertyTypeName: String,
    val specifications: List<String>,
    val area: Double,
    val rooms: Byte,
    val baths: Byte,
    val price: Double,
    val locationString: String,
    val latitude: Double,
    val longitude: Double,
    val uploadTime: String,
    val expireTime: String,
    val ownerId: String,
    val countryId: String,
    val governorateId: String,
    val featured: Boolean,
    val pinned: Boolean,
    val status: String,
    val sold: Boolean
)

@Serializable
data class CreatePropertyRequest(
    val images: List<String>,
    val video: String? = null,
    val title: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val propertyTypeId: String,
    val propertyTypeName: String,
    val specifications: List<String> = emptyList(),
    val area: Double,
    val rooms: Byte,
    val baths: Byte,
    val price: Double,
    val locationString: String,
    val latitude: Double,
    val longitude: Double,
    val countryId: String,
    val governorateId: String
)

@Serializable
data class UpdatePropertyRequest(
    val images: List<String>? = null,
    val video: String? = null,
    val title: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val propertyTypeId: String? = null,
    val propertyTypeName: String? = null,
    val specifications: List<String>? = null,
    val area: Double? = null,
    val rooms: Byte? = null,
    val baths: Byte? = null,
    val price: Double? = null,
    val locationString: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val countryId: String? = null,
    val governorateId: String? = null,
    val featured: Boolean? = null,
    val pinned: Boolean? = null,
    val status: String? = null,
    val sold: Boolean? = null
)

@Serializable
data class PropertySearchRequest(
    val query: String? = null,
    val categoryId: String? = null,
    val propertyTypeId: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val rooms: Byte? = null,
    val baths: Byte? = null,
    val minArea: Double? = null,
    val maxArea: Double? = null,
    val governorateId: String? = null,
    val countryId: String? = null,
    val page: Int = 1,
    val limit: Int = 20
)/*@Serializable
data class PropertySearchResponse(
    val properties: List<PropertyResponseDto>,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20
) */