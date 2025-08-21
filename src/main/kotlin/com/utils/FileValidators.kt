package com.utils

import com.config.StorageConfig
import com.dto.upload.FileValidationError
import com.dto.upload.UploadContext
import io.ktor.http.content.*
import java.io.File

object FileValidators {

    fun validateFiles(
        parts: List<PartData.FileItem>,
        context: UploadContext
    ): Pair<List<PartData.FileItem>, List<FileValidationError>> {
        val validFiles = mutableListOf<PartData.FileItem>()
        val errors = mutableListOf<FileValidationError>()

        // Validate file count based on context
        val (minFiles, maxFiles, allowedTypes) = getContextConstraints(context)

        if (parts.size < minFiles) {
            errors.add(FileValidationError(
                fileName = "general",
                error = "At least $minFiles file(s) required for $context"
            ))
        }

        if (parts.size > maxFiles) {
            errors.add(FileValidationError(
                fileName = "general",
                error = "Maximum $maxFiles file(s) allowed for $context"
            ))
        }

        parts.forEach { part ->
            val fileName = part.originalFileName ?: "unknown"
            val contentType = part.contentType?.toString() ?: ""

            // Validate file type
            if (!allowedTypes.contains(contentType)) {
                errors.add(FileValidationError(
                    fileName = fileName,
                    error = "File type '$contentType' not allowed. Allowed types: ${allowedTypes.joinToString()}"
                ))
                return@forEach
            }

            // Validate file size
            val maxSize = if (StorageConfig.allowedImageTypes.contains(contentType)) {
                StorageConfig.maxImageSize
            } else {
                StorageConfig.maxVideoSize
            }

            try {
                // Note: For real validation, you'd want to read the file size properly
                // This is a simplified version
                validFiles.add(part)
            } catch (e: Exception) {
                errors.add(FileValidationError(
                    fileName = fileName,
                    error = "Failed to validate file: ${e.message}"
                ))
            }
        }

        return Pair(validFiles, errors)
    }

    fun validateSingleFile(
        part: PartData.FileItem,
        context: UploadContext
    ): FileValidationError? {
        val (validFiles, errors) = validateFiles(listOf(part), context)
        return errors.firstOrNull()
    }

    private fun getContextConstraints(context: UploadContext): Triple<Int, Int, Set<String>> {
        return when (context) {
            UploadContext.PROPERTY_IMAGES -> Triple(
                1, // min files
                10, // max files
                StorageConfig.allowedImageTypes
            )
            UploadContext.PROPERTY_VIDEO -> Triple(
                0, // min files (optional)
                1, // max files
                StorageConfig.allowedVideoTypes
            )
            UploadContext.USER_PROFILE -> Triple(
                0, // min files (optional)
                1, // max files
                StorageConfig.allowedImageTypes
            )
            UploadContext.COUNTRY_FLAG,
            UploadContext.ONBOARDING_IMAGE,
            UploadContext.SPECIFICATION_ICON,
            UploadContext.SUBSCRIPTION_PLAN_IMAGE -> Triple(
                1, // min files (mandatory)
                1, // max files
                StorageConfig.allowedImageTypes
            )
            UploadContext.STORY_MEDIA -> Triple(
                1, // min files (mandatory)
                1, // max files
                StorageConfig.allowedImageTypes + StorageConfig.allowedVideoTypes
            )
        }
    }

    fun generateUniqueFileName(originalFileName: String): String {
        val timestamp = System.currentTimeMillis()
        val randomString = (1..8)
            .map { ('a'..'z').random() }
            .joinToString("")

        val extension = File(originalFileName).extension
        return "${timestamp}_${randomString}.$extension"
    }

    fun getFileExtension(fileName: String): String {
        return File(fileName).extension.lowercase()
    }

    fun isImageFile(contentType: String): Boolean {
        return StorageConfig.allowedImageTypes.contains(contentType)
    }

    fun isVideoFile(contentType: String): Boolean {
        return StorageConfig.allowedVideoTypes.contains(contentType)
    }

    fun getFolderByContext(context: UploadContext): String {
        return when (context) {
            UploadContext.PROPERTY_IMAGES,
            UploadContext.PROPERTY_VIDEO -> StorageConfig.Folders.PROPERTIES
            UploadContext.USER_PROFILE -> StorageConfig.Folders.USERS
            UploadContext.COUNTRY_FLAG -> StorageConfig.Folders.COUNTRIES
            UploadContext.ONBOARDING_IMAGE -> StorageConfig.Folders.ONBOARDING
            UploadContext.SPECIFICATION_ICON -> StorageConfig.Folders.SPECIFICATIONS
            UploadContext.STORY_MEDIA -> StorageConfig.Folders.STORIES
            UploadContext.SUBSCRIPTION_PLAN_IMAGE -> StorageConfig.Folders.SUBSCRIPTION_PLANS
        }
    }
}