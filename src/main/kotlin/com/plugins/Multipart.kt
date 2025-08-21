package com.plugins

import com.config.StorageConfig
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureMultipart() {
    install(PartialContent) {
        // Maximum number of ranges that will be accepted from HTTP request
        maxRangeCount = 10
    }

    // Serve static files from uploads directory (for local development)
    if (StorageConfig.isLocalStorage) {
        routing {
            staticFiles("/uploads", File(StorageConfig.baseUploadDir))
        }
    }
}