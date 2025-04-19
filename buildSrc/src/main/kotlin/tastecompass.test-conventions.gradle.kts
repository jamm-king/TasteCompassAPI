plugins {
    id("org.jetbrains.kotlin.jvm")
}

group = "com.tastecompass"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}