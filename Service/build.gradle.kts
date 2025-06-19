plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(":Analyzer"))
    implementation(project(":Embedding"))
    implementation(project(":Data"))
    implementation(project(":Domain"))
    implementation(project(":Redis"))
}