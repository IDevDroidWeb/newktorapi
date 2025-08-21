package com.routes

import com.dto.upload.*
import com.services.CountryService
import com.services.FileUploadService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.countryRoutes() {
    val countryService = CountryService()
    val fileUploadService = FileUploadService()

    route("/countries") {
        get {
            try {
                val countries = countryService.getAllCountries()
                call.respondSuccess(countries, "Countries retrieved")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get countries")
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("Invalid country ID")
                val country = countryService.getCountryById(id)
                call.respondSuccess(country, "Country found")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Country not found")
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val multipart = call.receiveMultipart()

                    var countryData: CountryUploadRequest? = null
                    var flagFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    countryData = Json.decodeFromString<CountryUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "flagImage") {
                                    flagFile = part
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val data = countryData ?: throw IllegalArgumentException("Country data is required")
                    val flag = flagFile ?: throw IllegalArgumentException("Flag image is required")

                    // Upload flag image
                    val uploadResult = fileUploadService.uploadSingleFile(flag, UploadContext.COUNTRY_FLAG)
                    if (uploadResult.isFailure) {
                        throw IllegalArgumentException("Flag image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }

                    val uploadedFlag = uploadResult.getOrThrow()

                    val country = countryService.createCountry(
                        data.nameAr,
                        data.nameEn,
                        uploadedFlag.url
                    )
                    call.respondSuccess(country, "Country created successfully with flag image")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create country")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid country ID")

                    val multipart = call.receiveMultipart()

                    var updateData: CountryUploadRequest? = null
                    var newFlagFile: PartData.FileItem? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "data") {
                                    updateData = Json.decodeFromString<CountryUploadRequest>(part.value)
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "flagImage") {
                                    newFlagFile = part
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

                    // Upload new flag if provided
                    if (newFlagFile != null) {
                        val uploadResult = fileUploadService.uploadSingleFile(newFlagFile!!, UploadContext.COUNTRY_FLAG)
                        if (uploadResult.isFailure) {
                            throw IllegalArgumentException("Flag image upload failed: ${uploadResult.exceptionOrNull()?.message}")
                        }
                        updates["flagImage"] = uploadResult.getOrThrow().url
                    }

                    val country = countryService.updateCountry(id, updates)
                    call.respondSuccess(country, "Country updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update country")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Invalid country ID")
                    val deleted = countryService.deleteCountry(id)
                    if (deleted) {
                        call.respondSuccess(mapOf("deleted" to true), "Country deleted successfully")
                    } else {
                        call.respondError("Failed to delete country")
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete country")
                }
            }
        }
    }
}