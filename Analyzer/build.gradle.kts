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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation(project(":BaseUtil"))
    implementation(project(":Embedding"))
}

tasks.test {
    useJUnitPlatform()
}