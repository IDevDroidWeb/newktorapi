package com.plugins

import com.config.AuthConfig
import com.utils.ResponseWrapper.respondError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            configure(AuthConfig.configureJWT())
            challenge { _, _ ->
                call.respondError(
                    "Token is not valid or has expired",
                    status = HttpStatusCode.Unauthorized
                )
            }
        }

        jwt("auth-temp") {
            configure(AuthConfig.configureJWT())
            validate { credential ->
                if (credential.payload.audience.contains(AuthConfig.jwtAudience)) {
                    val phone = credential.payload.getClaim("phone").asString()
                    val isTemp = credential.payload.getClaim("temp")?.asBoolean() ?: false

                    if (isTemp) {
                        JWTPrincipal(
                            mapOf(
                                "phone" to phone,
                                "isTemp" to isTemp.toString()
                            )
                        )
                    } else null
                } else null
            }
            challenge { _, _ ->
                call.respondError(
                    "Temporary token is not valid or has expired",
                    status = HttpStatusCode.Unauthorized
                )
            }
        }
    }

    install(RateLimit) {
        register(RateLimitName("api")) {
            rateLimiter(limit = 60, refillPeriod = 1.minutes)
        }
        register(RateLimitName("auth")) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
        }
    }
}