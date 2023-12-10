import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "grd.kotlin.auth-api"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("grd.kotlin.authapi.AuthApiApplicationKt")
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.1.5")
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.5")
    implementation("org.springframework.boot:spring-boot-starter-security:3.1.5")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.1.5")
    implementation("org.springframework.security:spring-security-core:6.1.5")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.15")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.15")
    runtimeOnly("org.springframework.boot:spring-boot-devtools:3.0.4")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")
    implementation("org.owasp:dependency-check-gradle:8.1.2")
    // Database
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("com.google.gms:google-services:4.3.15")
    implementation("org.postgresql:postgresql:42.7.1")

    // JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2")
    implementation("com.github.java-json-tools:json-patch:1.13")
    implementation("com.google.code.gson:gson:2.10.1")
    // JWT
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("io.klogging:klogging-jvm:0.4.13")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")
    runtimeOnly("org.reactivestreams:reactive-streams:1.0.4")
    // Test/Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.springframework.security:spring-security-test:6.0.2")
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
        freeCompilerArgs += "-Xjsr305=warn"
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    launchScript()
}

// Testing
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

val testCoverage by tasks.registering {
    group = "verification"
    description = "Runs the unit tests with coverage."

    dependsOn(":test", ":jacocoTestReport", ":jacocoTestCoverageVerification")
    val jacocoTestReport = tasks.findByName("jacocoTestReport")
    jacocoTestReport?.mustRunAfter(tasks.findByName("test"))
    tasks.findByName("jacocoTestCoverageVerification")?.mustRunAfter(jacocoTestReport)
}