package com.services

import com.config.AppConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import java.util.*

class SmsService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Serializable
    data class PlivoSmsRequest(
        val src: String,
        val dst: String,
        val text: String
    )

    suspend fun sendVerificationCode(phone: String, code: String = generateCode()): Boolean {
        return try {
            if (AppConfig.isDevelopment) {
                println("SMS Verification Code for $phone: $code")
                return true
            }

            val message = "Your Real Estate verification code is: $code. Do not share this code with anyone."

            val response = client.post("https://api.plivo.com/v1/Account/${AppConfig.plivoAuthId}/Message/") {
                contentType(ContentType.Application.Json)
                val credentials = "${AppConfig.plivoAuthId}:${AppConfig.plivoAuthToken}"
                val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
                header("Authorization", "Basic $encodedCredentials")

                setBody(PlivoSmsRequest(
                    src = AppConfig.plivoPhoneNumber,
                    dst = phone,
                    text = message
                ))
            }

            response.status == HttpStatusCode.Accepted || response.status == HttpStatusCode.Created
        } catch (e: Exception) {
            println("SMS sending failed: ${e.message}")
            false
        }
    }

    private fun generateCode(): String {
        return (100000..999999).random().toString()
    }
}