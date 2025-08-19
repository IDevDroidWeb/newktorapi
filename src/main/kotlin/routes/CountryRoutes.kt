package com.routes

import com.services.CountryService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.toObjectId
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateCountryRequest(
    val nameAr: String,
    val nameEn: String,
    val flagImage: String
)

@Serializable
data class UpdateCountryRequest(
    val nameAr: String? = null,
    val nameEn: String? = null,
    val flagImage: String? = null
)

fun Route.countryRoutes() {
    val countryService = CountryService()

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
                val id = call.parameters["id"]?.toObjectId()
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
                    val request = call.receive<CreateCountryRequest>()
                    val country = countryService.createCountry(
                        request.nameAr,
                        request.nameEn,
                        request.flagImage
                    )
                    call.respondSuccess(country, "Country created successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to create country")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toObjectId()
                        ?: throw IllegalArgumentException("Invalid country ID")
                    val request = call.receive<UpdateCountryRequest>()

                    val updates = mutableMapOf<String, Any>()
                    request.nameAr?.let { updates["nameAr"] = it }
                    request.nameEn?.let { updates["nameEn"] = it }
                    request.flagImage?.let { updates["flagImage"] = it }

                    if (updates.isEmpty()) {
                        call.respondError("No fields to update")
                        return@put
                    }

                    val country = countryService.updateCountry(id, updates)
                    call.respondSuccess(country, "Country updated successfully")
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to update country")
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toObjectId()
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