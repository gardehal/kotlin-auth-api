import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "grd.kotlin"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("grd.kotlin.authapi.AuthApiApplicationKt")
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.0.4")
    implementation("org.springframework.boot:spring-boot-starter-security:3.0.4")
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.4")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.15")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.15")
    runtimeOnly("org.springframework.boot:spring-boot-devtools:3.0.4")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")
    implementation("org.owasp:dependency-check-gradle:8.1.2")
    // Firebase
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("com.google.gms:google-services:4.3.15")
    // JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2")
    implementation("com.github.java-json-tools:json-patch:1.13")
    implementation("com.google.code.gson:gson:2.10.1")
    // JWT
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    // Logging
    implementation("io.klogging:klogging-jvm:0.4.13")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")
    runtimeOnly("org.reactivestreams:reactive-streams:1.0.4")
    // Test/Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.rest-assured:rest-assured:5.3.0")
    testImplementation("io.rest-assured:json-path:5.3.0")
    testImplementation("io.rest-assured:xml-path:5.3.0")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    // Test/DB
    testImplementation("com.h2database:h2:2.1.214")
    // Test/Mock
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("io.rest-assured:spring-mock-mvc:5.3.0")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.google.com/")
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

plugins {
    application
    java
    jacoco
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

//apply(plugin = "org.owasp.dependencycheck")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    launchScript()
}