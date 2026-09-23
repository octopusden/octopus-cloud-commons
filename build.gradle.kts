plugins {
    java
    `maven-publish`
    idea
    id("org.jetbrains.kotlin.jvm") apply (false)
    id("io.github.gradle-nexus.publish-plugin")
    signing
    // Kotlin static-analysis tools — declared at root (apply false), applied per Kotlin subproject below.
    id("io.gitlab.arturbosch.detekt") apply (false)
    id("org.jlleitschuh.gradle.ktlint") apply (false)
    // Octopus quality-gates convention plugin — configures detekt/ktlint and wires qualityStatic.
    id("org.octopusden.octopus-quality")
    id("org.sonarqube")
}

repositories {
    mavenCentral()
}

octopusQuality {
    // Regression guard on what this repository publishes to Maven Central, from octopus-base
    // v2.7.0. This repository had no guard at all before — it was pinned below v2.6.0, so the
    // release-time fat-jar guard did not apply either, and a new module could start publishing
    // without anyone deciding to.
    publication {
        enforceCentralPublications.set(true)
        centralPublications.set(
            setOf(
                // Only this module publishes; the rest of the repository is internal.
                ":octopus-security-common|mavenJava|" +
                    "org.octopusden.octopus-cloud-commons:octopus-security-common|" +
                    "[jar, jar:javadoc, jar:sources]",
            ),
        )
    }
    // Repo has no coverage tool / no unit-test coverage target — disable coverage verification.
    coverage {
        enabled.set(false)
    }
    // Enforce the gate: detekt/ktlint violations fail the build. Current debt is absorbed by
    // the committed detekt-baseline.xml / ktlint-baseline.xml files.
    kotlin {
        failOnViolation.set(true)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

group = "org.octopusden.octopus-cloud-commons"

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "signing")
    // Kotlin static analysis — must be applied per subproject so the convention plugin's
    // reactive configuration wires detekt/ktlintCheck tasks (avoids a hollow quality gate).
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "org.octopusden.octopus-cloud-commons"

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("Octopus module(${project.name}) for cloud-commons")
                    url.set("https://github.com/octopusden/octopus-cloud-commons.git")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set("https://github.com/octopusden/octopus-rm-gradle-plugin.git")
                        connection.set("scm:git://github.com/octopusden/octopus-cloud-commons.git")
                    }
                    developers {
                        developer {
                            id.set("octopus")
                            name.set("octopus")
                        }
                    }
                }
            }
        }
    }

    signing {
        isRequired = System.getenv().let {
            it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword")
        }
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }

    repositories {
        mavenCentral()
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(kotlin("stdlib"))

        testImplementation(platform("org.junit:junit-bom:${rootProject.properties["junit-jupiter.version"]}"))
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.junit.jupiter:junit-jupiter-params")
        testImplementation("org.junit.vintage:junit-vintage-engine")
        // Align the launcher Gradle injects with the junit-bom version; without
        // this pin test discovery fails with "OutputDirectoryProvider not available".
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
