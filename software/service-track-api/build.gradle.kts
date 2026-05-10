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
        properties["sonar.projectKey"] = "claudio712005_service-track-api"
        properties["sonar.organization"] = "claudio712005"
        properties["sonar.host.url"] = "https://sonarcloud.io"
        properties["sonar.token"] = System.getenv("SONAR_TOKEN")

        properties["sonar.sourceEncoding"] = "UTF-8"

        properties["sonar.coverage.jacoco.xmlReportPaths"] =
            subprojects.map {
                "${it.buildDir}/reports/jacoco/test/jacocoTestReport.xml"
            }

        properties["sonar.exclusions"] = listOf(
            "**/dto/**",
            "**/entity/**",
            "**/config/**",
            "**/openapi/generated/**"
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
