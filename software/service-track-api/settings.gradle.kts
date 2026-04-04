pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "service-track-api"

include("_domain")
include("_application")
include("_infrastructure")