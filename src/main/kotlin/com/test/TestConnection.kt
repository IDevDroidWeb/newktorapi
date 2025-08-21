package com.test

import com.config.DatabaseConfig
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import org.bson.Document

class TestConnection {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Testing MongoDB Atlas connection...")
            runBlocking {
                try {
                    DatabaseConfig.init()

                    // Test connection by trying to access a collection
                    val testCollection: MongoCollection<Document> = DatabaseConfig.database.getCollection("test")

                    // Try to insert and then delete a test document
                    val testDoc = Document("test", "connection_${System.currentTimeMillis()}")
                    val insertResult = testCollection.insertOne(testDoc)
                    println("✅ Insert successful: ${insertResult.insertedId}")

                    // Clean up
                    testCollection.deleteOne(Document("test", testDoc.getString("test")))

                    println("✅ Successfully connected to MongoDB Atlas!")
                    println("Database name: ${DatabaseConfig.database.name}")

                    // List collections to verify we can read from the database
                    val collections = DatabaseConfig.database.listCollectionNames()
                    println("Available collections:")
                    collections.collect { println("  - $it") }

                } catch (e: Exception) {
                    println("❌ Failed to connect to MongoDB Atlas:")
                    println("Error: ${e.message}")
                    e.printStackTrace()
                } finally {
                    try {
                        DatabaseConfig.close()
                    } catch (e: Exception) {
                        println("Warning: Error closing database connection: ${e.message}")
                    }
                }
            }
        }
    }
}