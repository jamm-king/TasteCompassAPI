plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
}

dependencies {
    implementation(project(":Data"))
    implementation(project(":BaseUtil"))
    implementation(project(":Analyzer"))
}