package com.utils

object Constants {
    // User Status
    const val USER_STATUS_ACTIVE = "active"
    const val USER_STATUS_INACTIVE = "inactive"
    const val USER_STATUS_BANNED = "banned"

    // Account Types
    const val ACCOUNT_TYPE_PERSON = "person"
    const val ACCOUNT_TYPE_COMPANY = "company"

    // Property Status
    const val PROPERTY_STATUS_ACTIVE = "active"
    const val PROPERTY_STATUS_INACTIVE = "inactive"

    // Story Types
    const val STORY_TYPE_IMAGE = "image"
    const val STORY_TYPE_VIDEO = "video"

    // Message Types
    const val MESSAGE_TYPE_TEXT = "text"
    const val MESSAGE_TYPE_IMAGE = "image"
    const val MESSAGE_TYPE_VIDEO = "video"

    // Gender Options
    const val GENDER_MALE = "male"
    const val GENDER_FEMALE = "female"
    const val GENDER_OTHER = "other"

    // Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PROPERTIES = "properties"
    const val COLLECTION_COUNTRIES = "countries"
    const val COLLECTION_GOVERNORATES = "governorates"
    const val COLLECTION_SPECIFICATIONS = "specifications"
    const val COLLECTION_STORIES = "stories"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_CHAT_ROOMS = "chat_rooms"
    const val COLLECTION_SUBSCRIPTION_PLANS = "subscription_plans"
    const val COLLECTION_ONBOARDING = "onboarding"

    // Pagination
    const val DEFAULT_PAGE = 1
    const val DEFAULT_LIMIT = 20
    const val MAX_LIMIT = 100

    // File Upload
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    const val MAX_VIDEO_SIZE = 50 * 1024 * 1024 // 50MB
    const val ALLOWED_IMAGE_EXTENSIONS = "jpg,jpeg,png,gif,webp"
    const val ALLOWED_VIDEO_EXTENSIONS = "mp4,mov,avi,mkv"
}