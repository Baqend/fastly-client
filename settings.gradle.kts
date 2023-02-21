rootProject.name = "fastly-client"

// FIXME: remove after: https://github.com/diffplug/spotless/issues/587
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.13.1.202206130422-r")
    }
    configurations.classpath {
        resolutionStrategy {
            force("org.eclipse.jgit:org.eclipse.jgit:5.13.1.202206130422-r")
        }
    }
}
