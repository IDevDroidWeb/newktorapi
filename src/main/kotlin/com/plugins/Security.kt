package com.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
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
            realm = AuthConfig.jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(AuthConfig.jwtSecret))
                    .withAudience(AuthConfig.jwtAudience)
                    .withIssuer(AuthConfig.jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(AuthConfig.jwtAudience)) {
                    val userId = credential.payload.getClaim("userId").asString()
                    val phone = credential.payload.getClaim("phone").asString()
                    val isTemp = credential.payload.getClaim("temp")?.asBoolean() ?: false

                    // Only allow non-temporary tokens for regular auth
                    if (!isTemp && userId != null && phone != null) {
                        JWTPrincipal(credential.payload)
                    } else null
                } else null
            }
            challenge { _, _ ->
                call.respondError(
                    "Token is not valid or has expired",
                    status = HttpStatusCode.Unauthorized
                )
            }
        }

        jwt("auth-temp") {
            realm = AuthConfig.jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(AuthConfig.jwtSecret))
                    .withAudience(AuthConfig.jwtAudience)
                    .withIssuer(AuthConfig.jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(AuthConfig.jwtAudience)) {
                    val phone = credential.payload.getClaim("phone").asString()
                    val isTemp = credential.payload.getClaim("temp")?.asBoolean() ?: false

                    // Only allow temporary tokens for temp auth
                    if (isTemp && phone != null) {
                        JWTPrincipal(credential.payload)
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