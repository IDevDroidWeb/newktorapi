package com

import com.config.DatabaseConfig
import com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = System.getenv("HOST") ?: "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseConfig.init()

    // Configure plugins
    configureSecurity()
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureMultipart() // NEW: Add multipart support
    configureRouting()
}