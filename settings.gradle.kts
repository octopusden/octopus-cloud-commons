rootProject.name = "octopus-cloud-commons"

pluginManagement {
    plugins {
        kotlin("jvm") version(extra["kotlin.version"] as String)
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("octopus-security-common")

project(":octopus-security-common").projectDir = file("./security-common")
