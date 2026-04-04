plugins {
    kotlin("jvm") version "2.0.21"
    id("io.quarkus") version "3.15.1" apply false
    id("org.openapi.generator") version "7.4.0" apply false
}

val quarkusPlatformGroupId by extra("io.quarkus.platform")
val quarkusPlatformArtifactId by extra("quarkus-bom")
val quarkusPlatformVersion by extra("3.15.1")

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}