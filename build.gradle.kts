plugins {
    java
    `maven-publish`
    idea
    id("org.jetbrains.kotlin.jvm") apply (false)
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
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
    }
}
