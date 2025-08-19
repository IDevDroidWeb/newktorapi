package com.config

import io.github.cdimascio.dotenv.dotenv

object AppConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val port = dotenv["PORT"]?.toInt() ?: 8080
    val host = dotenv["HOST"] ?: "0.0.0.0"
    val environment = dotenv["ENVIRONMENT"] ?: "development"
    val uploadDir = dotenv["UPLOAD_DIR"] ?: "uploads/"
    val maxFileSize = dotenv["MAX_FILE_SIZE"]?.toLong() ?: 10485760 // 10MB
    val rateLimitPerMinute = dotenv["RATE_LIMIT_PER_MINUTE"]?.toInt() ?: 60

    // Plivo SMS Configuration
    val plivoAuthId = dotenv["PLIVO_AUTH_ID"] ?: ""
    val plivoAuthToken = dotenv["PLIVO_AUTH_TOKEN"] ?: ""
    val plivoPhoneNumber = dotenv["PLIVO_PHONE_NUMBER"] ?: ""

    val isDevelopment = environment == "development"
}