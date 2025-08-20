package com.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.auth.jwt.*
import org.bson.types.ObjectId

object AuthConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val jwtSecret = dotenv["JWT_SECRET"] ?: "default-secret-key"
    val jwtIssuer = dotenv["JWT_ISSUER"] ?: "realestate-api"
    val jwtAudience = dotenv["JWT_AUDIENCE"] ?: "realestate-client"
    val jwtRealm = dotenv["JWT_REALM"] ?: "realestate-realm"

    private val algorithm = Algorithm.HMAC256(jwtSecret)

    fun generateToken(userId: String, phone: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId.toString())
            .withClaim("phone", phone)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
            .sign(algorithm)
    }

    fun generateTempToken(phone: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("phone", phone)
            .withClaim("temp", true)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 minutes
            .sign(algorithm)
    }

    fun configureJWT(): JWTAuthenticationProvider.Config.() -> Unit = {
        verifier(
            JWT.require(algorithm)
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .build()
        )
        validate { credential ->
            if (credential.payload.audience.contains(jwtAudience)) {
                val userId = credential.payload.getClaim("userId").asString()
                val phone = credential.payload.getClaim("phone").asString()
                val isTemp = credential.payload.getClaim("temp")?.asBoolean() ?: false

                JWTPrincipal(
                    mapOf(
                        "userId" to userId,
                        "phone" to phone,
                        "isTemp" to isTemp.toString()
                    ) as Payload
                )
            } else null
        }
        realm = jwtRealm
    }
}