enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("detekt")

rootProject.name = "SkyOcean"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
        maven(url = "https://maven.teamresourceful.com/repository/msrandom/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
