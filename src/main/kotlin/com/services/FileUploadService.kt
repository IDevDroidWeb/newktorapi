// src/main/kotlin/services/FileUploadService.kt
package com.services

import com.config.StorageConfig
import com.dto.upload.*
import com.utils.FileValidators
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class FileUploadService {

    suspend fun uploadFiles(
        parts: List<PartData.FileItem>,
        context: UploadContext
    ): Result<List<UploadedFile>> {
        return try {
            // Validate files
            val (validFiles, errors) = FileValidators.validateFiles(parts, context)

            if (errors.isNotEmpty()) {
                return Result.failure(Exception("Validation failed: ${errors.joinToString { it.error }}"))
            }

            // Upload files based on storage provider
            val uploadedFiles = when (StorageConfig.storageProvider) {
                "local" -> uploadToLocal(validFiles, context)
                "s3" -> uploadToS3(validFiles, context)
                else -> throw IllegalStateException("Unsupported storage provider: ${StorageConfig.storageProvider}")
            }

            Result.success(uploadedFiles)
        } catch (e: Exception) {
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
        val thumbnailDir = File(StorageConfig.baseUploadDir, "${StorageConfig.Folders.THUMBNAILS}/$folder")

        // Create directories if they don't exist
        uploadDir.mkdirs()
        thumbnailDir.mkdirs()

        val uploadedFiles = mutableListOf<UploadedFile>()
        val tempFiles = mutableListOf<File>() // For rollback

        try {
            files.forEach { part ->
                val originalFileName = part.originalFileName ?: "unknown"
                val uniqueFileName = FileValidators.generateUniqueFileName(originalFileName)
                val file = File(uploadDir, uniqueFileName)
                val contentType = part.contentType?.toString() ?: ""

                // Validate file size while reading
                var bytesRead = 0L
                val maxSize = if (FileValidators.isImageFile(contentType)) {
                    StorageConfig.maxImageSize
                } else {
                    StorageConfig.maxVideoSize
                }

                // Save original file with size validation
                part.streamProvider().use { input ->
                    file.outputStream().buffered().use { output ->
                        val buffer = ByteArray(8192)
                        var bytes: Int
                        while (input.read(buffer).also { bytes = it } != -1) {
                            bytesRead += bytes
                            if (bytesRead > maxSize) {
                                throw IllegalArgumentException("File $originalFileName exceeds maximum size limit")
                            }
                            output.write(buffer, 0, bytes)
                        }
                    }
                }

                tempFiles.add(file) // Track for potential rollback

                val fileUrl = "/uploads/$folder/$uniqueFileName"
                var thumbnailUrl: String? = null

                // Generate thumbnail for images with optimization
                if (FileValidators.isImageFile(contentType)) {
                    thumbnailUrl = generateOptimizedThumbnail(file, thumbnailDir, uniqueFileName, folder)

                    // Optimize original image too
                    optimizeImage(file)
                }

                uploadedFiles.add(
                    UploadedFile(
                        originalName = originalFileName,
                        fileName = uniqueFileName,
                        url = fileUrl,
                        thumbnailUrl = thumbnailUrl,
                        size = file.length(),
                        mimeType = contentType,
                        folder = folder
                    )
                )
            }

            uploadedFiles
        } catch (e: Exception) {
            // Rollback: delete any files that were created
            tempFiles.forEach { file ->
                if (file.exists()) {
                    file.delete()
                }
                // Also delete thumbnail if it exists
                val thumbFile = File(thumbnailDir, "thumb_${file.name}")
                if (thumbFile.exists()) {
                    thumbFile.delete()
                }
            }
            throw e
        }
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
            val contentType = part.contentType?.toString() ?: ""

            // S3 upload implementation would go here:
            // 1. Create S3Client with credentials
            // 2. Upload file using PutObjectRequest
            // 3. Generate presigned URLs or use public URLs
            // 4. Generate thumbnails and upload them too

            val s3Key = "$folder/$uniqueFileName"
            val fileUrl = "${StorageConfig.awsS3BaseUrl}/$s3Key"
            var thumbnailUrl: String? = null

            if (FileValidators.isImageFile(contentType)) {
                thumbnailUrl = "${StorageConfig.awsS3BaseUrl}/${StorageConfig.Folders.THUMBNAILS}/$folder/thumb_$uniqueFileName"
            }

            uploadedFiles.add(
                UploadedFile(
                    originalName = originalFileName,
                    fileName = uniqueFileName,
                    url = fileUrl,
                    thumbnailUrl = thumbnailUrl,
                    size = 0L, // Would get from S3 response
                    mimeType = contentType,
                    folder = folder
                )
            )
        }

        uploadedFiles
    }

   /* private suspend fun uploadToCloudinary(
        files: List<PartData.FileItem>,
        context: UploadContext
    ): List<UploadedFile> = withContext(Dispatchers.IO) {
        // TODO: Implement Cloudinary upload
        // This would use Cloudinary SDK to upload files
        val folder = FileValidators.getFolderByContext(context)
        val uploadedFiles = mutableListOf<UploadedFile>()

        files.forEach { part ->
            val originalFileName = part.originalFileName ?: "unknown"
            val uniqueFileName = FileValidators.generateUniqueFileName(originalFileName)
            val contentType = part.contentType?.toString() ?: ""

            // Upload to Cloudinary logic here
            val publicId = "$folder/$uniqueFileName"
            val fileUrl = "${StorageConfig.cloudinaryBaseUrl}/image/upload/$publicId"
            val thumbnailUrl = "${StorageConfig.cloudinaryBaseUrl}/image/upload/w_${StorageConfig.ThumbnailSizes.MEDIUM}/$publicId"

            // For now, just create a placeholder
            uploadedFiles.add(
                UploadedFile(
                    originalName = originalFileName,
                    fileName = uniqueFileName,
                    url = fileUrl,
                    thumbnailUrl = thumbnailUrl,
                    size = 0L, // TODO: Get actual file size
                    mimeType = contentType,
                    folder = folder
                )
            )
        }

        uploadedFiles
    }*/

    private fun generateOptimizedThumbnail(
        originalFile: File,
        thumbnailDir: File,
        fileName: String,
        folder: String
    ): String? {
        return try {
            val originalImage = ImageIO.read(originalFile)
            if (originalImage == null) {
                println("Could not read image file: ${originalFile.name}")
                return null
            }

            // Calculate dimensions maintaining aspect ratio
            val originalWidth = originalImage.width
            val originalHeight = originalImage.height
            val targetSize = StorageConfig.ThumbnailSizes.MEDIUM

            val (newWidth, newHeight) = if (originalWidth > originalHeight) {
                val ratio = targetSize.toDouble() / originalWidth
                targetSize to (originalHeight * ratio).toInt()
            } else {
                val ratio = targetSize.toDouble() / originalHeight
                (originalWidth * ratio).toInt() to targetSize
            }

            // Create high-quality thumbnail
            val thumbnailImage = originalImage.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
            )

            val bufferedThumbnail = BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB
            )

            val graphics = bufferedThumbnail.createGraphics()
            graphics.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON
            )
            graphics.drawImage(thumbnailImage, 0, 0, null)
            graphics.dispose()

            val extension = FileValidators.getFileExtension(fileName).lowercase()
            val outputFormat = when (extension) {
                "png" -> "png"
                "webp" -> "jpg" // Convert WebP to JPG for compatibility
                else -> "jpg"
            }

            val thumbnailFile = File(thumbnailDir, "thumb_${fileName.substringBeforeLast('.')}.${outputFormat}")
            ImageIO.write(bufferedThumbnail, outputFormat, thumbnailFile)

            "/uploads/${StorageConfig.Folders.THUMBNAILS}/$folder/thumb_${fileName.substringBeforeLast('.')}.${outputFormat}"
        } catch (e: Exception) {
            println("Failed to generate thumbnail for $fileName: ${e.message}")
            null
        }
    }

    private fun optimizeImage(imageFile: File) {
        try {
            val originalImage = ImageIO.read(imageFile)
            if (originalImage == null) return

            // Only optimize if image is too large
            val maxDimension = 1920 // Max width or height
            val width = originalImage.width
            val height = originalImage.height

            if (width <= maxDimension && height <= maxDimension) return

            // Calculate new dimensions
            val (newWidth, newHeight) = if (width > height) {
                val ratio = maxDimension.toDouble() / width
                maxDimension to (height * ratio).toInt()
            } else {
                val ratio = maxDimension.toDouble() / height
                (width * ratio).toInt() to maxDimension
            }

            // Create optimized image
            val optimizedImage = originalImage.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
            )

            val bufferedOptimized = BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB
            )

            val graphics = bufferedOptimized.createGraphics()
            graphics.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.drawImage(optimizedImage, 0, 0, null)
            graphics.dispose()

            // Overwrite original file with optimized version
            val extension = FileValidators.getFileExtension(imageFile.name)
            ImageIO.write(bufferedOptimized, extension.ifEmpty { "jpg" }, imageFile)

        } catch (e: Exception) {
            println("Failed to optimize image ${imageFile.name}: ${e.message}")
        }
    }

    private suspend fun rollbackUploads(context: UploadContext) {
        // TODO: Implement rollback logic
        // This would delete any files that were uploaded during the failed operation
        println("Rolling back uploads for context: $context")
    }

    fun deleteFile(fileUrl: String): Boolean {
        return try {
            when (StorageConfig.storageProvider) {
                "local" -> {
                    val file = File(".$fileUrl") // Remove leading slash for local files
                    if (file.exists()) {
                        file.delete()
                    } else false
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
}