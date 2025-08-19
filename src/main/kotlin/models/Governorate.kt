package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Governorate(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    @Contextual val countryId: ObjectId,
    val nameAr: String,
    val nameEn: String
)