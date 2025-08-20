package com.utils

import com.dto.common.ApiResponse
import com.dto.common.PaginatedResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object ResponseWrapper {
    suspend inline fun <reified T> ApplicationCall.respondSuccess(
        data: T,
        message: String = "Success",
        status: HttpStatusCode = HttpStatusCode.OK
    ) {
        respond(status, ApiResponse(success = true, message = message, data = data))
    }

    suspend fun ApplicationCall.respondError(
        message: String,
        errors: List<String>? = null,
        status: HttpStatusCode = HttpStatusCode.BadRequest
    ) {
        respond(status, ApiResponse<Unit>(success = false, message = message, errors = errors))
    }

    suspend inline fun <reified T> ApplicationCall.respondPaginated(
        items: List<T>,
        page: Int,
        limit: Int,
        total: Int,
        message: String = "Success"
    ) {
        val totalPages = (total + limit - 1) / limit
        val paginatedResponse = PaginatedResponse(items, page, limit, total, totalPages)
        respond(HttpStatusCode.OK, ApiResponse(success = true, message = message, data = paginatedResponse))
    }
}