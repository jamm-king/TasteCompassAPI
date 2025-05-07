plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":Controller"))
    implementation(project(":BaseUtil"))
    implementation(project(":Domain"))
}