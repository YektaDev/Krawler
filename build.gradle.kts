val ktorVersion: String by project
val okioVersion: String by project
val ktomlVersion: String by project

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
}

group = "dev.yekta"
version = "0.1.0"

repositories {
    mavenCentral()
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("com.squareup.okio:okio:$okioVersion")
    implementation("com.akuleshov7:ktoml-core:$ktomlVersion")
    implementation("com.akuleshov7:ktoml-file:$ktomlVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(20)
}
