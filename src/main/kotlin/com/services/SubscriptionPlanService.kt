package com.services

import com.models.SubscriptionPlan
import com.repositories.SubscriptionPlanRepository
import com.routes.CreateSubscriptionPlanRequest
import org.bson.types.ObjectId

class SubscriptionPlanService {
    private val subscriptionPlanRepository = SubscriptionPlanRepository()

    suspend fun createPlan(request: CreateSubscriptionPlanRequest): SubscriptionPlan {
        val plan = SubscriptionPlan(
            image = request.image,
            nameAr = request.nameAr,
            nameEn = request.nameEn,
            price = request.price,
            features = request.features,
            suitableAr = request.suitableAr,
            suitableEn = request.suitableEn,
            discountValue = request.discountValue
        )
        return subscriptionPlanRepository.create(plan)
    }

    suspend fun getPlanById(id: String): SubscriptionPlan {
        return subscriptionPlanRepository.findById(id)
            ?: throw NoSuchElementException("Subscription plan not found")
    }

    suspend fun getAllPlans(): List<SubscriptionPlan> {
        return subscriptionPlanRepository.findAll()
    }

    suspend fun updatePlan(id: String, updates: Map<String, Any>): SubscriptionPlan {
        val updated = subscriptionPlanRepository.updateById(id, updates)
        if (!updated) {
            throw IllegalStateException("Failed to update subscription plan")
        }
        return getPlanById(id)
    }

    suspend fun deletePlan(id: String): Boolean {
        return subscriptionPlanRepository.delete(id)
    }
}