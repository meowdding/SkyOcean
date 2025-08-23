plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

dependencies {
    implementation(libs.meowdding.resources)
    implementation(libs.gson)
}
