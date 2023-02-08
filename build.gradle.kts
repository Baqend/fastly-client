plugins {
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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create(project.name, MavenPublication::class.java) {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("${System.getenv("CI_API_V4_URL")}/projects/151/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
}
