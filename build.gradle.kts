val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mongodb_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    application
}

group = "com"
version = "0.0.1"

application {
    mainClass.set("com.ApplicationKt")
    //mainClass = "io.ktor.server.netty.EngineMain"
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server Core
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")

    // Content Negotiation & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")

    // Multipart Support (NEW)
    //implementation("io.ktor:ktor-server-multipart-jvm")

    // Authentication & Security
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")

    // HTTP Features
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-forwarded-header-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-rate-limit-jvm")

    // Ktor Client for HTTP requests
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")

    // Validation
    implementation("io.konform:konform:0.4.0")

    // Partial Content Support for File Serving (NEW)
    implementation("io.ktor:ktor-server-partial-content-jvm")

    // Swagger/OpenAPI
    implementation("io.ktor:ktor-server-swagger-jvm")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongodb_version")
    implementation("org.mongodb:bson-kotlinx:$mongodb_version")

    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // Environment Variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // AWS S3 SDK (for future S3 support) (NEW)
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.565")

    // Image Processing (NEW)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

/*dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // Ktor Server
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-rate-limit:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongodb_version")
    implementation("org.mongodb:bson-kotlinx:$mongodb_version")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.auth0:java-jwt:4.4.0")

    // Environment Variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // HTTP
    implementation("io.ktor:ktor-server-default-headers:${ktor_version}")
    implementation("io.ktor:ktor-server-forwarded-header:${ktor_version}")
}*/

tasks.create("stage").dependsOn("installDist")