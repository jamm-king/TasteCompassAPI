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

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20210307")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}