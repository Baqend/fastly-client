plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("pl.allegro.tech.build.axion-release") version "1.3.2"
    kotlin("jvm") version "1.6.10"
    `maven-publish`
    java
    signing
}

group = "com.baqend.fastly"
version = scmVersion.version

repositories {
    mavenCentral()
}

val ktorVersion = "2.0.1"
val coroutinesVersion = "1.6.1"
val jUnitVersion = "5.8.2"
val junitPlatformVersion = "1.8.2"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("ch.qos.logback:logback-classic:1.2.11")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishing {
    publications {
        create("shadow", MavenPublication::class.java) {
            project.shadow.component(this)
            pom {
                name.set("fastly-client")
                description.set("A client to interact with fastly dictionaries")
                url.set("https://github.com/Baqend/fastly-client")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/Baqend/fastly-client/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("com.baqend")
                        name.set("Baqend GmbH")
                        email.set("info@baqend.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Baqend/fastly-client")
                    developerConnection.set("scm:git:https://github.com/Baqend/fastly-client")
                    url.set("https://github.com/Baqend/fastly-client")
                }
            }
        }
    }

    repositories {
        // TODO(jd): For now we only publish to GitHub Packages until the library is ready for the big crowd
        //        maven {
        //            name = "OSSRH"
        //            url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        //            credentials {
        //                username = System.getenv("MAVEN_USERNAME")
        //                password = System.getenv("MAVEN_PASSWORD")
        //            }
        //        }

        maven {
            name = "Gitlab"
            url = uri("https://gitlab.orestes.info/api/v4/groups/325/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Baqend/fastly-client")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// signing {
//    sign(publishing.publications["fastlyClient"])
// }
