pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("SpellCheckingInspection")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "Krawler"
