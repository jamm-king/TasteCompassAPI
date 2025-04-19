plugins {
    id("tastecompass.kotlin-conventions")
    id("tastecompass.springboot-conventions")
    id("org.springframework.boot")
}

//configurations {
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}

//extra["springAiVersion"] = "1.0.0-M3"

repositories {
    mavenCentral()
//    maven { url = uri("https://repo.spring.io/release") }
//    maven { url = uri("https://repo.spring.io/snapshot") }
//    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")

    implementation(project(":Controller"))
    implementation(project(":BaseUtil"))
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
//    }
//}
//
//kotlin {
//    compilerOptions {
//        freeCompilerArgs.addAll("-Xjsr305=strict")
//    }
//}