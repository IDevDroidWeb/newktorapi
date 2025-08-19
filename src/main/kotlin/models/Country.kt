package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Country(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val nameAr: String,
    val nameEn: String,
    val flagImage: String
)