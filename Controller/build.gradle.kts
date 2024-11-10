plugins {
    kotlin("jvm") version "2.0.21"
    id("org.springframework.boot") version "3.3.5-SNAPSHOT"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.service"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/release") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.8.0")
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(project(":Data"))
    implementation(project(":BaseUtil"))
    implementation(project(":Analyzer"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}