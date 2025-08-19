package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class SubscriptionPlan(
    @BsonId @Contextual
    val id: ObjectId = ObjectId(),
    val image: String,
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val features: List<String>,
    val suitableAr: String,
    val suitableEn: String,
    val discountValue: Double = 0.0
)