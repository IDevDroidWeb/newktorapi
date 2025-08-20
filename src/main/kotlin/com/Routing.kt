package com

import com.routes.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "docs")

        route("/api/v1") {
            rateLimit(RateLimitName("api")) {
                // Auth routes with specific rate limiting
                route("/auth") {
                    rateLimit(RateLimitName("auth")) {
                        authRoutes()
                    }
                }

                // Public routes
                onboardingRoutes()
                countryRoutes()
                governorateRoutes()
                specificationRoutes()
                subscriptionPlanRoutes()

                // Protected routes
                userRoutes()
                propertyRoutes()
                storyRoutes()
                chatRoutes()
                adminRoutes()
            }
        }

        // Health check endpoint (no rate limiting)
        get("/health") {
            call.respond("OK")
        }
    }
}