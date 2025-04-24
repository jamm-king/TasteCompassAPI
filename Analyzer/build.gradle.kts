plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("tastecompass.test-conventions")
}

dependencies {
    implementation("com.openai:openai-java:1.3.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(":OpenAI"))
    implementation(project(":Embedding"))
}