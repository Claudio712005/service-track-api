plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21" apply false
    id("io.quarkus") version "3.15.1" apply false
    id("org.openapi.generator") version "7.4.0" apply false
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
