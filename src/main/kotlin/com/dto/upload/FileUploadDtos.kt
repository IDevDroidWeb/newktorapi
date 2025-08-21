package com.dto.upload

import kotlinx.serialization.Serializable

@Serializable
data class UploadedFile(
    val originalName: String,
    val fileName: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val size: Long,
    val mimeType: String,
    val folder: String
)

@Serializable
data class FileUploadResponse(
    val files: List<UploadedFile>,
    val message: String
)

@Serializable
data class FileValidationError(
    val fileName: String,
    val error: String
)

@Serializable
data class PropertyUploadRequest(
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
data class StoryUploadRequest(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val propertyId: String? = null
)

@Serializable
data class CountryUploadRequest(
    val nameAr: String,
    val nameEn: String
)

@Serializable
data class OnboardingUploadRequest(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val order: Int = 0
)

@Serializable
data class SpecificationUploadRequest(
    val nameAr: String,
    val nameEn: String
)

@Serializable
data class SubscriptionPlanUploadRequest(
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val features: List<String>,
    val suitableAr: String,
    val suitableEn: String,
    val discountValue: Double = 0.0
)

enum class FileType {
    IMAGE, VIDEO
}

enum class UploadContext {
    PROPERTY_IMAGES,
    PROPERTY_VIDEO,
    USER_PROFILE,
    COUNTRY_FLAG,
    ONBOARDING_IMAGE,
    SPECIFICATION_ICON,
    STORY_MEDIA,
    SUBSCRIPTION_PLAN_IMAGE
}