package com.config

import io.github.cdimascio.dotenv.dotenv

object StorageConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val storageProvider = dotenv["STORAGE_PROVIDER"] ?: "local" // local, s3
    val baseUploadDir = dotenv["UPLOAD_DIR"] ?: "uploads/"

    // AWS S3 Configuration
    val awsAccessKeyId = dotenv["AWS_ACCESS_KEY_ID"] ?: ""
    val awsSecretAccessKey = dotenv["AWS_SECRET_ACCESS_KEY"] ?: ""
    val awsS3BucketName = dotenv["AWS_S3_BUCKET_NAME"] ?: ""
    val awsS3Region = dotenv["AWS_S3_REGION"] ?: "us-east-1"
    val awsS3BaseUrl = "https://${awsS3BucketName}.s3.${awsS3Region}.amazonaws.com"

    // File size limits in bytes
    val maxImageSize = 10 * 1024 * 1024L // 10MB
    val maxVideoSize = 100 * 1024 * 1024L // 100MB

    // Allowed file types
    val allowedImageTypes = setOf("image/jpeg", "image/png", "image/webp", "image/jpg")
    val allowedVideoTypes = setOf("video/mp4", "video/mov", "video/avi", "video/quicktime")

    // Storage folders
    object Folders {
        const val PROPERTIES = "properties"
        const val USERS = "users"
        const val COUNTRIES = "countries"
        const val ONBOARDING = "onboarding"
        const val SPECIFICATIONS = "specifications"
        const val STORIES = "stories"
        const val SUBSCRIPTION_PLANS = "subscription-plans"
        const val THUMBNAILS = "thumbnails"
    }

    // Thumbnail sizes
    object ThumbnailSizes {
        const val SMALL = 150
        const val MEDIUM = 300
        const val LARGE = 600
    }

    val isLocalStorage = storageProvider == "local"
    val isS3Storage = storageProvider == "s3"
}