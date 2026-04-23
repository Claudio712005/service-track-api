plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21" apply false
    id("io.quarkus") version "3.15.1" apply false
    id("org.openapi.generator") version "7.4.0" apply false
    id("org.sonarqube") version "4.4.1.3373"
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

val quarkusPlatformGroupId by extra("io.quarkus.platform")
val quarkusPlatformArtifactId by extra("quarkus-bom")
val quarkusPlatformVersion by extra("3.15.1")

apply(from = "jacoco-config.gradle.kts")

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "service-track-api")
        property("sonar.projectName", "service-track-api")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "http://localhost:9000")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.gradle.skipCompile", "true")
        property("sonar.sourceEncoding", "UTF-8")
        property(
            "sonar.exclusions",
            "**/dto/**,**/entity/**,**/config/**,**/openapi/generated/**"
        )
    }
}

subprojects {
    sonarqube {
        properties {
            property("sonar.sources", "src/main/kotlin")
            property("sonar.tests", "src/test/kotlin")
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "${layout.buildDirectory.get().asFile}/reports/jacoco/test/jacocoTestReport.xml"
            )
        }
    }
}

tasks.named("sonar") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("check") })
}
