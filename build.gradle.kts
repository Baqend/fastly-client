plugins {
    base
    idea
    `maven-publish`
    java
    kotlin("jvm") version "1.8.10"
    id("net.researchgate.release") version "3.0.2"
    id("com.diffplug.spotless-changelog") version "3.0.1"
}

spotlessChangelog {
    ifFoundBumpBreaking("**BREAKING**")
    ifFoundBumpAdded("### Added")
    changelogFile("CHANGELOG.md")
    setAppendDashSnapshotUnless_dashPrelease(true)
    tagPrefix("v")
    commitMessage("Release v{{version}}")
    remote("origin")
    branch("release")
}
println("SpotlessChangelog Version Next: ${spotlessChangelog.versionNext}  Last: ${spotlessChangelog.versionLast}")

version = spotlessChangelog.versionNext
val isSnapshot = version.toString().endsWith("-SNAPSHOT")

println("Version: $version")

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

configure<net.researchgate.release.ReleaseExtension> {
    preTagCommitMessage.set("pre tag commit [ci skip]:")
    tagCommitMessage.set("creating tag:")
    newVersionCommitMessage.set("new version commit [ci skip]:")
    buildTasks.set(listOf("changelogBump"))
    ignoredSnapshotDependencies.set(listOf("net.researchgate:gradle-release"))
    // this is used by the plugin to select the part of the current version to update
    versionPatterns = mapOf(
        // with this pattern we select the patch version
        """(\d+)([^\d]*$)""" to KotlinClosure2<java.util.regex.Matcher, Project, String>(
            // this closure replaces the current patch version with: patch + 1 and "-SNAPSHOT" appended
            { matcher, _ -> matcher.replaceAll("${(matcher.group(0).toInt()) + 1}-SNAPSHOT") }
        ),
    )
    with(git) {
        requireBranch.set("main")
        pushToRemote.set("origin")
    }
}
