plugins {
    id("org.octopusden.release-management")
    java
    `maven-publish`
    idea
    id("org.jetbrains.kotlin.jvm") apply (false)
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")

    group = "org.octopusden.cloud-commons"

    java {
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(kotlin("stdlib"))

        testImplementation(enforcedPlatform("org.junit:junit-bom:${rootProject.properties["junit-jupiter.version"]}"))
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.junit.jupiter:junit-jupiter-params")
        testImplementation("org.junit.vintage:junit-vintage-engine")
    }
}
