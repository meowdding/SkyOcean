enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("detekt")

rootProject.name = "SkyOcean"

pluginManagement {
    repositories {
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
        maven(url = "https://maven.msrandom.net/repository/cloche")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
