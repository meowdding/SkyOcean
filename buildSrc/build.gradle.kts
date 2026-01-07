plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.2.0"
}

repositories {
    gradlePluginPortal()
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

dependencies {
    implementation(libs.meowdding.resources)
    implementation(libs.gson)
    implementation("net.peanuuutz.tomlkt:tomlkt:0.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}
