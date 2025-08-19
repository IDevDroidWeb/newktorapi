package com.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.cdimascio.dotenv.dotenv

object DatabaseConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    private val mongoUri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017/realestate_db"
    private val databaseName = dotenv["MONGODB_DATABASE"] ?: "realestate_db"

    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    fun init() {
        client = MongoClient.create(mongoUri)
        database = client.getDatabase(databaseName)
        println("Connected to MongoDB: $databaseName")
    }

    fun close() {
        client.close()
    }
}