rootProject.name = "octopus-cloud-commons"

pluginManagement {
    plugins {
        kotlin("jvm") version(extra["kotlin.version"] as String)
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
        id("io.gitlab.arturbosch.detekt") version(extra["detekt.version"] as String)
        id("org.jlleitschuh.gradle.ktlint") version(extra["ktlint.version"] as String)
        id("org.octopusden.octopus-quality") version(extra["octopus-quality.version"] as String)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("octopus-security-common")

project(":octopus-security-common").projectDir = file("./security-common")
