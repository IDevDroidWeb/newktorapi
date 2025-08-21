// src/main/kotlin/services/FileUploadService.kt
package com.services

import com.config.StorageConfig
import com.dto.upload.*
import com.utils.FileValidators
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileUploadService {

    suspend fun uploadFiles(
        parts: List<PartData.FileItem>,
        context: UploadContext
    ): Result<List<UploadedFile>> {
        return try {
            println("=== Upload Debug Info ===")
            println("Number of parts: ${parts.size}")
            parts.forEachIndexed { index, part ->
                println("Part $index:")
                println("  - Original filename: ${part.originalFileName}")
                println("  - Content type: ${part.contentType}")
                println("  - Name: ${part.name}")
            }

            // Validate files with detailed logging
            val (validFiles, errors) = FileValidators.validateFiles(parts, context)

            println("Validation results:")
            println("  - Valid files: ${validFiles.size}")
            println("  - Errors: ${errors.size}")
            errors.forEach { error ->
                println("  - Error: ${error.error}")
            }

            if (errors.isNotEmpty()) {
                return Result.failure(Exception("Validation failed: ${errors.joinToString { it.error }}"))
            }

            // Upload files based on storage provider
            val uploadedFiles = when (StorageConfig.storageProvider) {
                "local" -> uploadToLocal(validFiles, context)
                "s3" -> uploadToS3(validFiles, context)
                else -> throw IllegalStateException("Unsupported storage provider: ${StorageConfig.storageProvider}")
            }

            println("Upload completed successfully: ${uploadedFiles.size} files")
            Result.success(uploadedFiles)
        } catch (e: Exception) {
            println("Upload failed with exception: ${e.message}")
            e.printStackTrace()
            // Rollback on failure
            rollbackUploads(context)
            Result.failure(e)
        }
    }

    suspend fun uploadSingleFile(
        part: PartData.FileItem,
        context: UploadContext
    ): Result<UploadedFile> {
        val result = uploadFiles(listOf(part), context)
        return result.map { it.first() }
    }

    private suspend fun uploadToLocal(
        files: List<PartData.FileItem>,
        context: UploadContext
    ): List<UploadedFile> = withContext(Dispatchers.IO) {
        val folder = FileValidators.getFolderByContext(context)
        val uploadDir = File(StorageConfig.baseUploadDir, folder)

        println("Upload directory: ${uploadDir.absolutePath}")

        // Create directory if it doesn't exist
        if (!uploadDir.exists()) {
            val created = uploadDir.mkdirs()
            println("Created upload directory: $created")
        }

        val uploadedFiles = mutableListOf<UploadedFile>()
        val tempFiles = mutableListOf<File>() // For rollback

        try {
            files.forEachIndexed { index, part ->
                println("Processing file $index:")

                val originalFileName = part.originalFileName ?: "unknown_${System.currentTimeMillis()}.bin"
                println("  - Original filename: $originalFileName")

                val contentType = detectMimeType(part)
                println("  - Detected MIME type: $contentType")

                val uniqueFileName = FileValidators.generateUniqueFileName(originalFileName)
                println("  - Unique filename: $uniqueFileName")

                val file = File(uploadDir, uniqueFileName)
                println("  - Target file path: ${file.absolutePath}")

                // Determine max file size based on content type
                val maxSize = if (FileValidators.isImageFile(contentType)) {
                    StorageConfig.maxImageSize
                } else {
                    StorageConfig.maxVideoSize
                }
                println("  - Max allowed size: ${maxSize / 1024 / 1024} MB")

                // Save file directly without any processing
                val fileSize = saveFileDirectly(part, file, maxSize, originalFileName)
                println("  - File saved successfully, size: ${fileSize} bytes")

                tempFiles.add(file) // Track for potential rollback

                val fileUrl = "/uploads/$folder/$uniqueFileName"
                println("  - File URL: $fileUrl")

                uploadedFiles.add(
                    UploadedFile(
                        originalName = originalFileName,
                        fileName = uniqueFileName,
                        url = fileUrl,
                        thumbnailUrl = null, // No thumbnails generated
                        size = fileSize,
                        mimeType = contentType,
                        folder = folder
                    )
                )
            }

            uploadedFiles
        } catch (e: Exception) {
            println("Error during local upload: ${e.message}")
            e.printStackTrace()

            // Rollback: delete any files that were created
            tempFiles.forEach { file ->
                if (file.exists()) {
                    val deleted = file.delete()
                    println("Rollback - deleted file ${file.name}: $deleted")
                }
            }
            throw e
        }
    }

    private fun saveFileDirectly(
        part: PartData.FileItem,
        file: File,
        maxSize: Long,
        originalFileName: String
    ): Long {
        var bytesRead = 0L
        val buffer = ByteArray(8192)

        try {
            part.streamProvider().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    var bytesReadInChunk: Int
                    while (inputStream.read(buffer).also { bytesReadInChunk = it } != -1) {
                        bytesRead += bytesReadInChunk
                        if (bytesRead > maxSize) {
                            // Clean up partial file before throwing exception
                            if (file.exists()) {
                                file.delete()
                            }
                            throw IllegalArgumentException("File $originalFileName exceeds maximum size limit of ${maxSize / 1024 / 1024} MB")
                        }
                        outputStream.write(buffer, 0, bytesReadInChunk)
                    }
                    outputStream.flush() // Ensure all data is written
                }
            }

            println("    - File write completed: ${file.length()} bytes")

            // Verify file was written correctly
            if (!file.exists()) {
                throw IllegalStateException("File was not created: ${file.absolutePath}")
            }

            if (file.length() == 0L) {
                throw IllegalStateException("File is empty after write: ${file.absolutePath}")
            }

            return bytesRead

        } catch (e: Exception) {
            println("Error saving file directly: ${e.message}")
            // Clean up on error
            if (file.exists()) {
                file.delete()
            }
            throw e
        }
    }

    private fun detectMimeType(part: PartData.FileItem): String {
        // First try to get from part
        val partContentType = part.contentType?.toString()
        if (!partContentType.isNullOrBlank()) {
            println("    - MIME type from part: $partContentType")
            return partContentType
        }

        // Try to detect from filename extension
        val fileName = part.originalFileName
        if (!fileName.isNullOrBlank()) {
            val extension = FileValidators.getFileExtension(fileName).lowercase()
            val detectedType = when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "bmp" -> "image/bmp"
                "tiff", "tif" -> "image/tiff"
                "svg" -> "image/svg+xml"
                "mp4" -> "video/mp4"
                "avi" -> "video/avi"
                "mov" -> "video/quicktime"
                "wmv" -> "video/x-ms-wmv"
                "flv" -> "video/x-flv"
                "webm" -> "video/webm"
                "mkv" -> "video/x-matroska"
                "3gp" -> "video/3gpp"
                "ogv" -> "video/ogg"
                else -> "application/octet-stream"
            }
            println("    - MIME type detected from extension '$extension': $detectedType")
            return detectedType
        }

        println("    - MIME type fallback: application/octet-stream")
        return "application/octet-stream"
    }

    private suspend fun uploadToS3(
        files: List<PartData.FileItem>,
        context: UploadContext
    ): List<UploadedFile> = withContext(Dispatchers.IO) {
        // TODO: Implement S3 upload using AWS SDK
        // This is a placeholder for future S3 implementation
        val folder = FileValidators.getFolderByContext(context)
        val uploadedFiles = mutableListOf<UploadedFile>()

        files.forEach { part ->
            val originalFileName = part.originalFileName ?: "unknown"
            val uniqueFileName = FileValidators.generateUniqueFileName(originalFileName)
            val contentType = detectMimeType(part)

            // S3 upload implementation would go here:
            // 1. Create S3Client with credentials
            // 2. Upload file using PutObjectRequest
            // 3. Generate presigned URLs or use public URLs

            val s3Key = "$folder/$uniqueFileName"
            val fileUrl = "${StorageConfig.awsS3BaseUrl}/$s3Key"

            uploadedFiles.add(
                UploadedFile(
                    originalName = originalFileName,
                    fileName = uniqueFileName,
                    url = fileUrl,
                    thumbnailUrl = null, // No thumbnails for direct upload
                    size = 0L, // Would get from S3 response
                    mimeType = contentType,
                    folder = folder
                )
            )
        }

        uploadedFiles
    }

    private suspend fun rollbackUploads(context: UploadContext) {
        withContext(Dispatchers.IO) {
            try {
                val folder = FileValidators.getFolderByContext(context)
                println("Rolling back uploads for context: $context in folder: $folder")

                // Basic rollback implementation
                // In production, you'd want to track specific files created during this upload session
            } catch (e: Exception) {
                println("Error during rollback: ${e.message}")
            }
        }
    }

    fun deleteFile(fileUrl: String): Boolean {
        return try {
            when (StorageConfig.storageProvider) {
                "local" -> {
                    val file = File(".$fileUrl") // Remove leading slash for local files
                    if (file.exists() && file.isFile()) {
                        val deleted = file.delete()
                        println("Deleted file $fileUrl: $deleted")
                        deleted
                    } else {
                        println("File does not exist or is not a file: $fileUrl")
                        false
                    }
                }
                "s3" -> {
                    // TODO: Implement S3 file deletion using AWS SDK
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            println("Failed to delete file $fileUrl: ${e.message}")
            false
        }
    }

    fun deleteFiles(fileUrls: List<String>): Int {
        var deletedCount = 0
        fileUrls.forEach { url ->
            if (deleteFile(url)) {
                deletedCount++
            }
        }
        return deletedCount
    }

    // Utility function to validate uploaded file integrity
    fun validateUploadedFile(file: File): Boolean {
        return try {
            val exists = file.exists()
            val hasSize = file.length() > 0
            println("File validation - exists: $exists, size: ${file.length()}")
            exists && hasSize
        } catch (e: Exception) {
            println("Error validating file ${file.absolutePath}: ${e.message}")
            false
        }
    }

    // Helper function to bypass FileValidators validation for testing
    suspend fun uploadFilesWithoutValidation(
        parts: List<PartData.FileItem>,
        context: UploadContext
    ): Result<List<UploadedFile>> {
        return try {
            println("=== Direct Upload Without Validation ===")
            val uploadedFiles = when (StorageConfig.storageProvider) {
                "local" -> uploadToLocal(parts, context)
                "s3" -> uploadToS3(parts, context)
                else -> throw IllegalStateException("Unsupported storage provider: ${StorageConfig.storageProvider}")
            }

            Result.success(uploadedFiles)
        } catch (e: Exception) {
            println("Direct upload failed: ${e.message}")
            e.printStackTrace()
            rollbackUploads(context)
            Result.failure(e)
        }
    }
}