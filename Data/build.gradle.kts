plugins {
    kotlin("jvm") version "2.0.21"
    id("org.springframework.boot") version "3.3.5-SNAPSHOT"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.service"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/release") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.8.0")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("io.milvus:milvus-sdk-java:2.4.4")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(":BaseUtil"))
}

tasks.test {
    useJUnitPlatform()
}