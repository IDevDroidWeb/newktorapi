package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Governorate(
    @BsonId @Contextual
    val id: String = ObjectId().toHexString(),
    @Contextual val countryId: String,
    val nameAr: String,
    val nameEn: String
)