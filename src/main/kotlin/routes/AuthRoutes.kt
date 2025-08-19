package com.routes

import com.dto.auth.*
import com.services.AuthService
import com.utils.ResponseWrapper.respondError
import com.utils.ResponseWrapper.respondSuccess
import com.utils.Validators
import com.utils.getUserPhone
import com.utils.isTemporaryToken
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val authService = AuthService()

    post("/login") {
        try {
            val request = call.receive<LoginRequest>()

            // Use simple validation
            val errors = Validators.validateLogin(request.identifier, request.password)
            if (errors.isNotEmpty()) {
                call.respondError("Validation failed", errors)
                return@post
            }

            val response = authService.login(request)
            call.respondSuccess(response, "Login successful")
        } catch (e: Exception) {
            call.respondError(e.message ?: "Login failed")
        }
    }

    post("/register/step1") {
        try {
            val request = call.receive<RegisterStep1Request>()

            // Use simple validation
            val errors = Validators.validateRegisterStep1(request.phone)
            if (errors.isNotEmpty()) {
                call.respondError("Validation failed", errors)
                return@post
            }

            val response = authService.registerStep1(request)
            call.respondSuccess(response, "Verification code sent")
        } catch (e: Exception) {
            call.respondError(e.message ?: "Registration step 1 failed")
        }
    }

    authenticate("auth-temp") {
        post("/register/verify-phone") {
            try {
                val request = call.receive<VerifyPhoneRequest>()
                val phone = call.getUserPhone()

                if (call.isTemporaryToken()) {
                    val verified = authService.verifyPhone(request, phone)
                    if (verified) {
                        call.respondSuccess(mapOf("verified" to true), "Phone verified successfully")
                    } else {
                        call.respondError("Phone verification failed")
                    }
                } else {
                    call.respondError("Invalid temporary token", status = HttpStatusCode.Unauthorized)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Phone verification failed")
            }
        }

        post("/register/step2") {
            try {
                val request = call.receive<RegisterStep2Request>()
                val phone = call.getUserPhone()

                // Use simple validation
                val errors = Validators.validateRegisterStep2(request.name, request.email, request.password)
                if (errors.isNotEmpty()) {
                    call.respondError("Validation failed", errors)
                    return@post
                }

                val success = authService.registerStep2(request, phone)
                if (success) {
                    call.respondSuccess(mapOf("completed" to true), "Step 2 completed")
                } else {
                    call.respondError("Registration step 2 failed")
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Registration step 2 failed")
            }
        }

        post("/register/step3") {
            try {
                val request = call.receive<RegisterStep3Request>()
                val phone = call.getUserPhone()

                val success = authService.registerStep3(request, phone)
                if (success) {
                    call.respondSuccess(mapOf("completed" to true), "Step 3 completed")
                } else {
                    call.respondError("Registration step 3 failed")
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Registration step 3 failed")
            }
        }

        post("/register/final") {
            try {
                val phone = call.getUserPhone()
                val response = authService.registerFinal(phone)
                call.respondSuccess(response, "Registration completed successfully")
            } catch (e: Exception) {
                call.respondError(e.message ?: "Registration completion failed")
            }
        }
    }
}