rootProject.name = "cloud-commons"

pluginManagement {
    plugins {
        kotlin("jvm") version(extra["kotlin.version"] as String)
        id("org.octopusden.release-management") version(extra["release-management.version"] as String)
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
include("security-common")
