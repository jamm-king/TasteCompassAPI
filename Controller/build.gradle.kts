plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("tastecompass.test-conventions")
}

dependencies {
    implementation(project(":Data"))
    implementation(project(":Analyzer"))
    implementation(project(":Embedding"))
    implementation(project(":Domain"))
}