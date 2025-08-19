package com.repositories

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.config.DatabaseConfig
import com.models.Property
import com.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class PropertyRepository {
    private val collection = DatabaseConfig.database.getCollection<Property>(Constants.COLLECTION_PROPERTIES)

    suspend fun create(property: Property): Property {
        collection.insertOne(property)
        return property
    }

    suspend fun findById(id: ObjectId): Property? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findByOwnerId(ownerId: ObjectId, page: Int, limit: Int): List<Property> {
        return collection.find(Filters.eq("ownerId", ownerId))
            .skip((page - 1) * limit)
            .limit(limit)
            .sort(Sorts.descending("uploadTime"))
            .toList()
    }

    suspend fun search(
        query: String? = null,
        categoryId: ObjectId? = null,
        propertyTypeId: ObjectId? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        rooms: Byte? = null,
        baths: Byte? = null,
        minArea: Double? = null,
        maxArea: Double? = null,
        governorateId: ObjectId? = null,
        countryId: ObjectId? = null,
        page: Int,
        limit: Int
    ): List<Property> {
        val filters = mutableListOf<Bson>()

        // Always filter by active status
        filters.add(Filters.eq("status", Constants.PROPERTY_STATUS_ACTIVE))
        filters.add(Filters.eq("sold", false))

        // Text search in title and description
        if (!query.isNullOrBlank()) {
            filters.add(
                Filters.or(
                    Filters.regex("title", query, "i"),
                    Filters.regex("description", query, "i")
                )
            )
        }

        categoryId?.let { filters.add(Filters.eq("categoryId", it)) }
        propertyTypeId?.let { filters.add(Filters.eq("propertyTypeId", it)) }
        rooms?.let { filters.add(Filters.eq("rooms", it)) }
        baths?.let { filters.add(Filters.eq("baths", it)) }
        governorateId?.let { filters.add(Filters.eq("governorateId", it)) }
        countryId?.let { filters.add(Filters.eq("countryId", it)) }

        // Price range
        if (minPrice != null || maxPrice != null) {
            val priceFilter = when {
                minPrice != null && maxPrice != null -> Filters.and(
                    Filters.gte("price", minPrice),
                    Filters.lte("price", maxPrice)
                )
                minPrice != null -> Filters.gte("price", minPrice)
                maxPrice != null -> Filters.lte("price", maxPrice)
                else -> null
            }
            priceFilter?.let { filters.add(it) }
        }

        // Area range
        if (minArea != null || maxArea != null) {
            val areaFilter = when {
                minArea != null && maxArea != null -> Filters.and(
                    Filters.gte("area", minArea),
                    Filters.lte("area", maxArea)
                )
                minArea != null -> Filters.gte("area", minArea)
                maxArea != null -> Filters.lte("area", maxArea)
                else -> null
            }
            areaFilter?.let { filters.add(it) }
        }

        val finalFilter = if (filters.isNotEmpty()) Filters.and(filters) else Filters.empty()

        return collection.find(finalFilter)
            .sort(Sorts.orderBy(Sorts.descending("pinned"), Sorts.descending("featured"), Sorts.descending("uploadTime")))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
    }

    suspend fun countSearch(
        query: String? = null,
        categoryId: ObjectId? = null,
        propertyTypeId: ObjectId? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        rooms: Byte? = null,
        baths: Byte? = null,
        minArea: Double? = null,
        maxArea: Double? = null,
        governorateId: ObjectId? = null,
        countryId: ObjectId? = null
    ): Long {
        val filters = mutableListOf<Bson>()

        filters.add(Filters.eq("status", Constants.PROPERTY_STATUS_ACTIVE))
        filters.add(Filters.eq("sold", false))

        if (!query.isNullOrBlank()) {
            filters.add(
                Filters.or(
                    Filters.regex("title", query, "i"),
                    Filters.regex("description", query, "i")
                )
            )
        }

        categoryId?.let { filters.add(Filters.eq("categoryId", it)) }
        propertyTypeId?.let { filters.add(Filters.eq("propertyTypeId", it)) }
        rooms?.let { filters.add(Filters.eq("rooms", it)) }
        baths?.let { filters.add(Filters.eq("baths", it)) }
        governorateId?.let { filters.add(Filters.eq("governorateId", it)) }
        countryId?.let { filters.add(Filters.eq("countryId", it)) }

        if (minPrice != null || maxPrice != null) {
            val priceFilter = when {
                minPrice != null && maxPrice != null -> Filters.and(
                    Filters.gte("price", minPrice),
                    Filters.lte("price", maxPrice)
                )
                minPrice != null -> Filters.gte("price", minPrice)
                maxPrice != null -> Filters.lte("price", maxPrice)
                else -> null
            }
            priceFilter?.let { filters.add(it) }
        }

        if (minArea != null || maxArea != null) {
            val areaFilter = when {
                minArea != null && maxArea != null -> Filters.and(
                    Filters.gte("area", minArea),
                    Filters.lte("area", maxArea)
                )
                minArea != null -> Filters.gte("area", minArea)
                maxArea != null -> Filters.lte("area", maxArea)
                else -> null
            }
            areaFilter?.let { filters.add(it) }
        }

        val finalFilter = if (filters.isNotEmpty()) Filters.and(filters) else Filters.empty()
        return collection.countDocuments(finalFilter)
    }

    suspend fun updateById(id: ObjectId, updates: Map<String, Any>): Boolean {
        val updateDoc = Updates.combine(updates.map { Updates.set(it.key, it.value) })
        val result = collection.updateOne(Filters.eq("_id", id), updateDoc)
        return result.modifiedCount > 0
    }

    suspend fun updateStatus(id: ObjectId, status: String): Boolean {
        return updateById(id, mapOf("status" to status))
    }

    suspend fun markAsSold(id: ObjectId): Boolean {
        return updateById(id, mapOf("sold" to true))
    }

    suspend fun findAll(page: Int, limit: Int): List<Property> {
        return collection.find()
            .skip((page - 1) * limit)
            .limit(limit)
            .sort(Sorts.descending("uploadTime"))
            .toList()
    }

    suspend fun countAll(): Long {
        return collection.countDocuments()
    }

    suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }

    suspend fun findFeatured(limit: Int): List<Property> {
        return collection.find(
            Filters.and(
                Filters.eq("featured", true),
                Filters.eq("status", Constants.PROPERTY_STATUS_ACTIVE),
                Filters.eq("sold", false)
            )
        )
            .limit(limit)
            .sort(Sorts.descending("uploadTime"))
            .toList()
    }

    suspend fun findPinned(limit: Int): List<Property> {
        return collection.find(
            Filters.and(
                Filters.eq("pinned", true),
                Filters.eq("status", Constants.PROPERTY_STATUS_ACTIVE),
                Filters.eq("sold", false)
            )
        )
            .limit(limit)
            .sort(Sorts.descending("uploadTime"))
            .toList()
    }
}