enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("detekt")

rootProject.name = "SkyOcean"

pluginManagement {
    repositories {
        maven("https://maven.teamresourceful.com/repository/maven-private/")
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
        maven(url = "https://maven.msrandom.net/repository/cloche")
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.teamresourceful.com/repository/maven-private/")
        maven(url = "https://maven.teamresourceful.com/repository/msrandom/")
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.12.1")
        classpath("net.msrandom:minecraft-codev-fabric:0.6.4-1") {
            version { strictly("0.6.4-1") }
        }
    }
}
