plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
}

dependencies {
    implementation(project(":_application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.19")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("${rootProject.rootDir}/openapi.yaml")
    outputDir.set("${project.layout.buildDirectory.get()}/generated/openapi")

    apiPackage.set("br.com.servicetrack.infrastructure.api")
    modelPackage.set("br.com.servicetrack.infrastructure.api.dto")

    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "documentationProvider" to "none",
        "annotationLibrary" to "none",
        "serializationLibrary" to "jackson"
    ))
}

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("openApiGenerate")
}