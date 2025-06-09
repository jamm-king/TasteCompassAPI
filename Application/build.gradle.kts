plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    implementation(project(":Controller"))
    implementation(project(":Service"))
    implementation(project(":BaseUtil"))
    implementation(project(":Domain"))
}