plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("tastecompass.test-conventions")
}

dependencies {
    implementation("io.milvus:milvus-sdk-java:2.4.4")
    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.3.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(":BaseUtil"))
}