plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":Analyzer"))
    implementation(project(":Embedding"))
    implementation(project(":Data"))
    implementation(project(":Domain"))
}