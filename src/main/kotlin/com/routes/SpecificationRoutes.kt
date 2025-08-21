package com.routes

import com.dto.upload.*
import com.services.SpecificationService
import com.services.FileUploadService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.specificationRoutes() {
    val specificationService = SpecificationService()
    val fileUploadService = FileUploadService()

    route("/specifications") {
        get {
            try {
                val specifications = specificationService.getAllSpecifications()
                call.respondSuccess(specifications, "Specifications retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get specifications")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val multipart = call.receiveMultipart()

                    var specData: SpecificationUploadRequest? = null
                    var iconFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    specData = Json.decodeFromString<SpecificationUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "iconImage") {
                                    iconFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = specData ?: throw IllegalArgumentException("Specification data is required")
                    val icon = iconFile ?: throw IllegalArgumentException("Icon image is required")

                    // Upload icon image
                    val uploadResult = fileUploadService.uploadSingleFile(icon, UploadContext.SPECIFICATION_ICON)
                    if (uploadResult.isFailure) {
                        throw IllegalArgumentException("Icon upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }

                    val uploadedIcon = uploadResult.getOrThrow()

                    val specification = specificationService.createSpecification(
                        data.nameAr,
                        data.nameEn,
                        uploadedIcon.url
                    )
                    call.respondSuccess(specification, "Specification created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create specification")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid specification ID")

                    val multipart = call.receiveMultipart()

                    var updateData: SpecificationUploadRequest? = null
                    var newIconFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    updateData = Json.decodeFromString<SpecificationUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "iconImage") {
                                    newIconFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = updateData ?: throw IllegalArgumentException("Update data is required")

                    val updates = mutableMapOf<String, Any>()
                    updates["nameAr"] = data.nameAr
                    updates["nameEn"] = data.nameEn

                    // Upload new icon if provided
                    if (newIconFile != null) {
                        val uploadResult = fileUploadService.uploadSingleFile(newIconFile!!, UploadContext.SPECIFICATION_ICON)
                        if (uploadResult.isFailure) {
                            throw IllegalArgumentException("Icon upload failed: ${uploadResult.exceptionOrNull()?.message}")
                        }
                        updates["iconImage"] = uploadResult.getOrThrow().url
                    }

                    val specification = specificationService.updateSpecification(id, updates)
                    call.respondSuccess(specification, "Specification updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update specification")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid specification ID")
                    val deleted = specificationService.deleteSpecification(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Specification deleted successfully")
                    } else {
                        call.respondError("Failed to delete specification")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete specification")
                }
            }
        }
    }
}