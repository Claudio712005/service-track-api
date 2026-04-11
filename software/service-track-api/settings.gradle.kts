pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "service-track-api"

include(":_domain", ":_application", ":_infrastructure")