package com.plugins

import com.utils.ResponseWrapper.respondError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    call.respondError(
                        message = cause.message ?: "Invalid request",
                        status = HttpStatusCode.BadRequest
                    )
                }
                is IllegalStateException -> {
                    call.respondError(
                        message = cause.message ?: "Invalid state",
                        status = HttpStatusCode.BadRequest
                    )
                }
                is NoSuchElementException -> {
                    call.respondError(
                        message = "Resource not found",
                        status = HttpStatusCode.NotFound
                    )
                }
                else -> {
                    call.application.log.error("Unhandled exception", cause)
                    call.respondError(
                        message = "Internal server error",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondError(
                message = "Endpoint not found",
                status = HttpStatusCode.NotFound
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respondError(
                message = "Unauthorized access",
                status = HttpStatusCode.Unauthorized
            )
        }
    }
}