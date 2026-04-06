    plugins {
        kotlin("jvm")
    }

    dependencies {
        implementation(project(":_domain"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        testImplementation("io.mockk:mockk:1.13.10")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    }

    tasks.test {
        useJUnitPlatform()
    }