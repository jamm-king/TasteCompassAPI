plugins {
    kotlin("jvm") version "2.0.21"
}

group = "com.service"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.8.0")
    implementation("org.json:json:20210307")

    implementation(project(":BaseUtil"))
}

tasks.test {
    useJUnitPlatform()
}