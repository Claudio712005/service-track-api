plugins {
    kotlin("jvm")
    id("io.quarkus")
    id("org.openapi.generator")
}

val qGroupId = rootProject.extra["quarkusPlatformGroupId"] as String
val qArtifactId = rootProject.extra["quarkusPlatformArtifactId"] as String
val qVersion = rootProject.extra["quarkusPlatformVersion"] as String

dependencies {
    implementation(project(":_application"))

    implementation(enforcedPlatform("$qGroupId:$qArtifactId:$qVersion"))

    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-rest-client-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

openApiGenerate {
    generatorName.set("kotlin")

    inputSpec.set(rootProject.file("openapi.yaml").absolutePath)

    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)

    apiPackage.set("br.com.servicetrack.infrastructure.api")
    modelPackage.set("br.com.servicetrack.infrastructure.api.dto")

    configOptions.set(
        mapOf(
            "serializationLibrary" to "jackson",
            "interfaceOnly" to "true",
            "useJakartaEe" to "true",
            "skipDefaultInterface" to "true"
        )
    )
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-java-parameters")
    }
}