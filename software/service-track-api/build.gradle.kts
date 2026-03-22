buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.fasterxml.jackson.core:jackson-core:2.16.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.16.1")
        classpath("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    }
}

plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("org.openapi.generator") version "7.4.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}