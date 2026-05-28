plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
    id("org.openapi.generator")
}

val qGroupId = rootProject.extra["quarkusPlatformGroupId"] as String
val qArtifactId = rootProject.extra["quarkusPlatformArtifactId"] as String
val qVersion = rootProject.extra["quarkusPlatformVersion"] as String

dependencies {
    implementation(project(":_domain"))
    implementation(project(":_application"))

    implementation(enforcedPlatform("$qGroupId:$qArtifactId:$qVersion"))
    implementation("io.quarkus:quarkus-qute")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    testImplementation("io.mockk:mockk:1.13.10")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-jacoco")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

openApiGenerate {
    inputSpec.set(rootProject.file("openapi.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("br.com.servicetrack.infrastructure.api")
    modelPackage.set("br.com.servicetrack.infrastructure.api.dto")
    generatorName.set("jaxrs-spec")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useJakartaEe" to "true",
            "dateLibrary" to "java8",
            "returnResponse" to "true",
            "useSwaggerAnnotations" to "false",
            "openApiNullable" to "false"
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/openapi/src/gen/java"))
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
    inputs.dir(layout.buildDirectory.dir("generated/openapi"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-java-parameters")
    }
}
