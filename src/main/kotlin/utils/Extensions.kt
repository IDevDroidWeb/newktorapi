package com.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun ApplicationCall.getUserId(): ObjectId {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString()
        ?: throw IllegalStateException("User ID not found in token")
    return ObjectId(userId)
}

fun ApplicationCall.getUserPhone(): String {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("phone")?.asString()
        ?: throw IllegalStateException("Phone not found in token")
}

fun ApplicationCall.isTemporaryToken(): Boolean {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("temp")?.asBoolean() ?: false
}

fun LocalDateTime.toIsoString(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun String.toObjectId(): ObjectId? {
    return try {
        ObjectId(this)
    } catch (e: Exception) {
        null
    }
}