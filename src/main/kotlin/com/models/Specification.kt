package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Specification(
    @BsonId @Contextual
    val id: String = ObjectId().toHexString(),
    val nameAr: String,
    val nameEn: String,
    val iconImage: String
)